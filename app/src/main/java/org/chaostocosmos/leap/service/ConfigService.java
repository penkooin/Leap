package org.chaostocosmos.leap.service;

import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.PreFilters;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.service.filter.BasicAuthFilter;
import org.chaostocosmos.leap.service.filter.ConfigRequestFilter;

/**
 * ConfigurationService
 * 
 * @author 9ins
 */
@ServiceMapper(mappingPath = "/config")
public class ConfigService extends AbstractService {

    @PreFilters(filterClasses = { BasicAuthFilter.class, ConfigRequestFilter.class })
    @MethodMapper(method = REQUEST.POST, mappingPath="/set", autheticated = {}, allowed = {}, forbidden = {})
    public void setConfig(Request request) {
    }

    @PreFilters(filterClasses = { BasicAuthFilter.class, ConfigRequestFilter.class })
    @MethodMapper(method = REQUEST.POST, mappingPath="/save", autheticated = {}, allowed = {}, forbidden = {})
    public void saveConfig(Request request) {
    }

    @Override
    public Exception errorHandling(Response response, Exception e) {
        return e;
    }
}
