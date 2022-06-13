package org.chaostocosmos.leap.http.resources;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.commons.UNIT;
import org.chaostocosmos.leap.http.context.Context;
import org.chaostocosmos.leap.http.context.Host;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;

import com.google.gson.Gson;

/**
 * TEMPLATE
 * 
 * @author 9ins
 */
public class TemplateBuilder {
    /**
     * Make monitoring page 
     * @param contextPath
     * @param host
     * @return
     * @throws Exception
     */
    public static String buildMonitoringPage(String contextPath, Host<?> host) throws Exception {
        String monitorPage = host.getResource().getTemplatePage("templates/monitor.html", null);
        String scriptPage = host.getResource().getTemplatePage("script/script.js", null);
        return host.getResource().getTemplatePage("templates/default.html", Map.of("@serverName", host.getHost(), "@javascript", scriptPage, "@body", monitorPage));
    }
    /**
     * Make welcome page for directory view
     * @param contextPath
     * @param host
     * @return
     * @throws Exception
     */
    public static String buildWelcomeResourceHtml(String contextPath, Host<?> host) throws Exception {
        String resourcePage = host.getResource().getResourcePage(Map.of("@resourceList", buildResourceJson(contextPath, host)));
        String welcomePage = host.getResource().getWelcomePage(Map.of("@serverName", host.getHost(), "@body", resourcePage));
        String scriptPage = host.getResource().getTemplatePage("script/script.js", null);
        return host.getResource().getTemplatePage("templates/default.html", Map.of("@serverName", host.getHost(), "@javascript", scriptPage, "@body", welcomePage));
    }
    /**
     * Make resources page
     * @param contextPath
     * @param host
     * @return
     * @throws Exception
     */
    public static String buildResourceHtml(String contextPath, Host<?> host) throws Exception {
        String resourcePage = host.getResource().getResourcePage(Map.of("@resourceList", buildResourceJson(contextPath, host)));
        String javascript = host.getResource().getTemplatePage("script/script.js", null);
        return host.getResource().getTemplatePage("templates/default.html", Map.of("@serverName", host.getHost(), "@javascript", javascript, "@body", resourcePage));
    }
    /**
     * Build http error page
     * @param host
     * @param type
     * @param errorCode
     * @param message
     * @return
     * @throws Exception
     * @throws ImageProcessingException
     */
    public static String buildErrorHtml(Host<?> host, MSG_TYPE type, int errorCode, String message) throws Exception {
        String title = Context.getMessages().getHttpMsg(errorCode, "- "+type.name());        
        String errorPage = host.getResource().getErrorPage(Map.of("@code", errorCode, "@type", title, "@message", message));;
        return host.getResource().getTemplatePage("templates/default.html", Map.of("@serverName", host.getHost(), "@javascript", "", "@body", errorPage));
    }
    /**
     * Create http response page
     * @param host
     * @param type
     * @param code
     * @param message
     * @return
     * @throws Exception
     * @throws ImageProcessingException
     */
    public static String buildResponseHtml(Host<?> host, MSG_TYPE type, int code, String message) throws Exception {
        String responsePage = host.getResource().getResponsePage(Map.of("@code", code, "@type", type.name(), "@message", message));
        return host.getResource().getTemplatePage("templates/default.html", Map.of("@serverName", host.getHost(), "@javascript", "", "@body", responsePage));
    }        
    /**
     * Make resources Json
     * @param path
     * @param hosts
     * @return
     * @throws Exception
     */
    public static String buildResourceJson(String contextPath, Host<?> host) throws Exception {
        String path = contextPath.charAt(contextPath.length() - 1) == '/' ? contextPath.substring(0, contextPath.lastIndexOf('/')) : contextPath;
        final String path1 = path.equals("") ? "/" : path;
        System.out.println(host.getStatic().resolve(path1.substring(1))+"____________________________");
        List<File> resourceInfos = Arrays.asList(host.getStatic().resolve(path1.substring(1)).toFile().listFiles())
                                         .stream()
                                         .sorted(Comparator.comparing(f -> f.isDirectory() ? -1 : 1))
                                         .filter(f -> host.getForbiddenFiltering().exclude(f.getName()))
                                         .collect(Collectors.toList());
        System.out.println(resourceInfos);
        int pathCnt = path.length() - path.replace("/", "").length();
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("parent", pathCnt > 1 ? path.substring(0, path.lastIndexOf("/")): "/");
        params.put("host", host.getHost()+":"+host.getPort()+"/");
        params.put("elements", resourceInfos.stream().map(f -> {
                        String img = f.isFile() ? "/img/file.png" : "/img/dir.png";
                        String file = f.isFile() ? f.getName() : f.getName()+"/";
                        String uri = path+"/"+file;
                        long lastModified = f.lastModified();
                        String size = f.isFile() ? UNIT.MB.get(f.length())+" "+UNIT.MB.name() : "-";
                        return Map.of("img", img, "file", file, "uri", uri, "lastModified", new Date(lastModified).toString(), "size", size);
        }).collect(Collectors.toList()));
        return new Gson().toJson(params);
    }    
}
