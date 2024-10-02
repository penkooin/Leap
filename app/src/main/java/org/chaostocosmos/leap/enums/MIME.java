package org.chaostocosmos.leap.enums;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.chaostocosmos.leap.common.utils.UtilBox;
import org.chaostocosmos.leap.context.Context;

/**
 * Mime type enum
 * 
 * @author 9ins
 */ 
public enum MIME {
    //binary & encoded
    APPLICATION_OCTET_STREAM("APPLICATION_OCTET_STREAM"),    
    APPLICATION_PKCS12(Context.get().mime().getMime("APPLICATION_PKCS12")),
    APPLICATION_VND_MSPOWERPOINT(Context.get().mime().getMime("APPLICATION_VND_MSPOWERPOINT")),
    APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT(Context.get().mime().getMime("APPLICATION_VND_OPENXMLFORMATS_OFFICEDOCUMENT_WORDPROCESSINGML_DOCUMENT")),
    APPLICATION_X_WWW_FORM_URLENCODED(Context.get().mime().getMime("APPLICATION_X_WWW_FORM_URLENCODED")),
    MULTIPART_FORM_DATA(Context.get().mime().getMime("MULTIPART_FORM_DATA")),
    MULTIPART_BYTERANGES(Context.get().mime().getMime("MULTIPART_BYTERANGES")),
    APPLICATION_VND_APPLE_INSTALLER_XML(Context.get().mime().getMime("APPLICATION_VND_APPLE_INSTALLER_XML")),
    APPLICATION_OGG(Context.get().mime().getMime("APPLICATION_OGG")),
    APPLICATION_X_SHAR(Context.get().mime().getMime("APPLICATION_X_SHAR")),
    //compressed
    APPLICATION_ZIP(Context.get().mime().getMime("APPLICATION_ZIP")),
    APPLICATION_X_GZIP(Context.get().mime().getMime("APPLICATION_X_GZIP")),
    APPLICATION_JAVA_ARCHIVE(Context.get().mime().getMime("APPLICATION_JAVA_ARCHIVE")),
    APPLICATION_X_BZIP(Context.get().mime().getMime("APPLICATION_X_BZIP")),
    APPLICATION_X_BZIP2(Context.get().mime().getMime("APPLICATION_X_BZIP2")),    
    APPLICATION_EPUB_ZIP(Context.get().mime().getMime("APPLICATION_EPUB_ZIP")),
    APPLICATION_X_RAR_COMPRESSED(Context.get().mime().getMime("APPLICATION_X_RAR_COMPRESSED")),
    APPLICATION_X_TAR(Context.get().mime().getMime("APPLICATION_X_TAR")),
    APPLICATION_X_ZIP_COMPRESSED(Context.get().mime().getMime("APPLICATION_X_ZIP_COMPRESSED")),
    APPLICATION_X_7Z_COMPRESSED(Context.get().mime().getMime("APPLICATION_X_7Z_COMPRESSED")),
    //document
    APPLICATION_PDF(Context.get().mime().getMime("APPLICATION_PDF")),
    APPLICATION_X_MSDOWNLOAD(Context.get().mime().getMime("APPLICATION_X_MSDOWNLOAD")),
    APPLICATION_VND_MS_POWERPOINT(Context.get().mime().getMime("APPLICATION_VND_MS_POWERPOINT")),
    APPLICATION_VND_MS_EXCEL(Context.get().mime().getMime("APPLICATION_VND_MS_EXCEL")),
    APPLICATION_VND_VISIO(Context.get().mime().getMime("APPLICATION_VND_VISIO")),
    APPLICATION_RTF(Context.get().mime().getMime("APPLICATION_RTF")),
    APPLICATION_X_ABIWORD(Context.get().mime().getMime("APPLICATION_X_ABIWORD")),
    APPLICATION_VND_AMAZON_EBOOK(Context.get().mime().getMime("APPLICATION_VND_AMAZON_EBOOK")),
    APPLICATION_MSWORD(Context.get().mime().getMime("APPLICATION_MSWORD")),
    APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION(Context.get().mime().getMime("APPLICATION_VND_OASIS_OPENDOCUMENT_PRESENTATION")),
    APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET(Context.get().mime().getMime("APPLICATION_VND_OASIS_OPENDOCUMENT_SPREADSHEET")),
    APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT(Context.get().mime().getMime("APPLICATION_VND_OASIS_OPENDOCUMENT_TEXT")),
    APPLICATION_X_CSH(Context.get().mime().getMime("APPLICATION_X_CSH")),
    APPLICATION_X_SH(Context.get().mime().getMime("APPLICATION_X_SH")),
    APPLICATION_JSON(Context.get().mime().getMime("APPLICATION_JSON")),
    APPLICATION_XML(Context.get().mime().getMime("APPLICATION_XML")),
    APPLICATION_VND_MOZILLA_XUL_XML(Context.get().mime().getMime("APPLICATION_VND_MOZILLA_XUL_XML")),
    APPLICATION_XHTML_XML(Context.get().mime().getMime("APPLICATION_XHTML_XML")),
    APPLICATION_JAVASCRIPT(Context.get().mime().getMime("APPLICATION_JAVASCRIPT")),
    //text
    TEXT_PLAIN(Context.get().mime().getMime("TEXT_PLAIN")),
    TEXT_HTML(Context.get().mime().getMime("TEXT_HTML")),
    TEXT_XML(Context.get().mime().getMime("TEXT_XML")),
    TEXT_JSON(Context.get().mime().getMime("TEXT_JSON")),
    TEXT_CSS(Context.get().mime().getMime("TEXT_CSS")),
    TEXT_CSV(Context.get().mime().getMime("TEXT_CSV")),
    TEXT_JAVASCRIPT(Context.get().mime().getMime("TEXT_JAVASCRIPT")),
    APPLICATION_JS(Context.get().mime().getMime("APPLICATION_JS")),
    TEXT_CALENDAR(Context.get().mime().getMime("TEXT_CALENDAR")),

    //image
    IMAGE_X_ICON(Context.get().mime().getMime("IMAGE_X_ICON")),
    IMAGE_GIF(Context.get().mime().getMime("IMAGE_GIF")),
    IMAGE_PNG(Context.get().mime().getMime("IMAGE_PNG")),
    IMAGE_JPEG(Context.get().mime().getMime("IMAGE_JPEG")),
    IMAGE_BMP(Context.get().mime().getMime("IMAGE_BMP")),
    IMAGE_WEBP(Context.get().mime().getMime("IMAGE_WEBP")),
    IMAGE_SVG_XML(Context.get().mime().getMime("IMAGE_SVG_XML")),
    IMAGE_TIFF(Context.get().mime().getMime("IMAGE_TIFF")),
    APPLICATION_X_SHOCKWAVE_FLASH(Context.get().mime().getMime("APPLICATION_X_SHOCKWAVE_FLASH")),
    IMAGE_VND_MICROSOFT_ICON(Context.get().mime().getMime("IMAGE_VND_MICROSOFT_ICON")),

    //audio
    AUDIO_AAC(Context.get().mime().getMime("AUDIO_AAC")),
    AUDIO_MIDI(Context.get().mime().getMime("AUDIO_MIDI")),
    AUDIO_MPEG(Context.get().mime().getMime("AUDIO_MPEG")),
    AUDIO_WEBM(Context.get().mime().getMime("AUDIO_WEBM")),
    AUDIO_OGG(Context.get().mime().getMime("AUDIO_OGG")),
    AUDIO_X_WAV(Context.get().mime().getMime("AUDIO_X_WAV")),

    //video
    VIDEO_MP4(Context.get().mime().getMime("VIDEO_MP4")),
    VIDEO_X_FLV(Context.get().mime().getMime("VIDEO_X_FLV")),
    VIDEO_QUICKTIME(Context.get().mime().getMime("VIDEO_QUICKTIME")),
    VIDEO_X_MSVIDEO(Context.get().mime().getMime("VIDEO_X_MSVIDEO")),
    VIDEO_X_MS_WMV(Context.get().mime().getMime("VIDEO_X_MS_WMV")),
    APPLICATION_X_MPEGURL(Context.get().mime().getMime("APPLICATION_X_MPEGURL")),
    AUDIO_WAV(Context.get().mime().getMime("AUDIO_WAV")),
    VIDEO_WEBM(Context.get().mime().getMime("VIDEO_WEBM")),
    VIDEO_OGG(Context.get().mime().getMime("VIDEO_OGG")),
    VIDEO_MPEG(Context.get().mime().getMime("VIDEO_MPEG")),
    VIDEO_3GPP(Context.get().mime().getMime("VIDEO_3GPP")),
    VIDEO_3GPP2(Context.get().mime().getMime("VIDEO_3GPP2")),

    //etc stuff
    APPLICATION_X_FONT_WOFF(Context.get().mime().getMime("APPLICATION_X_FONT_WOFF")),
    APPLICATION_X_FONT_TTF(Context.get().mime().getMime("APPLICATION_X_FONT_TTF"))
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
     * Get mime type object by specified resource
     * @param resourcePath
     * @return
     */
    public static MIME mimeType(Path resourcePath) {
        return mimeType(UtilBox.probeContentType(resourcePath));
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
