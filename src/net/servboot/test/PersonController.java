package net.servboot.test;

import net.servboot.annotations.Controller;
import net.servboot.annotations.GET;
import net.servboot.annotations.POST;
import net.servboot.annotations.Path;
import net.servboot.context.DataBaseContext;
import net.servboot.controllers.ControllerBase;
import net.servboot.database.ConnectionManager;
import net.servboot.response.Response;
import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Controller("person")
public class PersonController extends ControllerBase {
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GET()
    @Path("all")
    public Response findAll() {
        return ok(DataBaseContext.getPersonDataSet().find());
    }

    @GET("find/{id}")
    @Path("find/{id}")
    public Response find(int id) throws Exception {
        return ok(personService.findById(id));
    }

    @GET("find/index/{index}")
    @Path("find/index/{index}")
    public Response findByIndex(int index){
        return ok();
    }

    @GET()
    @Path("count")
    public Response count() throws Exception {
        Map<String, Object> map = new HashMap<>();

        map.put("count", personService.findAll().toList().size());
        map.put("utc", Instant.now().toString());

        return ok(map);
    }

    @POST("create")
    @Path("create")
    public Response create(Person person) throws Exception {
        ConnectionManager.begin();
//        personService.add(person);
        ConnectionManager.commit();
        return ok(person);
    }

    @POST("addImage")
    @Path("addImage")
    public Response addImage(File file) throws Exception {
        return file(new FileInputStream(file), file.getName());
    }

    @GET("add")
    @Path("add")
    public Response add(){
        Person person = new Person();
        return ok(person);
    }

    @GET("update")
    @Path("update")
    public Response update(){
        return ok(true);
    }

}
