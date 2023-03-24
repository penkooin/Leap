package org.chaostocosmos.leap.service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chaostocosmos.leap.context.Context;
import org.chaostocosmos.leap.enums.HTTP;
import org.chaostocosmos.leap.enums.MIME;
import org.chaostocosmos.leap.http.HTTPException;
import org.chaostocosmos.leap.http.Request;
import org.chaostocosmos.leap.http.Response;
import org.chaostocosmos.leap.resource.ResourcesModel;
import org.chaostocosmos.leap.service.model.StreamingModel;

/**
 * AbstractStreamingService
 * 
 * @author 9ins
 */
public abstract class AbstractStreamingService extends AbstractService implements StreamingModel {

    private final Pattern RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");
    private long EXPIRE_TIME = 1000 * 60 * 60 * 24;

    /**
     * Constructs with streaming media type & buffer size
     * @param mimeType
     * @param bufferSize
     */
    public AbstractStreamingService(MIME mimeType) {
        if(mimeType != MIME.VIDEO_MP4 
           && mimeType != MIME.VIDEO_X_FLV 
           && mimeType != MIME.VIDEO_QUICKTIME 
           && mimeType != MIME.VIDEO_X_MSVIDEO 
           && mimeType != MIME.VIDEO_X_MS_WMV 
           && mimeType != MIME.APPLICATION_X_MPEGURL 
           && mimeType != MIME.APPLICATION_OCTET_STREAM 
           && mimeType != MIME.APPLICATION_ZIP 
           ) {
           throw new HTTPException(HTTP.RES415, Context.messages().<String>error(22, "Specified media type is not supported: "+mimeType.mimeType()));
        }
    }

    public void streaming(final Request request, final Response response) throws Exception {
        String reqFile = (String) request.getParameter("file");
        reqFile = reqFile.charAt(0) == '/' ? reqFile.substring(1) : reqFile;
        if(reqFile == null || reqFile.equals("")) {
            throw new HTTPException(HTTP.RES412, "Parameter not found(file). Streaming request must have field of file.");
        }
        Path resourcePath = super.serviceManager.getHost().getStatic().resolve(reqFile);
        if(!resourcePath.toFile().exists()) {
            throw new HTTPException(HTTP.RES404, "Specified resource not found: "+resourcePath.toAbsolutePath().toString().replace("\\", "/"));
        }
        String range = request.getReqHeader().get("Range");
        if(range == null || range.equals("")) {
            throw new HTTPException(HTTP.RES412, "Header field not found(Range). Streaming request header must have field of Range");
        }
        Matcher matcher = RANGE_PATTERN.matcher(range);
        long fileLength = resourcePath.toFile().length();
        int position = 0;
        if (matcher.matches()) {
            String startGroup = matcher.group("start");
            position = startGroup.isEmpty() ? position : Integer.valueOf(startGroup);
            position = position < 0 ? 0 : position;
        }
        int bufferSize = Context.host(request.getHostId()).getStreamingBufferSize();
        int length = position + bufferSize >= fileLength ? (int)fileLength - position : bufferSize;
        byte[] body = super.resourcesModel.getResource(resourcePath).getBytes2(position, length);
        int contentLength = body.length;
        long lastModified = TimeUnit.MILLISECONDS.convert(contentLength, TimeUnit.MILLISECONDS);
        long expire = System.currentTimeMillis() + EXPIRE_TIME;
        MIME mimeType = MIME.VIDEO_MP4;
    
        //Fill Response
        super.logger.debug("Video streaming called: "+resourcePath.toString()+" ======================== length: "+fileLength);
        super.logger.debug("Content start: "+position+"  length: "+contentLength);        
        response.addHeader("Content-Disposition", String.format("inline;filename=\"%s\"", resourcePath.toFile().getName()));
        response.addHeader("Accept-Ranges", "bytes");
        response.addHeader("Last-Modified", String.valueOf(lastModified));
        response.addHeader("Expires", String.valueOf(expire));
        response.addHeader("Content-Type", mimeType.mimeType());
        response.addHeader("Content-Range", String.format("bytes %s-%s/%s", position, position + length -1, fileLength));
        response.addHeader("Content-Length", String.format("%s", contentLength));
        response.setResponseCode(206);
        response.setBody(body);
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
        ResourcesModel resource = super.getResourcesModel();
        Object res = resource.getContextResource(contextPath);
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
