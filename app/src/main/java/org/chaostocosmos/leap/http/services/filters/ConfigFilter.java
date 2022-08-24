package org.chaostocosmos.leap.http.services.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.annotation.PostFilter;
import org.chaostocosmos.leap.http.annotation.PreFilter;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * ConfigurationFilter
 * 
 * @author 9ins
 */
public class ConfigFilter extends AbstractFilter <Request, Response> {

    public enum CONFIG {
        CHART,
        HOSTS,
        MESSAGES,
        MIME,
        SERVER,
        CONFIG_PATH,
    }

    Gson gson = new GsonBuilder().setPrettyPrinting().create(); 

    @Override 
    @PreFilter
    public void filterRequest(Request request) throws Exception {
        super.filterRequest(request);
        Map<CONFIG, Map<String, Object>> configMap = extractConfigMap(request);        
    }

    @Override
    @PostFilter
    public void filterResponse(Response response) throws Exception {

    }

    @SuppressWarnings("unchecked")
    public Map<CONFIG, Map<String, Object>> extractConfigMap(Request request) throws IOException {
        Map<String, byte[]> bodyMap = request.getBodyPart().getBody();
        Map<CONFIG, Map<String, Object>> configMap = new HashMap<>();
        for(String key : bodyMap.keySet()) {
            try {
                configMap.put(CONFIG.valueOf(key), (Map<String, Object>) this.gson.fromJson(new String(bodyMap.get(key), request.charset()), Map.class));
            } catch(Exception e) {
                throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES412.code(), "Configuration extracting fail: "+e.getMessage());
            }
        }                        
        return configMap;
    }
}
