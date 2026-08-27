package net.servboot.test;

import net.servboot.annotations.Controller;
import net.servboot.annotations.GET;
import net.servboot.annotations.POST;
import net.servboot.annotations.Path;
import net.servboot.context.DataBaseContext;
import net.servboot.controllers.ControllerBase;
import net.servboot.database.ConnectionManager;
import net.servboot.orm.DataSet;
import net.servboot.orm.ModelIterator;
import net.servboot.response.Response;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("person")
public class PersonController extends ControllerBase {
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GET()
    @Path("all")
    public Response findAll() throws Exception {
        List<Person> persons = personService.findAll().toList();
        return ok(persons);
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
        DataSet<Person> dt = DataBaseContext.personDataSet.clone();
        ResultSet rs = ConnectionManager.getConnection().createStatement().executeQuery(dt.getCommand());
        ModelIterator<Person> modelIterator = new ModelIterator<>(Person.class, rs);
        Map<String, Object> map = new HashMap<>();

        int count = 0;
        for (Person p : modelIterator) {
            count++;
        }

        map.put("count", count);
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
