package tagger;


import common.Ensure;

import java.io.IOException;
import java.io.PrintStream;

public class WordSuggestions {
    public static int MAX_ALL_SUGGESTED = 1000;
    public static int MAX_WORD_SUGGESTIONS = 100;

    Word[] words = new Word[MAX_WORD_SUGGESTIONS];
    double[] probs = new double[MAX_WORD_SUGGESTIONS];
    Word[] allSuggested = new Word[MAX_ALL_SUGGESTED];
    private int nWanted;
    private int nFound;
    private int nSuggested;
    private double worstProb;
    private int worstIndex;
    private String prefix;

    public WordSuggestions() {
        setNWanted(5);
        nSuggested = 0;
    }

    public void clear(String p) {
        prefix = p;
        nFound = 0;
        worstProb = 0;
    }

    public void clearAllSuggested() {
        for (int i = 0; i < nSuggested; i++)
            allSuggested[i].suggested = false;
        nSuggested = 0;
    }

    // void PrintHTML(std::ostream &out) const;
    public int getNFound() {
        return nFound;
    }

    public int getNWanted() {
        return nWanted;
    }

    public void setNWanted(int n) {
        nWanted = n;
    }

    public double getWorstProb() {
        return worstProb;
    }
    //public Word operator[](int n) { return words[n]; }

    void addCandidate(Word w, double prob) {
        Ensure.ensure(!w.isSuggested());
        Ensure.ensure(prob >= worstProb);
        if (nFound < nWanted) {
            words[nFound] = w;
            probs[nFound] = prob;
            nFound++;
            if (nFound == nWanted)
                findWorst();
            return;
        }
        words[worstIndex] = w;
        probs[worstIndex] = prob;
        findWorst();
    }

    void findWorst() {
        worstProb = probs[0];
        worstIndex = 0;
        for (int i = 1; i < nFound; i++)
            if (probs[i] < worstProb) {
                worstProb = probs[i];
                worstIndex = i;
            }
    }

    // PN not used and variable never defined
    /*void Sort() {
        if (!xRepeatSuggestions && nSuggested < MAX_ALL_SUGGESTED - MAX_WORD_SUGGESTIONS)
            for (int i = 0; i < nFound; i++) {
                words[i].suggested = 1;
                allSuggested[nSuggested++] = words[i];
            }
        switch (xSortMode) {
            case NONE:
                return;
            case ALPHA:
                for (int j = nFound; j > 1; j--)
                    for (int i = 1; i < j; i++)
                        if (strcmp(words[i - 1].String(), words[i].String()) > 0) {
                            Swap(words[i - 1], words[i]);
                            Swap(probs[i - 1], probs[i]);
                        }
                break;
            case LENGTH:
                for (int j = nFound; j > 1; j--)
                    for (int i = 1; i < j; i++) {
                        int cmp = words[i - 1].StringLen() - words[i].StringLen();
                        if (cmp > 0 || (cmp == 0 && strcmp(words[i - 1].String(), words[i].String()) > 0)) {
                            Swap(words[i - 1], words[i]);
                            Swap(probs[i - 1], probs[i]);
                        }
                    }
                break;
            case PROB:
                for (int j = nFound; j > 1; j--)
                    for (int i = 1; i < j; i++)
                        if (probs[i - 1] < probs[i]) {
                            Swap(words[i - 1], words[i]);
                            Swap(probs[i - 1], probs[i]);
                        }
                break;
        }
    }*/

    boolean contains(Word w) {
        for (int i = 0; i < getNFound(); i++)
            if (w == words[i])
                return true;
        return false;
    }

    void Print(PrintStream out) throws IOException {
        if (nFound == 0)
            out.print("---\n");
        else {
            int maxLen = 0;
            for (int i = 0; i < nFound; i++) {
                int len = words[i].getStringLen();
                if (len > maxLen)
                    maxLen = len;
            }
            for (int i = 0; i < nFound; i++) {
                out.print(i + 1);
                out.print(' ');
                out.print(prefix);
                out.print(words[i]);
                out.print(prefix.length());
                int spaces = maxLen - words[i].getStringLen() + 2;
                for (int j = 0; j < spaces; j++)
                    out.print(' ');
                out.print(String.valueOf(probs[i]));
                out.print('\n');
            }
        }
    }
}
/*
        inline std::ostream& operator<<(std::ostream& os, WordSuggestions &s) {
        //  if (xPrintHTML) s.PrintHTML(os); else
        s.Print(os);
        return os;
        }
        inline std::ostream& operator<<(std::ostream& os, WordSuggestions *s) {
        if (s) os << *s; else os << "(null WordSuggestions)"; return os;
        }*/