package net.servboot.test;

import net.servboot.annotations.Controller;
import net.servboot.annotations.GET;
import net.servboot.annotations.POST;
import net.servboot.annotations.Path;
import net.servboot.orm.Select;
import net.servboot.orm.context.DataBaseContext;
import net.servboot.controllers.ControllerBase;
import net.servboot.orm.context.DataSet;
import net.servboot.orm.Insert;
import net.servboot.response.Response;
import net.servboot.utils.reflection.ReflectionUtils;
import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("person")
public class PersonController extends ControllerBase {

    private final PersonService personService;
    private final DataBaseContext dataBaseContext;

    public PersonController(PersonService personService, DataBaseContext dataBaseContext) {
        personService.setPersonDataSet(dataBaseContext.getPersonDataSet());
        this.personService = personService;
        this.dataBaseContext = dataBaseContext;
    }

    @GET()
    @Path("all")
    public Response findAll() throws Exception {
        DataSet<Person> personDataSet = dataBaseContext.getPersonDataSet();
        List<Person> people = personDataSet.find();

        return ok(people);
    }

    @GET("find/{id}")
    @Path("find/{id}")
    public Response find(int id) throws Exception {
        Select<Person> select = Select.of(Person.class, Map.of(Person.class, ReflectionUtils.getAllFields(Person.class).stream().filter(f -> f.getName().equalsIgnoreCase("id")).collect(Collectors.toSet())));
        return ok(personService.findById(id));
    }

    @GET("find/index/{index}")
    @Path("find/index/{index}")
    public Response findByIndex(int index){
        return ok(dataBaseContext.getPersonDataSet().find().get(index));
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
        p.setName("Teste");
        p.setLastName("Silva");
        p.setCpf("123.456.789-01");
        p.setDateOfBirth(Date.from(Instant.now()));
        p.setEmail("teste1@gmail.com");
        p.setRecordDateTime(LocalDateTime.now());
        Insert<Person> insert = new Insert<>(p);
        String command = insert.getCommand();

        DataSet<Person> dataSet = this.dataBaseContext.getPersonDataSet();

        dataSet.put(p);
        dataSet.persist();
        return ok(p);
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
