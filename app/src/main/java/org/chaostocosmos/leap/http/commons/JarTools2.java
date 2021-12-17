package org.chaostocosmos.leap.http.commons;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Jar management tool object
 * 
 * @author 9ins
 */
public class JarTools2 {

    /**
     * Add all directory items to jar on specific resource path
     * @param jarPath
     * @param jarResourcePath
     * @param directoryPath
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void addDirectoryInJar(Path jarPath, String jarResourcePath, Path directoryPath) throws IOException, URISyntaxException {
        if(!jarPath.toFile().getName().endsWith(".jar")) {
            throw new IllegalArgumentException("Specified Path must be jar file: "+jarPath.toString());
        }
        if(!Files.isDirectory(directoryPath)) {
            throw new IllegalArgumentException("Directory path must be directory: "+directoryPath.toString());            
        }
        Files.walk(directoryPath).forEach(p -> {
            try {
                addFileInJar(jarPath, jarResourcePath, p); 
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Add file to jar file on matching resource path in jar
     * @param jarPath
     * @param jarResourcePath
     * @param filePath
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void addFileInJar(Path jarPath, String jarResourcePath, Path filePath) throws IOException, URISyntaxException {
        if(!jarPath.toFile().getName().endsWith(".jar")) {
            throw new IllegalArgumentException("Specified Path must be jar file: "+jarPath.toString());
        }
        URI uri = new URI("jar", jarPath.toUri().toString(), null);
        try(FileSystem fileSystem = FileSystems.newFileSystem(uri, new HashMap<>())) {
            Files.walk(filePath).forEach(p -> {
                Path jarInnerPath = fileSystem.getPath(jarResourcePath).resolve(filePath.toString()); 
                try {
                    System.out.println(jarInnerPath.toString()+"   "+filePath);
                    if(Files.isDirectory(filePath)) {
                        Files.createDirectories(jarInnerPath);
                    } else {
                        Files.write(jarInnerPath, Files.readAllBytes(filePath), StandardOpenOption.CREATE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch(IOException ioe) {
            throw ioe;
        }        
    }

    /**
     * Delete files in jar with including patterns
     * @param jarPath
     * @param excluedPatterns
     * @throws URISyntaxException
     * @throws IOException
     */
    public static void deleteInJar(Path jarPath, List<String> excluedPatterns) throws URISyntaxException, IOException {
        if(!jarPath.toFile().getName().endsWith(".jar")) {
            throw new IllegalArgumentException("Specified Path must be jar file: "+jarPath.toString());
        }
        URI uri = new URI("jar", jarPath.toUri().toString(), null);
        try(FileSystem fileSystem = FileSystems.newFileSystem(uri, new HashMap<>())) {
            Files.walk(fileSystem.getPath(""))
                 .forEach(p -> {
                    if (excluedPatterns != null 
                        && excluedPatterns.size() > 0 
                        && excluedPatterns
                            .stream()
                            .anyMatch(m -> p.toString().matches(".*"+Arrays.asList(m.split(Pattern.quote("*")))
                            .stream().map(s -> s.equals("") ? "" : "("+Pattern.quote(s)+")")
                            .collect(Collectors.joining("(.*)"))+".*"))) {
                        try {
                            Files.delete(p);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                        System.out.println(String.format("JAR: %-60s  DELETED: %-30s", p.toString(), p.getFileName().toString()));
                    }
                 });
        } catch(IOException ioe) {
            throw ioe;
        }
    }

    /**
     * Extract files in jar file with excluding patterns
     * @param jarPath
     * @param matchingPatterns
     * @param outputPath
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<Path> extractJar(Path jarPath, List<String> matchingPatterns, Path outputPath) throws URISyntaxException, IOException {
        if(!jarPath.toFile().getName().endsWith(".jar")) {
            throw new IllegalArgumentException("Specified Path must be jar file: "+jarPath.toString());             
        }
        URI uri = new URI("jar", jarPath.toUri().toString(), null);
        try(FileSystem fileSystem = FileSystems.newFileSystem(uri, new HashMap<>())) {
            return Files.walk(fileSystem.getPath("")).map(p -> {
                Path path = Paths.get(outputPath.toString(), p.toString());
                try {
                    if (matchingPatterns != null 
                        && matchingPatterns.size() > 0 
                        && matchingPatterns
                            .stream()
                            .anyMatch(m -> p.toString().matches(".*"+Arrays.asList(m.split(Pattern.quote("*")))
                            .stream()
                            .map(s -> s.equals("") ? "" : "("+Pattern.quote(s)+")")
                            .collect(Collectors.joining("(.*)"))+".*"))) {
                        System.out.println(String.format("JAR RESOURCE: %-50s  EXCLUDE: %-30s", p.getParent().toString(), p.getFileName().toString()));
                    } else {
                        //System.out.println(p); 
                        if(Files.isDirectory(p)) {
                            Files.createDirectories(path); 
                        } else {
                            Files.write(path, Files.readAllBytes(p), StandardOpenOption.CREATE); 
                        }
                    }
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                }
                return path;
            }).filter(p -> p != null).collect(Collectors.toList());
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Build jar file with specified directory sub items
     * @param rootPath
     * @param jarPath
     * @param excludePatterns
     * @throws URISyntaxException
     * @throws IOException
     */
    public static void buildJar(Path rootPath, Path jarPath, final List<String> excludePatterns) throws URISyntaxException, IOException {
        if(!Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Specified Path to be jar must be directory: "+rootPath.toString());             
        }
        if(!jarPath.toFile().getName().endsWith(".jar")) {
            throw new IllegalArgumentException("Specified Path must be jar file: "+jarPath.toString());             
        }        
        URI uri = new URI("jar", jarPath.toAbsolutePath().toUri().toString(), null);
        try(FileSystem jarFileSystem = FileSystems.newFileSystem(uri, Map.of("create", "true"))) {
            Path jarInnerPath = jarFileSystem.getPath("");
            Files.walk(rootPath)
                .forEach(p -> {
                    try {
                        if (excludePatterns != null 
                            && excludePatterns.size() > 0 
                            && excludePatterns
                                .stream()
                                .anyMatch(m -> p.toString()
                                .matches(".*"+Arrays.asList(m.split(Pattern.quote("*")))
                                .stream()
                                .map(s -> s.equals("") ? "" : "("+Pattern.quote(s)+")").collect(Collectors.joining("(.*)"))+".*"))) {
                            System.out.println(String.format("JAR: %-60s  EXCLUDE: %-30s", p.toString(), p.getFileName().toString()));
                        } else {
                            Path path = jarInnerPath.resolve(rootPath.relativize(p).toString()).normalize();
                            //System.out.println(path);
                            if(Files.isDirectory(p)) {                            
                                Files.createDirectories(path); 
                            } else {                                
                                Files.write(path, Files.readAllBytes(p), StandardOpenOption.CREATE); 
                            }
                        }
                    } catch(IOException ioe) {
                        ioe.printStackTrace();
                    }
                });
        }
    }    

    public static void main(String[] args) throws Exception { 
        List<String> excludePatterns = Arrays.asList("*JarTools.class*");
        addDirectoryInJar(Paths.get("D:\\0.github\\chaos-commons\\tmp\\log4j-jndi-remover.jar"), "test/resource/", Paths.get("./tmp/org"));
        //addFileInJar(Paths.get("D:\\0.github\\chaos-commons\\tmp\\log4j-jndi-remover.jar"), "test/resource/", Paths.get("D:\\0.github\\chaos-commons\\tmp\\20150427_083607.jpg"));
        //deleteInJar(Paths.get("D:\\0.github\\chaos-commons\\tmp\\log4j-jndi-remover.jar"), excludePatterns);
        //extractJar(Paths.get("D:\\0.github\\chaos-commons\\tmp\\log4j-jndi-remover.jar"), excludePatterns, Paths.get("./tmp/out"));
        //buildJar(Paths.get("./tmp/out"), Paths.get("./tmp/out.jar"), excludePatterns);
    }
}
