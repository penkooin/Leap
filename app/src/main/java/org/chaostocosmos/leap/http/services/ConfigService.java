package org.chaostocosmos.leap.http.services;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.annotation.FilterMapper;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.services.filters.BasicAuthFilter;
import org.chaostocosmos.leap.http.services.filters.ConfigFilter;

/**
 * ConfigurationService
 * 
 * @author 9ins
 */
@ServiceMapper(path = "/config")
public class ConfigService extends AbstractService {

    @FilterMapper(preFilters = { BasicAuthFilter.class, ConfigFilter.class })
    @MethodMappper(mappingMethod = REQUEST_TYPE.POST, path="/set")
    public void setConfig(Request request) {
    }

    @FilterMapper(preFilters = { BasicAuthFilter.class, ConfigFilter.class })
    @MethodMappper(mappingMethod = REQUEST_TYPE.POST, path="/save")
    public void saveConfig(Request request) {

    }

    @Override
    public Throwable errorHandling(Response response, Throwable t) throws Throwable {
        return t;
    }    
}
