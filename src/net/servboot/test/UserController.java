package net.servboot.test;

import net.servboot.annotations.Controller;
import net.servboot.annotations.GET;
import net.servboot.annotations.Path;
import net.servboot.context.DataBaseContext;
import net.servboot.controllers.ControllerBase;
import net.servboot.response.Response;

@Controller("user")
public class UserController extends ControllerBase {

    @GET
    @Path("find/all")
    public Response findAll() {
        return ok(DataBaseContext.getUserDataSet().find());
    }
}
