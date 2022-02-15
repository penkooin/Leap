package org.chaostocosmos.leap.http.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mime type enum
 * 
 * @author 9ins
 */
public enum MIME_TYPE {
    APPLICATION_X_ZIP("application/x-zip-compressed"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    APPLICATION_PKCS12("application/pkcs12"),
    APPLICATION_VND_MSPOWERPOINT("application/vnd.mspowerpoint"),
    APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    APPLICATION_X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded"),
    APPLICATION_XHTML_XML("application/xhtml+xml"),
    APPLICATION_XML("application/xml"),
    APPLICATION_JSON("application/json"),
    APPLICATION_JAVASCRIPT("application/javascript"),
    APPLICATION_PDF("application/pdf"),
    APPLICATION_JAVA_ARCHIVE("application/java-archive"),
    APPLICATION_ZIP("application/zip"),
    MULTIPART_FORM_DATA("multipart/form-data"),
    MULTIPART_BYTERANGES("multipart/byteranges"),
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    TEXT_XML("text/xml"),
    TEXT_JSON("text/json"),
    TEXT_CSS("text/css"),
    TEXT_JAVASCRIPT("text/javascript"),

    IMAGE_GIF("image/gif"),
    IMAGE_PNG("image/png"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_BMP("image/bmp"),
    IMAGE_WEBP("image/webp"),
    AUDIO_MIDI("audio/midi"),
    AUDIO_MPEG("audio/mpeg"),
    AUDIO_WEBM("audio/webm"),
    AUDIO_OGG("audio/ogg"),
    AUDIO_WAV("audio/wav"),
    VIDEO_WEBM("video/webm"),
    VIDEO_OGG("video/ogg"),
    ;

    String mimeType;

    /**
     * Initializer
     * @param mimeType
     */
    MIME_TYPE(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Get mime type
     * @return
     */
    public String getMimeType() {
        return this.mimeType;
    }

    /**
     * Get mime type from content type string
     * @param mimeType
     * @return
     */
    public static MIME_TYPE getMimeType(String mimeType) {
        return MIME_TYPE.valueOf(mimeType.toUpperCase().replace("/", "_").replace("-", "_").replace(".", "_"));
    }

    /**
     * Get mime type list of specfied parameter
     * @param type
     * @return
     */
    public List<MIME_TYPE> getMimeTypes(String type) {
        return Arrays.asList(MIME_TYPE.values()).stream().filter(m -> m.name().startsWith(type)).collect(Collectors.toList());
    }
}
