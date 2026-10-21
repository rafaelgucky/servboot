package net.servboot.server;

import net.servboot.client.ClientRequestTask;

import java.net.Socket;
import java.util.List;
import java.util.Queue;

public interface IClientExecutor {
    void setMaxExecutors(int maxExecutors);
    int  getMaxExecutors();
    Queue<Socket> getClients() ;
    List<ClientRequestTask> getExecutors();
    void start() throws InterruptedException;
    void stop();
    void addClient(Socket socket);
}
