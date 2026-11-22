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

/**
 * A utility class providing static methods for common image processing operations.
 * Automatically converts loaded images into screen-compatible VRAM-managed BufferedImages for maximum Java2D hardware acceleration.
 */
public final class ImageUtils {

    private static final GraphicsConfiguration GRAPHICS_CONFIG = initGraphicsConfig();

    private ImageUtils() {}

    private static GraphicsConfiguration initGraphicsConfig() {
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                return GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice()
                        .getDefaultConfiguration();
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Creates a hardware-accelerated BufferedImage compatible with the active display configuration.
     */
    public static BufferedImage createCompatibleImage(int width, int height) {
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        if (GRAPHICS_CONFIG != null) {
            return GRAPHICS_CONFIG.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        }
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Converts an arbitrary BufferedImage (e.g. from ImageIO.read) into a VRAM-managed screen-compatible image.
     */
    public static BufferedImage toCompatibleImage(BufferedImage src) {
        if (src == null || GRAPHICS_CONFIG == null) return src;
        if (src.getColorModel().equals(GRAPHICS_CONFIG.getColorModel())) {
            return src;
        }
        BufferedImage compatible = GRAPHICS_CONFIG.createCompatibleImage(src.getWidth(), src.getHeight(), Transparency.TRANSLUCENT);
        Graphics2D g2d = compatible.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();
        return compatible;
    }

    public static BufferedImage importImage(String name, int w, int h) {
        try {
            byte[] imageBytes = AssetManager.getInstance().getAsset(name);
            if (imageBytes == null) throw new IOException("Asset not found in bundle: " + name);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) return null;
            if (w == -1 && h == -1) {
                return name.contains("/levels/rooms/") || name.contains("/minimap/") ? image : toCompatibleImage(image);
            }
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
        if (newW <= 0 || newH <= 0) {
            Logger.getInstance().notify("Attempted to resize image to zero dimensions.", Message.WARNING);
            return createCompatibleImage(1, 1);
        }
        BufferedImage newImg = createCompatibleImage(newW, newH);
        Graphics2D g2d = newImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(img, 0, 0, newW, newH, null);
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
        BufferedImage dest = createCompatibleImage(w, h);
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
        int newWidth = Math.max(1, (int) Math.floor(w * cos + h * sin));
        int newHeight = Math.max(1, (int) Math.floor(h * cos + w * sin));

        BufferedImage rotated = createCompatibleImage(newWidth, newHeight);
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