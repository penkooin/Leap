package org.chaostocosmos.leap.http;

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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chaostocosmos.leap.http.commons.UtilBox;

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
    private ResourceHelper(Path HOME_PATH) {
        this.HOME_PATH = HOME_PATH;
    }

    /**
     * Get ResourceHelper instance
     * @return
     * @throws IOException
     */
    public static ResourceHelper getInstance() {
        return getInstance(Context.getDefaultDocroot());
    }

    /**
     * Get ResourceHelper instance
     * @param HOME_PATH
     * @throws IOException
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
     * @throws URISyntaxException
     * @throws WASException
     * @throws IOException
     */
    public static  String getMimeType(HttpRequestDescriptor request) throws WASException {
        return UtilBox.probeContentType(getResourcePath(request));
    }

    /**
     * Get resource Path
     * @param request
     * @return
     * @throws URISyntaxException
     * @throws WASException
     * @throws IOException
     */
    public static Path getResourcePath(HttpRequestDescriptor request) throws WASException {
        return getResourcePath(request.getRequestedHost(), request.getContextPath());
    }

    /**
     * Get server resource
     * @param request
     * @return
     * @throws IOException
     * @throws WASException
     * @throws URISyntaxException
     */    
    public static String getResourceContents(HttpRequestDescriptor request) throws WASException {
        return getResourceContents(request.getRequestedHost(), request.getContextPath());
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
    public static Path getResourcePath(String host, String path) throws WASException {
        path = path.charAt(0) == '/' ? path.substring(1) : path;
        Path docroot = null;
        Path reqPath = null;
        Hosts vhost = HostsManager.getInstance().getHosts(host);
        if(vhost != null) {
            docroot = vhost.getDocroot().toAbsolutePath();
        } else {
            docroot = Context.getDefaultDocroot().toAbsolutePath(); 
        }
        reqPath = getStaticPath(host).resolve(path).toAbsolutePath();
        if(!validatePath(docroot, reqPath)) {
            throw new WASException(MSG_TYPE.ERROR, 19, new Object[]{host});
        }
        return reqPath;
    }

    /**
     * Get response html file contents
     * @param host
     * @param code
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws WASException
     */
    public static Path getResponseResourcePath(String host, int code) throws WASException, URISyntaxException {
        Object msg = Context.getConfigValue("message.http."+code);
        if(msg == null) {
            throw new WASException(MSG_TYPE.HTTP, 500);
        }
        return getStaticPath(host).resolve(Context.getConfigValue("static-resource.response").toString());
    }

    /**
     * Get server resource
     * @param host
     * @param path
     * @return
     * @throws IOException
     * @throws WASException
     * @throws URISyntaxException
     */
    public static String getResourceContents(String host, String path) throws WASException {
        Path rPath = getResourcePath(host, path);
        try {
            return Files.readString(rPath);
        } catch (IOException e) {
            throw new WASException(e);
        }
    }

    /**
     * Get response page with code
     * @param code
     * @return
     * @throws IOException
     * @throws WASException
     * @throws URISyntaxException
     */
    public static String getResponsePage(String host, int code) throws IOException, WASException, URISyntaxException {
        return getResourceContent(host, "response.html", Map.of("@code", code, "@message", Context.getHttpMsg(code)));
    }

    /**
     * Get resource replaced with specified params
     * @param resourcePath
     * @param param
     * @return
     * @throws IOException
     * @throws WASException
     */
    public static String getResourceContent(String host, String contentName, final Map<String, Object> param) throws WASException {
        try {
            Path path = getStaticPath(host).resolve(contentName.replace("/", File.separator));
            String all = Files.readString(path, Context.charset());
            if(param == null) {
                return all;
            }
            for(Entry<String, Object> e : param.entrySet()) {
                all = all.replace(e.getKey(), e.getValue().toString());
            }
            return all;
        } catch (IOException e) {
            throw new WASException(MSG_TYPE.ERROR, 38, contentName);
        }        
    }

    /**
     * Get binary data
     * @param resourcePath
     * @return
     * @throws WASException
     * @throws IOException
     */
    public static byte[] getBinaryResource(Path resourcePath) throws WASException {
        try {
            return Files.readAllBytes(resourcePath);
        } catch (IOException e) {
            throw new WASException(MSG_TYPE.ERROR, 39, resourcePath);
        }
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
        File file = Context.getHomePath().resolve("config").resolve("trademark").toFile();
        return UtilBox.readAllString(new FileInputStream(file)); 
    }

    /**
     * Extract resurces from jar or file
     * @param resourcePath
     * @param targetPath
     * @return
     * @throws WASException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<File> extractResource(String resourcePath, final Path targetPath) throws IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL res = classLoader.getResource(resourcePath);
        String protocol = res.getProtocol();
        // if (res == null) {
        //     throw new WASException(MSG_TYPE.ERROR, "error020", resourcePath);
        // }
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
        }).filter(Objects::nonNull)
          .collect(Collectors.toList());
        pStream.close();
        return results;
    }

    /**
     * Get WAS Home path
     * @param host
     * @return
     * @throws WASException
     */
    public static Path getHomePath(String host) throws WASException {
        if(Context.getDefaultHost().equals(host)) {
            return Context.getDefaultDocroot();
        } else {
            return Context.getVirtualHosts(host).getDocroot();
        }
    }

    /**
     * Get webapp path
     * @param host
     * @return
     * @throws WASException
     */
    public static Path getWebAppPath(String host) throws WASException {
        return getHomePath(host).resolve("webapp");
    }

    /**
     * Get webapp/WEB-INF path
     * @param host
     * @return
     * @throws WASException
     */
    public static Path getWebInfPath(String host) throws WASException {
        return getWebAppPath(host).resolve("WEB-INF");
    }

    /**
     * Get webapp/WEB-INF/static path
     * @param host
     * @return
     * @throws WASException
     */
    public static Path getStaticPath(String host) throws WASException {
        return getWebInfPath(host).resolve("static");
    }
}
