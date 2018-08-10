package org.toubassi.raycast;

import java.util.Arrays;

/**
 * Created by gtoubassi on 8/7/18.
 */
public class Map {
    private static final int WIDTH = 200;
    private static final int HEIGHT = 100;

    int[] data;

    public Map() {

        data = new int[WIDTH * HEIGHT];
        Arrays.fill(data, -1);
        frameWall(0, 0, WIDTH, HEIGHT);
        frameWall(25, 25, 25, 25);
        frameWall(150, 50, 25, 25);
    }

    public int isWall(float x, float y) {
        return data[((int)y) * WIDTH + ((int)x)];
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    private void frameWall(int rectX, int rectY, int width, int height) {
        int textureOffset = 0;
        for (int x = rectX; x < rectX + width; x++) {
            data[rectY * WIDTH + x] = textureOffset++;
        }
        for (int y = rectY; y < rectY + height; y++) {
            data[y * WIDTH + rectX + width - 1] = textureOffset++;
        }
        for (int x = rectX + width - 1; x >= rectX; x--) {
            data[(rectY + height - 1) * WIDTH + x] = textureOffset++;
        }
        for (int y = rectY + height - 1; y >= rectY ; y--) {
            data[y * WIDTH + rectX] = textureOffset++;
        }
    }
}
