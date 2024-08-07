package com.flex.annotations;

import com.flex.models.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    Scope scope() default Scope.SINGLETON;

    String name() default "NONE";
}
