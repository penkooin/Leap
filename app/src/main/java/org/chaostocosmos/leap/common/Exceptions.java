package org.chaostocosmos.leap.common;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Exception treations 
 * 
 * @9ins
 */
public class Exceptions {

    /**
     * Wrap stream api being throwables
     * @param consumer
     * @return
     */
    public static <T> Consumer<T> wrap(Consumer<T> consumer) {
        return o -> {
            try {
                consumer.accept(o);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }; 
    }

    public static <T, R> Function<T, R> fwrap(Function<T, R> function) {
        return o -> {
            try {
                return function.apply(o);
            } catch (Throwable e) {
                return null;
                //throw new RuntimeException(e);
            }
        };
    }
}
