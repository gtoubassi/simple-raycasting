package org.toubassi.raycast;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * Created by gtoubassi on 8/9/18.
 */
public class Screen {
    public static int WIDTH = 320;
    public static int HEIGHT = 200;

    private BufferStrategy strategy;
    private BufferedImage image;
    private WritableRaster raster;
    public int[] vram = new int[WIDTH * HEIGHT * 3];

    public Screen(JFrame container) {
        container.createBufferStrategy(2);
        this.strategy = container.getBufferStrategy();
        vram = new int[Screen.WIDTH*Screen.HEIGHT*3];

        image = new BufferedImage(Screen.WIDTH, Screen.HEIGHT, BufferedImage.TYPE_INT_RGB);
        raster = image.getRaster();

        raster.setPixels(0, 0, Screen.WIDTH, Screen.HEIGHT, vram);
    }

    public void zeroFill() {
        Arrays.fill(vram, 0);
    }

    public void flush() {
        raster.setPixels(0, 0, 320, 200, vram);
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        g.drawImage(image, 0, 0, null);

        // finally, we've completed drawing so clear up the graphics
        // and flip the buffer over
        g.dispose();
        strategy.show();
    }
}
