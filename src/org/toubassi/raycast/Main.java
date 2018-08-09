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
    private float[] rayCastAngles = new float[320];
    private long tickCount = 0;

    public Main() {
        image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);
        raster = image.getRaster();
        pixels = new int[320*200*3];
        raster.setPixels(0, 0, 320, 200, pixels);

        map = new Map();
        playerX = map.getWidth() / 2;
        playerY = map.getHeight() / 2;
        playerOrientation = (float)Math.PI/4;
        computeRayCastAngles();
    }

    private void computeRayCastAngles() {
        float viewingAngle = (float)(100 /*degrees*/ * Math.PI / 180f);

        if (true) {
            float delta = (float) (Math.tan(viewingAngle / 2) / 160);

            float d = 0;
            for (int ray = 0; ray < 160; ray++, d += delta) {
                float angle = (float) Math.atan(d);
                rayCastAngles[160 - ray - 1] = angle;
                rayCastAngles[160 + ray] = -angle;
            }
        }
        else {
            float deltaAngle = (viewingAngle / 2f) / 160f;
            float angle = 0;
            for (int ray = 0; ray < 160; ray++, angle += deltaAngle) {
                rayCastAngles[160 - ray - 1] = angle;
                rayCastAngles[160 + ray] = -angle;
            }
        }

        //for (int i = 1; i < rayCastAngles.length; i++) {
        //    System.out.println(i + " " + rayCastAngles[i] + " delta " + (rayCastAngles[i] - rayCastAngles[i-1]));
        //}
        //System.exit(0);
    }

    public float traceDistanceForAngle(float angle) {
        float rise = (float)Math.sin(angle);
        float run = (float)Math.cos(angle);
        float dx, dy;

        if (rise >= 0) {
            if (run >= 0) {
                if (rise > run) {
                    dy = 1;
                    dx = run / rise;
                } else {
                    dx = 1;
                    dy = rise / run;
                }
            }
            else {
                if (rise > -run) {
                    dy = 1;
                    dx = run / rise;
                } else {
                    dx = -1;
                    dy = -rise / run;
                }
            }
        }
        else {
            if (run >= 0) {
                if (-rise > run) {
                    dy = -1;
                    dx = run / -rise;
                } else {
                    dx = 1;
                    dy = rise / run;
                }
            }
            else {
                if (-rise > -run) {
                    dy = -1;
                    dx = -run / rise;
                } else {
                    dx = -1;
                    dy = -rise / run;
                }
            }
        }

        // cast the ray!
        float x = playerX;
        float y = playerY;

        int i = 0;
        for (; i < 100000; i++) {
            if (map.isWall(x, y)) {
                break;
            }
            x += dx;
            y += dy;
        }

        return (float)(i * Math.sqrt(dx*dx + dy*dy));
    }

    public void paintScene(int grayLevel) {
        for (int x = 0; x < 320; x++) {
            int margin = Math.max(0, (int)(200 - 200 / (.05*distances[x])) / 2);
            //int margin = (int)(200 * distances[x] / map.maxObservableDistance() / 2);
            int adjustedGray = (int)(grayLevel * (map.maxObservableDistance() - distances[x]) / map.maxObservableDistance() / 1.1);
            int y = 0;
            for (; y < margin; y++) {
                // set pixels to black
                pixels[3*(y * 320 + x) + 0] = 0;
                pixels[3*(y * 320 + x) + 1] = 0;
                pixels[3*(y * 320 + x) + 2] = 0;
            }
            for (; y < 200 - margin; y++) {
                // set pixels to gray
                pixels[3*(y * 320 + x) + 0] = adjustedGray;
                pixels[3*(y * 320 + x) + 1] = adjustedGray;
                pixels[3*(y * 320 + x) + 2] = adjustedGray;
            }
            for (; y < 200; y++) {
                // set pixels to black
                pixels[3*(y * 320 + x) + 0] = 0;
                pixels[3*(y * 320 + x) + 1] = 0;
                pixels[3*(y * 320 + x) + 2] = 0;
            }
        }
    }

    public void render() {
        for (int i = 0; i < 320; i++) {
            float angle = rayCastAngles[i];
            distances[i] = (float)Math.sin(Math.PI/2f - Math.abs(angle)) * traceDistanceForAngle(playerOrientation + angle);
        }

        paintScene(127);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gameLoop();
    }


    // see http://www.cokeandcode.com/info/tut2d.html
    // https://lodev.org/cgtutor/raycasting.html
    public void gameLoop() {
        // Get hold of a graphics context for the accelerated
        // surface and blank it out
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        tickCount++;
        long start = System.nanoTime();
        render();
        long duration = System.nanoTime() - start;
        raster.setPixels(0, 0, 320, 200, pixels);
        g.drawImage(image, 0, 0, null);
        if (tickCount % 100 == 0) {
            System.out.println((1d/duration * 1e9) + " fps (" + duration +"ns)");
        }

        // finally, we've completed drawing so clear up the graphics
        // and flip the buffer over
        g.dispose();
        strategy.show();

        if (leftPressed) {
            playerOrientation += 1.5/180f * Math.PI;
            playerOrientation = playerOrientation % ((float)(2*Math.PI));
        }
        if (rightPressed) {
            playerOrientation -= 1.5/180f * Math.PI;
            playerOrientation = playerOrientation % ((float)(2*Math.PI));
        }
        if (upPressed) {
            float newX = playerX + (float)Math.cos(playerOrientation);
            float newY = playerY + (float)Math.sin(playerOrientation);
            if (!map.isWall(newX, newY)) {
                playerX = newX;
                playerY = newY;
            }
        }
        if (downPressed) {
            float newX = playerX - (float)Math.cos(playerOrientation);
            float newY = playerY - (float)Math.sin(playerOrientation);
            if (!map.isWall(newX, newY)) {
                playerX = newX;
                playerY = newY;
            }
        }
        //if (leftPressed || rightPressed || upPressed || downPressed) {
        //    System.out.println(playerX + "," + playerY + "  " + playerOrientation * 180/Math.PI + "°");
        //}
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
        timer = new Timer(10, this);
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
