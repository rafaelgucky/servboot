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
import java.util.Map;

@Controller("person")
public class PersonController extends ControllerBase {
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GET("test")
    @Path("test")
    public Response test() throws Exception {
        DataSet<Person> pds = DataBaseContext.getPersonDataSet();
        pds.filter("Person.id", "=", 10);

        return ok(OrmReflectionUtils.getAllEntitiesByResultSet(Person.class, ConnectionManager.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery(pds.getCommand())).stream().map(Person::getPets).toArray());
    }

    @GET()
    @Path("all")
    public Response findAll() throws Exception{
        return ok(Person.getService().findAll());
    }

    @GET("find/{id}")
    @Path("find/{id}")
    public Response find(int id) throws Exception {
        return ok(Person.getService().findById(id));
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
        for (; modelIterator.hasNext(); ) {
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
        Pet pet = new Pet();
        pet.name = "Osvaldo";

        Person person = new Person();
        person.name = "Nicanor";
        person.lastName = "Vareta";
        person.age = 25;
//        person.pet = pet;
//        personService.add(person);
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
        return ok(true);
    }

}
