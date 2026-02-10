package net.servboot.controllers;

import net.servboot.request.Request;
import net.servboot.response.Response;

public class ControllerBase {
    public Request Request;

    public void setRequest(Request request){
        this.Request = request;
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
