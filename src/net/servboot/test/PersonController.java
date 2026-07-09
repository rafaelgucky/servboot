package net.servboot.test;

import net.servboot.annotations.Controller;
import net.servboot.annotations.GET;
import net.servboot.annotations.POST;
import net.servboot.annotations.Path;
import net.servboot.controllers.ControllerBase;
import net.servboot.database.ConnectionManager;
import net.servboot.database.ServBootQuery;
import net.servboot.response.Response;
import net.servboot.utils.reflection.ColumnUtils;

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

    @GET("test")
    @Path("test")
    public Response test() {
        ServBootQuery<Person> sbq = new ServBootQuery<>(Person.class);
        ServBootQuery<Pet> sql2 = sbq.map(Person::getPet);
//        List<String> columns = sbq.getColumns();
        return ok(ColumnUtils.getDataBaseName(Person.class, "Pet.id"));
    }

    @GET("all")
    @Path("all")
    public Response findAll() throws Exception {
        return ok(personService.findAll());
    }

    @GET("find/{id}")
    @Path("find/{id}")
    public Response find(int id){
        return ok(personService.find(id));
    }

    @GET("find/index/{index}")
    @Path("find/index/{index}")
    public Response findByIndex(int index){
        return ok(personService.findByIndex(index));
    }

    @GET("count")
    @Path("count")
    public Response count() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("count", personService.count());
        map.put("utc", Instant.now().toString());
        return ok(map);
    }

    @POST("create")
    @Path("create")
    public Response create(Person person) throws Exception {
        ConnectionManager.begin();
        personService.add(person);
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
        Pet pet = new Pet();
        pet.name = "Osvaldo";

        Person person = new Person();
        person.name = "Nicanor";
        person.lastName = "Vareta";
        person.age = 25;
//        person.pet = pet;
        personService.add(person);
        return ok(person);
    }

    @GET("update")
    @Path("update")
    public Response update(){
        Person person = new Person();
        person.id = 6800;
        person.name = "Nicanor";
        person.lastName = "Vassoura";
        person.age = 45;
        return ok(personService.update(person));
    }

}
