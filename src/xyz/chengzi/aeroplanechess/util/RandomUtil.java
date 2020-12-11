package xyz.chengzi.aeroplanechess.util;

import java.util.Random;

public final class RandomUtil {
    private static final Random RANDOM = new Random();

    /**
     * @param begin 1
     * @param end   6
     *
     * @return a number between 1 to 6
     */
    public static int nextInt(int begin, int end) {
        return begin + RANDOM.nextInt(end - begin + 1);
    }
}
