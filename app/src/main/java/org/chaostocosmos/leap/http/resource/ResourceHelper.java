package org.chaostocosmos.leap.http.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chaostocosmos.leap.http.LeapApp;
import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.common.LoggerFactory;
import org.chaostocosmos.leap.http.common.UtilBox;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.HTTP;

/**
 * Resource helper object
 * 
 * @author 9ins
 * @2021.09.19
 */
public class ResourceHelper {    
    /**
     * home path
     */
    Path HOME_PATH;

    /**
     * ResourceHandler
     */
    private static ResourceHelper resourceHelper;

    /**
     * Private constructor
     * @param HOME_PATH
     */
    private ResourceHelper(Path HOME_PATH) {
        this.HOME_PATH = HOME_PATH;
    }

    /**
     * Get ResourceHelper instance
     * @return
     */
    public static ResourceHelper getInstance() {
        return getInstance(LeapApp.HOME_PATH);
    }

    /**
     * Get ResourceHelper instance
     * @param HOME_PATH
     */
    public static ResourceHelper getInstance(Path HOME_PATH) {
        if(resourceHelper == null) {
            resourceHelper = new ResourceHelper(HOME_PATH);
        }
        return resourceHelper;
    }

    /**
     * Get mime type
     * @param request
     * @return
     */
    public static  String getMimeType(Request request) {
        return UtilBox.probeContentType(getResourcePath(request));
    }

    /**
     * Get resource Path
     * @param request
     * @return
     */
    public static Path getResourcePath(Request request) {
        return getResourcePath(request.getHostId(), request.getContextPath());
    }

    /**
     * Get resource Path
     * @param serverName
     * @param contextPath
     * @return
     */
    public static Path getResourcePath(String hostId, String contextPath) {
        contextPath = contextPath.charAt(0) == '/' ? contextPath.substring(1) : contextPath;
        Host<?> hosts = Context.hosts().getHost(hostId);
        Path docroot = hosts.getDocroot().toAbsolutePath();
        Path reqPath = getStaticPath(hostId).resolve(contextPath).toAbsolutePath();        
        if(!validatePath(docroot, reqPath)) {
            throw new HTTPException(HTTP.RES403, contextPath);
        }
        LoggerFactory.getLogger(hostId).debug("REQUEST PATH: "+reqPath.toString()); 
        return reqPath.normalize();
    }

    /**
     * Get response html file contents
     * @param hostId
     * @param code
     * @return
     */
    public static Path getResponseResourcePath(String hostId) {
        return Context.hosts().getHost(hostId).getTemplates().resolve("response.html");
    }    

    /**
     * Get static resource path
     * @param hostId
     * @param resourceName
     * @return
     */
    public static Path getStaticResourcePath(String hostId, String resourceName) {
        return getResourcePath(hostId, resourceName);
    }

    /**
     * Get binary data
     * @param resourcePath
     * @return
     */
    public static byte[] getBinaryResource(Path resourcePath) {
        try {
            return Files.readAllBytes(resourcePath);
        } catch (IOException e) {
            throw new HTTPException(HTTP.RES500, Context.messages().<String> error(14, resourcePath));
        }
    }

    /**
     * Path validation check
     * @param docroot
     * @param resource
     * @return
     */
    public static boolean validatePath(Path docroot, Path resource) {
        if(resource.normalize().startsWith(docroot.normalize())) {
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
    public static String getResourceContent(String resourceName) throws IOException, URISyntaxException {
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
        File file = Context.getHomePath().resolve("config").resolve("trademark").toFile();
        return UtilBox.readAllString(new FileInputStream(file)); 
    }

    /**
     * Extract resurces from jar or file
     * @param resourcePath
     * @param targetPath
     * @return
     * @throws HTTPException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<File> extractResource(String resourcePath, final Path targetPath) throws IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL res = classLoader.getResource(resourcePath);
        String protocol = res.getProtocol();
        Stream<Path> pStream;
        if(protocol.equals("jar")) {
            try (FileSystem fileSystem = FileSystems.newFileSystem(res.toURI(), new HashMap<>())) {
                pStream = Files.walk(fileSystem.getPath(resourcePath)); 
            }
        } else if(protocol.equals("file")) {
            pStream = Files.walk(Paths.get(res.toURI()));
        } else {
            throw new IllegalArgumentException("Resource protocol isn't supported!!! : "+res.getProtocol());
        }
        List<File> results = pStream.map(p -> {
            try {
                long modMillis = Files.getLastModifiedTime(p).toMillis();
                String ps = p.toString().replace("\\", "/");
                Path path = protocol.equals("jar") 
                            ? targetPath.resolve(ps) 
                            : Paths.get(targetPath.toAbsolutePath().toString(), 
                            ps.toString().substring(ps.toString().indexOf(resourcePath)-1).replace("\\", "/"));
                if(Files.isDirectory(p)) {
                    path.toFile().mkdirs();
                } else { 
                    if(path.toFile().lastModified() != modMillis) {
                        path.toFile().delete(); 
                    }
                    if(!path.toFile().exists()) {
                        File file = Files.write(path, Files.readAllBytes(p), StandardOpenOption.CREATE).toFile();
                        file.setLastModified(modMillis);
                        return file;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        pStream.close();
        return results;
    }

    /**
     * Get WAS Home path
     * @param hostId
     * @return
     * @throws HTTPException
     */
    public static Path getDocroot(String hostId) throws HTTPException {
        return Context.hosts().getDocroot(hostId).normalize().toAbsolutePath();
    }

    /**
     * Get webapp path
     * @param hostId
     * @return
     * @throws HTTPException
     */
    public static Path getWebAppPath(String hostId) throws HTTPException {
        return getDocroot(hostId).resolve("webapp");
    }

    /**
     * Get webapp/WEB-INF path
     * @param hostId
     * @return
     * @throws HTTPException
     */
    public static Path getWebInfPath(String hostId) throws HTTPException {
        return getWebAppPath(hostId).resolve("WEB-INF");
    }

    /**
     * Get webapp/WEB-INF/static path
     * @param hostId
     * @return
     * @throws HTTPException
     */
    public static Path getStaticPath(String hostId) throws HTTPException {
        return getWebInfPath(hostId).resolve("static");
    }

}
