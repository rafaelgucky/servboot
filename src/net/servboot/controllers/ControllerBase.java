package net.servboot.controllers;

import net.servboot.client.ClientRequestTask;
import net.servboot.io.ServBootFile;
import net.servboot.request.Request;
import net.servboot.request.Route;
import net.servboot.response.Response;

import java.io.InputStream;

public class ControllerBase {

    public Request getRequest() {
        if (Thread.currentThread() instanceof ClientRequestTask thread) {
            return thread.getRequest();
        }

        return null;
    }

    public Response file(InputStream inputStream, String fileName) {
        return new Response(200).setBody(new ServBootFile(inputStream, fileName, false));
    }

    public Response file(InputStream inputStream, String fileName, boolean download) {
        return new Response(200).setBody(new ServBootFile(inputStream, fileName, download));
    }

    public Response ok(Object object){
        return new Response(200).setBody(object);
    }

    public Response ok(){
        return new Response(200);
    }

    public Response created(Object object){
        return new Response(201).setBody(object);
    }

    public Response created(){
        return new Response(201);
    }

    public Response badRequest(Object object){
        return new Response(400).setBody(object);
    }

    public Response badRequest(){
        return new Response(400);
    }

    public Response notFound(Object object){
        return new Response(404).setBody(object);
    }

    public Response notFound(){
        return new Response(404);
    }
}
