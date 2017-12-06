package net.bot.graphics;

public class ShaderSources {

    private String mKey;
    private String mVertexFile;
    private String mFragmentFile;

    public ShaderSources(String key, String vertexFile, String fragmentFile) {
        mKey = key;
        mVertexFile = vertexFile;
        mFragmentFile = fragmentFile;
    }

    public String getKey() {
        return mKey;
    }

    public String getVertexFile() {
        return mVertexFile;
    }

    public String getFragmentFile() {
        return mFragmentFile;
    }
}
