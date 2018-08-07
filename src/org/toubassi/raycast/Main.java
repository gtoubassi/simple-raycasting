package org.toubassi.raycast;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Main extends JPanel implements ActionListener {
    private Canvas canvas;
    private BufferStrategy strategy;
    BufferedImage image;
    WritableRaster raster;
    int[] pixels = new int[320*200*3];
    Map map;
    float playerX, playerY, playerOrientation;
    float[] distances = new float[320];
    boolean upPressed, downPressed, leftPressed, rightPressed;
    private Timer timer;

    public Main() {
        image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);
        raster = image.getRaster();
        pixels = new int[320*200*3];
        raster.setPixels(0, 0, 320, 200, pixels);

        map = new Map();
        playerX = map.getWidth() / 2;
        playerY = map.getHeight() / 2;
        playerOrientation = (float)Math.PI/4f;
    }

    public void render() {
        // assume 100degree viewing angle
        float viewingAngle = (float)(60 * Math.PI / 180f);
        float startAngle = playerOrientation - viewingAngle/2;
        float endAngle = playerOrientation + viewingAngle/2;

        int ray = 0;
        for (float angle = startAngle; angle < endAngle && ray < 320; angle += viewingAngle / 320f, ray++) {
            float rise = (float)Math.sin(angle);
            float run = (float)Math.cos(angle);
            float dx, dy;

            if (Math.abs(rise) > Math.abs(run)) {
                dy = 1;
                dx = run / rise;
            }
            else {
                dx = 1;
                dy = rise / run;
            }

            // cast the ray!
            float x = playerX;
            float y = playerY;

            int i = 0;
            for (; i < 10000; i++) {
                if (map.isWall((int)x, (int)y)) {
                    break;
                }
                x += dx;
                y += dy;
            }

            distances[ray] = (float)(i * Math.sqrt(dx*dx + dy*dy));
        }

        //for (int i = 0; i < distances.length;i++) {
        //    System.out.println(distances[i]);
        //}

        for (int x = 0; x < 320; x++) {
            int margin = (int)(200 * distances[x] / map.maxObservableDistance() / 2);
            //System.out.println(x + " " + margin);
            int y = 0;
            for (; y < margin; y++) {
                // set pixels to black
                pixels[3*(y * 320 + x) + 0] = 0;
                pixels[3*(y * 320 + x) + 1] = 0;
                pixels[3*(y * 320 + x) + 2] = 0;
            }
            for (; y < 200 - margin; y++) {
                // set pixels to gray
                pixels[3*(y * 320 + x) + 0] = 127;
                pixels[3*(y * 320 + x) + 1] = 127;
                pixels[3*(y * 320 + x) + 2] = 127;
            }
            for (; y < 200; y++) {
                // set pixels to black
                pixels[3*(y * 320 + x) + 0] = 0;
                pixels[3*(y * 320 + x) + 1] = 0;
                pixels[3*(y * 320 + x) + 2] = 0;
            }
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gameLoop();
    }


    // see http://www.cokeandcode.com/info/tut2d.html
    public void gameLoop() {
        // Get hold of a graphics context for the accelerated
        // surface and blank it out
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        if (true) {
            render();
            raster.setPixels(0, 0, 320, 200, pixels);
            g.drawImage(image, 0, 0, null);
        }
        else {
            for (int i = 0; i < pixels.length; i += 3) {
                pixels[i] = 255;
                pixels[i+1] = 0;
                pixels[i+2] = 0;
            }
            raster.setPixels(0, 0, 320, 200, pixels);
            g.drawImage(image, 0, 0, null);
        }

        // finally, we've completed drawing so clear up the graphics
        // and flip the buffer over
        g.dispose();
        strategy.show();

        if (leftPressed) {
            playerOrientation -= 1.5/180f * Math.PI;
        }
        if (rightPressed) {
            playerOrientation += 1.5/180f * Math.PI;
        }
        if (upPressed) {
            float newX = playerX + (float)Math.cos(playerOrientation);
            float newY = playerY + (float)Math.sin(playerOrientation);
            if (!map.isWall((int)newX, (int)newY)) {
                playerX = newX;
                playerY = newY;
            }
        }
        if (downPressed) {
            float newX = playerX - (float)Math.cos(playerOrientation);
            float newY = playerY - (float)Math.sin(playerOrientation);
            if (!map.isWall((int)newX, (int)newY)) {
                playerX = newX;
                playerY = newY;
            }
        }
    }

    public void createAndShowGUI() {
        JFrame container = new JFrame("Ray Casting");
        container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = (JPanel) container.getContentPane();
        panel.setPreferredSize(new Dimension(320, 200));
        panel.setLayout(null);

        setBounds(0, 0, 320, 200);
        panel.add(this);

        setIgnoreRepaint(true);

        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        container.createBufferStrategy(2);
        this.strategy = container.getBufferStrategy();
        addKeyListener(new KeyInputHandler());
        this.requestFocus();
        timer = new Timer(5, this);
        timer.start();
    }

    public static void main(String[] args) {
        final Main main = new Main();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                main.createAndShowGUI();
            }
        });
    }

    private class KeyInputHandler extends KeyAdapter {

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                upPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                downPressed = true;
            }
        }


        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                upPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                downPressed = false;
            }
        }
    }

}
