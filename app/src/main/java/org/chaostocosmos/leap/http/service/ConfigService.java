package org.chaostocosmos.leap.http.service;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.FilterIndicates;
import org.chaostocosmos.leap.http.annotation.MethodIndicates;
import org.chaostocosmos.leap.http.annotation.ServiceIndicates;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.service.filter.BasicAuthFilter;
import org.chaostocosmos.leap.http.service.filter.ConfigRequestFilter;

/**
 * ConfigurationService
 * 
 * @author 9ins
 */
@ServiceIndicates(path = "/config")
public class ConfigService extends AbstractService {

    @FilterIndicates(preFilters = { BasicAuthFilter.class, ConfigRequestFilter.class })
    @MethodIndicates(mappingMethod = REQUEST_TYPE.POST, path="/set")
    public void setConfig(Request request) {
    }

    @FilterIndicates(preFilters = { BasicAuthFilter.class, ConfigRequestFilter.class })
    @MethodIndicates(mappingMethod = REQUEST_TYPE.POST, path="/save")
    public void saveConfig(Request request) {

    }

    @Override
    public Exception errorHandling(Response response, Exception e) {
        return e;
    }    
}
