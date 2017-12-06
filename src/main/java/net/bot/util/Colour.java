package net.bot.util;

public class Colour {

    private int mRed, mGreen, mBlue;

    public Colour(int r, int g, int b) {
        mRed = r;
        mGreen = g;
        mBlue = b;
    }

    public int getRed() {
        return mRed;
    }

    public int getGreen() {
        return mGreen;
    }

    public int getBlue() {
        return mBlue;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Colour)) {
            return false;
        }
        Colour c = (Colour) obj;
        return c.getRed() == mRed & c.getGreen() == mGreen && c.getBlue() == mBlue;
    }

}
