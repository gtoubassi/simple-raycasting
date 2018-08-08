package org.toubassi.raycast;

/**
 * Created by gtoubassi on 8/7/18.
 */
public class Map {
    byte[][] data = new byte[100][100];

    public Map() {

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                data[x][y] = (byte)(x < 3 || x > 96 || y < 3 || y > 96 ? 1 : 0);
            }
        }

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

    public int maxObservableDistance() {
        return 142;
    }
}
