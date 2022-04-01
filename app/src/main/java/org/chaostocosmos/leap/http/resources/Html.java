package org.chaostocosmos.leap.http.resources;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chaostocosmos.leap.http.commons.Constants;

/**
 * Html handing
 * 
 * @author 9ins
 */
public class Html {
    
    /**
     * Make resources html page
     * @param path
     * @param hosts
     * @param resNames
     * @return
     * @throws IOException
     */
    public static String makeResourceHtml(String path_, Hosts hosts, List<String> resNames) throws IOException {
        final String path = path_.charAt(path_.length() - 1) == '/' ? path_.substring(0, path_.length() - 1) : path_;        
        Stream<File> fstream = resNames.stream()
                                       .map(s -> hosts.getStatic().resolve(path_.charAt(0) == '/' ? path_.substring(1) : path_).resolve(s).toFile())
                                       .filter(f -> f.isFile())
                                       .sorted();
        Stream<File> dstream = resNames.stream()
                                       .map(s -> hosts.getStatic().resolve(path_.charAt(0) == '/' ? path_.substring(1) : path_).resolve(s).toFile())
                                       .filter(f -> f.isDirectory())
                                       .sorted();
        Stream<File> files = Stream.concat(dstream, fstream);
        Map<String, Object> params = new HashMap<>();
        params.put("@path", path);
        params.put("@host", hosts.getHost()+":"+hosts.getPort());
        params.put("@list", files.map(f -> {
                            String type = f.isFile() ? "file" : "dir";
                            String resource = type.equals("file") ? f.getName() : f.getName()+"/";
                            String size = type.equals("file") ? f.length()+"" : "-";
                            return "<img style=\"vertical-align:middle\" src=\"../img/"+type.toLowerCase()+".png\" alt=\"["+type.toUpperCase()+"]\">"
                                    +"<span class=\"label\"> <a href=\""+path+"/"+f.getName()+"\">"+resource+"</a></span>"
                                    +"<span class=\"lastModi\">"+new Date(f.lastModified())+"</span>"
                                    +"<span class=\"fileSize\">"+size+"</span>"
                                    +"<span class=\"desc\">-</span>";
        }).collect(Collectors.joining(Constants.LS)));
        return hosts.getResource().getResourcePage(params);
    }
}


