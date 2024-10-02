package org.chaostocosmos.leap.service;

import org.chaostocosmos.leap.annotation.MethodMapper;
import org.chaostocosmos.leap.annotation.PreFilters;
import org.chaostocosmos.leap.annotation.ServiceMapper;
import org.chaostocosmos.leap.enums.REQUEST;
import org.chaostocosmos.leap.filter.BasicAuthFilter;
import org.chaostocosmos.leap.filter.ConfigRequestFilter;
import org.chaostocosmos.leap.http.HttpRequest;
import org.chaostocosmos.leap.http.HttpResponse;
import org.chaostocosmos.leap.service.abstraction.AbstractService;

/**
 * ConfigurationService
 * 
 * @author 9ins
 */
@ServiceMapper(mappingPath = "/config")
public class ConfigService extends AbstractService {

    @PreFilters(filterClasses = { BasicAuthFilter.class, ConfigRequestFilter.class })
    @MethodMapper(method = REQUEST.POST, mappingPath="/set", autheticated = {}, allowed = {}, forbidden = {})
    public void setConfig(HttpRequest request) {
    }

    @PreFilters(filterClasses = { BasicAuthFilter.class, ConfigRequestFilter.class })
    @MethodMapper(method = REQUEST.POST, mappingPath="/save", autheticated = {}, allowed = {}, forbidden = {})
    public void saveConfig(HttpRequest request) {
    }

    @Override
    public Exception errorHandling(HttpResponse response, Exception e) {
        return e;
    }
}
