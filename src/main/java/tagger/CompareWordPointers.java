package tagger;

import java.util.Comparator;

public class CompareWordPointers implements Comparator<Word> {
    public int compare(Word w1, Word w2) {
        return w1.getString().compareTo(w2.getString());
    }

}
