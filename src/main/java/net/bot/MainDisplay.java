package net.bot;

import static net.bot.util.MainDisplayConstants.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.IOException;
import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import net.bot.entities.EntityBot;
import net.bot.event.handler.DisplayEventHandler;
import net.bot.event.handler.EntityEventHandler;
import net.bot.event.handler.KeyboardEventHandler;
import net.bot.event.listener.IKeyboardEventListener;
import net.bot.graphics.Shader;
import net.bot.graphics.ShaderCompilationException;
import net.bot.graphics.ShaderLoader;
import net.bot.util.Colour;
import net.bot.maths.Matrix4f;
import net.bot.maths.Vector2f;

public class MainDisplay {

    private double lastFrameTime = System.currentTimeMillis();
    private long window;
    private boolean[] arrowKeysPressed;

    public MainDisplay() {
        init();

        arrowKeysPressed = new boolean[4];

        KeyboardEventHandler.addListener(new IKeyboardEventListener() {
            @Override
            public void onKeyPressed(int key) {
                switch (key) {
                case GLFW_KEY_UP:
                    arrowKeysPressed[0] = true;
                    break;
                case GLFW_KEY_DOWN:
                    arrowKeysPressed[1] = true;
                    break;
                case GLFW_KEY_LEFT:
                    arrowKeysPressed[2] = true;
                    break;
                case GLFW_KEY_RIGHT:
                    arrowKeysPressed[3] = true;
                    break;
                case GLFW_KEY_R:
                    EntityBot redBot = new EntityBot();
                    redBot.setColour(new Colour(255, 0, 0));
                    EntityEventHandler.botCreated(redBot);
                    break;
                case GLFW_KEY_B:
                    EntityBot blueBot = new EntityBot();
                    blueBot.setColour(new Colour(0, 0, 255));
                    EntityEventHandler.botCreated(blueBot);
                    break;
                default:
                    break;
                }
            }

            @Override
            public void onKeyReleased(int key) {
                switch (key) {
                case GLFW_KEY_UP:
                    arrowKeysPressed[0] = false;
                    break;
                case GLFW_KEY_DOWN:
                    arrowKeysPressed[1] = false;
                    break;
                case GLFW_KEY_LEFT:
                    arrowKeysPressed[2] = false;
                    break;
                case GLFW_KEY_RIGHT:
                    arrowKeysPressed[3] = false;
                    break;
                }
            }
        });
    }

    public void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are
                                  // already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden
                                                  // after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be
                                                   // resizable

        // Create the window
        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, GAME_TITLE, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // Register a keyboard listener
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                KeyboardEventHandler.keyPressed(key);
            } else if (action == GLFW_RELEASE) {
                KeyboardEventHandler.keyReleased(key);
            }
        });

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Load game assets
        try {
            ShaderLoader.loadAll();
        } catch (IOException | ShaderCompilationException e) {
            e.printStackTrace();
            return;
        }

    }

    public void run() {

        // Set the clear color
        glClearColor(0.2F, 0.2F, 0.2F, 0F);
        glEnable(GL_DEPTH_TEST);

        // Create and set the projection matrix for all shaders
        Matrix4f pr_matrix = Matrix4f.orthographic(0, (float) SCREEN_WIDTH, 0, (float) SCREEN_HEIGHT, -1f, 1f);
        for (Shader shader : ShaderLoader.iterShaders()) {
            shader.setMatrix4f("pr_matrix", pr_matrix);
        }

        Vector2f viewOffset = new Vector2f();

        while (!glfwWindowShouldClose(window)) {
            updateViewOffset(arrowKeysPressed, viewOffset);
            pr_matrix = Matrix4f.orthographic(
                    viewOffset.x, (float) (viewOffset.x + SCREEN_WIDTH),
                    viewOffset.y, (float) (viewOffset.y + SCREEN_HEIGHT),
                    -1f, 1f);
            for (Shader shader : ShaderLoader.iterShaders()) {
                shader.setMatrix4f("pr_matrix", pr_matrix);
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            // Update all listeners with the latest delta
            DisplayEventHandler.update(getDelta());

            glfwSwapBuffers(window); // swap the color buffers
        }

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();

    }

    private double getDelta() {
        double delta = System.currentTimeMillis() - lastFrameTime;
        lastFrameTime = System.currentTimeMillis();
        return delta;
    }

    private void updateViewOffset(boolean[] directions, Vector2f offset) {
        // Up
        if (directions[0]) {
            offset.y += SCREEN_PAN;
            if (offset.y > BOARD_HEIGHT - SCREEN_HEIGHT) {
                offset.y = (float) (BOARD_HEIGHT - SCREEN_HEIGHT);
            }
        }
        // Down
        if (directions[1]) {
            offset.y -= SCREEN_PAN;
            if (offset.y < 0)
                offset.y = 0;
        }
        // Left
        if (directions[2]) {
            offset.x -= SCREEN_PAN;
            if (offset.x < 0)
                offset.x = 0;
        }
        // Right
        if (directions[3]) {
            offset.x += SCREEN_PAN;
            if (offset.x > BOARD_WIDTH - SCREEN_WIDTH) {
                offset.x = (float) (BOARD_WIDTH - SCREEN_WIDTH);
            }
        }
    }

}
