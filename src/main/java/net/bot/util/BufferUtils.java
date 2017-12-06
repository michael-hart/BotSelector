package net.bot.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BufferUtils {

    // Do not allow user to instantiate this class
    private BufferUtils() {}

    public static ByteBuffer asByteBuffer(byte[] array) {
        ByteBuffer result = ByteBuffer.allocateDirect(array.length).order(ByteOrder.nativeOrder());
        result.put(array).flip();
        return result;
    }

    public static FloatBuffer asFloatBuffer(float[] array) {
        FloatBuffer result = 
                ByteBuffer.allocateDirect(array.length << 2)
                          .order(ByteOrder.nativeOrder())
                          .asFloatBuffer();
        result.put(array).flip();
        return result;
    }

    public static IntBuffer asIntBuffer(int[] array) {
        IntBuffer result = 
                ByteBuffer.allocateDirect(array.length << 2)
                          .order(ByteOrder.nativeOrder())
                          .asIntBuffer();
        result.put(array).flip();
        return result;
    }
}
