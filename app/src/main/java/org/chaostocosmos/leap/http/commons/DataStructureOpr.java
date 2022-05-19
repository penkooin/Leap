package org.chaostocosmos.leap.http.commons;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * StructureOpr
 * 
 * @author 9ins
 */
public class DataStructureOpr {
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
	 * @param obj
	 * @param keys
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T findValue(Object obj, Object[] keys) {
		if (obj instanceof List) {
			List<T> list = (List<T>) obj;
			if (keys.length == 1) {
				int idx = Integer.parseInt(keys[0]+"");
				if (list.size() > idx) {
					return list.get(idx);
				} else {
					throw new IllegalArgumentException("There isn't exist value of key: "+Arrays.asList(keys).stream().map(o -> o+"").collect(Collectors.joining(".")));
				}
			} else if (list.size() > 0 && keys.length > 1) {
				int idx = Integer.parseInt(keys[0]+"");
				Object[] subKeys = new Object[keys.length - 1];
				System.arraycopy(keys, 1, subKeys, 0, subKeys.length);
				return findValue(list.get(idx), subKeys);
			}
		} else if (obj instanceof Map) {
			Map<String, T> map = (Map<String, T>) obj;
			if (keys.length == 1 && map.containsKey(keys[0])) {
				return map.get(keys[0]);
			} else if (keys.length > 1 && map.containsKey(keys[0])) {
				Object[] subKeys = new Object[keys.length - 1];
				System.arraycopy(keys, 1, subKeys, 0, subKeys.length);
				return findValue(map.get(keys[0]), subKeys);
			}
		}
		throw new IllegalArgumentException("There isn't exist value of key: "+Arrays.asList(keys).stream().map(o -> o+"").collect(Collectors.joining(".")));
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
    public static <T> void putValue(Object obj, Object[] keys, T value) {
        ((Map)findValue(obj, Arrays.copyOfRange(keys, 0, keys.length-1))).put(keys[keys.length-1], value); 
    }    
}
