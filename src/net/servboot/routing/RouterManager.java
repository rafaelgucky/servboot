package net.servboot.routing;

import net.servboot.annotations.Controller;
import net.servboot.annotations.Path;
import net.servboot.dependency.DependencyInjectionContainer;
import net.servboot.dependency.IDependencyInjectionContainer;
import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.reflection.method.MethodUtils;
import net.servboot.utils.strings.StringUtils;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class RouterManager {
    private static RouterManager instance;
    private String basePath, projectName = "";
    private IDependencyInjectionContainer dependencyInjectionContainer;
    private final Map<String, Object> controllersPool = new LinkedHashMap<>();
    private final List<Route> routesPool = new LinkedList<>();

    private RouterManager() { }

    public static RouterManager getInstance() {
        if (instance == null) {
            instance = new RouterManager();
            instance.setDependencyInjectionContainer(DependencyInjectionContainer.getInstance());
        }

        return instance;
    }

    public IDependencyInjectionContainer getDependencyInjectionContainer() {
        return dependencyInjectionContainer;
    }

    private void setDependencyInjectionContainer(IDependencyInjectionContainer dependencyInjectionContainer) {
        this.dependencyInjectionContainer = dependencyInjectionContainer;
    }

    public void init() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        loadControllers(new File(getBasePath()));
    }

    public String getBasePath() throws IOException {
        if (basePath == null) {
            basePath = Collections.list(Thread.currentThread().getContextClassLoader().getResources("")).getFirst().getPath();
        }

        return basePath;
    }

    public String getProjectName() throws IOException {
        if (projectName.isEmpty() && !getBasePath().isEmpty()) {
            for (int i = getBasePath().length() - 2; i >= 0; i--) {
                if (getBasePath().charAt(i) == '/' || getBasePath().charAt(i) == '\\') break;
                projectName += getBasePath().charAt(i);
            }

            projectName = StringUtils.reverse(projectName);
        }

        return projectName;
    }

    public Map<String, Object> getControllersPool() {
        return controllersPool;
    }

    public List<Route> getRoutesPool() {
        return routesPool;
    }

    public Route getRoute(String url) {
        Route route = routesPool.stream().filter(r -> StringUtils.equalsIgnorePathParams(r.getPath(), url)).findFirst().orElse(null);

        if (route != null) {
            try {
                Object controller = getDependencyInjectionContainer().loadInstance(route.getController().getClass());
                route = new Route(route.getPath(), controller, route.getMethod());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return route;
    }

    public Object getController(String url) {
        for (Route route : routesPool) {
            if (StringUtils.equalsIgnorePathParams(route.getPath(), url)) {
                return route.getController();
            }
        }

        return null;
    }

    /**
     * Load all controllers
     * @param file Base file
     * @throws IOException If error on files
     * @throws ClassNotFoundException If error loading controller
     */
    private synchronized void loadControllers(File file) throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                loadControllers(child);
            }
        } else {
            if (file.getName().endsWith(".class")) {
                Class<?> clazz = Class.forName(
                        file.getPath()
                                .substring(file.getPath().lastIndexOf(getProjectName()) + getProjectName().length() + 1)
                                .replace("\\", ".")
                                .replace("/", ".")
                                .replace(".class", ""));

                Controller annotation = clazz.getAnnotation(Controller.class);
                if (annotation != null) {
                    getControllersPool().put(annotation.value(), clazz);

                   List<Method> methods = MethodUtils.getMethods(clazz).stream()
                           .filter(method -> method.getAnnotation(Path.class) != null).toList();

                   for (Method method : methods) {
                       getRoutesPool().add(new Route(annotation.value() + (!(annotation.value().endsWith("/") || method.getAnnotation(Path.class).value().startsWith("/")) ? "/" : "")  + method.getAnnotation(Path.class).value(), getDependencyInjectionContainer().loadInstance(clazz), method));
                   }
                }
            }
        }
    }
}
