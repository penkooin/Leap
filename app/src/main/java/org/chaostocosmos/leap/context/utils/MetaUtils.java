package org.chaostocosmos.leap.context.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Metadata Utility object
 * 
 * @author 9ins
 */
public class MetaUtils {
 
    /**
     * Compare YAML
     * @param map1
     * @param map2
     * @param path
     */
    public static List<Diff> compareDiffMaps(Map<String, Object> original, Map<String, Object> modified, String path) {
        List<Diff> differences = new ArrayList<>();
        java.util.Set<String> originalKeys = new HashSet<>(original.keySet());

        for (String key : originalKeys) {
            String currentPath = path.isEmpty() ? key : path + "." + key;
            Object originalValue = original.get(key);
            Object modifiedValue = modified.get(key);

            if (originalValue instanceof Map && modifiedValue instanceof Map) {
                Map<String, Object> originalMap = (Map<String, Object>) originalValue;
                Map<String, Object> modifiedMap = (Map<String, Object>) modifiedValue;
                differences.addAll(compareDiffMaps(originalMap, modifiedMap, currentPath));
            } else if(originalValue instanceof List && modifiedValue instanceof List) {
                List<String> originalkeyList = (List<String>) originalValue;
                List<String> modifiedkeyList = (List<String>) modifiedValue;
                for(int i=0; i<originalkeyList.size(); i++) {
                    Object originalValue1 = originalkeyList.get(i);
                    Object modifiedValue2 = modifiedkeyList.get(i);
                    if(originalValue1 instanceof Map && modifiedValue2 instanceof Map) {
                        differences.addAll(compareDiffMaps((Map<String, Object>) originalValue1, (Map<String, Object>) modifiedValue2, currentPath));
                    }
                }
            } else if (!originalValue.equals(modifiedValue)) {
                differences.add(new Diff(currentPath, originalValue, modifiedValue));
            }
        }
        return differences;
    }    
}
