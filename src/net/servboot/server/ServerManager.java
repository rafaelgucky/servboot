package net.servboot.server;

import net.servboot.client.ClientRequestTask;
import net.servboot.thread.ThreadManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public final class ServerManager {
    private static int port = 5000;
    private static int maxRequests = Integer.MAX_VALUE;
    private static volatile boolean running = true;
    private static ServerSocket server;
    private static Consumer<Exception> logger;
    private static final List<ClientRequestTask> threadsPool = new LinkedList<>();
    private static final Stack<String> threadNames = new Stack<>();

    public static ServerSocket getServer() {
        return server;
    }

    public static void setLogger(Consumer<Exception> logger) {
        ServerManager.logger = logger;
    }

    public static Consumer<Exception> getLogger() {
        return logger;
    }

    public static List<ClientRequestTask> getThreadsPool() {
        return threadsPool;
    }

    public static Stack<String> getThreadsNames() {
        return threadNames;
    }

    public static void setPort(int p) {
        port = p;
    }

    public static void setMaxRequests(int mr) {
        maxRequests = mr;
    }

    public static boolean initServer(){
        try{
            server = new ServerSocket(port);
            server.setReceiveBufferSize(Integer.MAX_VALUE);
            return true;
        } catch(IOException ex){
            return false;
        }
    }

    public void stopServer() {
        running = false;
    }

    public static ClientRequestTask getThread() throws InterruptedException {
        synchronized (threadsPool) {
            // (getTotalMemory() * 100 / getMaxMemory() >= 90) -> se a ocupação é maior que 90%
            // (getFreeMemory() / 1024 / 1024 < 50) -> se a memória livre é menor que 50MB
            if (getTotalMemory() * 100.0 / getMaxMemory() >= 90 && getFreeMemory() / 1024 / 1024 < 50) {
                System.gc();
            }

            while (threadsPool.size() >= maxRequests) {
                threadsPool.wait();
            }

            ClientRequestTask thread = new ClientRequestTask();
            thread.setName(getThreadName());
            threadsPool.add(thread);

            return thread;
        }
    }

    public static void removeThread(ClientRequestTask thread) {
        synchronized (threadsPool) {
            threadsPool.remove(thread);
            threadsPool.notifyAll();
        }
    }

    public static String getThreadName() {
        synchronized (threadNames) {
            if (!threadNames.empty()) {
                return threadNames.pop();
            }

            return "thread_" + ThreadManager.getNext();
        }
    }

    public static void startServer() throws InterruptedException, IOException {
        while(running) {
            ClientRequestTask thread = getThread();
            Socket client = server.accept();
            thread.setClient(client);
            thread.start();
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
