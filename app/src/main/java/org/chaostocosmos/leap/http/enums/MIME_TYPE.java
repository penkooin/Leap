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
    APPLICATION_OCTET_STREAM("APPLICATION_OCTET_STREAM"),    
    APPLICATION_PKCS12(Context.getMime().getMime("APPLICATION_PKCS12")),
    APPLICATION_VND_MSPOWERPOINT(Context.getMime().getMime("APPLICATION_VND_MSPOWERPOINT")),
    APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT(Context.getMime().getMime("APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT")),
    APPLICATION_X_WWW_FORM_URLENCODED(Context.getMime().getMime("APPLICATION_X_WWW_FORM_URLENCODED")),
    MULTIPART_FORM_DATA(Context.getMime().getMime("MULTIPART_FORM_DATA")),
    MULTIPART_BYTERANGES(Context.getMime().getMime("MULTIPART_BYTERANGES")),
    APPLICATION_VND_APPLE_INSTALLER_XML(Context.getMime().getMime("APPLICATION_VND_APPLE_INSTALLER_XML")),
    APPLICATION_OGG(Context.getMime().getMime("APPLICATION_OGG")),
    APPLICATION_X_SHAR(Context.getMime().getMime("APPLICATION_X_SHAR")),
    //compressed
    APPLICATION_ZIP(Context.getMime().getMime("APPLICATION_ZIP")),
    APPLICATION_X_GZIP(Context.getMime().getMime("APPLICATION_X_GZIP")),
    APPLICATION_JAVA_ARCHIVE(Context.getMime().getMime("APPLICATION_JAVA_ARCHIVE")),
    APPLICATION_X_BZIP(Context.getMime().getMime("APPLICATION_X_BZIP")),
    APPLICATION_X_BZIP2(Context.getMime().getMime("APPLICATION_X_BZIP2")),    
    APPLICATION_EPUB_ZIP(Context.getMime().getMime("APPLICATION_EPUB_ZIP")),
    APPLICATION_X_RAR_COMPRESSED(Context.getMime().getMime("APPLICATION_X_RAR_COMPRESSED")),
    APPLICATION_X_TAR(Context.getMime().getMime("APPLICATION_X_TAR")),
    APPLICATION_X_ZIP_COMPRESSED(Context.getMime().getMime("APPLICATION_X_ZIP_COMPRESSED")),
    APPLICATION_X_7Z_COMPRESSED(Context.getMime().getMime("APPLICATION_X_7Z_COMPRESSED")),
    //document
    APPLICATION_PDF(Context.getMime().getMime("APPLICATION_PDF")),
    APPLICATION_X_MSDOWNLOAD(Context.getMime().getMime("APPLICATION_X_MSDOWNLOAD")),
    APPLICATION_VND_MS_POWERPOINT(Context.getMime().getMime("APPLICATION_VND_MS_POWERPOINT")),
    APPLICATION_VND_MS_EXCEL(Context.getMime().getMime("APPLICATION_VND_MS_EXCEL")),
    APPLICATION_VND_VISIO(Context.getMime().getMime("APPLICATION_VND_VISIO")),
    APPLICATION_RTF(Context.getMime().getMime("APPLICATION_RTF")),
    APPLICATION_X_ABIWORD(Context.getMime().getMime("APPLICATION_X_ABIWORD")),
    APPLICATION_VND_AMAZON_EBOOK(Context.getMime().getMime("APPLICATION_VND_AMAZON_EBOOK")),
    APPLICATION_MSWORD(Context.getMime().getMime("APPLICATION_MSWORD")),
    APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION(Context.getMime().getMime("APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION")),
    APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET(Context.getMime().getMime("APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET")),
    APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT(Context.getMime().getMime("APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT")),
    APPLICATION_X_CSH(Context.getMime().getMime("APPLICATION_X_CSH")),
    APPLICATION_X_SH(Context.getMime().getMime("APPLICATION_X_SH")),
    APPLICATION_JSON(Context.getMime().getMime("APPLICATION_JSON")),
    APPLICATION_XML(Context.getMime().getMime("APPLICATION_XML")),
    APPLICATION_VND_MOZILLA_XUL_XML(Context.getMime().getMime("APPLICATION_VND_MOZILLA_XUL_XML")),
    APPLICATION_XHTML_XML(Context.getMime().getMime("APPLICATION_XHTML_XML")),
    APPLICATION_JAVASCRIPT(Context.getMime().getMime("APPLICATION_JAVASCRIPT")),
    //text
    TEXT_PLAIN(Context.getMime().getMime("TEXT_PLAIN")),
    TEXT_HTML(Context.getMime().getMime("TEXT_HTML")),
    TEXT_XML(Context.getMime().getMime("TEXT_XML")),
    TEXT_JSON(Context.getMime().getMime("TEXT_JSON")),
    TEXT_CSS(Context.getMime().getMime("TEXT_CSS")),
    TEXT_CSV(Context.getMime().getMime("TEXT_CSV")),
    TEXT_JAVASCRIPT(Context.getMime().getMime("TEXT_JAVASCRIPT")),
    APPLICATION_JS(Context.getMime().getMime("APPLICATION_JS")),
    TEXT_CALENDAR(Context.getMime().getMime("TEXT_CALENDAR")),

    //image
    IMAGE_X_ICON(Context.getMime().getMime("IMAGE_X_ICON")),
    IMAGE_GIF(Context.getMime().getMime("IMAGE_GIF")),
    IMAGE_PNG(Context.getMime().getMime("IMAGE_PNG")),
    IMAGE_JPEG(Context.getMime().getMime("IMAGE_JPEG")),
    IMAGE_BMP(Context.getMime().getMime("IMAGE_BMP")),
    IMAGE_WEBP(Context.getMime().getMime("IMAGE_WEBP")),
    IMAGE_SVG_XML(Context.getMime().getMime("IMAGE_SVG_XML")),
    IMAGE_TIFF(Context.getMime().getMime("IMAGE_TIFF")),
    APPLICATION_X_SHOCKWAVE_FLASH(Context.getMime().getMime("APPLICATION_X_SHOCKWAVE_FLASH")),

    //audio
    AUDIO_AAC(Context.getMime().getMime("AUDIO_AAC")),
    AUDIO_MIDI(Context.getMime().getMime("AUDIO_MIDI")),
    AUDIO_MPEG(Context.getMime().getMime("AUDIO_MPEG")),
    AUDIO_WEBM(Context.getMime().getMime("AUDIO_WEBM")),
    AUDIO_OGG(Context.getMime().getMime("AUDIO_OGG")),
    AUDIO_X_WAV(Context.getMime().getMime("AUDIO_X_WAV")),

    //video
    VIDEO_MP4(Context.getMime().getMime("VIDEO_MP4")),
    VIDEO_X_FLV(Context.getMime().getMime("VIDEO_X_FLV")),
    VIDEO_QUICKTIME(Context.getMime().getMime("VIDEO_QUICKTIME")),
    VIDEO_X_MSVIDEO(Context.getMime().getMime("VIDEO_X_MSVIDEO")),
    VIDEO_X_MS_WMV(Context.getMime().getMime("VIDEO_X_MS_WMV")),
    APPLICATION_X_MPEGURL(Context.getMime().getMime("APPLICATION_X_MPEGURL")),
    AUDIO_WAV(Context.getMime().getMime("AUDIO_WAV")),
    VIDEO_WEBM(Context.getMime().getMime("VIDEO_WEBM")),
    VIDEO_OGG(Context.getMime().getMime("VIDEO_OGG")),
    VIDEO_MPEG(Context.getMime().getMime("VIDEO_MPEG")),
    VIDEO_3GPP(Context.getMime().getMime("VIDEO_3GPP")),
    VIDEO_3GPP2(Context.getMime().getMime("VIDEO_3GPP2")),

    //etc stuff
    APPLICATION_X_FONT_WOFF(Context.getMime().getMime("APPLICATION_X_FONT_WOFF")),
    APPLICATION_X_FONT_TTF(Context.getMime().getMime("APPLICATION_X_FONT_TTF"))
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
