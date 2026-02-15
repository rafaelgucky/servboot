package net.servboot.controllers;

import net.servboot.annotations.Controller;
import net.servboot.annotations.GET;
import net.servboot.response.Response;

import java.io.File;

@Controller("")
public class HomeController extends ControllerBase {
    @GET("/")
    public Response index() {
        return ok(new File("src/net/servboot/static/home.html"));
    }
}
