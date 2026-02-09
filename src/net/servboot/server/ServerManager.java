package net.servboot.server;

import net.servboot.client.ClientRequestTask;
import net.servboot.controllers.ControllerBase;
import net.servboot.utils.reflection.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class ServerManager {
    private ServerSocket server;
    private List<Thread> threadsPool = new LinkedList<>();
    private List<ControllerBase> controllersPool = new LinkedList<>();
    private List<Class<?>> requestContainerDI = new  LinkedList<>();
    private List<Object> aplicationContainerDI = new  LinkedList<>();
    private int port;
    private int maxRequests;
    private boolean running = true;

    public ServerManager(int port, int maxRequests) {
        this.port = port;
        this.maxRequests = maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public boolean initServer(){
        try{
            server = new ServerSocket(port);
            return true;
        } catch(IOException ex){
            return false;
        }
    }

    public void startServer() {
        try{
            while(running) {
                if(threadsPool.size() <= maxRequests){
                    Socket client = server.accept();
                    ClientRequestTask c = new ClientRequestTask(server, client, Thread.currentThread(), threadsPool, controllersPool, requestContainerDI, aplicationContainerDI);
                    threadsPool.add(c);
                    c.start();
                } else {
                    Thread.sleep(10);
                }
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void stopServer() {
        running = false;
    }

    // ================ Container DI ================== //

    public void addRequestScoped(Class<?> clazz){
        this.requestContainerDI.add(clazz);
    }

    public void addApplicationScoped(Class<?> clazz){
        aplicationContainerDI.add(ReflectionUtils.instantiate(clazz));
    }
}
