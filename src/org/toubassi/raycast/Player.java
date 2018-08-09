package org.toubassi.raycast;

/**
 * Created by gtoubassi on 8/9/18.
 */
public class Player {
    public float x;
    public float y;
    private int orientation = 45;

    // I thought this optimization might be useful but I need to be
    // able to quickly compute sin/cos of (playerOrientation + raycastangle)
    // which means I need to compute 1152 cos and sin terms (360 degrees of
    // view with 320 width screen covering 100 degrees of viewing angle = 360/100*320 = 1152)
    // Alternatively you can use sin(α + β) = sin α cos β + cos α sin β which means we'd
    // compute 360 values for orientation + 100 for ray cast and then we'd turn the
    // trig function into 2 multiplies and an add (no idea if thats better).
    // Short story as implemented this is not productive optimization

    private float[] orientationAngles = new float[360];
    private float[] sineOrientationAngles = new float[360];
    private float[] cosineOrientationAngles = new float[360];

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.orientation = 45;

        for (int i = 0; i < orientationAngles.length; i++) {
            orientationAngles[i] = (float)(i * Math.PI / 180);
            sineOrientationAngles[i] = (float)Math.sin(orientationAngles[i]);
            cosineOrientationAngles[i] = (float)Math.cos(orientationAngles[i]);
        }
    }

    public float getOrientation() {
        return orientationAngles[orientation];
    }

    public float getSineOrientation() {
        return sineOrientationAngles[orientation];
    }

    public float getCosineOrientation() {
        return cosineOrientationAngles[orientation];
    }

    public void moveForward(Map map) {
        float newX = x + getCosineOrientation();
        float newY = y + getSineOrientation();
        if (!map.isWall(newX, newY)) {
            x = newX;
            y = newY;
        }
    }

    public void moveBackward(Map map) {
        float newX = x - getCosineOrientation();
        float newY = y - getSineOrientation();
        if (!map.isWall(newX, newY)) {
            x = newX;
            y = newY;
        }
    }

    public void rotateLeft() {
        orientation += 2;
        if (orientation >= orientationAngles.length) {
            orientation = 0;
        }
    }

    public void rotateRight() {
        orientation -= 2;
        if (orientation < 0) {
            orientation = orientationAngles.length - 1;
        }
    }
}
