package me.vitalframework.annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ConditionalOnClass(name = "org.bukkit.plugin.java.JavaPlugin")
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresSpigot {
}
