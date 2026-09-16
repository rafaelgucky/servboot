package net.servboot.client;

import net.servboot.dependency.DependencyInjectionContainer;
import net.servboot.headers.HeaderBuilder;
import net.servboot.headers.Headers;
import net.servboot.io.FormDataReader;
import net.servboot.io.RequestBufferedReader;
import net.servboot.io.ServBootFile;
import net.servboot.io.ShortArrayInputStream;
import net.servboot.request.Request;
import net.servboot.response.Response;
import net.servboot.server.ServerManager;
import net.servboot.utils.io.NameGenerator;
import net.servboot.utils.json.Json;
import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.reflection.method.MethodUtils;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public final class ClientRequestTask extends Thread implements Closeable, Comparable<ClientRequestTask> {
    private Socket client;
    private Request request;
    private Consumer<ClientRequestTask> onFinalize;
    private final Map<String, Object> locals = new LinkedHashMap<>();

    public void setClient(Socket client) {
        this.client = client;
    }

    public Request getRequest() {
        return this.request;
    }

    public void setOnFinalize(Consumer<ClientRequestTask> onFinalize) {
        this.onFinalize = onFinalize;
    }

    public Consumer<ClientRequestTask> getOnFinalize() {
        return onFinalize;
    }

    public Map<String, Object> getLocals() {
        return this.locals;
    }

    public void addLocal(String key, Object value) {
        this.locals.put(key, value);
    }

    public Object getLocal(String key) {
        return locals.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        try {
            InputStream in =  client.getInputStream();
            ShortArrayInputStream shortArray = new ShortArrayInputStream(in);
            InputStreamReader isr = new InputStreamReader(shortArray, StandardCharsets.UTF_8);
            RequestBufferedReader rbr = new RequestBufferedReader(shortArray, isr);

            String url = rbr.readLine();
            if(url.isEmpty()){
                return;
            }

            url = url.trim();
            String method = url.substring(0, url.indexOf(' '));
            url = url.replace(method, "");
            String newUrl = url.substring(0, url.lastIndexOf(' ')).trim();
            String requestRoute = newUrl.substring(0, (newUrl.indexOf("?") > 0 ? newUrl.indexOf("?") : newUrl.length())).trim();

            this.request = new Request(method, requestRoute.substring(0, (requestRoute.lastIndexOf('/') >= url.length() - 1 ? requestRoute.lastIndexOf('/') : requestRoute.length())));
            request.setClientOutputStream(client.getOutputStream());

            if (Thread.currentThread().isInterrupted()) return;

            String line;
            while (!(line = rbr.readLine()).isEmpty()) {
                request.addHeader(line);
            }

            if(request.getContentLength() > 0){
                if(request.getContentType().contains("multipart/form-data"))
                {
                    FormDataReader fdr = new FormDataReader(in, isr, shortArray, request.getHeader("boundary"), request.getContentLength());
                    Map<String, Object> formData = fdr.readFormData();
                    for(Map.Entry<String, Object> entry : formData.entrySet()){
                        if(entry.getValue() instanceof List<?> files){
                            request.addFile(entry.getKey(), (List<File>) files);
                        } else {
                            request.addParameter(entry.getKey(), entry.getValue().toString());
                        }
                    }
                } else if (request.getContentType().contains("json")) {
                    request.setStringBody(rbr.readBody(request.getContentLength()));
                }
            }

            // Chamar o controller
            Object controllerResult;
            try {
                controllerResult = this.request.getRoute().call(MethodUtils.getSortedParameters(this.request.getRoute().getMethod(), this.request.getParameters(), this.request.getFiles()));
            } catch (Exception e) {
                if (ServerManager.getLogger() != null) {
                    ServerManager.getLogger().accept(e);
                }
                return;
            }

            short statusCode = 200;

            if(controllerResult instanceof Response response){
                statusCode = response.getResponseCode();
                controllerResult = response.getBody();
            }

            // Devolver resposta ao cliente
            if(controllerResult == null){
                client.getOutputStream().write(HeaderBuilder.build(Headers.TEXT_PLAIN, statusCode, 0));
            } else if(ReflectionUtils.isPrimitive(controllerResult.getClass())) {
                int extraBytes = 0;
                for(char c : controllerResult.toString().toCharArray()){
                    extraBytes += c > 127 ? 1 : 0;
                }
                client.getOutputStream().write(HeaderBuilder.build(Headers.TEXT_PLAIN, statusCode, controllerResult.toString().length() + extraBytes));
                client.getOutputStream().write(controllerResult.toString().getBytes(StandardCharsets.UTF_8));
            } else if(controllerResult instanceof File file){
                try(
                    InputStream reader = new FileInputStream(file);
                ) {
                    client.getOutputStream().write(HeaderBuilder.build(
                            Headers.getValueFromFileExtension(file.getName().substring(file.getName().indexOf(".") + 1)),
                            statusCode, file.length()));
                    client.getOutputStream().write(reader.readAllBytes());
                }
            } else if(controllerResult instanceof ServBootFile sbInputStream){
                byte[] bytes = sbInputStream.getInputStream().readAllBytes();
                sbInputStream.getInputStream().close();

                client.getOutputStream().write(HeaderBuilder.build(
                        Headers.getValueFromFileExtension(sbInputStream.getExtension()),
                        statusCode, bytes.length,
                        sbInputStream.isDownload(), sbInputStream.getFileName())
                );
                client.getOutputStream().write(bytes);
            } else {
                String json = Json.encode(controllerResult);
                int extraBytes = 0;

                for(char c : json.toCharArray()){
                    extraBytes += c > 127 ? 1 : 0;
                }

                client.getOutputStream().write(HeaderBuilder.build(Headers.APPLICATION_JSON, statusCode, json.length() + extraBytes));
                client.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            this.close();
        }
    }

    @Override
    public void close() {
        try {
            this.client.getOutputStream().flush();
            this.client.getInputStream().close();
            this.client.close();

            if (this.getOnFinalize() != null) {
                this.getOnFinalize().accept(this);
            }

            ServerManager.getThreadsNames().push(this.getName());
            ServerManager.removeThread(this);

            this.cleanFiles();
            this.interrupt();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int compareTo(ClientRequestTask o) {
        return this.getName().compareTo(o.getName());
    }

    public void cleanFiles() throws IOException {
        if (this.request != null) {
            Set<String> keys = request.getFiles().keySet();
            for(String key : keys) {
                for(File file : request.getFiles().get(key)) {
                    Files.deleteIfExists(file.toPath());
                }
            }
        }
    }
}
