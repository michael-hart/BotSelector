package net.bot.maths;

import java.nio.FloatBuffer;

import net.bot.util.BufferUtils;

public class Matrix4f {

    public float[] elements = new float[4 * 4];

    public Matrix4f() {
        
    }

    public static Matrix4f identity() {
        Matrix4f result = new Matrix4f();
        result.elements[0 + 0*4] = 1f;
        result.elements[1 + 1*4] = 1f;
        result.elements[2 + 2*4] = 1f;
        result.elements[3 + 3*4] = 1f;
        return result;
    }

    public static Matrix4f orthographic(float left, float right, float bottom,
            float top, float near, float far) {
        Matrix4f result = identity();

        result.elements[0 + 0*4] = 2.0f / (right - left);
        result.elements[1 + 1*4] = 2.0f / (top - bottom);
        result.elements[2 + 2*4] = 2.0f / (near - far);

        result.elements[0 + 3*4] = (left + right) / (left - right);
        result.elements[1 + 3*4] = (bottom + top) / (bottom - top);
        result.elements[2 + 3*4] = (far + near) / (far - near);

        return result;
    }

    public static Matrix4f translate(Vector2f vector) {
        Matrix4f result = identity();

        result.elements[0 + 3*4] = vector.x;
        result.elements[1 + 3*4] = vector.y;

        return result;
    }

    public static Matrix4f translate(Vector3f vector) {
        Matrix4f result = identity();

        result.elements[0 + 3*4] = vector.x;
        result.elements[1 + 3*4] = vector.y;
        result.elements[2 + 3*4] = vector.z;

        return result;
    }

    public static Matrix4f scale(Vector3f vector) {
        Matrix4f result = new Matrix4f();

        result.elements[0 + 0*4] = vector.x;
        result.elements[1 + 1*4] = vector.y;
        result.elements[2 + 2*4] = vector.z;
        result.elements[3 + 3*4] = 1f;

        return result;
    }

    /**
     * Return a matrix representing a 2-dimensional rotation only
     * @param angle The angle in degrees
     * @return the matrix representing the rotation
     */
    public static Matrix4f rotate(double angle) {
        Matrix4f result = identity();
        double r = Math.toRadians(angle);
        float cos = (float) Math.cos(r);
        float sin = (float) Math.sin(r);

        result.elements[0 + 0*4] = cos;
        result.elements[1 + 0*4] = -sin;
        result.elements[0 + 1*4] = sin;
        result.elements[1 + 1*4] = cos;

        return result;
    }

    /**
     * Multiply current matrix by the given matrix
     * @param matrix The matrix to multiply by
     * @return A new matrix containing the multiplication result
     */
    public Matrix4f multiply(Matrix4f matrix) {
        Matrix4f result = new Matrix4f();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                float sum = 0f;
                for (int e = 0; e < 4; e++) {
                    sum += this.elements[x + e*4] * matrix.elements[e + y*4];
                }
                result.elements[x + y*4] = sum;
            }
        }
        return result;
    }

    /**
     * Multiply current matrix by given vector
     * @return Vector4f containing result
     */
    public Vector4f multiply(Vector4f vector) {
        float[] sums = new float[4];
        for (int row = 0; row < 4; row++) {
            sums[row] = elements[0 + row*4] * vector.x +
                        elements[1 + row*4] * vector.y +
                        elements[2 + row*4] * vector.z +
                        elements[3 + row*4] * vector.w;
        }
        return Vector4f.fromArray(sums);
    }

    public FloatBuffer toFloatBuffer() {
        return BufferUtils.asFloatBuffer(elements);
    }
}
