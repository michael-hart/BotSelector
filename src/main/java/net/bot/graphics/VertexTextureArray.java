package net.bot.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import net.bot.util.BufferUtils;

public class VertexTextureArray extends VertexArray {

    // Texture buffer object
    private int tbo;

    public VertexTextureArray(float[] vertices, byte[] indices, float[] textureCoordinates) {
        super(vertices, indices);

        // Create and bind texture
        tbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, tbo);
        glBufferData(GL_ARRAY_BUFFER, BufferUtils.asFloatBuffer(textureCoordinates), GL_STATIC_DRAW);
        glVertexAttribPointer(Shader.TCOORD_ATTRIB, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(Shader.TCOORD_ATTRIB);

        // Unbind to clean up
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

}
