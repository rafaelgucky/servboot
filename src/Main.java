import net.servboot.client.ClientRequestTask;
import net.servboot.controllers.ControllerBase;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Thread> threadsPool = new LinkedList<>();
        List<ControllerBase> controllersPool = new LinkedList<>();

        // Server
        ServerSocket server = null;

        try{
            server = new ServerSocket(5000);
            while(true) {
                if(threadsPool.size() <= 10){
                    Socket client = server.accept();
                    ClientRequestTask c = new ClientRequestTask(server, client, threadsPool, controllersPool);
                    threadsPool.add(c);
                    c.start();
                } else {
                    System.out.println("Sem espaço para mais requisições: " + threadsPool.size());
                    Thread.sleep(10);
                }
            }
        } catch (Exception ex){
            System.out.println("Erro na inicialização do servidor: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
