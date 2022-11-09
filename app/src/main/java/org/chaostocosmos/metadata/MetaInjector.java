package org.chaostocosmos.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MetadataInjector
 * 
 * @author 9ins
 */
public class MetaInjector {
    /**
     * Inject value to specified object field
     * @param <T>
     * @param metadata
     * @param field
     * @param expr
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     */
    public static Object inject(Metadata metadata, Object obj) throws IllegalArgumentException, 
                                                                      IllegalAccessException, 
                                                                      ClassNotFoundException, 
                                                                      NoSuchMethodException, 
                                                                      SecurityException, 
                                                                      InstantiationException, 
                                                                      InvocationTargetException, 
                                                                      NoSuchFieldException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for(Field field : fields) {
            MetaField meta = field.getDeclaredAnnotation(MetaField.class);
            if(meta != null) {
                String expr = meta.expr();
                field.setAccessible(true);
                if(field.getType().equals(boolean.class)) {
                    field.setBoolean(obj, metadata.<Boolean> getValue(expr).booleanValue());
                } else if(field.getType().equals(byte.class)) {
                    field.setByte(obj, metadata.<Byte> getValue(expr).byteValue());
                } else if(field.getType().equals(char.class)) {
                    field.setChar(obj, metadata.<Character> getValue(expr).charValue());
                } else if(field.getType().equals(int.class)) {
                    field.setInt(obj, metadata.<Double> getValue(expr).intValue());
                } else if(field.getType().equals(float.class)) {
                    field.setFloat(obj, metadata.<Double> getValue(expr).floatValue());
                } else if(field.getType().equals(double.class)) {
                    field.setDouble(obj, metadata.<Double> getValue(expr).doubleValue());
                } else if(field.getType().equals(Boolean.class)) {
                    field.set(obj, metadata.<Boolean> getValue(expr));
                } else if(field.getType().equals(Byte.class)) {
                    field.set(obj, Byte.valueOf(metadata.<Byte> getValue(expr)));
                } else if(field.getType().equals(Character.class)) {
                    field.set(obj, metadata.<Character> getValue(expr));
                } else if(field.getType().equals(Integer.class)) {
                    field.set(obj, Integer.valueOf(metadata.<Double> getValue(expr).intValue()));
                } else if(field.getType().equals(Float.class)) {
                    field.set(obj, Float.valueOf(metadata.<Double> getValue(expr).floatValue()));
                } else if(field.getType().equals(Double.class)) {
                    field.set(obj, metadata.<Double> getValue(expr).floatValue());
                } else if(field.getType().equals(String.class)) {
                    field.set(obj, metadata.<String> getValue(expr));
                } else if(field.getType().equals(Class.class)) {
                    throw new IllegalArgumentException("Class type field is not supprted !!!");
                } else if(field.getType().equals(List.class)) {
                    List<Object> list = metadata.<List<Object>> getValue(expr);
                    if(field.getGenericType() != null) {
                        List<Object> result = new ArrayList<>();
                        for( int i=0; i < list.size(); i++ ) {
                            String genericTypeName = ((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]).getName();
                            Class<?> clazz = Class.forName(genericTypeName);
                            Constructor<?> constructor = clazz.getConstructor(new Class<?>[0]);
                            Object instance = constructor.newInstance(new Object[0]);
                            MetaField metaField = instance.getClass().getDeclaredField("username").getAnnotation(MetaField.class);
                            String expr1 = metaField.expr();
                            Object old = changeAnnotationValue(metaField, "expr", expr1.replace("[i]", "["+i+"]"));
                            result.add(inject(metadata, instance));
                            changeAnnotationValue(metaField, "expr", old);
                        }
                        list = result;
                    }
                    field.set(obj, list);
                } else if(field.getType().equals(Map.class)) {
                    field.set(obj, metadata.getValue(expr));
                }
            }
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue) throws NoSuchFieldException, 
                                                                                                          SecurityException, 
                                                                                                          IllegalArgumentException, 
                                                                                                          IllegalAccessException {
        Object handler = Proxy.getInvocationHandler(annotation);
        Field f = handler.getClass().getDeclaredField("memberValues");
        f.setAccessible(true);
        Map<String, Object> memberValues = (Map<String, Object>) f.get(handler);
        Object oldValue = memberValues.get(key);
        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException();
        }
        memberValues.put(key, newValue);
        return oldValue;
    }    
}
