package org.chaostocosmos.leap.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.chaostocosmos.leap.http.service.filter.IFilter;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreFilters {
    /**
     * Array of mapping classes for filtering
     * @return
     */
    Class<? extends IFilter>[] filterClasses() default {};
}
