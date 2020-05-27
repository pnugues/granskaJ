package tagger;

public class Letter {

    public static byte P = -4; // punctuation except sentence terminators
    public static byte E = -3; // sentence terminators
    public static byte S = -2; // space and nonprintables
    public static byte D = -1; // digits
    public static byte C = 1;  // consonants
    public static byte V = 2;  // vowels
    public static char[] lowers = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
            16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
            32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,

            64, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111,
            112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 91, 92, 93, 94, 95,
            96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111,
            112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127,

            128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143,
            144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159,
            160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175,
            176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191,

            192, 193, 194, 195, 228, 229, 198, 199, 200, 233, 202, 203, 204, 205, 206, 207,
            208, 209, 210, 211, 212, 213, 246, 215, 216, 217, 218, 219, 220, 221, 222, 223,
            224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239,
            240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255
    };
    public static char[] uppers = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
            16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
            32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,

            64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
            80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
            96, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
            80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 123, 124, 125, 126, 127,

            128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143,
            144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159,
            160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175,
            176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191,

            192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207,
            208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223,
            224, 225, 226, 227, 196, 197, 230, 231, 232, 201, 234, 235, 236, 237, 238, 239,
            240, 241, 242, 243, 244, 245, 214, 247, 248, 249, 250, 251, 252, 253, 254, 255
    };
    public static byte[] letters = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, S, S, S, S, S, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            S, E, P, P, P, P, P, P, P, P, P, P, P, P, E, P,
            D, D, D, D, D, D, D, D, D, D, P, P, P, P, P, E,

            P, V, C, C, C, V, C, C, C, V, C, C, C, C, C, V,
            C, C, C, C, C, V, C, C, C, V, C, P, P, P, P, P,
            P, V, C, C, C, V, C, C, C, V, C, C, C, C, C, V,
            C, C, C, C, C, V, C, C, C, V, C, P, P, P, P, P,

            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,

            0, 0, 0, 0, V, V, 0, 0, 0, V, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, V, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, V, V, 0, 0, 0, V, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, V, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    public static byte[] rowPos = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,

            0, 3, 4, 4, 3, 2, 3, 3, 3, 2, 3, 3, 3, 4, 4, 2,
            2, 2, 2, 3, 2, 2, 4, 2, 4, 2, 4, 0, 0, 0, 0, 0,
            0, 3, 4, 4, 3, 2, 3, 3, 3, 2, 3, 3, 3, 4, 4, 2,
            2, 2, 2, 3, 2, 2, 4, 2, 4, 2, 4, 0, 0, 0, 0, 0,

            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,

            0, 0, 0, 0, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    public static byte[] colPos = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,

            0, 1, 5, 3, 3, 3, 4, 5, 6, 8, 7, 8, 9, 7, 6, 9,
            10, 1, 4, 2, 5, 7, 4, 2, 2, 6, 1, 0, 0, 0, 0, 0,
            0, 1, 5, 3, 3, 3, 4, 5, 6, 8, 7, 8, 9, 7, 6, 9,
            10, 1, 4, 2, 5, 7, 4, 2, 2, 6, 1, 0, 0, 0, 0, 0,

            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,

            0, 0, 0, 0, 11, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 11, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    static int EXTRA_PENALTY = 5;
    static int SWAP_PENALTY = 2;
    static int DOUBLE_PENALTY = 3;
    static int WRONG_PENALTY = 4;
    static int[][] d = new int[100][100];

    static boolean isVowel(char c) {
        if (c >= 128) {
            switch (c) {
                case 'Å':
                case 'Ä':
                case 'É':
                case 'Ö':
                case 'å':
                case 'ä':
                case 'é':
                case 'ö':
                    return true;
                default:
                    return false;
            }
        }
        return letters[(char) c] == V;
    }

    static boolean isConsonant(char c) {
        if (c >= 128) {
            switch (c) {
                case 'Å':
                case 'Ä':
                case 'É':
                case 'Ö':
                case 'å':
                case 'ä':
                case 'é':
                case 'ö':
                    return false;
                default:
                    return true;
            }
        }
        return letters[(char) c] == C;
    }

    static boolean isSpace(char c) {
        return letters[(char) c] == S;
    }

    static boolean containsVowel(String string) { // true if string has at least one vowel
        for (int i = 0; i < string.length(); i++) {
            if (isVowel(string.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    static String str2HTML(String s) { // ugly stuff
        StringBuffer temp = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '<':
                    temp.append("&lt;");
                    break;
                case '>':
                    temp.append("&gt;");
                    break;
                case '&':
                    temp.append("&amp;");
                    break;
                case '"':
                    temp.append("&quot;");
                    break;
                case '\n':
                    break;
                //"&nbsp;"            fprintf(yyout, " ");
                //"&eacute;"          fprintf(yyout, "é");
                default:
                    temp.append(s.charAt(i));
            }
        }
        return temp.toString();
    }

    static boolean isPrint(char c) {
        return letters[(char) c] != 0;
    }

    static boolean isDigit(char c) {
        return letters[(char) c] == D;
    }

    static boolean IsPunct(char c) {
        return letters[(char) c] <= E;
    }

    static boolean isLetter(char c) {
        return letters[(char) c] >= C;
    }

    static boolean isUpper(char c) {
        return uppers[(char) c] == (char) c && isLetter(c);
    }

    static boolean isLower(char c) {
        return lowers[(char) c] == (char) c && isLetter(c);
    }

    static boolean isEnder(char c) {
        return letters[(char) c] == E;
    }

    static void capitalize(String s) {
        s = s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    static char lower(char c) {
        return (char) lowers[(char) c];
    }

    static char upper(char c) {
        return (char) uppers[(char) c];
    }

    static boolean isForeign(char c) {
        return c == 'Ø' || c == lower('Ø');
    } // more to do

    static boolean containsDigit(String string) { // true if string has at least one vowel
        for (int i = 0; i < string.length(); i++) {
            if (isDigit(string.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    static void toLower(String string) {
        string = string.toLowerCase();
    }

    static void toUpper(String string) {
        string = string.toUpperCase();
    }

    static int strCaseCmp(String s1, String s2) {
        return s1.toLowerCase().compareTo(s1.toLowerCase());
    }

    static String reverse(String string) {
        StringBuffer buffer = new StringBuffer(string);
        string = buffer.reverse().toString();
        return string;
    }

    static String spaceFix(String s) {
        StringBuffer temp = new StringBuffer(s);
        for (int i = 0; i < temp.length(); i++) {
            if (isSpace(temp.charAt(i)) && temp.charAt(i) != '\n') {
                temp.setCharAt(i, ' ');
            }
        }
        s = temp.toString();
        return s;
    }

    static String punctFix(String s) {
        StringBuffer temp = new StringBuffer(s);
        for (int i = 0; i < temp.length(); i++) {
            if (IsPunct(temp.charAt(i))) {
                temp.deleteCharAt(i);
            }
        }
        s = temp.toString();
        return s;
    }

    static String space2Punct(String s) {
        StringBuffer temp = new StringBuffer(s);
        int s_inx = 0;
        int t_inx = 0;
        for (; s_inx < s.length(); s_inx++, t_inx++) {
            if (isSpace(s.charAt(s_inx)) || s.charAt(s_inx) == '.') {
                temp.setCharAt(t_inx, '.');
                while (isSpace(s.charAt(s_inx + 1)) || IsPunct(s.charAt(s_inx + 1))) {
                    s_inx++;
                }
            } else {
                temp.setCharAt(t_inx, s.charAt(s_inx));
            }
        }
        if (temp.charAt(t_inx - 1) != '.') {
            temp.setCharAt(t_inx++, '.');
        }
        s = temp.toString();
        return s;
    }

    int keyboardDistance(char a, char b) {
        int r = rowPos[(char) a] - rowPos[(char) b];
        int c = colPos[(char) a] - colPos[(char) b];
        int dist = Math.abs(r) + Math.abs(c);
        if (dist == 0 && a != b)
            return 10;
        return dist;
    }

    void initEditDistance() {
        d[0][0] = 1;
        int i;
        for (i = 1; i < 100; i++)
            d[0][i] = d[0][i - 1] * EXTRA_PENALTY;
        for (i = 1; i < 100; i++)
            d[i][0] = d[i - 1][0] * EXTRA_PENALTY;
    }

    int editDistance(String string1, String string2) {
        char[] s1 = new char[100];
        char[] s2 = new char[100];
        s1[0] = 0;
        s2[0] = 0;
        int len1;
        for (len1 = 0; string1.length() < len1; len1++) {
            s1[1 + len1] = string1.charAt(len1);
        }
        int len2;
        for (len2 = 0; string2.length() < len2; len2++) {
            s2[1 + len2] = string2.charAt(len2);
        }
        for (int i = 2; i <= len1 + len2; i++)
            for (int a = Math.max(1, i - len2), b = i - a; a <= Math.min(len1, i) && b >= 1; a++, b--) {
                if (s1[a] == s2[b]) {
                    d[a][b] = d[a - 1][b - 1];
                    if (s1[a - 1] == s1[a] && s1[a - 1] != s2[b - 1])
                        d[a][b] = d[a - 2][b - 1] * DOUBLE_PENALTY;
                    else if (s2[b - 1] == s2[b] && s2[b - 1] != s1[a - 1])
                        d[a][b] = d[a - 1][b - 2] * DOUBLE_PENALTY;
                } else {
                    d[a][b] = d[a - 1][b - 1] * keyboardDistance(s1[a], s2[b]) * WRONG_PENALTY;
                    if (s1[a - 1] == s2[b] && s1[a] == s2[b - 1]) {
                        int d2 = d[a - 2][b - 2] * SWAP_PENALTY;
                        if (d2 < d[a][b])
                            d[a][b] = d2;
                    }
                }
                int d2 = d[a - 1][b] * EXTRA_PENALTY;
                if (d2 < d[a][b])
                    d[a][b] = d2;
                d2 = d[a][b - 1] * EXTRA_PENALTY;
                if (d2 < d[a][b])
                    d[a][b] = d2;
            }
        return d[len1][len2];
    }
}