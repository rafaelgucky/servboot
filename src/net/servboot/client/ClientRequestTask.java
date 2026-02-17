package net.servboot.client;

import net.servboot.controllers.ControllerBase;
import net.servboot.headers.HeaderBuilder;
import net.servboot.headers.Headers;
import net.servboot.io.FormDataReader;
import net.servboot.io.RequestBufferedReader;
import net.servboot.io.ServBootFile;
import net.servboot.io.ShortArrayInputStream;
import net.servboot.request.Request;
import net.servboot.request.RequestMapper;
import net.servboot.response.Response;
import net.servboot.utils.io.NameGenerator;
import net.servboot.utils.json.Json;
import net.servboot.utils.reflection.ReflectionUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClientRequestTask extends Thread {
    public ServerSocket server;
    public Socket client;
    Request request;
    private final Thread currentThread;
    private final List<Thread> threads;
    private final List<ControllerBase> controllers;
    private final List<Class<?>> requestContainerDI;
    private final List<Object> applicationContainerDI;

    public ClientRequestTask(ServerSocket server, Socket client,
                             Thread currentThread, List<Thread> threads,
                             List<ControllerBase> controllers,
                             List<Class<?>> requestContainerDI,
                             List<Object> applicationContainerDI) {
        this.server = server;
        this.client = client;
        this.currentThread = currentThread;
        this.threads = threads;
        this.controllers = controllers;
        this.requestContainerDI = requestContainerDI;
        this.applicationContainerDI = applicationContainerDI;
    }

    @Override
    public void run() {
        try {
            InputStream in =  client.getInputStream();
            ShortArrayInputStream shortArray = new ShortArrayInputStream(in);
            InputStreamReader isr = new InputStreamReader(shortArray, StandardCharsets.UTF_8);
            RequestBufferedReader rbr = new RequestBufferedReader(shortArray, isr);
            RequestMapper requestMapper = new RequestMapper(this.currentThread, this.applicationContainerDI,  this.requestContainerDI);

            String url = rbr.readLine();
            if(url.isEmpty()){
                return;
            }
            request = requestMapper.map(url);
            request.setClientOutputStream(client.getOutputStream());

            String line;
            while (!(line = rbr.readLine()).isEmpty()) {
                request.addHeader(line);
            }

            if(request.getContentLength() > 0){
                //client.setSoTimeout(15000);
                if(request.getContentType().contains("multipart/form-data"))
                {
                    FormDataReader fdr = new FormDataReader(in, isr, shortArray, request.getHeader("boundary"), request.getContentLength());
                    Map<String, Object> formData = fdr.readFormData();
                    for(Map.Entry<String, Object> entry : formData.entrySet()){
                        if(entry.getValue() instanceof List files){
                            request.addFile(entry.getKey(), files);
                        } else {
                            request.addParameter(entry.getKey(), entry.getValue().toString());
                        }
                    }
                } else if (request.getContentType().contains("json")) {
                    request.setStringBody(rbr.readBody(request.getContentLength()));
                }
            }

            // Chamar o controller
            Object controllerResult = requestMapper.invoke(controllers);
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

            try{
                client.getOutputStream().close();
            } catch (IOException ex) {

            }

            try{
                Set<String> keys = request.getFiles().keySet();
                for(String key : keys) {
                    for(File file : request.getFiles().get(key)) {
                        Files.deleteIfExists(file.toPath());
                        Thread.yield();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            threads.remove(this);
        }
    }
}
