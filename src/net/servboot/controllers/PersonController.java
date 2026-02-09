package net.servboot.controllers;

import net.servboot.annotations.Controller;
import net.servboot.annotations.GET;
import net.servboot.annotations.POST;
import net.servboot.models.Person;
import net.servboot.response.Response;
import net.servboot.service.PersonService;

@Controller("/api/person")
public class PersonController extends ControllerBase {
    private final PersonService personService;

    public PersonController(PersonService personService) {
       this.personService = personService;
    }

    @POST("/{age}/{lastName}/{name}")
    public Response findAll(String name, int age, String lastName, String email, Person person) {
        System.out.println("-------------------------- PERSON FIND ALL -----------------------");
        System.out.println("URL: " + Request.getUrl());
        System.out.println("NAME: " + name);
        System.out.println("LAST NAME: " + lastName);
        System.out.println("AGE: " + age);
        System.out.println("EMAIL: " + email);
        System.out.println("AUTHORIZATION: " + Request.getHeader("Authorization"));
        System.out.println("PERSON: " + person);
        System.out.println("INJECTED PERSON: " + personService.getPerson());
//        System.out.println("FILE: " + pdf.size());
//        System.out.println("IMAGES: " + imgs.size());
        System.out.println("------------------------------------------------------------------");
        return Response.badRequest().object(person);
    }

    @GET("/create")
    public void create(String name){
        System.out.println("-------------------------- PERSON CREATE -------------------------");
        System.out.println("Name: " + name);
        text("Name: " + name);
        System.out.println("------------------------------------------------------------------");
    }
}
