package net.servboot.controllers;

import net.servboot.request.Request;

public class ControllerBase {
    protected Request request;

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
