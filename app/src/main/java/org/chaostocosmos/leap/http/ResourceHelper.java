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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private ResourceHelper(Path HOME_PATH) throws IOException {
        this.HOME_PATH = HOME_PATH;
    }

    /**
     * Get ResourceHelper instance
     * @return
     * @throws IOException
     */
    public static ResourceHelper getInstance() throws IOException {
        return getInstance(Context.getDefaultDocroot());
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
        Hosts vhost = VirtualHostManager.getInstance().getVirtualHost(host);
        if(vhost != null) {
            docroot = vhost.getDocroot().toAbsolutePath();
        } else {
            docroot = Context.getDefaultDocroot().toAbsolutePath(); 
        }
        reqPath = getStaticPath(host).resolve(path).toAbsolutePath();
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
            FileSystem fileSystem = FileSystems.newFileSystem(res.toURI(), new HashMap<>());
            pStream = Files.walk(fileSystem.getPath(resourcePath));
        } else if(protocol.equals("file")) {
            pStream = Files.walk(Paths.get(res.toURI()));
        } else {
            throw new IllegalArgumentException("Resource protocol isn't supported!!! : "+res.getProtocol());
        }
        return pStream.map(p -> {
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
                    if(!path.toFile().exists() || path.toFile().lastModified() != modMillis) {
                        //System.out.println("File created..........."+modMillis+"  "+path.toFile().lastModified());                         
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
