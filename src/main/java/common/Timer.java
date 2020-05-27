package common;

public class Timer {
    static long starttime;

    static public void start() {
        starttime = System.currentTimeMillis();
    }

    static public long get() {
        return System.currentTimeMillis() - starttime;
    }

    static public long restart()    // returns time in ns since last (Re)start
    {
        long s = starttime;
        starttime = System.currentTimeMillis();
        return starttime - s;
    }

    static long clocksPerSec() {
        return 1000000;
    }

}
