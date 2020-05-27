package common;

public class Ensure {
    public static void ensure(boolean fact) {
        assert (fact);
    }

    public static void ensure(int fact) {
        assert (fact != 0);
    }

    /*static void ensure(Word fact) {
        assert (fact != null);
    }*/
}
