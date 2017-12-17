package net.bot.entities;

import static net.bot.util.RandomUtil.rand;

import net.bot.graphics.Shader;
import net.bot.graphics.ShaderLoader;
import net.bot.graphics.VertexArray;
import net.bot.maths.Matrix4f;
import net.bot.maths.Vector2f;
import net.bot.maths.Vector3f;
import net.bot.maths.Vector4f;

public class EntityFoodSpeck extends Entity {

    private static final int MIN_FOOD_SIZE = 2;
    private static final int MAX_FOOD_SIZE = 8;
    private static final float SIZE_MULTIPLIER = 1000;

    // private static final int MIN_FRAMES_ALIVE = 60000;
    // private static final int MAX_FRAMES_ALIVE = 600000;

    // private int mFramesBeforeDeath;

    private VertexArray mVAO;
    private Shader mShader;
    private float[] mVertexBuf = {
            -1, -1, 0,
            -1,  1, 0,
             1, -1, 0,
             1,  1, 0
    };
    private byte[] mIndexBuf = {
            0, 2, 3,
            0, 1, 3
    };
    private boolean graphicsInit = false;

    public EntityFoodSpeck() {
        super();
        setSize((rand.nextInt(MAX_FOOD_SIZE - MIN_FOOD_SIZE) + MIN_FOOD_SIZE) / SIZE_MULTIPLIER);
        // mFramesBeforeDeath = rand.nextInt(MAX_FRAMES_ALIVE - MIN_FRAMES_ALIVE) +
        // MIN_FRAMES_ALIVE;
        setPosition(new Vector2f(rand.nextFloat(), rand.nextFloat()));
        setVelocity(new Vector2f(0, 0));
        setFoodLevel(getSize() * 10);
    }

    @Override
    public void update(double delta) {
        // No logic required
    }

    @Override
    public void draw() {
        if (!graphicsInit) {
            initGraphics();
        }

        // Assume that the shader has already been enabled

        // Set required uniform variables
        mShader.setMatrix4f("translate", Matrix4f.translate(getPosition()));
        mShader.setMatrix4f("scale", Matrix4f.scale(new Vector3f(getSize(), getSize(), 0)));
        mShader.setUniform4f("colour", new Vector4f(
                getColour().getRed() / 256f,
                getColour().getGreen() / 256f,
                getColour().getBlue() / 256f,
                1));
        mVAO.bind();
        mVAO.draw();
        mVAO.unbind();
    }

    @Override
    public void consume(Entity entity) {
        // Do nothing
    }

    private void initGraphics() {
        mShader = ShaderLoader.getShader(ShaderLoader.KEY_FOOD_SPECK_SHADER);
        mVAO = new VertexArray(mVertexBuf, mIndexBuf);
    }

}
