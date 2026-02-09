package net.servboot.response;

import net.servboot.headers.HeaderBuilder;
import net.servboot.headers.Headers;
import net.servboot.request.Request;
import net.servboot.utils.json.Json;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class Response {
    private Request request;
    private Response response;

    public Response(Request request) {
        this.request = request;
    }

    private short responseCode;

    private Response(short code) {
        responseCode = code;
    }

    public Response ok() {
        this.responseCode = 200;
        return this;
    }

    public Response created() {
        this.responseCode = 201;
        return this;
    }

    public Response noContent() {
        this.responseCode = 204;
        return this;
    }

    public Response badRequest() {
        this.responseCode = 400;
        return this;
    }

    public Response unauthorized() {
        this.responseCode = 401;
        return this;
    }

    public Response forbidden() {
        this.responseCode = 403;
        return this;
    }

    public Response notFound() {
        this.responseCode = 404;
        return this;
    }

    public Response internalServerError() {
        this.responseCode = 500;
        return this;
    }

    public Response object(Object object) {
        if(object == null) throw new RuntimeException("object is null");

        try{
            OutputStream out = request.getClientOutputStream();
            String json = Json.encode(object);
            int extraBytes = 0;

            for(char c : json.toCharArray()){
                if(c > 127){
                    extraBytes++;
                }
            }

            for(byte b : HeaderBuilder.build(Headers.APPLICATION_JSON, responseCode, json.length() + extraBytes)) {
                out.write(b);
            }

            try(
                    OutputStreamWriter writer = new OutputStreamWriter(request.getClientOutputStream(), StandardCharsets.UTF_8);
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
        return this;
    }
}
