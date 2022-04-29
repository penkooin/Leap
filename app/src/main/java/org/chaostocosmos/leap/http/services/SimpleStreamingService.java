package org.chaostocosmos.leap.http.services;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chaostocosmos.leap.http.HttpRequestDescriptor;
import org.chaostocosmos.leap.http.HttpResponseDescriptor;
import org.chaostocosmos.leap.http.annotation.MethodMappper;
import org.chaostocosmos.leap.http.annotation.ServiceMapper;
import org.chaostocosmos.leap.http.enums.REQUEST_TYPE;
import org.chaostocosmos.leap.http.resources.MediaStreamer;
import org.chaostocosmos.leap.http.resources.Resources;
import org.chaostocosmos.leap.http.resources.WatchResources.ResourceInfo;

@ServiceMapper(path = "/streaming")
public class SimpleStreamingService extends AbstractLeapService {

    private static final int BUFFER_LENGTH = 1024 * 100;
    private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24;
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");
    
    @Override
    public Throwable errorHandling(HttpResponseDescriptor response, Throwable throwable) throws Throwable {
        throwable.printStackTrace();
        return null;
    }

    @MethodMappper(mappingMethod = REQUEST_TYPE.GET, path = "/video")
    public void stream(final HttpRequestDescriptor request, final HttpResponseDescriptor response) throws Exception {
        processRequest(request, response);
    }

    /**
     * Process streaming requeset.
     * @param request
     * @param response
     * @throws Exception
     */
    private void processRequest(final HttpRequestDescriptor request, final HttpResponseDescriptor response) throws Exception {
        String videoFilename = request.getParameter("file");
        Path video = super.serviceManager.getHosts().getStatic().resolve("video").resolve(videoFilename);
        String range = request.getReqHeader().get("Range");
        Matcher matcher = RANGE_PATTERN.matcher(range);
        super.logger.debug("Request Range: "+range);

        ResourceInfo resourceInfo = super.getResource().getResourceInfo(video);
        MediaStreamer mediaStreamer = new MediaStreamer(resourceInfo);

        long fileLength = video.toFile().length();
        int start = 0, end = 0;
        if (matcher.matches()) {
            String startGroup = matcher.group("start");
            start = startGroup.isEmpty() ? start : Integer.valueOf(startGroup);
            start = start < 0 ? 0 : start;
        }

        int len = (int)(start + BUFFER_LENGTH > fileLength - 1 ? fileLength - 1 : BUFFER_LENGTH);
        byte[] body = mediaStreamer.getProgress(start, len);
        int contentLength = body.length;

        super.logger.debug("Video streaming called: "+videoFilename+" ====================================================="+fileLength);
        System.out.println("Content start: "+start+"  length: "+contentLength);        
        response.addHeader("Content-Disposition", String.format("inline;filename=\"%s\"", videoFilename));
        response.addHeader("Accept-Ranges", "bytes");
        response.addHeader("Last-Modified", Files.getLastModifiedTime(video).toMillis());
        response.addHeader("Expires", System.currentTimeMillis() + EXPIRE_TIME);
        response.addHeader("Content-Type", Files.probeContentType(video));
        response.addHeader("Content-Range", String.format("bytes %s-%s/%s", start, start+len, fileLength));
        response.addHeader("Content-Length", String.format("%s", contentLength));
        response.setStatusCode(206);
        response.setBody(body);
    }    

    /**
     * Get file partial data bytes
     * @param contextPath
     * @param start
     * @param length
     * @return
     * @throws Exception
     */
    public byte[] getFilePartial(String contextPath, long start, int length) throws Exception {
        Resources resource = super.getResource();
        Object res = resource.getContextResourceInfo(contextPath);
        byte[] data = new byte[length];
        if(res instanceof File) {
            FileInputStream in = new FileInputStream((File)res);
            in.skip(start);
            int len = in.read(data);
            in.close();
        } else if(res instanceof byte[]) {
            byte[] fileBytes = (byte[])res;
            System.arraycopy(fileBytes, (int)start, data, 0, length);
        }
        return data;
    }   
}
