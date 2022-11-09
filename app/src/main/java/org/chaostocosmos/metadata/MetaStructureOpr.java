package org.chaostocosmos.metadata;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * StructureOpr
 * 
 * @author 9ins
 */
public class MetaStructureOpr {
	/**
	 * Get structural data value
	 * @param <T>
	 * @param obj
	 * @param expr
	 * @return
	 */
	public static <T> T getValue(Object obj, String expr) {
		return findValue(obj, expr.split("\\."));
	}

	/**
	 * Find value of key on structural data
	 * @param <T>
	 * @param obj
	 * @param keys
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T> T findValue(Object obj, String[] keys) {
		System.out.println(Arrays.toString(keys)+"  "+obj.getClass().getName());
		if(keys.length == 0) {
			return (T) obj;
		}
		if(obj instanceof List) {
			List<T> l = (List<T>) obj;			
			int idx = Integer.valueOf(StringTools.substringBetweenFirst(String.valueOf(keys[0]), '[', ']'));			
			System.out.println("idx: "+idx);
			return findValue(l.get(idx), Arrays.copyOfRange(keys, 1, keys.length));
		} else if(obj instanceof Map) {
			Map<String, T> m = (Map<String, T>) obj;
			String s = StringTools.substringFirst(keys[0], '[', ']');
			//System.out.println(s);
			if(s == null) {
				return findValue(m.get(keys[0]), Arrays.copyOfRange(keys, 1, keys.length));
			} else {
				String key = keys[0].substring(0, keys[0].indexOf("["));
				return findValue(m.get(key), keys);
			}
		} else {
			System.out.println(obj);
			return (T) obj;
		}
	}

	/**
	 * Set value to config by path expresstion
	 * @param <T>
	 * @param obj
	 * @param expr
	 * @param value
	 */
	public static <T> void setValue(Object obj, String expr, T value) {
		putValue(obj, expr.split("\\."), value);
	}

	/**
	 * Put value to config by path expr key array
	 * @param <T>
	 * @param obj
	 * @param keys
	 * @param value
	 */
	@SuppressWarnings("unchecked")
    public static <T> void putValue(Object obj, String[] keys, T value) {
		T parent = findValue(obj, Arrays.copyOfRange(keys, 0, keys.length-1));
		if(parent == null) {
			if(obj instanceof List) {
				((List<T>)obj).set(Integer.valueOf(keys[keys.length-1]+""), value);
			} else if(obj instanceof Map) {
				((Map<String, T>)obj).put(keys[keys.length-1]+"", value);
			} else {
				throw new IllegalArgumentException("Parent object must be List or Map.");
			}
        } else if(parent instanceof List) {
			((List<T>)parent).set(Integer.valueOf(keys[keys.length-1]+""), value);
		} else if(parent instanceof Map) {
			((Map<String, T>)parent).put(keys[keys.length-1]+"", value);
		}
    }    
}
