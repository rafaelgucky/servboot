package net.servboot.annotations.methods;

import net.servboot.annotations.GET;
import net.servboot.annotations.POST;

import java.lang.annotation.Annotation;

public class MethodAnnotationService {
    public static boolean validAnnotation(Annotation annotation) {
        return annotation.annotationType().equals(GET.class)
                || annotation.annotationType().equals(POST.class);
    }
}
