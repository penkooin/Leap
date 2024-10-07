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
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.chaostocosmos.leap.Leap;
import org.chaostocosmos.leap.common.file.FileTools;
import org.chaostocosmos.leap.common.utils.ClassUtils;
import org.chaostocosmos.leap.common.utils.UtilBox;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.WAR_PATH;
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
        return getInstance(Leap.HOME_PATH);
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
    public static String getMimeType(HttpRequest<?> request) {
        return UtilBox.probeContentType(getResourcePath(request));
    }

    /**
     * Get resource Path
     * @param request
     * @return
     */
    public static Path getResourcePath(HttpRequest<?> request) {
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
        Path statics = host.getStatic().toAbsolutePath();
        Path reqPath = statics.resolve(path).toAbsolutePath();
        if(!validatePath(statics, reqPath)) {
            throw new LeapException(HTTP.RES400,"Requested path is wrong: "+contextPath);
        }
        host.getLogger().debug("REQUEST PATH: "+reqPath.toString()); 
        return reqPath.toAbsolutePath().normalize();
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
        File file = Context.get().getHome().resolve(WAR_PATH.CONFIG.path()).resolve("trademark").toFile();
        return UtilBox.readAllString(new FileInputStream(file)); 
    }

    /**
     * Extract resurces from jar or file
     * @param res
     * @param targetPath
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<File> extractResource(final String res, final Path targetPath, boolean removeTarget) throws IOException, URISyntaxException {        
        ClassLoader classLoader = ClassUtils.getClassLoader();
        URL url = classLoader.getResource(res);
        String protocol = url.getProtocol();        
        FileSystem fileSystem = null;
        Stream<Path> pStream = null;
        List<File> fileList = null;
        try {
            if(removeTarget) {
                Path backupPath = FileTools.backupSuffix(targetPath.resolve("bak").resolve("backup"));
                FileTools.directoryDelete(targetPath, backupPath);
            }
            if(protocol.equals("jar")) {
                fileSystem = FileSystems.newFileSystem(url.toURI(), new HashMap<>());
                pStream = Files.walk(fileSystem.getPath(res));
            } else if(protocol.equals("file")) {
                pStream = Files.walk(Paths.get(url.toURI()));
            } else {
                throw new IllegalArgumentException("Resource protocol isn't supported!!! : "+url.getProtocol());
            }
            fileList = pStream.map(p -> {
                try {
                    int idx = p.toString().lastIndexOf(res);
                    String relative = p.toString();
                    if(idx != -1) {
                        relative = relative.substring(idx);
                    }
                    relative = relative.indexOf("webapp") != -1 ? relative.replace("webapp", "") : relative;
                    if(relative.equals("")) {
                        return null;
                    }
                    relative = relative.charAt(0) == '/' ? relative.substring(1) : relative;
                    Path path = targetPath.resolve(relative).toAbsolutePath().normalize();
                    if(!Files.exists(path) || (Files.exists(path) && Files.getLastModifiedTime(path).compareTo(Files.getLastModifiedTime(p)) < 0)) {
                        if(Files.isDirectory(p)) {
                            Files.createDirectories(path);
                        } else {
                            if(Files.exists(path)) {
                                Files.delete(path);
                            }
                            byte[] bytes = Files.readAllBytes(p);
                            Path parent = path.getParent();
                            if(!Files.exists(parent)) {
                                Files.createDirectories(parent);
                            }
                            File file = Files.write(path, bytes, StandardOpenOption.CREATE).toFile();
                            Files.setLastModifiedTime(path, Files.getLastModifiedTime(path));
                            return file;    
                        }
                    }
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());        
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(pStream != null) pStream.close();
            if(fileSystem != null) fileSystem.close();
        }     
        return fileList;   
        /*
        if(protocol.equals("jar")) {
            try (FileSystem fileSystem = FileSystems.newFileSystem(url.toURI(), new HashMap<>())) {
                Stream<Path> pStream = Files.walk(fileSystem.getPath(res));
                if(excludeList != null) {
                    pStream = pStream.filter(p -> excludeList.stream().anyMatch(e -> !p.toString().startsWith(e))); 
                } 
                List<File> results = pStream.map(p -> {
                    try {
                        String relative = (p.toString().contains("webapp")) ? p.toString().replace("webapp/", "") : p.toString();
                        System.out.println(relative.toString());
                        Path path = targetPath.resolve(relative);
                        if(Files.isDirectory(p)) {
                            Files.createDirectories(path);
                        } else { 
                            if(!Files.exists(path) || (Files.exists(path) && Files.getLastModifiedTime(path).compareTo(Files.getLastModifiedTime(p)) < 0)) {
                                if(Files.exists(path)) {
                                    Files.delete(path);
                                }
                                byte[] bytes = Files.readAllBytes(p);
                                File file = Files.write(path, bytes, StandardOpenOption.CREATE).toFile();
                                Files.setLastModifiedTime(path, Files.getLastModifiedTime(path));
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
        } else if(protocol.equals("file")) {
            Stream<Path> pStream = Files.walk(Paths.get(url.toURI()));
            List<File> results = pStream.map(p -> {
                try {
                    String relative = (p.toString().contains("webapp")) ? p.toString().replace("webapp/", "") : p.toString();                    
                    Path path = targetPath.resolve(relative);
                    if(Files.isDirectory(p)) {
                        path.toFile().mkdirs();
                    } else { 
                        if(!Files.exists(path) || (Files.exists(path) && Files.getLastModifiedTime(path).compareTo(Files.getLastModifiedTime(p)) < 0)) {
                            if(Files.exists(path)) {
                                Files.delete(path);
                            }
                            byte[] bytes = Files.readAllBytes(p);
                            File file = Files.write(path, bytes, StandardOpenOption.CREATE).toFile();
                            Files.setLastModifiedTime(path, Files.getLastModifiedTime(path));
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
        } else {
            throw new IllegalArgumentException("Resource protocol isn't supported!!! : "+url.getProtocol());
        }
        */
    }

    /**
     * Whether specific name is exists in Path
     * @param path
     * @param name
     * @return
     */
    public static boolean isExist(Path path, String name) {
        OptionalInt startIndex = IntStream.range(0, path.getNameCount())
                .filter(i -> path.getName(i).toString().equals(name))
                .findFirst();
        if(startIndex.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get sub path object by name
     * @param path
     * @param name
     * @return
     */
    public static Path getSubPathFromName(Path path, String name) {
        OptionalInt startIndex = IntStream.range(0, path.getNameCount())
                .filter(i -> path.getName(i).toString().equals(name))
                .findFirst();
        if(startIndex.isPresent()) {
            return path.subpath(startIndex.getAsInt(), path.getNameCount());
        }
        return path;
    }    
          
    /**
     * Remove specific name in Path
     * @param path
     * @param nameToRemove
     * @return
     */
    public static Path removeNameFromPath(Path path, String nameToRemove) {
        OptionalInt nameIndex = IntStream.range(0, path.getNameCount())
            .filter(i -> path.getName(i).toString().equals(nameToRemove))
            .findFirst();
        if (nameIndex.isPresent()) {
            int index = nameIndex.getAsInt();
            Path newPath = IntStream.range(0, path.getNameCount())
                    .filter(i -> i != index)
                    .mapToObj(path::getName)
                    .reduce(Paths.get(""), Path::resolve);
            
            return newPath;
        }
        return path;
    }

    /**
     * Get WAS Home path
     * @param hostId
     * @return
     * @throws LeapException
     */
    public static Path getHome(String hostId) throws LeapException {
        return WAR_PATH.ROOT.getPath(hostId);
    }

    /**
     * Get webapp path
     * @param hostId
     * @return
     * @throws LeapException
     */
    public static Path getStatic(String hostId) throws LeapException {
        return WAR_PATH.STATIC.getPath(hostId);
    }

    /**
     * Get webapp/WEB-INF path
     * @param hostId
     * @return
     * @throws LeapException
     */
    public static Path getWEB_INF(String hostId) throws LeapException {
        return WAR_PATH.WEBINF.getPath(hostId);
    }

    /**
     * Get webapp/WEB-INF/static path
     * @param hostId
     * @return
     * @throws LeapException
     */
    public static Path getViews(String hostId) throws LeapException {
        return WAR_PATH.VIEWS.getPath(hostId);
    }
}
