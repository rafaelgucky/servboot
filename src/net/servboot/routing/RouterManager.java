package net.servboot.routing;

import net.servboot.annotations.Controller;
import net.servboot.annotations.Path;
import net.servboot.dependency.DependencyInjectionContainer;
import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.reflection.method.MethodUtils;
import net.servboot.utils.strings.StringUtils;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class RouterManager {
    private static String basePath, projectName = "";
    private static final Map<String, Object> controllersPool = new LinkedHashMap<>(); // Controller name : Controller
    private static final List<Route> routesPool = new LinkedList<>();

    public static void init() throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
        loadControllers(new File(getBasePath()));
        int x = 1;
    }

    public static String getBasePath() throws IOException {
        if (basePath == null) {
            basePath = Collections.list(Thread.currentThread().getContextClassLoader().getResources("")).getFirst().getPath();
        }

        return basePath;
    }

    public static String getProjectName() throws IOException {
        if (projectName.isEmpty() && !getBasePath().isEmpty()) {
            for (int i = getBasePath().length() - 2; i >= 0; i--) {
                if (getBasePath().charAt(i) == '/' || getBasePath().charAt(i) == '\\') break;
                projectName += getBasePath().charAt(i);
            }

            projectName = StringUtils.reverse(projectName);
        }

        return projectName;
    }

    public static Map<String, Object> getControllersPool() {
        return controllersPool;
    }

    public static List<Route> getRoutesPool() {
        return routesPool;
    }

    public static Route getRoute(String url) {
        return routesPool.stream().filter(route -> StringUtils.equalsIgnorePathParams(route.getPath(), url)).findFirst().orElse(null);
    }

    public static Object getController(String url) {
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
    private static synchronized void loadControllers(File file) throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException {
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
                       getRoutesPool().add(new Route(annotation.value() + (!(annotation.value().endsWith("/") || method.getAnnotation(Path.class).value().startsWith("/")) ? "/" : "")  + method.getAnnotation(Path.class).value(), ReflectionUtils.instantiate(clazz, true), method));
                   }
                }
            }
        }
    }
}
