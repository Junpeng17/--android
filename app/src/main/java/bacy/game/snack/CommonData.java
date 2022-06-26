package bacy.game.snack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CommonData {
    public static int screenWidth, screenHeight;
    public static int moveDirection;
    public static int snackLength;
    public static int bodySize;
    public static long gameTime;

    public CommonData() {
    }

    public void finalize() {

    }
    public int makeRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max)%(max-min+1) + min;
    }
}
