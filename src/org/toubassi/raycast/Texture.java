package org.toubassi.raycast;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by gtoubassi on 8/10/18.
 */
public class Texture {

    private int width, height;
    private int[] pixelRGB;

    public Texture() {
        try {
            BufferedImage texture = ImageIO.read(getClass().getResource("texture.png"));
            width = texture.getWidth();
            height = texture.getHeight();
            pixelRGB = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixelRGB[y * width + x] = texture.getRGB(x, y);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getRGB(int x, int y) {
        x = x % width;
        y = y % height;
        return pixelRGB[y * width + x];
    }
}
