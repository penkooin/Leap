package org.chaostocosmos.leap.http.services;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.http.WASException;
import org.chaostocosmos.leap.http.enums.MIME_TYPE;
import org.chaostocosmos.leap.http.enums.MSG_TYPE;
import org.chaostocosmos.leap.http.enums.RES_CODE;
import org.chaostocosmos.leap.http.resources.Resources;
import org.chaostocosmos.leap.http.resources.WatchResources.ResourceInfo;
import org.chaostocosmos.leap.http.services.model.StreamingModel;

/**
 * AbstractStreamingService
 * 
 * @author 9ins
 */
public abstract class AbstractStreamingService extends AbstractService implements StreamingModel {

    private final Pattern RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");
    private int bufferSize;
    private long EXPIRE_TIME = 1000 * 60 * 60 * 24;

    /**
     * Constructs with streaming media type & buffer size
     * @param mimeType
     * @param bufferSize
     */
    public AbstractStreamingService(MIME_TYPE mimeType, int bufferSize) {
        if(mimeType != MIME_TYPE.VIDEO_MP4 
           && mimeType != MIME_TYPE.VIDEO_X_FLV 
           && mimeType != MIME_TYPE.VIDEO_QUICKTIME
           && mimeType != MIME_TYPE.VIDEO_X_MSVIDEO
           && mimeType != MIME_TYPE.VIDEO_X_MS_WMV
           && mimeType != MIME_TYPE.APPLICATION_X_MPEGURL
           && mimeType != MIME_TYPE.APPLICATION_OCTET_STREAM
           && mimeType != MIME_TYPE.APPLICATION_ZIP
           ) {
           throw new WASException(MSG_TYPE.ERROR, 22, "Specified media type is not supported: "+mimeType.mimeType());
        }
        this.bufferSize = bufferSize;        
    }

    @Override
    public void serveGet(final Request request, final Response response) throws Exception {        
        String reqFile = request.getParameter("file");
        reqFile = reqFile.charAt(0) == '/' ? reqFile.substring(1) : reqFile;
        if(reqFile == null || reqFile.equals("")) {
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES412.code(), "Parameter not found(file). Streaming request must have field of file.");
        }
        Path file = super.serviceManager.getHost().getStatic().resolve(reqFile);
        if(!file.toFile().exists()) {
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES404.code(), "Specified resource not found: "+file.toAbsolutePath().toString().replace("\\", "/"));
        }
        ResourceInfo info = (ResourceInfo) super.resource.getResourceInfo(file);
        String range = request.getReqHeader().get("Range");
        if(range == null || range.equals("")) {
            throw new WASException(MSG_TYPE.HTTP, RES_CODE.RES412.code(), "Header field not found(Range). Streaming request header must have field of Range");
        }
        Matcher matcher = RANGE_PATTERN.matcher(range);
        long fileLength = file.toFile().length();
        int start = 0;
        if (matcher.matches()) {
            String startGroup = matcher.group("start");
            start = startGroup.isEmpty() ? start : Integer.valueOf(startGroup);
            start = start < 0 ? 0 : start;
        }
        int len = start + bufferSize > fileLength ? (int)fileLength - start : bufferSize;
        //MediaStreamer mediaStreamer = new MediaStreamer(info);        
        //byte[] body = mediaStreamer.getProgress(start, len);  
        byte[] body = info.getBytes1(start, len);
        int contentLength = body.length;
        long lastModified = info.getTime(TimeUnit.MILLISECONDS);
        long expire = System.currentTimeMillis() + EXPIRE_TIME;
        MIME_TYPE mimeType = info.getMimeType();

        //Fill Response
        super.logger.debug("Video streaming called: "+file.toString()+" ======================== length: "+fileLength);
        super.logger.debug("Content start: "+start+"  length: "+contentLength);        
        response.addHeader("Content-Disposition", String.format("inline;filename=\"%s\"", file.toFile().getName()));
        response.addHeader("Accept-Ranges", "bytes");
        response.addHeader("Last-Modified", lastModified);
        response.addHeader("Expires", expire);
        response.addHeader("Content-Type", mimeType.mimeType());
        response.addHeader("Content-Range", String.format("bytes %s-%s/%s", start, start + len -1, fileLength));
        response.addHeader("Content-Length", String.format("%s", contentLength));
        response.setResponseCode(206);
        response.setBody(body);        
        super.serveGet(request, response);
    }

    @Override
    public void forword(long position, int seconds) {
    }

    @Override
    public void backword(long position, int seconds) {
    }

    @Override
    public void replay() {
    }

    @Override
    public void previous() {
    }

    @Override
    public void next() {
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
