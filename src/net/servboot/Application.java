package net.servboot;

import net.servboot.server.ServerManager;

public final class Application {
    private final ServerManager serverManager;
    private final Thread curentThread;

    public Application() {
      this(Integer.MAX_VALUE);
    }

    public Application(int maxRequests) {
        this(5000, maxRequests);
    }

    public Application(int port, int maxRequests) {
        this.curentThread = Thread.currentThread();
        this.serverManager = new ServerManager(port, maxRequests);
    }

    public void setMaxRequests(int maxRequests) {
        this.serverManager.setMaxRequests(maxRequests);
    }

    public void run() {
        if(this.serverManager.initServer()){
            this.serverManager.startServer();
        } else {
            throw new RuntimeException("Error on server startup");
        }
    }

    // ============= Container DI ========================//

    public void addRequestScoped(Class<?> clazz){
        this.serverManager.addRequestScoped(clazz);
    }

    public void addApplicationScoped(Class<?> clazz){
        this.serverManager.addApplicationScoped(clazz);
    }
}
