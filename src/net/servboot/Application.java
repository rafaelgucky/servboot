package net.servboot;

import net.servboot.dependency.DependencyInjectionContainer;
import net.servboot.server.ServerManager;
import java.lang.reflect.InvocationTargetException;

public final class Application {
    private final ServerManager serverManager;

    public Application() {
      this(Integer.MAX_VALUE);
    }

    public Application(int maxRequests) {
        this(5000, maxRequests);
    }

    public Application(int port, int maxRequests) {
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
        DependencyInjectionContainer.addRequestScoped(clazz);
    }

    public void addApplicationScoped(Class<?> clazz)throws IllegalAccessException, InstantiationException, InvocationTargetException {
        DependencyInjectionContainer.addApplicationScoped(clazz);
    }
}
