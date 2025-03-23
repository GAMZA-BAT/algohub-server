package com.gamzabat.algohub.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Hidden;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Hidden
public @interface AuthedUser {
}
