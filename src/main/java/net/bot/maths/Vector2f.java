package net.bot.maths;

public class Vector2f {

    public float x, y;

    // Static methods
    public static void add(Vector2f left, Vector2f right, Vector2f result) {
        result.x = left.x + right.x;
        result.y = left.y + right.y;
    }

    public static void sub(Vector2f left, Vector2f right, Vector2f result) {
        result.x = left.x - right.x;
        result.y = left.y - right.y;
    }

    public Vector2f() {
        this(0f, 0f);
    }

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float length() {
        return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public void negate() {
        x *= -1;
        y *= -1;
    }

    public Vector2f scale(float factor) {
        x *= factor;
        y *= factor;
        return this;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
