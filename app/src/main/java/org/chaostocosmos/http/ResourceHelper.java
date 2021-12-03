package org.chaostocosmos.http;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.chaostocosmos.http.VirtualHostManager.VirtualHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource helper object
 * 
 * @author 9ins
 * @2021.09.19
 */
public class ResourceHelper {
    /**
     * logger
     */
    public static Logger logger = LoggerFactory.getLogger(ResourceHelper.class);

    /**
     * context
     */
    private static Context context = Context.getInstance();

    /**
     * Virtual host manager
     */
    private static VirtualHostManager virtualHostManager = VirtualHostManager.getInstance();

    /**
     * Get mime type
     * @param request
     * @return
     * @throws URISyntaxException
     * @throws WASException
     * @throws IOException
     */
    public static String getMimeType(HttpRequestDescriptor request) throws IOException, WASException, URISyntaxException {
        return Files.probeContentType(getResourcePath(request));
    }

    /**
     * Get resource Path
     * @param request
     * @return
     * @throws URISyntaxException
     * @throws WASException
     * @throws IOException
     */
    public static Path getResourcePath(HttpRequestDescriptor request) throws WASException, URISyntaxException, IOException {
        return getResourcePath(request.getUrl().toURI().getHost(), request.getContextPath());
    }

    /**
     * Get server resource
     * @param request
     * @return
     * @throws IOException
     * @throws WASException
     * @throws URISyntaxException
     */    
    public static String getReaourceContents(HttpRequestDescriptor request) throws IOException, WASException, URISyntaxException {
        return getReaourceContents(request.getUrl().toURI().getHost(), request.getContextPath());
    }   

    /**
     * Get resource Path
     * @param serverName
     * @param path
     * @return
     * @throws WASException
     * @throws URISyntaxException
     * @throws IOException
     */
    public static Path getResourcePath(String serverName, String path) throws WASException, IOException, URISyntaxException {
        path = path.charAt(0) == '/' ? path.substring(1) : path;
        Path docroot = null;
        Path reqPath = null;
        VirtualHost vhost = virtualHostManager.getVirtualHost(serverName);
        if(vhost != null) {
            docroot = vhost.getDocroot().toAbsolutePath();
        } else {
            docroot = HttpServer.rootDirectory().toAbsolutePath();
        }
        System.out.println(docroot.toString());
        reqPath = docroot.resolve("webapp").resolve("WEB-INF").resolve("static").resolve(path).toAbsolutePath();                
        if(!validatePath(docroot, reqPath)) {
            return docroot.resolve(context.getResponseResource(403));
        }
        return reqPath;
    }
    
    /**
     * Get server resource
     * @param serverName
     * @param path
     * @return
     * @throws IOException
     * @throws WASException
     * @throws URISyntaxException
     */
    public static String getReaourceContents(String serverName, String path) throws IOException, WASException, URISyntaxException {
        Path rPath = getResourcePath(serverName, path);
        return Files.readString(rPath);
    }

    /**
     * Get contents of specified path
     * @param resourcePath
     * @return
     * @throws IOException
     */
    public static String getResourceContents(Path resourcePath) throws IOException {
        return Files.readString(resourcePath);
    }

    /**
     * Get binary data
     * @param resourcePath
     * @return
     * @throws IOException
     */
    public static byte[] getBinaryResource(Path resourcePath) throws IOException {
        return Files.readAllBytes(resourcePath);
    }

    /**
     * Path validation check
     * @param docroot
     * @param resource
     * @return
     */
    public static boolean validatePath(Path docroot, Path resource) {
        docroot = docroot.normalize();
        resource = resource.normalize();
        if(resource.startsWith(docroot)) {
            return true;
        }
        return false;    
    }
}
