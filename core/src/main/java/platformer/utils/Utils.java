package platformer.utils;

import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Utils {

    private static volatile Utils instance = null;

    private Utils() {}

    public static Utils getInstance() {
        if (instance == null) {
            synchronized (Utils.class) {
                if (instance == null) {
                    instance = new Utils();
                }
            }
        }
        return instance;
    }

    /**
     * This method is used to import an image from a given path and resize it to the specified width and height.
     * If the width and height are both -1, the original size of the image is preserved.
     *
     * @param name The path of the image to be imported.
     * @param w The desired width of the image.
     * @param h The desired height of the image.
     * @return The imported image, resized to the specified dimensions. If an error occurs during import, null is returned.
     */
    public BufferedImage importImage(String name, int w, int h) {
        try {
            BufferedImage image = ImageIO.read(Objects.requireNonNull(getClass().getResource(name)));
            if (w == -1 && h == -1) return image;
            return resizeImage(image, w, h);

        } catch (Exception e) {
            if (name.contains("/levels/level")) return null;
            Logger.getInstance().notify("Importing image failed. "+"Name: " + name + " (w, h) = (" + w + ", " + h + ")", Message.ERROR);
        }
        return null;
    }

    /**
     * This method is used to flip an image horizontally.
     *
     * @param src The image to be flipped.
     * @return The flipped image.
     */
    public BufferedImage flipImage(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, src.getType());
        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.drawImage(src, w, 0, -w, h, null);
        graphics2D.dispose();
        return dest;
    }

    /**
     * This method is used to resize an image to the specified width and height.
     *
     * @param img The image to be resized.
     * @param newW The desired width of the image.
     * @param newH The desired height of the image.
     * @return The resized image.
     */
    public BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage newImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return newImg;
    }

    /**
     * Converts a given BufferedImage to a 2D array of grayscale values.
     * Each element in the array represents the grayscale value of the corresponding pixel in the image.
     * The grayscale value is calculated as the average of the red, green, and blue components of the pixel.
     *
     * @param image The BufferedImage to be converted to grayscale.
     * @return A 2D array of integers representing the grayscale values of the image.
     */
    public int[][] toGrayscale(BufferedImage image) {
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
    public BufferedImage rotateImage(BufferedImage image, double angle) {
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

    public <T> T[] reverseArray(T[] arr) {
        int start = 0;
        int end = arr.length - 1;
        while (start < end) {
            T temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
        return arr;
    }

    /**
     * This method retrieves all items from a map where each value is a list of items.
     * It iterates over the values of the map, which are lists, and adds all items from these lists to a new list.
     * The new list, containing all items from the lists in the map, is then returned.
     *
     * @param itemMap The map containing lists of items as values.
     * @return A list containing all items from the lists in the map.
     */
    public <T> List<T> getAllItems(Map<?, List<T>> itemMap) {
        List<T> allItems = new ArrayList<>();
        itemMap.values().forEach(allItems::addAll);
        return allItems;
    }

}
