package net.bot.graphics;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ShaderLoader {

    public static final String KEY_BOT_SHADER = "bot";
    public static final String KEY_FOOD_SPECK_SHADER = "speck";

    private static List<ShaderSources> mSources;
    static {
        mSources = new ArrayList<>();
        mSources.add(
                new ShaderSources(
                        KEY_BOT_SHADER, 
                        resource("shaders/bot.vert"),
                        resource("shaders/bot.frag")));
        mSources.add(
                new ShaderSources(
                        KEY_FOOD_SPECK_SHADER,
                        resource("shaders/speck.vert"),
                        resource("shaders/speck.frag")));
    }
    private static Map<String, Shader> mIDMap = new HashMap<>();

    public static void loadAll() throws IOException, ShaderCompilationException {
        for (ShaderSources ss : mSources) {
            int id = loadShaders(ss.getVertexFile(), ss.getFragmentFile());
            mIDMap.put(ss.getKey(), new Shader(id));
        }
    }

    public static Shader getShader(String key) {
        if (!mIDMap.containsKey(key)) {
            return null;
        }
        return mIDMap.get(key);
    }

    public static Iterable<Shader> iterShaders() {
        return new Iterable<Shader>() {
            @Override
            public Iterator<Shader> iterator() {
                return new Iterator<Shader>() {
                    private final Iterator<Map.Entry<String, Shader>> iter =
                            mIDMap.entrySet().iterator();
                    @Override
                    public boolean hasNext() {
                        return iter.hasNext();
                    }
                    @Override
                    public Shader next() {
                        return iter.next().getValue();
                    }
                };
            }
        };
    }

    public static int loadShaders(String vertexPath, String fragmentPath)
            throws IOException, ShaderCompilationException {
        // Read the source files into String objects
        String vertexSource = readFile(vertexPath);
        String fragmentSource = readFile(fragmentPath);

        int vertexID = glCreateShader(GL_VERTEX_SHADER);
        int fragmentID = glCreateShader(GL_FRAGMENT_SHADER);

        // Compile the shaders
        compile(vertexSource, vertexID);
        compile(fragmentSource, fragmentID);

        // Compile and link the program
        int programID = glCreateProgram();
        glAttachShader(programID, vertexID);
        glAttachShader(programID, fragmentID);
        glLinkProgram(programID);

        // Check the program
        String infoLog = glGetProgramInfoLog(programID,
                glGetProgrami(programID, GL_INFO_LOG_LENGTH));

        // If some log exists, print it 
        if (infoLog != null && infoLog.trim().length() != 0) {
            System.out.println(infoLog);
        }

        // If the link failed, return
        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            return -1;
        }

        // Clean up the shaders after the link
        glDetachShader(programID, vertexID);
        glDetachShader(programID, fragmentID);
        glDeleteShader(vertexID);
        glDeleteShader(fragmentID);

        // After successful compilation, return the program ID
        return programID;
    }

    private static String readFile(String path) throws IOException {
        String line = null;
        StringBuilder source = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
            while ((line = reader.readLine()) != null) {
                source.append(line + '\n');
            }
            reader.close();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return source.toString();
    }

    private static boolean compile(String source, int id) throws ShaderCompilationException {
        glShaderSource(id, source);
        glCompileShader(id);

        // Obtain the log of the compilation
        String infoLog = glGetShaderInfoLog(id,
                glGetShaderi(id, GL_INFO_LOG_LENGTH));
        if (infoLog != null && infoLog.trim().length() != 0) {
            System.out.println(infoLog);
        }

        // If the compilation failed, throw an exception
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new ShaderCompilationException(infoLog);
        }

        return true;
    }

    private static String resource(String path) {
        return ClassLoader.getSystemClassLoader().getResource(path).getPath();
    }
}
