package org.chaostocosmos.leap.http.resources;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import org.chaostocosmos.leap.http.commons.UNIT;
import org.chaostocosmos.leap.http.context.Host;

/**
 * TEMPLATE
 * 
 * @author 9ins
 */
public class TemplateFactory {
    /**
     * Make welcome page with directory view
     * @param contextPath
     * @param host
     * @return
     * @throws Exception
     */
    public static String getWelcomeResourceHtml(String contextPath, Host<?> host) throws Exception {
        String resourcePage = host.getResource().getResourcePage(Map.of("@resourceList", getResourceJson(contextPath, host)));
        String welcomePage = host.getResource().getWelcomePage(Map.of("@serverName", host.getHost(), "@body", resourcePage));
        String javascript = host.getResource().getTemplatePage("script/script.js", null);
        return host.getResource().getTemplatePage("templates/default.html", Map.of("@serverName", host.getHost(), "@javascript", javascript, "@body", welcomePage));
    }
    /**
     * Make resources page
     * @param contextPath
     * @param host
     * @return
     * @throws Exception
     */
    public static String getResourceHtml(String contextPath, Host<?> host) throws Exception {
        String resourcePage = host.getResource().getResourcePage(Map.of("@resourceList", getResourceJson(contextPath, host)));
        String javascript = host.getResource().getTemplatePage("script/script.js", null);
        return host.getResource().getTemplatePage("templates/default.html", Map.of("@serverName", host.getHost(), "@javascript", javascript, "@body", resourcePage));
    }    
    /**
     * Make resources Json
     * @param path
     * @param hosts
     * @return
     * @throws Exception
     */
    public static String getResourceJson(String contextPath, Host<?> host) throws Exception {
        String path = contextPath.charAt(contextPath.length() - 1) == '/' ? contextPath.substring(0, contextPath.lastIndexOf('/')) : contextPath;
        final String path1 = path.equals("") ? "/" : path;
        List<File> resourceInfos = Arrays.asList(host.getStatic().resolve(path1.substring(1)).toFile().listFiles())
                                         .stream()
                                         .sorted(Comparator.comparing(f -> f.isDirectory() ? -1 : 1))
                                         .filter(f -> host.getAccessFiltering().include(f.getName()) || host.getAccessFiltering().exclude(f.getName()))
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
