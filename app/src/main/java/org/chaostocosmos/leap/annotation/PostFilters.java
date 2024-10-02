package org.chaostocosmos.leap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.chaostocosmos.leap.filter.IFilter;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostFilters {
    /**
     * Array of mapping classes for filtering
     * @return
     */
    Class<? extends IFilter>[] filterClasses() default {};
}

