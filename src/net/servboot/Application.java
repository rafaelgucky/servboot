package net.servboot;

import net.servboot.dependency.DependencyInjectionContainer;
import net.servboot.dependency.IDependencyInjectionContainer;
import net.servboot.routing.RouterManager;
import net.servboot.server.IServerManager;
import net.servboot.server.ServerManager;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public final class Application {
    private final IServerManager serverManager;
    private final IDependencyInjectionContainer dependencyInjectionContainer;

    public Application() {
      this(Byte.MAX_VALUE);
    }

    public Application(int maxRequests) {
        this(5000, maxRequests);
    }

    public Application(int port, int maxRequests) {
        serverManager = ServerManager.getInstance();
        getServerManager().setPort(port);
        getServerManager().setMaxRequests(maxRequests);
        dependencyInjectionContainer = DependencyInjectionContainer.getInstance();
    }

    public IServerManager getServerManager() {
        return serverManager;
    }

    public IDependencyInjectionContainer getDependencyInjectionContainer() {
        return dependencyInjectionContainer;
    }

    public void setMaxRequests(int maxRequests) {
        getServerManager().setMaxRequests(maxRequests);
    }

    public void init() throws Exception {
        System.out.println("ServBoot: Loading routes...");
        RouterManager.getInstance().init();
        System.out.println("ServBoot: Routes loaded!");

        System.out.println("ServBoot: Starting server...");
        getServerManager().initServer();
        System.out.println("ServBoot: Server initialized! " + getServerManager().getServer().getInetAddress().getHostName());
    }

    public void run() throws IOException {
        getServerManager().startServer();
    }

    // ============= Container DI ========================//

    public void addRequestScoped(Class<?> clazz){
        getDependencyInjectionContainer().addRequestScoped(clazz);
    }

    public void addApplicationScoped(Class<?> clazz)throws IllegalAccessException, InstantiationException, InvocationTargetException {
        getDependencyInjectionContainer().addApplicationScoped(clazz);
    }

    // ==================== LOGGER =========================//

    public void setLogger(Consumer<Exception> logger) {
        getServerManager().setLogger(logger);
    }
}
