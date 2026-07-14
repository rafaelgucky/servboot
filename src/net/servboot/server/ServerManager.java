package net.servboot.server;

import net.servboot.client.ClientRequestTask;
import net.servboot.thread.ThreadManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.function.Consumer;

public final class ServerManager {
    private ServerSocket server;
    private final List<Thread> threadsPool = new LinkedList<>();
    private final Stack<String> threadNames = new Stack<>();
    private final Queue<ClientRequestTask> pendingThreads = new LinkedList<>();
    private Consumer<ClientRequestTask> onAcceptClient;
    private final int port;
    private int maxRequests;
    private boolean running = true;

    public ServerManager(int port, int maxRequests) {
        this.port = port;
        this.maxRequests = maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public void setOnAcceptClient(Consumer<ClientRequestTask> onAcceptClient) {
        this.onAcceptClient = onAcceptClient;
    }

    public boolean initServer(){
        try{
            this.server = new ServerSocket(port);
            return true;
        } catch(IOException ex){
            return false;
        }
    }

    public void stopServer() {
        running = false;
    }

    private ClientRequestTask getThread() throws InterruptedException {
        synchronized (this.threadsPool) {
            while (this.threadsPool.size() >= this.maxRequests) {
                this.threadsPool.wait();
            }

            ClientRequestTask thread = new ClientRequestTask();
            thread.setName(this.getThreadName());
            this.threadsPool.add(thread);

            return thread;
        }
    }

    private void removeThread(ClientRequestTask thread) {
        synchronized (this.threadsPool) {
            this.threadsPool.remove(thread);
            this.threadsPool.notify();
        }
    }

    private String getThreadName() {
        synchronized (this.threadNames) {
            if (!threadNames.empty()) {
                return this.threadNames.pop();
            }

            return "thread_" + ThreadManager.getNext();
        }
    }

    private void startPendingThreads() {
        synchronized (this.pendingThreads) {
            if (!pendingThreads.isEmpty()) {
                this.pendingThreads.poll().start();
            }
        }
    }

    public void startServer() {
        System.out.println("ServBoot: Server is started!");

        try{
            while(running) {
                Socket client = server.accept();
                ClientRequestTask thread = this.getThread();
                thread.setClient(client);

                thread.setOnFinalize(crt -> {
                    this.threadNames.push(crt.getName());
                    this.removeThread(crt);
                    this.startPendingThreads();

                    if (this.onAcceptClient != null) {
                        this.onAcceptClient.accept(crt);
                    }
                });

                if (this.threadsPool.size() >= this.maxRequests) {
                    this.pendingThreads.add(thread);
                } else {
                    thread.start();
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }
}
