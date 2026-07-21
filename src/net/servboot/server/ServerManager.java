package net.servboot.server;

import net.servboot.client.ClientRequestTask;
import net.servboot.thread.ThreadManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public final class ServerManager {
    private static ServerSocket server;
    private static final List<ClientRequestTask> threadsPool = new LinkedList<>();
    private static final Stack<String> threadNames = new Stack<>();
    private static int port = 5000;
    private static int maxRequests = Integer.MAX_VALUE;
    private static boolean running = true;

    public static ServerSocket getServer() {
        return server;
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
            long freeMemory = Runtime.getRuntime().freeMemory();
            long totalMemory = Runtime.getRuntime().totalMemory();
            long maxMemory = Runtime.getRuntime().maxMemory();

            if (totalMemory * 100 / maxMemory >= 90 && freeMemory / 1024 / 1024 < 50) {
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
}
