package net.bot.maths;

public class Vector4f {

    public float x, y, z, w;

    public Vector4f() {
        x = 0;
        y = 0;
        z = 0;
        w = 0;
    }

    public Vector4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public static Vector4f fromArray(float[] arr) {
        if (arr.length < 4) {
            return null;
        }
        return new Vector4f(arr[0], arr[1], arr[2], arr[3]);
    }

    public Vector2f concat2() {
        return new Vector2f(x, y);
    }
}
