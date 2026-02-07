package net.servboot.utils.reflection;

import net.servboot.annotations.Controller;
import java.io.File;
import java.util.*;

public final class AnnotationResearcher {
    private final ClassLoader cl;
    private static List<Class<?>> classes = null;
    private static String projectName = "";

    public AnnotationResearcher(Thread mainThread) {
        this.cl = mainThread.getContextClassLoader();
    }

    public List<Class<?>> getClasses() {
        return classes;
    }

    public synchronized List<Class<?>> findClazz(){
        try{
            String basePath = Collections.list(cl.getResources("")).getFirst().getPath();

            // Find project name
            if(projectName.isEmpty()){
                for (int i = basePath.length() - 2; i >= 0; i--) {
                    if (basePath.charAt(i) == '/' || basePath.charAt(i) == '\\') break;
                    projectName += basePath.charAt(i);
                }

                String temp = "";
                for (int i = projectName.length() - 1; i >= 0; i--) {
                    temp += projectName.charAt(i);
                }

                projectName = temp;
            }

            // Verifica se as classes já foram mapedas
            if(classes == null) {
                // Filtra por classes que tem a annotation @Controller
                List<Class<?>> auxClasses = findClazz(new File(basePath));
                if(auxClasses != null && !auxClasses.isEmpty()){
                    classes = auxClasses.stream()
                            .filter(clazz ->
                                    Arrays.stream(clazz.getDeclaredAnnotations())
                                            .anyMatch(annotation ->
                                                    annotation.annotationType().equals(Controller.class))).toList();
                }
            }

            return classes;
        } catch (Exception ex){
            System.out.println("ERROR ON CLASSES LOADER: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    private synchronized List<Class<?>> findClazz(File file){
        List<Class<?>> classes = new LinkedList<>();
        try{
            if (!file.exists()) return null;
            else if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    if (f.getName().endsWith(".class")) {
                        classes.add(Class.forName(
                                f.getPath().substring(
                                        f.getPath().lastIndexOf(projectName) + projectName.length() + 1
                                            )
                                            .replace("\\", ".")
                                            .replace("/", ".")
                                            .replace(".class", "")));
                    } else if(f.isDirectory()){
                        List<Class<?>> c = findClazz(f);
                        if(c != null) classes.addAll(c);
                    }
                }
            }
        } catch (Exception ex){
            System.out.println("Erro no carregamento de classes '.class' que contenham a anotação '@Controller': " + ex.getMessage());
            ex.printStackTrace();
        }
        return classes;
    }
}
