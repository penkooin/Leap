package org.chaostocosmos.leap.http.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.http.context.Context;

/**
 * Mime type enum
 * 
 * @author 9ins
 */
public enum MIME_TYPE {
    //binary & encoded
    APPLICATION_OCTET_STREAM(Context.getMime().getMime("application/octet-stream")),    
    APPLICATION_PKCS12(Context.getMime().getMime("application/pkcs12")),
    APPLICATION_VND_MSPOWERPOINT(Context.getMime().getMime("application/vnd.mspowerpoint")),
    APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT(Context.getMime().getMime("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    APPLICATION_X_WWW_FORM_URLENCODED(Context.getMime().getMime("application/x-www-form-urlencoded")),
    MULTIPART_FORM_DATA(Context.getMime().getMime("multipart/form-data")),
    MULTIPART_BYTERANGES(Context.getMime().getMime("multipart/byteranges")),
    APPLICATION_VND_APPLE_INSTALLER_XML(Context.getMime().getMime("application/vnd.apple.installer+xml")),
    APPLICATION_OGG(Context.getMime().getMime("application/ogg")),
    APPLICATION_X_SHAR(Context.getMime().getMime("application/x-shar")),
    //compressed
    APPLICATION_ZIP(Context.getMime().getMime("application/zip")),
    APPLICATION_JAVA_ARCHIVE(Context.getMime().getMime("application/java-archive")),
    APPLICATION_X_BZIP(Context.getMime().getMime("application/x-bzip")),
    APPLICATION_X_BZIP2(Context.getMime().getMime("application/x-bzip2")),    
    APPLICATION_EPUB_ZIP(Context.getMime().getMime("application/epub+zip")),
    APPLICATION_X_RAR_COMPRESSED(Context.getMime().getMime("application/x-rar-compressed")),
    APPLICATION_X_TAR(Context.getMime().getMime("application/x-tar")),
    APPLICATION_X_ZIP_COMPRESSED(Context.getMime().getMime("application/x-zip-compressed")),
    APPLICATION_X_7Z_COMPRESSED(Context.getMime().getMime("application/x-7z-compressed")),
    //document
    APPLICATION_PDF(Context.getMime().getMime("application/pdf")),
    APPLICATION_X_MSDOWNLOAD(Context.getMime().getMime("application/x-msdownload")),
    APPLICATION_VND_MS_POWERPOINT(Context.getMime().getMime("application/vnd.ms-powerpoint")),
    APPLICATION_VND_MS_EXCEL(Context.getMime().getMime("application/vnd.ms-excel")),
    APPLICATION_VND_VISIO(Context.getMime().getMime("application/vnd.visio")),
    APPLICATION_RTF(Context.getMime().getMime("application/rtf")),
    APPLICATION_X_ABIWORD(Context.getMime().getMime("application/x-abiword")),
    APPLICATION_VND_AMAZON_EBOOK(Context.getMime().getMime("application/vnd.amazon.ebook")),
    APPLICATION_MSWORD(Context.getMime().getMime("application/msword")),
    APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION(Context.getMime().getMime("application/vnd.oasis.opendocument.presentation")),
    APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET(Context.getMime().getMime("application/vnd.oasis.opendocument.spreadsheet")),
    APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT(Context.getMime().getMime("application/vnd.oasis.opendocument.text")),
    APPLICATION_X_CSH(Context.getMime().getMime("application/x-csh")),
    APPLICATION_X_SH(Context.getMime().getMime("application/x-sh")),
    APPLICATION_JSON(Context.getMime().getMime("application/json")),
    APPLICATION_XML(Context.getMime().getMime("application/xml")),
    APPLICATION_VND_MOZILLA_XUL_XML(Context.getMime().getMime("application/vnd.mozilla.xul+xml")),
    APPLICATION_XHTML_XML(Context.getMime().getMime("application/xhtml+xml")),
    APPLICATION_JAVASCRIPT(Context.getMime().getMime("application/javascript")),
    //text
    TEXT_PLAIN(Context.getMime().getMime("text/plain")),
    TEXT_HTML(Context.getMime().getMime("text/html")),
    TEXT_XML(Context.getMime().getMime("text/xml")),
    TEXT_JSON(Context.getMime().getMime("text/json")),
    TEXT_CSS(Context.getMime().getMime("text/css")),
    TEXT_CSV(Context.getMime().getMime("text/csv")),
    TEXT_JAVASCRIPT(Context.getMime().getMime("text/javascript")),
    APPLICATION_JS(Context.getMime().getMime("application/js")),
    TEXT_CALENDAR(Context.getMime().getMime("text/calendar")),

    //image
    IMAGE_X_ICON(Context.getMime().getMime("image/x-icon")),
    IMAGE_GIF(Context.getMime().getMime("image/gif")),
    IMAGE_PNG(Context.getMime().getMime("image/png")),
    IMAGE_JPEG(Context.getMime().getMime("image/jpeg")),
    IMAGE_BMP(Context.getMime().getMime("image/bmp")),
    IMAGE_WEBP(Context.getMime().getMime("image/webp")),
    IMAGE_SVG_XML(Context.getMime().getMime("image/svg+xml")),
    IMAGE_TIFF(Context.getMime().getMime("image/tiff")),
    APPLICATION_X_SHOCKWAVE_FLASH(Context.getMime().getMime("application/x-shockwave-flash")),

    //audio
    AUDIO_AAC(Context.getMime().getMime("audio/aac")),
    AUDIO_MIDI(Context.getMime().getMime("audio/midi")),
    AUDIO_MPEG(Context.getMime().getMime("audio/mpeg")),
    AUDIO_WEBM(Context.getMime().getMime("audio/webm")),
    AUDIO_OGG(Context.getMime().getMime("audio/ogg")),
    AUDIO_X_WAV(Context.getMime().getMime("audio/x-wav")),

    //video
    VIDEO_MP4(Context.getMime().getMime("video/mp4")),
    VIDEO_X_FLV(Context.getMime().getMime("video/x-flv")),
    VIDEO_QUICKTIME(Context.getMime().getMime("video/quicktime")),
    VIDEO_X_MSVIDEO(Context.getMime().getMime("video/x-msvideo")),
    VIDEO_X_MS_WMV(Context.getMime().getMime("video/x-ms-wmv")),
    APPLICATION_X_MPEGURL(Context.getMime().getMime("application/x-mpegURL")),
    AUDIO_WAV(Context.getMime().getMime("audio/wav")),
    VIDEO_WEBM(Context.getMime().getMime("video/webm")),
    VIDEO_OGG(Context.getMime().getMime("video/ogg")),
    VIDEO_MPEG(Context.getMime().getMime("video/mpeg")),
    VIDEO_3GPP(Context.getMime().getMime("video/3gpp")),
    VIDEO_3GPP2(Context.getMime().getMime("video/3gpp2")),

    //etc stuff
    APPLICATION_X_FONT_WOFF(Context.getMime().getMime("application/x-font-woff")),
    APPLICATION_X_FONT_TTF(Context.getMime().getMime("application/x-font-ttf"))
    ;
    /**
     * Mime type String
     */
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
    public String mimeType() {
        return this.mimeType;
    }
    /**
     * Get mime type from content type string
     * @param mimeType
     * @return
     */
    public static MIME_TYPE mimeType(String mimeType) {
        
        return mimeType == null ? null : MIME_TYPE.valueOf(mimeType.toUpperCase()
                                         .replace("/", "_")
                                         .replace("-", "_")
                                         .replace(".", "_")
                                         .replace("+", "_")
                                         );
    }
    /**
     * Get mime type list of specfied parameter
     * @param type
     * @return
     */
    public List<MIME_TYPE> mimeTypes(String type) {
        return Arrays.asList(MIME_TYPE.values()).stream().filter(m -> m.name().startsWith(type)).collect(Collectors.toList());
    }
}
