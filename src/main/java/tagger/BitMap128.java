package tagger;

import common.Ensure;

public class BitMap128 {
    static int N_128_FIELDS = 8;

    // PN. Not sure about this
    public int mask[] = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768};
    public short[] bits = new short[N_128_FIELDS];

    public static int size() {
        return N_128_FIELDS * 16;
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
        for (int i = 0; i < N_128_FIELDS; i++) {
            bits[i] = 0;
        }
    }

    short bits(int n) {
        return bits[n];
    }

    int mask(int n) {
        return mask[n];
    }
}
