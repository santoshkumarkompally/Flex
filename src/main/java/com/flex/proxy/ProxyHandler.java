package com.flex.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyHandler implements InvocationHandler {
    private final Object objectToHandle;

    public ProxyHandler(Object objectToHandle) {
        this.objectToHandle = objectToHandle;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println(" invoking proxy for class: " + objectToHandle.getClass().getName());
        return method.invoke(objectToHandle, args);
    }
}
