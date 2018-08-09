package org.toubassi.raycast;

/**
 * Created by gtoubassi on 8/7/18.
 */
public class Map {
    byte[][] data;

    public Map() {

        data = new byte[200][100];
        fillRect(0, 0, 200, 100, 1);
        fillRect(3, 3, 94, 94, 0);
        fillRect(103, 3, 94, 94, 0);
        fillRect(91, 75, 20, 22, 0);
        fillRect(91, 3, 20, 22, 0);
    }

    public boolean isWall(float x, float y) {
        return data[(int)x][(int)y] == 1;
    }

    public int getWidth() {
        return 100;
    }

    public int getHeight() {
        return 100;
    }

    private void fillRect(int x, int y, int width, int height, int value) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                data[i][j] = (byte)value;
            }
        }
    }
}
