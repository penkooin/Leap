package org.chaostocosmos.leap.filter;

import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.http.HttpRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * ConfigurationFilter
 * 
 * @author 9ins
 */
public class ConfigRequestFilter<T, R> extends AbstractRequestFilter<HttpRequest<T>> {

    /**
     * Config enum
     */
    public enum CONFIG {
        CHART,
        HOSTS,
        MESSAGES,
        MIME,
        SERVER,
        CONFIG_PATH,
    }

    /**
     * Gson object
     */
    Gson gson = new GsonBuilder().setPrettyPrinting().create(); 

    @Override 
    public void filterRequest(HttpRequest<T> request) throws Exception {
        super.filterRequest(request);
        //Map<CONFIG, Map<String, Object>> configMap = extractConfigMap(request);        
    }

    @SuppressWarnings("unchecked")
    public Map<CONFIG, Map<String, Object>> extractConfigMap(HttpRequest<Map<String, byte[]>> request) throws Exception {
        Map<String, byte[]> bodyMap = request.getBody().getBody();
        Map<CONFIG, Map<String, Object>> configMap = new HashMap<>();
        for(String key : bodyMap.keySet()) {
            try {
                configMap.put(CONFIG.valueOf(key), (Map<String, Object>) this.gson.fromJson(new String(bodyMap.get(key), request.charset()), Map.class));
            } catch(Exception e) {
                throw new LeapException(HTTP.RES412, "Configuration extracting fail: "+e.getMessage());
            }
        }                        
        return configMap;
    }
}
