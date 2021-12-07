package org.chaostocosmos.leap.http.servlet;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.chaostocosmos.leap.http.Context;
import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.MSG_TYPE;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.filter.IHttpFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstraction of SimpleServlet object
 * 
 * @author Kooin-Shin
 * @since 2021.09.15
 */
public abstract class AbstractLeapServlet implements ILeapServlet {
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(AbstractLeapServlet.class);

    /**
     * Context
     */
    private static final Context context = Context.getInstance();

    /**
     * Filter list
     */
    private List<IHttpFilter> filters;

    /**
     * Entry point of Servlet service.
     */
    @Override
    public void service(HttpRequestDescriptor request, HttpResponseDescriptor response, Method invokingMethod) throws Exception {        
        //Do filtering 
        for(IHttpFilter filter : this.filters) {
            filter.filterRequest(request);
        }
        switch(request.getRequestType()) {
            case GET: 
                doGet(request, response, invokingMethod);
            break;
            case POST:
                doPost(request, response, invokingMethod);
            break;
            default:
                throw new WASException(MSG_TYPE.ERROR, "error016", request.getRequestType().name());
        }
        for(IHttpFilter filter : this.filters) {
            filter.filterResponse(response);
        }
    }

    @Override
    public void doGet(HttpRequestDescriptor request, HttpResponseDescriptor response, Method invokingMethod) throws Exception {
        invokingMethod.invoke(this, request, response);
    }

    @Override
    public void doPost(HttpRequestDescriptor request, HttpResponseDescriptor response, Method invokingMethod) throws Exception {
        invokingMethod.invoke(this, request, response);
    }

    @Override
    public void applyFilters(List<IHttpFilter> filters) {
        this.filters = filters;
    }

    /**
     * Instantiate Servlet object
     * @return
     */
    public ILeapServlet newInstance() throws Exception {
        Constructor<?> constructor = this.getClass().getConstructor();
        return (ILeapServlet)constructor.newInstance();
    }
}
