package net.servboot.utils.reflection;

import net.servboot.annotations.GET;
import net.servboot.controllers.ControllerBase;
import net.servboot.request.Request;
import net.servboot.utils.json.Json;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class ReflectionUtils {
    public static List<Parameter> getMethodParameters(Method method){
        List<Parameter> parameters;
        if(method == null) return null;
        parameters = new LinkedList<>(Arrays.asList(method.getParameters()));
        return parameters;
    }

    public static List<Annotation> getMethodAnnotations(Method method){
        List<Annotation> annotations;
        annotations = new LinkedList<>(Arrays.asList(method.getAnnotations()));
        return annotations;
    }

    @SuppressWarnings("unchecked")
    public static <T> T convertFromString(Object value, Class<?> clazz){
        if(clazz.equals(boolean.class) || clazz.equals(Boolean.class)){
           return (T) Boolean.valueOf(value.toString());
        } else if(clazz.equals(char.class) || clazz.equals(Character.class)){
            return (T) Character.valueOf(value.toString().charAt(0));
        } else if(clazz.equals(byte.class) || clazz.equals(Byte.class)){
            return (T) Byte.valueOf(value.toString());
        } else if(clazz.equals(short.class) || clazz.equals(Short.class)){
            return (T) Short.valueOf(value.toString());
        } else if(clazz.equals(int.class) || clazz.equals(Integer.class)){
            return (T) Integer.valueOf(value.toString());
        }  else if(clazz.equals(long.class) || clazz.equals(Long.class)){
            return (T) Long.valueOf(value.toString());
        }  else if(clazz.equals(float.class) || clazz.equals(Float.class)){
            return (T) Float.valueOf(value.toString());
        }  else if(clazz.equals(double.class) || clazz.equals(Double.class)){
            return (T) Double.valueOf(value.toString());
        } else if(clazz.equals(String.class)){
            return (T) value;
        } else if(clazz.equals(File.class)){
            return (T) value;
        }
        else {
            return (T) value;
        }
    }

    public static Map<Object, Parameter> sortParameters(Request request, List<Parameter> parameters){
        Map<Object, Parameter> relation;

        if(request.getParameters() == null || parameters == null) return null;
        relation = new LinkedHashMap<>();

        for(Parameter parameter :  parameters){
            File file;
            if(request.getParameters().containsKey(parameter.getName())){
                relation.put(request.getParameters().get(parameter.getName()), parameter);
            } else if((file = request.getFile(parameter.getName().toLowerCase())) != null){
                relation.put(file, parameter);
            }
            else {
                if(Arrays.stream(request.getMethod().getAnnotations()).anyMatch(a -> a.annotationType().equals(GET.class))) throw new RuntimeException("JSON não permitido para métodos GET");
                try{
                    relation.put(parameter.getType().cast(Json.decode(request.getStringBody(), parameter.getType())), parameter);
                } catch (Exception ex){
                    System.out.println("Erro na organização dos parâmetros (ReflectionUtils, sortParametrers()). Message: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }

        return relation;
    }

    public static ControllerBase invoke(Request request, List<Object> params, List<ControllerBase> controllers){
        ControllerBase controller = null;

        if(request.getClazz() == null || request.getMethod() == null) throw new RuntimeException("No class or method found");

        try{
            if(!controllers.isEmpty() && controllers.stream().anyMatch(c -> c.getClass().equals(request.getClazz()))){
                controller = controllers.stream().filter(c -> c.getClass().equals(request.getClazz())).findFirst().get();
            } else {
                controller = (ControllerBase) request.getClazz().getDeclaredConstructor().newInstance();
            }
            request.getMethod().setAccessible(true);
            controller.setRequest(request);

            switch (params.size()) {
                case 0:
                    request.getMethod().invoke(controller);
                    break;
                case 1:
                    request.getMethod().invoke(controller, params.getFirst());
                    break;
                case 2:
                    request.getMethod().invoke(controller, params.getFirst(), params.get(1));
                    break;
                case 3:
                    request.getMethod().invoke(controller, params.getFirst(), params.get(1), params.get(2));
                    break;
                case 4:
                    request.getMethod().invoke(controller, params.getFirst(), params.get(1), params.get(2), params.get(3));
                    break;
                case 5:
                    request.getMethod().invoke(controller, params.getFirst(), params.get(1), params.get(2), params.get(3), params.get(4));
                    break;
                case 6:
                    request.getMethod().invoke(controller, params.getFirst(), params.get(1), params.get(2), params.get(3), params.get(4), params.get(5));
                    break;
                case 7:
                    request.getMethod().invoke(controller, params.getFirst(), params.get(1), params.get(2), params.get(3), params.get(4), params.get(5), params.get(6));
                    break;
                case 8:
                    request.getMethod().invoke(controller, params.getFirst(), params.get(1), params.get(2), params.get(3), params.get(4), params.get(5), params.get(6), params.get(7));
                    break;
                case 9:
                    request.getMethod().invoke(controller, params.getFirst(), params.get(1), params.get(2), params.get(3), params.get(4), params.get(5), params.get(6), params.get(7), params.get(8));
                    break;
                case 10:
                    request.getMethod().invoke(controller, params.getFirst(), params.get(1), params.get(2), params.get(3), params.get(4), params.get(5), params.get(6), params.get(7), params.get(8), params.get(9));
                    break;
                default:
                    System.out.println("NECESSÁRIO ADICIONAR SUPORTE A MAIS PARÂMETROS");
            }
        } catch (Exception ex){
            System.out.println("Erro ao invocar método: " + ex.getMessage());
            ex.printStackTrace();
        }
        return controller;
    }

    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Boolean.class
                || clazz == Character.class
                || clazz == Byte.class
                || clazz == Short.class
                || clazz == Integer.class
                || clazz == Float.class
                || clazz == Double.class
                || clazz == String.class;
    }
}
