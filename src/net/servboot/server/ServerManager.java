package net.servboot.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.Consumer;

public final class ServerManager implements IServerManager {
    private static ServerManager instance;
    private int port = 5000;
    private volatile boolean running = true;
    private ServerSocket server;
    private final IClientExecutor clientExecutor;
    private Consumer<Exception> logger;

    private ServerManager(IClientExecutor clientExecutor) {
        this.clientExecutor = clientExecutor;
    }

    public static ServerManager getInstance() {
        if (instance == null) {
            instance = new ServerManager(ClientExecutor.getInstance());
        }

        return instance;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public ServerSocket getServer() {
        return server;
    }

    @Override
    public IClientExecutor getClientExecutor() {
        return clientExecutor;
    }

    @Override
    public Consumer<Exception> getLogger() {
        return logger;
    }

    @Override
    public void setLogger(Consumer<Exception> logger) {
        this.logger = logger;
    }

    @Override
    public int getMaxRequests() {
        return getClientExecutor().getMaxExecutors();
    }

    @Override
    public void setMaxRequests(int maxRequests) {
        getClientExecutor().setMaxExecutors(maxRequests);
    }

    @Override
    public void initServer() throws IOException {
        server = new ServerSocket(port);
    }

    @Override
    public void startServer() throws IOException {
        while(running) {
            getClientExecutor().addClient(server.accept());
        }
    }

    @Override
    public void stopServer() {
        running = false;
    }

    @Override
    public long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    @Override
    public long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    @Override
    public int getExecutingThreads() {
        return Thread.activeCount();
    }
}
