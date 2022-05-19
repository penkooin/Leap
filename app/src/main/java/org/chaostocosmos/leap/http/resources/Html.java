package org.chaostocosmos.leap.http.resources;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chaostocosmos.leap.http.commons.Constants;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;

/**
 * Html handing
 * 
 * @author 9ins
 */
public class Html {

    /**
     * Make welcome page with directory view
     * @param contextPath
     * @param host
     * @param resNames
     * @return
     * @throws Exception
     */
    public static String makeWelcomeResourceHtml(String contextPath, Host<?> host, List<String> resNames) throws Exception {
        return host.getResource().getWelcomePage(Map.of("@serverName", host.getHost(), "@resource", makeResourceHtml(contextPath, host, resNames)));        
    }
    
    /**
     * Make resources html page
     * @param path
     * @param hosts
     * @param resNames
     * @return
     * @throws Exception
     */
    public static String makeResourceHtml(String contextPath, Host<?> host, List<String> resNames) throws Exception {
        final String path = contextPath.charAt(contextPath.length() - 1) == '/' ? contextPath.substring(0, contextPath.length() - 1) : contextPath;        
        Stream<File> fstream = resNames.stream()
                                       .map(s -> host.getStatic().resolve(contextPath.charAt(0) == '/' ? contextPath.substring(1) : contextPath).resolve(s).toFile())
                                       .filter(f -> f.isFile() && host.getAccessFiltering().include(f.getName()) || host.getForbiddenFiltering().exclude(f.getName()))
                                       .sorted();
        Stream<File> dstream = resNames.stream()
                                       .map(s -> host.getStatic().resolve(contextPath.charAt(0) == '/' ? contextPath.substring(1) : contextPath).resolve(s).toFile())
                                       .filter(f -> f.isDirectory() && host.getAccessFiltering().include(f.getName()) || host.getForbiddenFiltering().exclude(f.getName()))
                                       .sorted();
        Stream<File> files = Stream.concat(dstream, fstream);        
        
        String[] paths = path.split(Pattern.quote("/"));
        paths[0] = "..";
        String dir = "";
        String p = "";
        for(int i=0; i<paths.length; i++) {
            p +="/"+paths[i];
            dir += "<a href=\""+p+"\">"+paths[i]+"</a>/";
        }
        Map<String, Object> params = new HashMap<>();
        params.put("@path", paths.length > 1 ? dir : "");
        params.put("@parent", paths.length > 1 ? path: "");
        params.put("@moveUp", paths.length > 1 ? "..": "");
        params.put("@host", host.getHost()+":"+host.getPort());
        params.put("@list", files.map(f -> {
                            String type = f.isFile() ? "file" : "dir";
                            String resource = type.equals("file") ? f.getName() : f.getName()+"/";
                            String size = type.equals("file") ? f.length()+"" : "-";
                            return "<img style=\"vertical-align:middle\" src=\"/img/"+type.toLowerCase()+".png\" alt=\"["+type.toUpperCase()+"]\">"
                                    +"<span class=\"label\"> <a href=\""+path+"/"+f.getName()+"\">"+resource+"</a></span>"
                                    +"<span class=\"lastModi\">"+new Date(f.lastModified())+"</span>"
                                    +"<span class=\"fileSize\">"+size+"</span>"
                                    +"<span class=\"desc\">-</span>";
        }).collect(Collectors.joining(Constants.LS)));
        return host.getResource().getResourcePage(params);
    }

    /**
     * Make redirect page
     * @param seconds
     * @param url
     * @return
     */
    public static String makeRedirectHtml(String protocol, int seconds, String url) {
        return protocol+" 200 Leap Load-Balance redirect.\r\n"
                +"Date: "+new Date()+"\r\n"
                +"Server: Leap?/"+Context.getServer().getLeapVersion()+"("+System.getProperty("os.name")+") java/"+System.getProperty("java.version")+"\r\n"
                +"Content-Type: text/html; charset=iso-8859-1\r\n"
                +"\r\n"
                +"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">"
                +"<html><head><title>Leap Load-Balance redirect!!!</title>"
                +"<meta http-equiv = \"refresh\" content = \""+seconds+"; url = '"+url+"'\" />"
                +"</head><body></body></html>";
    }

    /**
     * Make load-balance redirect
     * @param protocol
     * @param url
     * @return
     */
    public static String makeRedirect(String protocol, int seconds, String url) {
        return protocol+" 307 Leap Load-Balance redirect.\r\n"
               +"Date: "+new Date()+"\r\n"
               +"Server: Leap?/"+Context.getServer().getLeapVersion()+"("+System.getProperty("os.name")+") java/"+System.getProperty("java.version")+"\r\n"
               +"Refresh: "+seconds+"; URL="+url+"\r\n"
               +"Connection: close"+"\r\n"
               +"Content-Type: text/html; charset=iso-8859-1\r\n\r\n";
    }
}


