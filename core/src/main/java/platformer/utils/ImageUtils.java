package platformer.utils;

import platformer.animation.AssetManager;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * A utility class providing static methods for common image processing operations.
 * This class handles tasks such as loading, resizing, flipping, and rotating images.
 */
public final class ImageUtils {

    private ImageUtils() {}

    /**
     * This method is used to import an image from a given path and resize it to the specified width and height.
     * If the width and height are both -1, the original size of the image is preserved.
     *
     * @param name The path of the image to be imported.
     * @param w The desired width of the image.
     * @param h The desired height of the image.
     * @return The imported image, resized to the specified dimensions. If an error occurs during import, null is returned.
     */
    public static BufferedImage importImage(String name, int w, int h) {
        try {
            byte[] imageBytes = AssetManager.getInstance().getAsset(name);
            if (imageBytes == null) throw new IOException("Asset not found in bundle: " + name);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (w == -1 && h == -1) return image;
            return resizeImage(image, w, h);
        } catch (Exception e) {
            if (name.contains("/levels/level")) return null;
            Logger.getInstance().notify("Importing image failed. Name: " + name, Message.ERROR);
        }
        return null;
    }

    /**
     * This method is used to resize an image to the specified width and height.
     *
     * @param img The image to be resized.
     * @param newW The desired width of the image.
     * @param newH The desired height of the image.
     * @return The resized image.
     */
    public static BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
        if (newW == 0 || newH == 0) {
            Logger.getInstance().notify("Attempted to resize image to zero dimensions.", Message.WARNING);
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage newImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return newImg;
    }

    /**
     * This method is used to flip an image horizontally.
     *
     * @param src The image to be flipped.
     * @return The flipped image.
     */
    public static BufferedImage flipImage(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, src.getType());
        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.drawImage(src, w, 0, -w, h, null);
        graphics2D.dispose();
        return dest;
    }

    /**
     * Converts a given BufferedImage to a 2D array of grayscale values.
     * Each element in the array represents the grayscale value of the corresponding pixel in the image.
     * The grayscale value is calculated as the average of the red, green, and blue components of the pixel.
     *
     * @param image The BufferedImage to be converted to grayscale.
     * @return A 2D array of integers representing the grayscale values of the image.
     */
    public static int[][] toGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] grayscale = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                grayscale[y][x] = (r + g + b) / 3;
            }
        }
        return grayscale;
    }

    /**
     * Rotates a given BufferedImage by a specified angle.
     * The rotation is performed around the center of the image.
     *
     * @param image The BufferedImage to be rotated.
     * @param angle The angle in degrees by which to rotate the image.
     * @return A new BufferedImage that is the rotated version of the original image.
     */
    public static BufferedImage rotateImage(BufferedImage image, double angle) {
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = image.getWidth();
        int h = image.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2.0, (newHeight - h) / 2.0);
        at.rotate(rads, w / 2.0, h / 2.0);
        g2d.setTransform(at);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rotated;
    }
}