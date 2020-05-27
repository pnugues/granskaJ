package tagger;

import common.Ensure;

import java.io.IOException;
import java.io.PrintStream;

public class StyleWord {
    public static int MAX_STYLES = 20;      // can be set arbitrarily
    public static int MAX_ALTERNATIVES = 3;
    public static int MAX_PARAGRAPH = 10;
    private static char[][] styles = new char[MAX_STYLES][MAX_PARAGRAPH];
    public int style;
    private Word word;
    private WordTag wordTag;
    private char[] paragraph = new char[MAX_PARAGRAPH];
    private char comment;
    private Word[] alt = new Word[MAX_ALTERNATIVES];
    private boolean[] owns_mem = new boolean[MAX_ALTERNATIVES];
    private int nAlts;


    StyleWord() {
        word = null;
        wordTag = null;
        style = 0;
        paragraph[0] = '\0';
        paragraph[MAX_PARAGRAPH - 1] = '\0';
        comment = 0;
        for (int i = 0; i < MAX_ALTERNATIVES; i++) {
            alt[i] = null;
            owns_mem[i] = false;
        }
        nAlts = 0;
    }

    public StyleWord(Word w) {
        word = w;
    } // just for FindSyleWord()

    public Word getWord() {
        return word;
    }

    public WordTag getWordTag() {
        return wordTag;
    }

    public char[] getParagraph() {
        return paragraph;
    }

    public char getComment() {
        return comment;
    }

    public int getNAlternatives() {
        return nAlts;
    }

    public Word getAlternative(int n) {
        Ensure.ensure(n < nAlts);
        return alt[n];
    }

    public int getStyle() {
        return style;
    }

    void print(PrintStream out) throws IOException {
        out.print(word);
        out.print(' ');
        out.print(wordTag.getTag());
        out.print(' ');
        out.print(style);
        out.print(" [");
        int mask = 1;
        for (int i = 0; i < MAX_STYLES; i++, mask <<= 1)
            if ((mask & style) != 0) {
                out.print(styles[i]);
                out.print('.');
            }
        out.print("] ");
        out.print(paragraph);
        if (alt[0] != null) {
            out.print(" (");
            out.print(alt[0]);
            for (int j = 1; j < MAX_ALTERNATIVES; j++)
                if (alt[j] != null) {
                    out.print(", ");
                    out.print(alt[j]);
                }
            out.print(')');
        }
        if (comment != 0) {
            out.print(" \"");
            out.print(comment);
            out.print("\"");
        }
    }

    boolean compareStyleWords(StyleWord w1, StyleWord w2) {
        return w1.getWordTag() == w2.getWordTag();
    }

    int keyStyleWord(StyleWord w) {
        return w.getWord().getString().hashCode();
    }
    /*
    inline std::ostream& operator<<(std::ostream& os, StyleWord &w) {
        w.Print(os);
        return os;
    }
    inline std::ostream& operator<<(std::ostream& os, StyleWord *w) {
        if (w) os << *w;
  else os << "(null StyleWord)";
        return os;
    }*/
}
