package net.servboot.test.controller;

import net.servboot.annotations.Controller;
import net.servboot.annotations.GET;
import net.servboot.annotations.Path;
import net.servboot.controllers.ControllerBase;
import net.servboot.orm.context.DataBaseContext;
import net.servboot.response.Response;
import net.servboot.test.model.Event;

@Controller("event")
public class EventController extends ControllerBase {

    private final DataBaseContext dataBaseContext;

    public EventController(DataBaseContext dataBaseContext) {
        this.dataBaseContext = dataBaseContext;
    }

    @GET
    @Path("find/all")
    public Response findAll() {
        return ok(dataBaseContext.getEventDataSet().find());
    }

    @GET
    @Path("create")
    public Response create() {
        Event event = new Event();

        event.setName("Teste");
        event.setDescription("Teste Descrição");
        event.setOwner(dataBaseContext.getUserDataSet().filter("id", "=", 1).find().getFirst());
        event.setPeople(dataBaseContext.getPersonDataSet().find());

        dataBaseContext.getEventDataSet().put(event);
        dataBaseContext.getEventDataSet().persist();

        return ok(event);
    }
}
