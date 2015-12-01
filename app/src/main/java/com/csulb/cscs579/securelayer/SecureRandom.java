package com.csulb.cscs579.securelayer;

/**
 * Created by JAY on 11/21/15.
 */
public class SecureRandom extends java.security.SecureRandom {

    @Override
    public int nextInt() {
        int i = 0;
        while (i <= 0) {
            i = super.nextInt();
        }
        return i;
    }

    @Override
    public int nextInt(int bound) {
        int i = 0;
        if (bound > 0) {
            while (i <= 0) {
                i = super.nextInt(bound);
            }
        } else {
            i = super.nextInt(bound);
        }
        return i;
    }

    @Override
    public long nextLong() {
        long l = 0;
        while (l <= 0) {
            l = super.nextLong();
        }
        return l;
    }
}
