package org.chaostocosmos.leap.common.data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
	private static <T> T findValue(Object obj, Object[] keys) {
		if (obj instanceof List) {
			List<T> list = (List<T>) obj;
			if (keys.length == 1) {
				int idx = Integer.parseInt(keys[0]+"");
				if (list.size() > idx) {
					return (T) list.get(idx);
				} else {
					return null;
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
				return (T) map.get(keys[0]);
			} else if (keys.length > 1 && map.containsKey(keys[0])) {
				Object[] subKeys = new Object[keys.length - 1];
				System.arraycopy(keys, 1, subKeys, 0, subKeys.length);
				return findValue(map.get(keys[0]), subKeys);
			}
		}
		return null;
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
    public static <T> void putValue(Object obj, Object[] keys, T value) {
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
