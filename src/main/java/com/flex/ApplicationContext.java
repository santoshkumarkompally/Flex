package com.flex;

import com.flex.annotations.Component;
import com.flex.exception.InitiationException;
import com.flex.models.Autowired;
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

    public <T> T getBean(Class<T> clazz) {
        Class<T> classesImplementingTheInterface = getClassesImplementingTheInterface(clazz);
        return createBean(clazz, classesImplementingTheInterface);
    }

    private Set<Class<?>> getComponents(Class<?> applicationContext) {
        Reflections reflection = new Reflections(applicationContext.getPackage().getName());
        return reflection.getTypesAnnotatedWith(Component.class)
                .stream().filter(x -> !x.isInterface())
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> getClassesImplementingTheInterface(Class<T> interfaceItem) {
        Set<Class<?>> implementingClasses = components
                .stream()
                .filter(x -> List.of(x.getInterfaces()).contains(interfaceItem))
                .collect(Collectors.toSet());

        //TODO: need to implement which one to pick based on qualifier.
        if (implementingClasses.size() > 1) {
            throw new RuntimeException("More than one classes are implementing the interface.");
        }
        return (Class<T>) implementingClasses.stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("some occur occurred in the framework"));
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
        Object[] parameters = Arrays.stream(parameterTypes)
                .map(this::getBean)
                .toArray();

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
}
