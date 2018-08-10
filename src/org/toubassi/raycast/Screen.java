package org.toubassi.raycast;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * Created by gtoubassi on 8/9/18.
 */
public class Screen {
    public static final int WIDTH = 320;
    public static final int HEIGHT = 200;

    private BufferStrategy strategy;
    private BufferedImage image;
    public int[] vram;
    private int contentXOffset, contentYOffset;

    public Screen(JFrame container) {
        container.createBufferStrategy(2);

        this.strategy = container.getBufferStrategy();
        vram = new int[Screen.WIDTH*Screen.HEIGHT*3];

        image = new BufferedImage(Screen.WIDTH, Screen.HEIGHT, BufferedImage.TYPE_INT_RGB);
        DataBufferInt dataBuffer = (DataBufferInt)image.getRaster().getDataBuffer();
        vram = dataBuffer.getData();
        Arrays.fill(vram, 0);

        contentXOffset = container.getWidth() - WIDTH;
        contentYOffset = container.getHeight() - HEIGHT;
    }

    public void zeroFill() {
        Arrays.fill(vram, 0);
    }

    public void flush() {
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        g.drawImage(image, contentXOffset, contentYOffset, null);

        // finally, we've completed drawing so clear up the graphics
        // and flip the buffer over
        g.dispose();
        strategy.show();
    }
}
