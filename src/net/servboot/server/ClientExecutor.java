package net.servboot.server;

import net.servboot.client.ClientRequestTask;

import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class ClientExecutor implements IClientExecutor {
    private static ClientExecutor instance;
    private int maxExecutors = Byte.MAX_VALUE;
    private Thread executor;
    private final Queue<Socket> clients;
    private final List<ClientRequestTask> executors;
    private final Stack<String> threadNames;

    private ClientExecutor() {
        clients = new LinkedList<>();
        executors = new LinkedList<>();
        threadNames = new Stack<>();
    }

    public static ClientExecutor getInstance() {
        synchronized (ClientExecutor.class) {
            if (instance == null) {
                instance = new ClientExecutor();
                try {
                    instance.start();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return instance;
    }

    @Override
    public int getMaxExecutors() {
        return maxExecutors;
    }

    @Override
    public void setMaxExecutors(int maxExecutors) {
        this.maxExecutors = maxExecutors;
    }

    @Override
    public Queue<Socket> getClients() {
        return clients;
    }

    @Override
    public void addClient(Socket client) {
        synchronized (clients) {
            clients.add(client);
            clients.notifyAll();
        }
    }

    @Override
    public List<ClientRequestTask> getExecutors() {
        return executors;
    }

    public Stack<String> getThreadNames() {
        return threadNames;
    }

    @Override
    public void start() throws InterruptedException {
        if (executor != null) {
            return;
        }

        fillThreadNamesPool();

        executor = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    synchronized (clients) {
                        Socket client;
                        ClientRequestTask thread;

                        while (clients.isEmpty()) {
                            clients.wait();
                        }

                        do {
                            client = clients.poll();
                        } while (client == null || client.isClosed() || !client.isConnected());

                        thread = getThread();
                        thread.setClient(client);
                        thread.start();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        executor.start();
    }

    @Override
    public void stop() {
        executor.interrupt();
    }

    private void fillThreadNamesPool() {
        for (int i = maxExecutors; i > 0; i--) {
            threadNames.push("thread_" + i);
        }
    }

    private ClientRequestTask getThread() throws InterruptedException {
        synchronized (executors) {
            long usedMemory = ServerManager.getInstance().getTotalMemory() - ServerManager.getInstance().getFreeMemory();

            // if the memory used is greater than 90% of the total memory
            if (usedMemory > ServerManager.getInstance().getMaxMemory() * 0.9) {
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

    private String getThreadName() {
        synchronized (threadNames) {
            return threadNames.pop();
        }
    }

    public void removeThread(ClientRequestTask thread) {
        synchronized (executors) {
            executors.remove(thread);
            executors.notifyAll();
        }
    }
}
