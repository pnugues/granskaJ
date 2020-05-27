package common;

public class Basics {
    public static boolean neg(boolean a) {
        return !a;
    }

    public static boolean neg(int a) {
        return a != 0;
    }

    public static String dont(boolean a) {
        if (a) return " don't ";
        else return " ";
    }

    public static String noOrNuff(boolean a) {
        if (a) return "";
        else return "no ";
    }

    public static char optS(boolean a) {
        if (a) return 's';
        else return '\0';
    }

}
