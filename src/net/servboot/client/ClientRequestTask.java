package net.servboot.client;

import net.servboot.controllers.ControllerBase;
import net.servboot.headers.HeaderBuilder;
import net.servboot.headers.Headers;
import net.servboot.io.FormDataReader;
import net.servboot.io.RequestBufferedReader;
import net.servboot.io.ShortArrayInputStream;
import net.servboot.request.Request;
import net.servboot.request.RequestMapper;
import net.servboot.utils.inputstream.ByteArrayInputStreamUtils;
import net.servboot.utils.inputstream.InputStreamUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClientRequestTask extends Thread {
    public ServerSocket server;
    public Socket client;
    private final List<Thread> threads;
    private final List<ControllerBase> controllers;
    private static final String currentPath = System.getProperty("user.dir");
    private static final String faviconPath = "\\src\\net\\servboot\\static\\favicon.ico";
    private static final String homePath = "\\src\\net\\servboot\\static\\home.html";
    private static final String catPath = "\\src\\net\\servboot\\static\\images\\gato.jpg";

    public ClientRequestTask(ServerSocket server, Socket client, List<Thread> threads, List<ControllerBase> controllers) {
        this.server = server;
        this.client = client;
        this.threads = threads;
        this.controllers = controllers;
    }

    @Override
    public void run() {
        OutputStream out = null;
        boolean isFavicon = false;

        try {
            out = client.getOutputStream();
            InputStream in =  client.getInputStream();
            ShortArrayInputStream shortArray = new ShortArrayInputStream(in);
            InputStreamReader isr = new InputStreamReader(shortArray, StandardCharsets.UTF_8);
            RequestBufferedReader rbr = new RequestBufferedReader(isr);
            RequestMapper requestMapper = new RequestMapper();

            String url = rbr.readHeaderLine();
            if(url.isEmpty()){
                isFavicon = true;
                return;
            }
            Request request = requestMapper.map(url);

            System.out.println(url);

            String line;
            while (!(line = rbr.readHeaderLine()).isEmpty()) {
                System.out.println(line);
                request.addHeader(line);

                if(!request.getCookies().isEmpty()){
                    System.out.println("----------------- COOKIES --------------------");
                    for(Map.Entry<String, String> cookie : request.getCookies().entrySet()){
                        System.out.println(cookie.getKey() + ": " + cookie.getValue());
                    }
                    System.out.println("----------------------------------------------");
                }

                if(request.getContentLength() > 0){
                    //client.setSoTimeout(15000);
                    if(request.getContentType().contains("multipart/form-data")
                        || request.getContentType().contains("json"))
                    {
                        FormDataReader fdr = new FormDataReader(in, isr, shortArray, request.getHeader("boundary"), request.getContentLength());
                        Map<String, Object> formData = fdr.readFormData();
                        for(Map.Entry<String, Object> entry : formData.entrySet()){
                            System.out.println(entry.getKey() + ": " + entry.getValue());
                        }
                        //request.setStringBody(rbr.readBody(request.getContentLength()));
                        break;
                    } else if(request.getContentType().contains("jpeg")
                                || request.getContentType().contains("jpg")
                                ||  request.getContentType().contains("png")
                                ||  request.getContentType().contains("pdf")
                                ||  request.getContentType().contains("text/plain"))
                    {

                        break;
                    }
                }



                // TEMPORÁRIO
                else if(request.getUrl().contains("favicon.ico")){
                    File favicon = new File(currentPath + faviconPath);
                    isFavicon = true;

                    System.out.println("GET FAVICON");
                    out.write(HeaderBuilder.build(Headers.IMAGE_ICON, favicon.length(), favicon.getName()));
                    FileInputStream fis = new FileInputStream(favicon);

                    out.write(fis.readAllBytes());
                    break;
                } else if(request.getUrl().contains("gato.jpg")){
                    File catFile = new File(currentPath + catPath);
                    isFavicon = true;

                    System.out.println("GET CAT");
                    out.write(HeaderBuilder.build(Headers.IMAGE_JPG, catFile.length(), catFile.getName()));
                    FileInputStream fis = new FileInputStream(catFile);

                    out.write(fis.readAllBytes());
                    break;
                }
            }

            if(isFavicon) {
                return;
            }

            //System.out.println(request.getStringBody());

            // Chamar o controller
            ControllerBase controller = requestMapper.invoke(controllers);
            if(controller != null && controllers.stream().noneMatch(c -> c.getClass().equals(controller.getClass()))) {
                this.controllers.add(controller);
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } finally {
            if(!isFavicon && out != null){
                try{
                    File body = new File(currentPath + homePath);
                    InputStream is = new FileInputStream(body);

                    out.write(HeaderBuilder.build(Headers.TEXT_HTML, body.length()));
                    out.write(is.readAllBytes());
                    out.flush();
                    //out.close();
                } catch (IOException ex) {
                    System.out.println("Erro ao fechar a stream de saída: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            System.out.println("----- Fim da leitura dos dados do cliente -----");
            threads.remove(this);
        }
    }
}
