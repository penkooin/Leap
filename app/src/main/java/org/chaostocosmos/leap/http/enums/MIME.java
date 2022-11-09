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
public enum MIME {
    //binary & encoded
    APPLICATION_OCTET_STREAM("APPLICATION_OCTET_STREAM"),    
    APPLICATION_PKCS12(Context.mime().getMime("APPLICATION_PKCS12")),
    APPLICATION_VND_MSPOWERPOINT(Context.mime().getMime("APPLICATION_VND_MSPOWERPOINT")),
    APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT(Context.mime().getMime("APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT")),
    APPLICATION_X_WWW_FORM_URLENCODED(Context.mime().getMime("APPLICATION_X_WWW_FORM_URLENCODED")),
    MULTIPART_FORM_DATA(Context.mime().getMime("MULTIPART_FORM_DATA")),
    MULTIPART_BYTERANGES(Context.mime().getMime("MULTIPART_BYTERANGES")),
    APPLICATION_VND_APPLE_INSTALLER_XML(Context.mime().getMime("APPLICATION_VND_APPLE_INSTALLER_XML")),
    APPLICATION_OGG(Context.mime().getMime("APPLICATION_OGG")),
    APPLICATION_X_SHAR(Context.mime().getMime("APPLICATION_X_SHAR")),
    //compressed
    APPLICATION_ZIP(Context.mime().getMime("APPLICATION_ZIP")),
    APPLICATION_X_GZIP(Context.mime().getMime("APPLICATION_X_GZIP")),
    APPLICATION_JAVA_ARCHIVE(Context.mime().getMime("APPLICATION_JAVA_ARCHIVE")),
    APPLICATION_X_BZIP(Context.mime().getMime("APPLICATION_X_BZIP")),
    APPLICATION_X_BZIP2(Context.mime().getMime("APPLICATION_X_BZIP2")),    
    APPLICATION_EPUB_ZIP(Context.mime().getMime("APPLICATION_EPUB_ZIP")),
    APPLICATION_X_RAR_COMPRESSED(Context.mime().getMime("APPLICATION_X_RAR_COMPRESSED")),
    APPLICATION_X_TAR(Context.mime().getMime("APPLICATION_X_TAR")),
    APPLICATION_X_ZIP_COMPRESSED(Context.mime().getMime("APPLICATION_X_ZIP_COMPRESSED")),
    APPLICATION_X_7Z_COMPRESSED(Context.mime().getMime("APPLICATION_X_7Z_COMPRESSED")),
    //document
    APPLICATION_PDF(Context.mime().getMime("APPLICATION_PDF")),
    APPLICATION_X_MSDOWNLOAD(Context.mime().getMime("APPLICATION_X_MSDOWNLOAD")),
    APPLICATION_VND_MS_POWERPOINT(Context.mime().getMime("APPLICATION_VND_MS_POWERPOINT")),
    APPLICATION_VND_MS_EXCEL(Context.mime().getMime("APPLICATION_VND_MS_EXCEL")),
    APPLICATION_VND_VISIO(Context.mime().getMime("APPLICATION_VND_VISIO")),
    APPLICATION_RTF(Context.mime().getMime("APPLICATION_RTF")),
    APPLICATION_X_ABIWORD(Context.mime().getMime("APPLICATION_X_ABIWORD")),
    APPLICATION_VND_AMAZON_EBOOK(Context.mime().getMime("APPLICATION_VND_AMAZON_EBOOK")),
    APPLICATION_MSWORD(Context.mime().getMime("APPLICATION_MSWORD")),
    APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION(Context.mime().getMime("APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION")),
    APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET(Context.mime().getMime("APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET")),
    APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT(Context.mime().getMime("APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT")),
    APPLICATION_X_CSH(Context.mime().getMime("APPLICATION_X_CSH")),
    APPLICATION_X_SH(Context.mime().getMime("APPLICATION_X_SH")),
    APPLICATION_JSON(Context.mime().getMime("APPLICATION_JSON")),
    APPLICATION_XML(Context.mime().getMime("APPLICATION_XML")),
    APPLICATION_VND_MOZILLA_XUL_XML(Context.mime().getMime("APPLICATION_VND_MOZILLA_XUL_XML")),
    APPLICATION_XHTML_XML(Context.mime().getMime("APPLICATION_XHTML_XML")),
    APPLICATION_JAVASCRIPT(Context.mime().getMime("APPLICATION_JAVASCRIPT")),
    //text
    TEXT_PLAIN(Context.mime().getMime("TEXT_PLAIN")),
    TEXT_HTML(Context.mime().getMime("TEXT_HTML")),
    TEXT_XML(Context.mime().getMime("TEXT_XML")),
    TEXT_JSON(Context.mime().getMime("TEXT_JSON")),
    TEXT_CSS(Context.mime().getMime("TEXT_CSS")),
    TEXT_CSV(Context.mime().getMime("TEXT_CSV")),
    TEXT_JAVASCRIPT(Context.mime().getMime("TEXT_JAVASCRIPT")),
    APPLICATION_JS(Context.mime().getMime("APPLICATION_JS")),
    TEXT_CALENDAR(Context.mime().getMime("TEXT_CALENDAR")),

    //image
    IMAGE_X_ICON(Context.mime().getMime("IMAGE_X_ICON")),
    IMAGE_GIF(Context.mime().getMime("IMAGE_GIF")),
    IMAGE_PNG(Context.mime().getMime("IMAGE_PNG")),
    IMAGE_JPEG(Context.mime().getMime("IMAGE_JPEG")),
    IMAGE_BMP(Context.mime().getMime("IMAGE_BMP")),
    IMAGE_WEBP(Context.mime().getMime("IMAGE_WEBP")),
    IMAGE_SVG_XML(Context.mime().getMime("IMAGE_SVG_XML")),
    IMAGE_TIFF(Context.mime().getMime("IMAGE_TIFF")),
    APPLICATION_X_SHOCKWAVE_FLASH(Context.mime().getMime("APPLICATION_X_SHOCKWAVE_FLASH")),

    //audio
    AUDIO_AAC(Context.mime().getMime("AUDIO_AAC")),
    AUDIO_MIDI(Context.mime().getMime("AUDIO_MIDI")),
    AUDIO_MPEG(Context.mime().getMime("AUDIO_MPEG")),
    AUDIO_WEBM(Context.mime().getMime("AUDIO_WEBM")),
    AUDIO_OGG(Context.mime().getMime("AUDIO_OGG")),
    AUDIO_X_WAV(Context.mime().getMime("AUDIO_X_WAV")),

    //video
    VIDEO_MP4(Context.mime().getMime("VIDEO_MP4")),
    VIDEO_X_FLV(Context.mime().getMime("VIDEO_X_FLV")),
    VIDEO_QUICKTIME(Context.mime().getMime("VIDEO_QUICKTIME")),
    VIDEO_X_MSVIDEO(Context.mime().getMime("VIDEO_X_MSVIDEO")),
    VIDEO_X_MS_WMV(Context.mime().getMime("VIDEO_X_MS_WMV")),
    APPLICATION_X_MPEGURL(Context.mime().getMime("APPLICATION_X_MPEGURL")),
    AUDIO_WAV(Context.mime().getMime("AUDIO_WAV")),
    VIDEO_WEBM(Context.mime().getMime("VIDEO_WEBM")),
    VIDEO_OGG(Context.mime().getMime("VIDEO_OGG")),
    VIDEO_MPEG(Context.mime().getMime("VIDEO_MPEG")),
    VIDEO_3GPP(Context.mime().getMime("VIDEO_3GPP")),
    VIDEO_3GPP2(Context.mime().getMime("VIDEO_3GPP2")),

    //etc stuff
    APPLICATION_X_FONT_WOFF(Context.mime().getMime("APPLICATION_X_FONT_WOFF")),
    APPLICATION_X_FONT_TTF(Context.mime().getMime("APPLICATION_X_FONT_TTF"))
    ;
    /**
     * Mime type String
     */
    String mimeType;
    /**
     * Initializer
     * @param mimeType
     */
    MIME(String mimeType) {
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
    public static MIME mimeType(String mimeType) {
        
        return mimeType == null ? null : MIME.valueOf(mimeType.toUpperCase()
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
    public List<MIME> mimeTypes(String type) {
        return Arrays.asList(MIME.values()).stream().filter(m -> m.name().startsWith(type)).collect(Collectors.toList());
    }
}
