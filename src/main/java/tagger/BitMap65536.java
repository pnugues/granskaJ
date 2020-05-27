package tagger;

import common.Ensure;

import java.io.Serializable;

public class BitMap65536 implements Serializable {
    public static int[] mask = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768};
    public static short[] bits = new short[4096];

    public static int size() {
        return 65536;
    }

    static int fields() {
        return 4096;
    }

    void setBit(int b) {
        Ensure.ensure(b < size());
        bits[b >> 4] = (short) (bits(b >> 4) | mask(b & 15));
    }

    public void unsetBit(int b) {
        Ensure.ensure(b < size());
        bits[b >> 4] = (short) (bits(b >> 4) & (mask(b & 15) ^ 0xFFFF));
    }

    public boolean getBit(int b) {
        Ensure.ensure(b < size());
        return (bits(b >> 4) & mask(b & 15)) != 0;
    }

    public void clear() {
        for (int i = 0; i < fields(); i++)
            bits[i] = 0;
    }

    short bits(int n) {
        return bits[n];
    }

    int mask(int n) {
        return mask[n];
    }

}