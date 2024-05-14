package me.paypur.tconjei;

public class TConJEI {
    public static final String MOD_ID = "tconjei";

    public static boolean inBox(double mX, double mY, float x, float y, float w, float h) {
        return (x <= mX && mX <= x + w && y <= mY && mY <= y + h);
    }
}
