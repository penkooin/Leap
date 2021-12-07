package org.chaostocosmos.leap.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.chaostocosmos.leap.http.VirtualHostManager.VirtualHost;
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
    Logger logger = LoggerFactory.getLogger(ResourceHelper.class);

    /**
     * home path
     */
    Path HOME_PATH;

    /**
     * Context map
     */
    Map<String, Context> contextMap;

    /**
     * ResourceHandler
     */
    private static ResourceHelper resourceHelper;

    /**
     * Private constructor
     * @param HOME_PATH
     * @throws IOException
     */
    private ResourceHelper(Path HOME_PATH) throws IOException {
        this.HOME_PATH = HOME_PATH;        
    }

    /**
     * Get ResourceHelper instance
     * @return
     * @throws IOException
     */
    public static ResourceHelper getInstance() throws IOException {
        return getInstance(Paths.get("."));
    }

    /**
     * Get ResourceHelper instance
     * @param HOME_PATH
     * @throws IOException
     */
    public static ResourceHelper getInstance(Path HOME_PATH) throws IOException {
        if(resourceHelper == null) {
            resourceHelper = new ResourceHelper(HOME_PATH);
        }
        return resourceHelper;
    }

    /**
     * Get mime type
     * @param request
     * @return
     * @throws URISyntaxException
     * @throws WASException
     * @throws IOException
     */
    public String getMimeType(HttpRequestDescriptor request) throws IOException, WASException, URISyntaxException {
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
    public Path getResourcePath(HttpRequestDescriptor request) throws WASException, URISyntaxException, IOException {
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
    public String getReaourceContents(HttpRequestDescriptor request) throws IOException, WASException, URISyntaxException {
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
    public Path getResourcePath(String host, String path) throws WASException, IOException, URISyntaxException {
        path = path.charAt(0) == '/' ? path.substring(1) : path;
        Path docroot = null;
        Path reqPath = null;
        VirtualHost vhost = VirtualHostManager.getInstance().getVirtualHost(host);
        if(vhost != null) {
            docroot = vhost.getDocroot().toAbsolutePath();
        } else {
            docroot = LeapHttpServer.WAS_HOME.toAbsolutePath(); 
        }
        logger.debug(docroot.toString());
        reqPath = getStaticPath().resolve(path).toAbsolutePath();                
        if(!validatePath(docroot, reqPath)) {
            throw new WASException(MSG_TYPE.ERROR, "error019", new Object[]{host});
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
    public String getReaourceContents(String serverName, String path) throws IOException, WASException, URISyntaxException {
        Path rPath = getResourcePath(serverName, path);
        return Files.readString(rPath);
    }

    /**
     * Get contents of specified path
     * @param resourcePath
     * @return
     * @throws IOException
     */
    public String getResourceContents(Path resourcePath) throws IOException {
        return Files.readString(resourcePath);
    }

    /**
     * Get binary data
     * @param resourcePath
     * @return
     * @throws IOException
     */
    public byte[] getBinaryResource(Path resourcePath) throws IOException {
        return Files.readAllBytes(resourcePath);
    }

    /**
     * Path validation check
     * @param docroot
     * @param resource
     * @return
     */
    public boolean validatePath(Path docroot, Path resource) {
        docroot = docroot.normalize();
        resource = resource.normalize();
        if(resource.startsWith(docroot)) {
            return true;
        }
        return false;    
    }

    /**
     * Get contents of Resource
     * @param resourceName
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public String getResourceContent(String resourceName) throws IOException, URISyntaxException {
        InputStream is = UtilBox.class.getResourceAsStream(resourceName);
        return UtilBox.readAllString(is);
    }

    /**
     * Get trademark string
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String getTrademark() throws FileNotFoundException, IOException {
        File file = getWebInfPath().resolve("trademark").toAbsolutePath().toFile();
        return UtilBox.readAllString(new FileInputStream(file));
    }

    /**
     * Extract environment resources
     * @param homePath
     * @throws IOException
     */
    public static void buildEnv(Path homePath) throws IOException {
        Path WEBAPP = ResourceHelper.getWebAppPath();
        if(!WEBAPP.toFile().exists()) {
            Files.createDirectories(WEBAPP);
        }
        Path WEBINF = ResourceHelper.getWebInfPath();
        if(!WEBINF.toFile().exists()) {
            Files.createDirectories(WEBINF);
        }
        Path STATIC = ResourceHelper.getStaticPath();
        if(!STATIC.toFile().exists()) {
            Files.createDirectories(STATIC);
        }
        String[] filenames = {"trademark", "config.yml", "logback.xml"};
        for(String filename : filenames) {
            Path PATH = WEBINF.resolve(filename);
            if(!PATH.toFile().exists()) {
                String p = PATH.toString().substring(ResourceHelper.getHomePath().toString().length()).replace("\\", "/");
                if(UtilBox.isFileSame(PATH.toFile().lastModified(), UtilBox.class.getResource(p).openConnection().getLastModified())) {
                    Files.writeString(PATH, UtilBox.readAllString(UtilBox.class.getResourceAsStream(p)));
                }
            }                
        }
        String p = STATIC.toString().substring(ResourceHelper.getHomePath().toString().length()+1).replace("\\", "/");
        List<String> files = IOUtils.readLines(UtilBox.class.getClassLoader().getResourceAsStream(p), Charsets.UTF_8);
        for(String s : files) {
            if(!homePath.resolve(STATIC.resolve(s)).toFile().exists()) {
                p = "/" + p + "/" + s;
                Files.writeString(homePath.resolve(STATIC.resolve(s)), UtilBox.readAllString(UtilBox.class.getResourceAsStream(p)));
            }
        }
    }

    /**
     * Get WAS Home path
     * @return
     */
    public static Path getHomePath() {
        return LeapHttpServer.WAS_HOME;
    }

    /**
     * Get webapp path
     * @return
     */
    public static Path getWebAppPath() {
        return getHomePath().resolve("webapp");
    }

    /**
     * Get webapp/WEB-INF path
     * @return
     */
    public static Path getWebInfPath() {
        System.out.println(getWebAppPath().resolve("WEB-INF").toString());
        return getWebAppPath().resolve("WEB-INF");
    }

    /**
     * Get webapp/WEB-INF/static path
     * @return
     */
    public static Path getStaticPath() {
        return getWebInfPath().resolve("static");
    }
}
