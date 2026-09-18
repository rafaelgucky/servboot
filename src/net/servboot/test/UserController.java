package net.servboot.test;

import net.servboot.annotations.Controller;
import net.servboot.annotations.GET;
import net.servboot.annotations.Path;
import net.servboot.controllers.ControllerBase;
import net.servboot.orm.context.DataBaseContext;
import net.servboot.response.Response;

@Controller("user")
public class UserController extends ControllerBase {

    private final DataBaseContext dataBaseContext;

    public UserController(DataBaseContext dataBaseContext) {
        this.dataBaseContext = dataBaseContext;
    }

    @GET
    @Path("find/all")
    public Response findAll() {
        return ok(dataBaseContext.getUserDataSet().find());
    }

    @GET
    @Path("create")
    public Response create(User user) {
        User u = new User();
        u.setPerson(dataBaseContext.getPersonDataSet().find().getFirst());
        dataBaseContext.getUserDataSet().put(u);
        dataBaseContext.getUserDataSet().persist();
        return ok(u);
    }
}
