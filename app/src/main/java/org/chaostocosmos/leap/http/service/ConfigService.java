package org.chaostocosmos.leap.http.service;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.enums.REQUEST;
import org.chaostocosmos.leap.http.inject.FilterIndicates;
import org.chaostocosmos.leap.http.inject.MethodIndicates;
import org.chaostocosmos.leap.http.inject.ServiceIndicates;
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
    @MethodIndicates(method = REQUEST.POST, path="/set")
    public void setConfig(Request request) {
    }

    @FilterIndicates(preFilters = { BasicAuthFilter.class, ConfigRequestFilter.class })
    @MethodIndicates(method = REQUEST.POST, path="/save")
    public void saveConfig(Request request) {

    }

    @Override
    public Exception errorHandling(Response response, Exception e) {
        return e;
    }    
}
