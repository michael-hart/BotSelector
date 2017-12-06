package net.bot.graphics;

import static org.lwjgl.opengl.GL20.*;

import java.util.HashMap;
import java.util.Map;

import net.bot.maths.Matrix4f;
import net.bot.maths.Vector2f;
import net.bot.maths.Vector3f;
import net.bot.maths.Vector4f;

public class Shader {

    // Attribute definitions
    public static final int VERTEX_ATTRIB = 0;
    public static final int TCOORD_ATTRIB = 1;

    private int mID = 0;
    private Map<String, Integer> locationCache = new HashMap<>();

    public Shader(int id) {
        mID = id;
    }

    public int getID() {
        return mID;
    }

    public int getUniform(String name) {
        if (locationCache.containsKey(name)) {
            return locationCache.get(name);
        }
        int result = glGetUniformLocation(mID, name);
        if (result == -1) {
            System.err.println("Could not find uniform variable " + name);
        }
        locationCache.put(name, result);
        return result;
    }

    public void setUniform1i(String name, int value) {
        enable();
        glUniform1i(getUniform(name), value);
    }

    public void setUniform1f(String name, float value) {
        enable();
        glUniform1f(getUniform(name), value);
    }

    public void setUniform2f(String name, Vector2f vector) {
        enable();
        glUniform2f(getUniform(name), vector.x, vector.y);
    }

    public void setUniform3f(String name, Vector3f vector) {
        enable();
        glUniform3f(getUniform(name), vector.x, vector.y, vector.z);
    }

    public void setUniform4f(String name, Vector4f vector) {
        enable();
        glUniform4f(getUniform(name), vector.x, vector.y, vector.z, vector.w);
    }

    public void setMatrix4f(String name, Matrix4f matrix) {
        enable();
        glUniformMatrix4fv(getUniform(name), false, matrix.toFloatBuffer());
    }

    public void enable() {
        glUseProgram(mID);
    }

    public void disable() {
        glUseProgram(0);
    }
}
