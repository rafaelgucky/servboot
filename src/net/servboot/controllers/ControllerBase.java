package net.servboot.controllers;

import net.servboot.headers.HeaderBuilder;
import net.servboot.headers.Headers;
import net.servboot.request.Request;
import net.servboot.utils.json.Json;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class ControllerBase {
    public Request Request;

    public void text(String text){
        try(
            OutputStream out = Request.getClientOutputStream();
        ){
            for(byte b : HeaderBuilder.build(Headers.TEXT_TXT, text.length())){
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
        List<Short> bytes;

        if(file == null) throw new RuntimeException("file is null");
        else if(!file.exists()) throw new RuntimeException("file does not exist");
        else if(!file.isFile()) throw new RuntimeException("file is not a file");

        bytes = new LinkedList<>();

        String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1);
        for(byte b : HeaderBuilder.build(Headers.getValueFromFileExtension(extension), file.length())){
            bytes.add((short) b);
        }

        try (
            InputStream inputStream = new FileInputStream(file);
        ){
            int readed;
            while((readed = inputStream.read()) != -1){
                bytes.add((short) readed);
            }
        } catch (IOException  ioException){
            throw new RuntimeException(ioException);
        }

        try(
            OutputStream out = Request.getClientOutputStream();
        ){
            for (Short b : bytes) {
                out.write(b);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void object(Object object) {
        List<Short> bytes;
        if(object == null) throw new RuntimeException("object is null");

        bytes = new LinkedList<>();

        try{
            OutputStream out = Request.getClientOutputStream();
            String json = Json.encode(object);
            int extraBytes = 0;

            for(char c : json.toCharArray()){
                if(c > 127){
                    extraBytes++;
                }
            }

            for(byte b : HeaderBuilder.build(Headers.APPLICATION_JSON, json.length() + extraBytes)) {
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
