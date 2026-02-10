package net.servboot.request;

import net.servboot.annotations.Controller;
import net.servboot.annotations.methods.MethodAnnotationService;
import net.servboot.controllers.ControllerBase;
import net.servboot.utils.reflection.AnnotationResearcher;
import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.url.StringUrlUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class RequestMapper {
    private AnnotationResearcher anr;
    private final List<Object> paramsToInvoke = new LinkedList<>();
    private List<Class<?>> requestContainerDI;
    private List<Object> applicationContainerDI;
    private static final String currentPath = System.getProperty("user.dir");
    private static final String appMapping = "\\src\\mapping.json";
    private Request request;

    public RequestMapper(Thread currentThread, List<Object> applicationContainerDI, List<Class<?>> requestContainerDI) {
        anr = new AnnotationResearcher(currentThread);
        this.requestContainerDI = requestContainerDI;
        this.applicationContainerDI = applicationContainerDI;
    }

    public Request map(String url){
        String method;
        String newUrl;
        String requestRoute;

        // Revisar
        if(url == null || url.isEmpty()) throw new RuntimeException("The URL cannot be empty");

        url = url.trim();

        method = url.substring(0, url.indexOf(' '));
        url = url.replace(method, "");
        newUrl = url.substring(0, url.lastIndexOf(' ')).trim();
        requestRoute = newUrl.substring(0, (newUrl.indexOf("?") > 0 ? newUrl.indexOf("?") : newUrl.length())).trim();

        request = new Request(method, requestRoute.substring(0, (requestRoute.lastIndexOf('/') >= url.length() - 1 ? requestRoute.lastIndexOf('/') : requestRoute.length())));

        if(newUrl.contains("?")){
            String queryStrings = newUrl.substring(newUrl.indexOf("?") + 1);

            while(!queryStrings.isEmpty()){
                String queryStringName = queryStrings.substring(0, (queryStrings.contains("=") ?  queryStrings.indexOf('=') : queryStrings.length()));
                String queryStringValue = queryStrings.substring(queryStrings.indexOf('=') + 1, (queryStrings.contains("&") ?  queryStrings.indexOf('&') : queryStrings.length()));
                request.addParameter(queryStringName, queryStringValue);
                queryStrings = queryStrings.substring((queryStrings.contains("&") ? queryStrings.indexOf("&") + 1 : queryStrings.length()));
            }
        }

        return request;
    }

    public Object invoke(List<ControllerBase> controllers){
        List<Class<?>> classes;

        try {
            classes = anr.getClasses();
            if(classes == null || classes.isEmpty()){
                classes = anr.findClazz();
            }
            // Define a classe e o metodo da requisição
            if(setClazzMethod(classes)){
                Map<Object, Parameter> params = ReflectionUtils.sortParameters(request, ReflectionUtils.getMethodParameters(request.getMethod()));

                if(params != null){
                    for(Entry<Object, Parameter> entry : params.entrySet()){
                        paramsToInvoke.add(ReflectionUtils.convertFromString(entry.getKey(), entry.getValue().getType()));
                    }
                }
            }

            return ReflectionUtils.invoke(request, paramsToInvoke, controllers, requestContainerDI, applicationContainerDI);
        } catch (Exception ex) {
            System.out.println("Erro na classe RequestMapper, invoke(). Message: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    private boolean setClazzMethod(List<Class<?>> classes){
        if(classes == null || classes.isEmpty()) throw new  RuntimeException("The list of classes cannot be empty");
        try {
            for (Class<?> clazz : classes) {
                for (Annotation clazzAnnotation : Arrays.stream(clazz.getDeclaredAnnotations()).filter(a -> a.annotationType().equals(Controller.class)).toList()) {
                    for (Method m : clazz.getDeclaredMethods()) {
                        for (Annotation methodAnnotation : Arrays.stream(m.getDeclaredAnnotations()).filter(MethodAnnotationService::validAnnotation).toList()) {
                            String controllerRoute = clazzAnnotation.annotationType()
                                    .getMethod("value")
                                    .invoke(clazzAnnotation)
                                    .toString().trim();
                            String methodRoute = methodAnnotation.getClass()
                                    .getMethod("value")
                                    .invoke(methodAnnotation)
                                    .toString().trim();
                            String apiRoute = StringUrlUtils.format((controllerRoute + methodRoute));

                            // Verifica se é a rota correta

                            if (StringUrlUtils.match(request, apiRoute)) {
                                this.request.setClazz(clazz);
                                this.request.setMethod(m);
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Erro na extração de classe + method. ( " + this.getClass().getName() + " )");
            ex.printStackTrace();
        }
        return false;
    }
}
