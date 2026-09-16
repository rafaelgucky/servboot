package net.servboot.test;

import net.servboot.annotations.Controller;
import net.servboot.annotations.GET;
import net.servboot.annotations.POST;
import net.servboot.annotations.Path;
import net.servboot.orm.context.DataBaseContext;
import net.servboot.controllers.ControllerBase;
import net.servboot.orm.DataSet;
import net.servboot.orm.Insert;
import net.servboot.response.Response;
import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("person")
public class PersonController extends ControllerBase {

    private final PersonService personService;
    private final DataBaseContext dataBaseContext;

    public PersonController(PersonService personService, DataBaseContext dataBaseContext) {
        this.personService = personService;
        this.dataBaseContext = dataBaseContext;
    }

    @GET()
    @Path("all")
    public Response findAll() {
        DataSet<Person> personDataSet = dataBaseContext.getPersonDataSet();
        List<Person> people = personDataSet.find();

        return ok(people);
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
    public Response create(Person person) {
        Person p = new Person();
        p.setId(1);
        p.setName("Teste");
        p.setCpf("123.456.789-00");
        p.setDateOfBirth(Date.from(Instant.now()));
        p.setEmail("teste@gmail.com");
        p.setRecordDateTime(LocalDateTime.now());
        Insert<Person> insert = new Insert<>(p);
        String command = insert.getCommand();

        DataSet<Person> dataSet = this.dataBaseContext.getPersonDataSet();

        dataSet.add(person);
        return ok(person);
    }

    @POST
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
