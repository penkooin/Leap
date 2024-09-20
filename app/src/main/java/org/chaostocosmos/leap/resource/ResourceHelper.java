package org.chaostocosmos.leap.resource;

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

import org.chaostocosmos.leap.LeapApp;
import org.chaostocosmos.leap.common.UtilBox;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.WEB_PATH;
import org.chaostocosmos.leap.exception.LeapException;
import org.chaostocosmos.leap.http.HttpRequest;

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
    public static  String getMimeType(HttpRequest request) {
        return UtilBox.probeContentType(getResourcePath(request));
    }

    /**
     * Get resource Path
     * @param request
     * @return
     */
    public static Path getResourcePath(HttpRequest request) {
        return getResourcePath(Context.get().host(request.getHostId()), request.getContextPath());
    }

    /**
     * Get resource Path
     * @param host
     * @param contextPath
     * @return
     */
    public static Path getResourcePath(Host<?> host, final String contextPath) {
        String path = contextPath.charAt(0) == '/' ? contextPath.substring(1) : contextPath;
        Path webinf = host.getWebInf().toAbsolutePath();
        Path reqPath = webinf.resolve(path).toAbsolutePath();        
        if(!validatePath(webinf, reqPath)) {
            throw new LeapException(HTTP.RES400,"Requested path is wrong: "+contextPath);
        }
        host.getLogger().debug("REQUEST PATH: "+reqPath.toString()); 
        return reqPath.normalize();
    }

    /**
     * Get response html file contents
     * @param hostId
     * @param code
     * @return
     */
    public static Path getResponseResourcePath(String hostId) {
        return Context.get().hosts().getHost(hostId).getTemplates().resolve("response.html");
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
            throw new LeapException(HTTP.RES500, e);
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
        File file = Context.get().getHome().resolve(WEB_PATH.CONFIG.name().toLowerCase()).resolve("trademark").toFile();
        return UtilBox.readAllString(new FileInputStream(file)); 
    }

    /**
     * Extract resurces from jar or file
     * @param res
     * @param targetPath
     * @return
     * @throws LeapException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<File> extractResource(String res, final Path targetPath) throws IOException, URISyntaxException {        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(res);
        String protocol = url.getProtocol();
        Stream<Path> pStream;
        if(protocol.equals("jar")) {
            try (FileSystem fileSystem = FileSystems.newFileSystem(url.toURI(), new HashMap<>())) {
                pStream = Files.walk(fileSystem.getPath(res)); 
            }
        } else if(protocol.equals("file")) {
            pStream = Files.walk(Paths.get(url.toURI()));
        } else {
            throw new IllegalArgumentException("Resource protocol isn't supported!!! : "+url.getProtocol());
        }
        List<File> results = pStream.map(p -> {
            try {
                long modMillis = Files.getLastModifiedTime(p).toMillis();
                String ps = p.toString().replace("\\", "/");
                Path path = protocol.equals("jar") ? targetPath.resolve(ps) : Paths.get(targetPath.toAbsolutePath().toString(), ps.substring(ps.indexOf(res)-1).replace("\\", "/").replace("/webapp", "/"));
                if(Files.isDirectory(p)) {
                    path.toFile().mkdirs();
                } else { 
                    if(path.toFile().lastModified() != modMillis || path.toFile().length() != p.toFile().length()) {
                        path.toFile().delete(); 
                    }
                    if(!path.toFile().exists()) {
                        byte[] bytes = Files.readAllBytes(p);
                        File file = Files.write(path, bytes, StandardOpenOption.CREATE).toFile();
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
     * @throws LeapException
     */
    public static Path getHome(String hostId) throws LeapException {
        return WEB_PATH.HOME.getPath(hostId);
    }

    /**
     * Get webapp path
     * @param hostId
     * @return
     * @throws LeapException
     */
    public static Path getStatic(String hostId) throws LeapException {
        return WEB_PATH.STATIC.getPath(hostId);
    }

    /**
     * Get webapp/WEB-INF path
     * @param hostId
     * @return
     * @throws LeapException
     */
    public static Path getWEB_INF(String hostId) throws LeapException {
        return WEB_PATH.WEBINF.getPath(hostId);
    }

    /**
     * Get webapp/WEB-INF/static path
     * @param hostId
     * @return
     * @throws LeapException
     */
    public static Path getViews(String hostId) throws LeapException {
        return WEB_PATH.VIEWS.getPath(hostId);
    }

}
