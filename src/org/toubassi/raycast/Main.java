package org.toubassi.raycast;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Main extends JPanel implements ActionListener {
    private Screen screen;
    private Map map;
    private Player player;
    boolean upPressed, downPressed, leftPressed, rightPressed;
    private float[] rayCastAngles = new float[Screen.WIDTH];
    private float[] cosineRayCastAngles = new float[Screen.WIDTH];
    private long tickCount = 0;
    private long accumulatedRenderNanoTime = 0;
    private long startFrameTime = 0;

    public Main() {
        map = new Map();
        player = new Player(map.getWidth() / 2, map.getHeight() / 2);
        computeRayCastAngles();
    }

    private void computeRayCastAngles() {
        float viewingAngle = (float)(100 /*degrees*/ * Math.PI / 180f);
        int halfScreenWidth = Screen.WIDTH / 2;

        float delta = (float) (Math.tan(viewingAngle / 2) / halfScreenWidth);

        float d = 0;
        for (int ray = 0; ray < halfScreenWidth; ray++, d += delta) {
            float angle = (float) Math.atan(d);
            rayCastAngles[halfScreenWidth - ray - 1] = angle;
            rayCastAngles[halfScreenWidth + ray] = -angle;
        }

        for (int ray = 0; ray < rayCastAngles.length; ray++) {
            cosineRayCastAngles[ray] = (float)Math.cos(rayCastAngles[ray]);
        }
    }

    public float castRayForAngle(float startX, float startY, float angle) {
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
        float x = startX;
        float y = startY;
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

    public void gameLoop() {
        if (startFrameTime == 0) {
            startFrameTime = System.currentTimeMillis();
        }
        tickCount++;

        // To get cleaner render fps lets get GC out of the way.
        // at the large expense of actual fps.
        System.gc();

        long start = System.nanoTime();

        // Perform ray casting and render to vram
        int[] vram = screen.vram;

        screen.zeroFill();

        for (int x = 0; x < Screen.WIDTH; x++) {
            float angle = rayCastAngles[x];
            float distance = cosineRayCastAngles[x] * castRayForAngle(player.x, player.y, player.getOrientation() + angle);

            int margin = Math.max(0, (int)(Screen.HEIGHT - Screen.HEIGHT / (.05*distance)) / 2);
            int gray = 0xff & (int)(127 * (Math.max(0, 150 - distance)) / 150 / 1.1);
            int pixelRGB =gray << 16 | gray << 8 | gray;

            for (int y = margin; y < Screen.HEIGHT - margin; y++) {
                vram[y * Screen.WIDTH + x] = pixelRGB;
            }
        }

        accumulatedRenderNanoTime += System.nanoTime() - start;
        final int ticksPerLog = 100;
        if (tickCount % ticksPerLog == 0) {
            long nanosPerFrame = accumulatedRenderNanoTime / ticksPerLog;
            double renderFPS = (1d/nanosPerFrame * 1e9);
            double totalFPS = 1d / ((System.currentTimeMillis() - startFrameTime) / ticksPerLog) * 1e3;
            System.out.printf("%g render fps (%dns)   %f perceived fps\n", renderFPS, nanosPerFrame, totalFPS);
            accumulatedRenderNanoTime = 0;
            startFrameTime = System.currentTimeMillis();
        }

        screen.flush();

        if (leftPressed) {
            player.rotateLeft();
        }
        if (rightPressed) {
            player.rotateRight();
        }
        if (upPressed) {
            player.moveForward(map);
        }
        if (downPressed) {
            player.moveBackward(map);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gameLoop();
    }

    public void createAndShowGUI() {
        JFrame container = new JFrame("Ray Casting");
        container.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel panel = (JPanel) container.getContentPane();
        panel.setPreferredSize(new Dimension(Screen.WIDTH, Screen.HEIGHT));
        panel.setLayout(null);

        setBounds(0, 0, Screen.WIDTH, Screen.HEIGHT);
        panel.add(this);

        setIgnoreRepaint(true);

        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        screen = new Screen(container);
        addKeyListener(new KeyInputHandler());
        this.requestFocus();
        Timer timer = new Timer(5, this);
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
