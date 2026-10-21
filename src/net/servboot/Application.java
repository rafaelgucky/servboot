package net.servboot;

import net.servboot.dependency.DependencyInjectionContainer;
import net.servboot.routing.RouterManager;
import net.servboot.server.ServerManager;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public final class Application {
    public Application() {
      this(Integer.MAX_VALUE);
    }

    public Application(int maxRequests) {
        this(5000, maxRequests);
    }

    public Application(int port, int maxRequests) {
        ServerManager.getInstance().setPort(port);
        ServerManager.getInstance().setMaxRequests(maxRequests);
    }

    public void setMaxRequests(int maxRequests) {
        ServerManager.getInstance().setMaxRequests(maxRequests);
    }

    public void init() throws Exception {
        System.out.println("ServBoot: Loading routes...");
        RouterManager.init();
        System.out.println("ServBoot: Routes loaded!");

        System.out.println("ServBoot: Starting server...");
        ServerManager.getInstance().initServer();
        System.out.println("ServBoot: Server initialized! " + ServerManager.getInstance().getServer().getInetAddress().getHostName());
    }

    public void run() throws IOException {
        ServerManager.getInstance().startServer();
    }

    // ============= Container DI ========================//

    public void addRequestScoped(Class<?> clazz){
        DependencyInjectionContainer.addRequestScoped(clazz);
    }

    public void addApplicationScoped(Class<?> clazz)throws IllegalAccessException, InstantiationException, InvocationTargetException {
        DependencyInjectionContainer.addApplicationScoped(clazz);
    }

    // ==================== LOGGER =========================//

    public void setLogger(Consumer<Exception> logger) {
        ServerManager.getInstance().setLogger(logger);
    }
}
