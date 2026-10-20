package net.servboot.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.Consumer;

public final class ServerManager {
    private static int port = 5000;
    private static volatile boolean running = true;
    private static ServerSocket server;
    private static Consumer<Exception> logger;

    public static ServerSocket getServer() {
        return server;
    }

    public static void setLogger(Consumer<Exception> logger) {
        ServerManager.logger = logger;
    }

    public static Consumer<Exception> getLogger() {
        return logger;
    }


    public static void setPort(int p) {
        port = p;
    }

    public static void setMaxRequests(int mr) {
        ThreadExecutor.getInstance().setMaxExecutors(mr);
    }

    public static boolean initServer(){
        try{
            server = new ServerSocket(port);
            return true;
        } catch(IOException ex){
            return false;
        }
    }

    public void stopServer() {
        running = false;
    }

    public static void startServer() throws IOException {
        while(running) {
            ThreadExecutor.getInstance().addClient(server.accept());
        }
    }

    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public  static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public static int getExecutingThreads() {
        return Thread.activeCount();
    }
}
