package org.chaostocosmos.leap.resource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.common.SIZE;
import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.context.Host;
import org.chaostocosmos.leap.enums.TEMPLATE;

import com.google.gson.Gson;

/**
 * TEMPLATE
 * 
 * @author 9ins
 */
public class TemplateBuilder {
    /**
     * Build Pre tag
     * @param contents
     * @return
     */
    public static String buildPreTag(String contents) {
        String html = "<div><pre>"+contents+"</pre></div>";
        return html;
    }

    /**
     * Make monitoring page 
     * @param contextPath
     * @param host
     * @return
     * @throws Exception
     */
    public static String buildMonitoringPage(String contextPath, Host<?> host) throws Exception {
        String url = host.<String> getProtocol().toLowerCase()+"://"+host.getHost()+":"+host.getPort();
        String monitorPage = host.getResource().getTemplatePage(TEMPLATE.MONITOR.path(), Map.of("@url", url));                 
        String script = host.getResource().getTemplatePage("/script/refreshImage.js", Map.of("@interval", Context.get().server().getMonitoringInterval(), "@url", url));
        System.out.println(script+"-----------------------------------------------");
        System.out.println(monitorPage+"-----------------------------------------------");
        return host.getResource().getTemplatePage(TEMPLATE.DEFAULT.path(), Map.of("@serverName", host.getHost(), "@script", script, "@body", monitorPage));
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
        String script = host.getResource().getTemplatePage("/script/genDir.js", null);        
        return host.getResource().getTemplatePage(TEMPLATE.DEFAULT.path(), Map.of("@serverName", host.getHost(), "@script", script, "@body", welcomePage));
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
        String script = host.getResource().getTemplatePage("/script/genDir.js", null);
        return host.getResource().getTemplatePage(TEMPLATE.DEFAULT.path(), Map.of("@serverName", host.getHost(), "@script", script, "@body", resourcePage));
    }

    /**
     * Build http error page
     * @param host
     * @param errorCode
     * @param message
     * @param stackTrace
     * @return
     * @throws IOException
     */
    public static String buildErrorHtml(Host<?> host, int errorCode, String message, String stackTrace) throws IOException {
        String title = Context.get().message().http(errorCode);
        String errorPage = host.getResource().getErrorPage(Map.of("@code", errorCode, "@type", title, "@title", message, "@stacktrace", stackTrace));
        return host.getResource().getTemplatePage(TEMPLATE.DEFAULT.path(), Map.of("@serverName", host.getHost(), "@script", "", "@body", errorPage));
    }

    /**
     * Create http response page
     * @param host
     * @param code
     * @param message
     * @return
     * @throws IOException
     */
    public static String buildResponseHtml(Host<?> host, int code, String message) throws IOException {
        String responsePage = host.getResource().getResponsePage(Map.of("@code", code, "@type", "", "@message", message));
        return host.getResource().getTemplatePage(TEMPLATE.DEFAULT.path(), Map.of("@serverName", host.getHost(), "@javascript", "", "@body", responsePage));
    }        

    /**
     * Make resources Json
     * @param path
     * @param hosts
     * @return
     * @throws Exception
     */
    public static String buildResourceJson(String contextPath, Host<?> host) {
        String path = contextPath.charAt(contextPath.length() - 1) == '/' ? contextPath.substring(0, contextPath.lastIndexOf('/')) : contextPath;
        final String path1 = path.equals("") ? "/" : path;
        List<File> resourceInfos = Arrays.asList(host.getWebInf().resolve(path1.substring(1)).toFile().listFiles())
                                         .stream()
                                         .sorted(Comparator.comparing(f -> f.isDirectory() ? -1 : 1))
                                         .filter(f -> host.getForbiddenFiltering().exclude(f.getName()))
                                         .collect(Collectors.toList());
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
                        String size = f.isFile() ? SIZE.MB.get(f.length())+" "+SIZE.MB.name() : "-";
                        String inMemory = f.isDirectory() ? "-" : host.getResource().isInMemory(f.toPath()) ? "In-Memory resource" : "File resource";
                        return Map.of("img", img, "file", file, "uri", uri, "lastModified", new Date(lastModified).toString(), "size", size, "desc", inMemory);
        }).collect(Collectors.toList()));
        String json = new Gson().toJson(params);
        return json;
    }    
}
