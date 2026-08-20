package net.servboot.client;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class ClientRequestTask extends Thread implements Closeable, Comparable<ClientRequestTask> {
    private Socket client;
    private Request request;
    private Consumer<ClientRequestTask> onFinalize;

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
            Object controllerResult = this.request.getRoute().call(MethodUtils.getSortedParameters(this.request.getRoute().getMethod(), this.request.getParameters()));
            short statusCode = 200;

            if(controllerResult instanceof Response response){
                statusCode = response.getResponseCode();
                controllerResult = response.getBody();
            }

            // Devolver resposta ao cliente
            if(controllerResult == null){
                client.getOutputStream().write(HeaderBuilder.build(Headers.TEXT_PLAIN, statusCode, 0));
                client.getOutputStream().flush();
            } else if(ReflectionUtils.isPrimitive(controllerResult.getClass())) {
                int extraBytes = 0;
                for(char c : controllerResult.toString().toCharArray()){
                    extraBytes += c > 127 ? 1 : 0;
                }
                client.getOutputStream().write(HeaderBuilder.build(Headers.TEXT_PLAIN, statusCode, controllerResult.toString().length() + extraBytes));
                client.getOutputStream().write(controllerResult.toString().getBytes(StandardCharsets.UTF_8));
                client.getOutputStream().flush();
            } else if(controllerResult instanceof File file){
                try(
                    InputStream reader = new FileInputStream(file);
                ) {
                    client.getOutputStream().write(HeaderBuilder.build(
                            Headers.getValueFromFileExtension(file.getName().substring(file.getName().indexOf(".") + 1)),
                            statusCode, file.length()));
                    client.getOutputStream().write(reader.readAllBytes());
                    client.getOutputStream().flush();
                }
            } else if(controllerResult instanceof ServBootFile sbInputStream){
                File responseFile;

                do{
                    responseFile = new File(System.getProperty("java.io.tmpdir") + "/" + NameGenerator.generateName(sbInputStream.getExtension()));
                } while (responseFile.exists());

                Files.createFile(responseFile.toPath());

                try(
                    OutputStream out = new FileOutputStream(responseFile);
                ){
                    sbInputStream.getInputStream().transferTo(out);
                } catch(IOException ioe){
                    ioe.printStackTrace();
                }

                try(
                    InputStream reader = new FileInputStream(responseFile);
                ) {
                    client.getOutputStream().write(HeaderBuilder.build(
                            Headers.getValueFromFileExtension(sbInputStream.getExtension()),
                            statusCode, responseFile.length(), sbInputStream.isDownload(), sbInputStream.getFileName()));
                    client.getOutputStream().write(reader.readAllBytes());
                    client.getOutputStream().flush();
                }

                Files.deleteIfExists(responseFile.toPath());
            } else {
                String json = Json.encode(controllerResult);
                int extraBytes = 0;

                for(char c : json.toCharArray()){
                    extraBytes += c > 127 ? 1 : 0;
                }

                client.getOutputStream().write(HeaderBuilder.build(Headers.APPLICATION_JSON, statusCode, json.length() + extraBytes));
                client.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
                client.getOutputStream().flush();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                this.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() throws IOException {
        client.getOutputStream().close();
        this.cleanFiles();

        if (this.getOnFinalize() != null) {
            this.getOnFinalize().accept(this);
        }

        ServerManager.getThreadsNames().push(this.getName());
        ServerManager.removeThread(this);

        this.interrupt();
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
