package net.servboot.test;

import net.servboot.annotations.Controller;
import net.servboot.annotations.GET;
import net.servboot.annotations.Path;
import net.servboot.controllers.ControllerBase;
import net.servboot.response.Response;
import net.servboot.server.ServerManager;
import java.util.HashMap;
import java.util.Map;

@Controller("server")
public class ServerController extends ControllerBase {

    @GET
    @Path("info")
    public Response info() {
        Map<String, Object> info = new HashMap<>();

        info.put("unit", "MB");
        info.put("totalMemory", ServerManager.getTotalMemory() / 1024 / 1024);
        info.put("freeMemory", ServerManager.getFreeMemory() / 1024 / 1024);
        info.put("maxMemory", ServerManager.getMaxMemory() / 1024 / 1024);
        info.put("memoryOccupation", (ServerManager.getTotalMemory() - ServerManager.getFreeMemory()) / 1024 / 1024);
        info.put("activeThreads", ServerManager.getExecutingThreads());

        return ok(info);
    }

    @GET
    @Path("gc")
    public Response gc() {
        System.gc();
        return info();
    }

    @GET
    @Path("count")
    public Response count() {
        return ok(ServerManager.getExecutingThreads());
    }

    @GET
    @Path("namesPool")
    public Response namesPool() {
        return ok(ServerManager.getThreadsNames());
    }
}
