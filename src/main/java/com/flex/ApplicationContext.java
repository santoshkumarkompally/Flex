package com.flex;

import com.flex.annotations.Component;
import com.flex.annotations.Qualifier;
import com.flex.exception.InitiationException;
import com.flex.annotations.Autowired;
import com.flex.proxy.ProxyHandler;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationContext {
    private final Set<Class<?>> components;

    public ApplicationContext(Class<?> clazz) {
        components = getComponents(clazz);
    }

    /**
     * @param clazz
     * @return bean of requested class
     * This only accepts implementation with one interface
     */
    public <T> T getBean(Class<T> clazz) {
        Class<T> classesImplementingTheInterface = getClassesImplementingTheInterface(clazz, "NONE");
        return createBean(clazz, classesImplementingTheInterface);
    }

    private <T> T getBeanWithQualifier(Class<T> clazz, Annotation[] annotation) {
        String qualifierName = Arrays.stream(annotation).filter(x -> x instanceof Qualifier)
                .map(x -> ((Qualifier) x).name())
                .findFirst().orElse("NONE");

        Class<T> classesImplementingTheInterface = getClassesImplementingTheInterface(clazz, qualifierName);
        return createBean(clazz, classesImplementingTheInterface);
    }

    private Set<Class<?>> getComponents(Class<?> applicationContext) {
        Reflections reflection = new Reflections(applicationContext.getPackage().getName());
        return reflection.getTypesAnnotatedWith(Component.class)
                .stream().filter(x -> !x.isInterface())
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> getClassesImplementingTheInterface(Class<T> interfaceItem, String qualifier) {
        Set<Class<?>> implementingClasses = components
                .stream()
                .filter(x -> List.of(x.getInterfaces()).contains(interfaceItem))
                .collect(Collectors.toSet());

        if (implementingClasses.isEmpty()) {
            throw new RuntimeException("There are no implementations found for interface: " + interfaceItem.getName());
        }

        // If there is only one implementation qualifier is not considered.
        if (implementingClasses.size() == 1) {
            return (Class<T>) implementingClasses.stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("some occur occurred in the framework"));
        }

        List<Class<?>> implementingClassesWithQualifiers = implementingClasses.stream().filter(
                x -> isGivenClassAnnotatedWithGivenQualifier(x, qualifier)
        ).collect(Collectors.toList());

        if (implementingClassesWithQualifiers.size() == 1) {
            return (Class<T>) implementingClassesWithQualifiers.get(0);
        }
        throw new InitiationException("More than one qualifier with name: " + qualifier + " found for implementations " +
                "of class: " + interfaceItem.getName());
    }

    private boolean isGivenClassAnnotatedWithGivenQualifier(Class<?> clazz, String qualifierName) {
        Set<Annotation> annotationsWithQualifier = Arrays.stream(clazz.getAnnotations())
                .filter(x -> x instanceof Component)
                .filter(y -> ((Component) y).name().equals(qualifierName))
                .collect(Collectors.toSet());

        if (annotationsWithQualifier.isEmpty()) {
            return false;
        }

        return true;
    }

    private boolean findComponentOnConstructor(Constructor<?> constructor) {
        Annotation[] annotations = constructor.getAnnotations();

        for (Annotation annotation : annotations) {
            if (annotation instanceof Autowired) {
                return true;
            }
        }
        return false;
    }

    private <T> T createBean(Class<T> clazz, Class<T> implementingClass) {
        List<Constructor<?>> constructors = Arrays
                .stream(implementingClass.getConstructors())
                .collect(Collectors.toList());
        Constructor<?> constructor;

        //default constructor.
        if (constructors.size() == 1) {
            constructor = constructors.get(0);
        } else {
            List<Constructor<?>> constructorsWithComponentAnnotation = constructors
                    .stream()
                    .filter(this::findComponentOnConstructor)
                    .collect(Collectors.toList());
            if (constructorsWithComponentAnnotation.size() > 1) {
                throw new InitiationException("Class has more than one component: " + implementingClass.getName());
            }
            constructor = constructorsWithComponentAnnotation.get(0);
        }

        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        Object[] parameters = getParameterObjects(parameterTypes, parameterAnnotations);

        try {
            Object object = Proxy.newProxyInstance(
                    ApplicationContext.class.getClassLoader(),
                    new Class[]{clazz},
                    new ProxyHandler(constructor.newInstance(parameters)));
            return clazz.cast(object);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException initiationException) {
            throw new InitiationException("Exception occurred while initiating the framework");
        }
    }

    private Object[] getParameterObjects(Class<?>[] parameterTypes, Annotation[][] parameterAnnotations) {
        Object[] parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = getBeanWithQualifier(parameterTypes[i], parameterAnnotations[i]);
        }
        return parameters;
    }
}
