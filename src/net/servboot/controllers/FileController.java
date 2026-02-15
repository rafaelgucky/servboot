package net.servboot.controllers;

import net.servboot.annotations.Controller;
import net.servboot.annotations.GET;
import net.servboot.annotations.POST;
import net.servboot.response.Response;
import net.servboot.service.FileService;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Controller("/api/files")
public class FileController extends ControllerBase {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @POST("insert")
    public Response save(File file) {
        return ok(fileService.save(file));
    }

    @GET("count")
    public Response count(){
        Map<String, Long> map = new HashMap<>();
        map.put("count", fileService.count());
        map.put("Teste", 3425L);

        return ok(map);
    }

    @GET("find/{name}")
    public Response find(String name){
        Map<String, InputStream> map = fileService.find(name);

        if(!map.isEmpty()){
            String fileName = (String) map.keySet().toArray()[0];
            return file(map.get(fileName), fileName, false);
        }

        return notFound(new File("src/net/servboot/static/home.html"));
    }

    @GET("find/{id}")
    public Response find(int id){
        Map<String, InputStream> map = fileService.find(id);

        if(!map.isEmpty()){
            String fileName = (String) map.keySet().toArray()[0];
            return file(map.get(fileName), fileName, false);
        }

        return notFound(new File("src/net/servboot/static/home.html"));
    }

    @GET("find/all/names")
    public Response findAllNames(){
        return ok(fileService.findAllNames());
    }

    @GET("")
    public Response insertView(){
        return ok(new File("src/net/servboot/static/files/insertFile.html"));
    }
}
