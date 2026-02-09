package net.servboot.controllers;

import net.servboot.headers.HeaderBuilder;
import net.servboot.headers.Headers;
import net.servboot.request.Request;
import net.servboot.response.Response;
import net.servboot.utils.json.Json;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ControllerBase {
    public Request Request;
    public Response Response;

    public void setRequest(Request request){
        this.Request = request;
        this.Response = new Response(request);
    }

    public void text(String text){
        try(
            OutputStream out = Request.getClientOutputStream();
        ){
            for(byte b : HeaderBuilder.build(Headers.TEXT_TXT, (short) 200, text.length())){
                out.write(b);
            }

            for(int i = 0; i < text.length(); i++){
                out.write(text.charAt(i));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void file(File file) {
        if(file == null) throw new RuntimeException("file is null");
        else if(!file.exists()) throw new RuntimeException("file does not exist");
        else if(!file.isFile()) throw new RuntimeException("file is not a file");

        try(
            OutputStream out = Request.getClientOutputStream();
            InputStream inputStream = new FileInputStream(file)
        ){
            String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
            for(byte b : HeaderBuilder.build(Headers.getValueFromFileExtension(extension), (short) 200, file.length())){
                out.write(b);
            }

            int readed;
            while((readed = inputStream.read()) != -1){
                out.write(readed);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void object(Object object) {
        if(object == null) throw new RuntimeException("object is null");

        try{
            OutputStream out = Request.getClientOutputStream();
            String json = Json.encode(object);
            int extraBytes = 0;

            for(char c : json.toCharArray()){
                if(c > 127){
                    extraBytes++;
                }
            }

            for(byte b : HeaderBuilder.build(Headers.APPLICATION_JSON, (short) 200, json.length() + extraBytes)) {
                out.write(b);
            }

            try(
                OutputStreamWriter writer = new OutputStreamWriter(Request.getClientOutputStream(), StandardCharsets.UTF_8);
            ){
                for(int i = 0; i < json.length(); i++) {
                    writer.append(json.charAt(i));
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
