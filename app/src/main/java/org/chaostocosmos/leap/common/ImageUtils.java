package org.chaostocosmos.leap.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;

/**
 * Image Utilility 
 * 
 * @author Kooin-Shin
 */
public class ImageUtils {
    
    /**
     * Save BufferedImage to File
     * @param bufferedImage
     * @param saveFile
     * @param imageFormat
     * @param params
     * @throws ImageWriteException
     * @throws IOException
     */
    public static void saveBufferedImage(BufferedImage bufferedImage, File saveFile, ImageFormat imageFormat, Map<String, Object> params) throws ImageWriteException, IOException {
        Imaging.writeImage(bufferedImage, saveFile, imageFormat, params);
    }

    /**
     * Get bytes of BufferedImage
     * @param bufferedImage
     * @param imageFormat
     * @param params
     * @return
     * @throws ImageWriteException
     * @throws IOException
     */
    public static byte[] getBufferedImageBytes(BufferedImage bufferedImage, ImageFormat imageFormat, Map<String, Object> params) throws ImageWriteException, IOException {
        return Imaging.writeImageToBytes(bufferedImage, imageFormat, params);
    }
}
