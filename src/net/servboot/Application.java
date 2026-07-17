package net.servboot;

import net.servboot.database.ConnectionManager;
import net.servboot.dependency.DependencyInjectionContainer;
import net.servboot.routing.RouterManager;
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

    public void init() throws Exception {
        System.out.println("ServBoot: Loading connections...");
        ConnectionManager.init();
        System.out.println("ServBoot: Connections loaded!");

        System.out.println("ServBoot: Loading routes...");
        RouterManager.init();
        System.out.println("ServBoot: Routes loaded!");

        System.out.println("ServBoot: Starting server...");
        this.serverManager.initServer();
        System.out.println("ServBoot: Server initialized! " + this.serverManager.getServer().getLocalSocketAddress().toString());
    }

    public void run() {
        this.serverManager.startServer();
    }

    // ============= Container DI ========================//

    public void addRequestScoped(Class<?> clazz){
        DependencyInjectionContainer.addRequestScoped(clazz);
    }

    public void addApplicationScoped(Class<?> clazz)throws IllegalAccessException, InstantiationException, InvocationTargetException {
        DependencyInjectionContainer.addApplicationScoped(clazz);
    }
}
