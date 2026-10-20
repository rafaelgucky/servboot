package net.servboot.server;

import net.servboot.client.ClientRequestTask;
import net.servboot.thread.ThreadManager;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class ThreadExecutor {
    private static ThreadExecutor instance;
    private int maxExecutors = Integer.MAX_VALUE;
    private final Queue<Socket> clients;
    private final List<ClientRequestTask> executors;
    private final Stack<String> threadNames;

    private ThreadExecutor() {
        clients = new LinkedList<>();
        executors = new LinkedList<>();
        threadNames = new Stack<>();
    }

    public static ThreadExecutor getInstance() {
        synchronized (ThreadExecutor.class) {
            if (instance == null) {
                instance = new ThreadExecutor();
                try {
                    instance.start();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return instance;
    }

    public int setMaxExecutors() {
        return maxExecutors;
    }

    public void setMaxExecutors(int maxExecutors) {
        this.maxExecutors = maxExecutors;
    }

    public Queue<Socket> getClients() {
        return clients;
    }

    public void addClient(Socket client) {
        synchronized (clients) {
            clients.add(client);
            clients.notifyAll();
        }
    }

    public List<ClientRequestTask> getExecutors() {
        return executors;
    }

    public Stack<String> getThreadNames() {
        return threadNames;
    }

    private void start() throws InterruptedException {
        Thread executor = new Thread(() -> {
            while (true) {
                try {
                    while (true) {
                        synchronized (clients) {
                            Socket client;
                            ClientRequestTask thread;

                            while (clients.isEmpty()) {
                                clients.wait();
                            }

                            do {
                                client = clients.poll();
                            } while (client == null || client.isClosed());

                            thread = getThread();
                            thread.setClient(client);
                            thread.start();
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        executor.start();
    }

    public ClientRequestTask getThread() throws InterruptedException {
        synchronized (executors) {
            // (getTotalMemory() * 100 / getMaxMemory() >= 90) -> se a ocupação é maior que 90%
            // (getFreeMemory() / 1024 / 1024 < 50) -> se a memória livre é menor que 50MB
            if (ServerManager.getTotalMemory() * 100.0 / ServerManager.getMaxMemory() >= 90 && ServerManager.getFreeMemory() / 1024 / 1024 < 50) {
                System.gc();
            }

            while (executors.size() >= maxExecutors) {
                executors.wait();
            }

            ClientRequestTask thread = new ClientRequestTask();
            thread.setName(getThreadName());
            executors.add(thread);

            return thread;
        }
    }

    public void removeThread(ClientRequestTask thread) {
        synchronized (executors) {
            executors.remove(thread);
            executors.notifyAll();
        }
    }

    public String getThreadName() {
        synchronized (threadNames) {
            if (!threadNames.empty()) {
                return threadNames.pop();
            }

            return "thread_" + ThreadManager.getNext();
        }
    }
}
