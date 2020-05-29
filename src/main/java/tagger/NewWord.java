package tagger;

import common.Ensure;
import common.Message;
import common.MessageType;

import java.io.IOException;
import java.io.PrintStream;

public class NewWord extends Word {

    public static int MAX_WORD_TAG_SUFFIXES = 8;
    public boolean isAnalyzed;
    public boolean isCompoundAnalyzed;
    public boolean isDerived;
    private WordTag[] suffix = new WordTag[MAX_WORD_TAG_SUFFIXES];
    private int nSuffixes;
    private boolean isAlwaysCapped;

    public NewWord() {
        isWord = true;
        next = this;
    }

    // jbfix: bit field initiated in init list to silent purify
    public NewWord(String s) {
        nSuffixes = 0;
        isAnalyzed = false;
        isCompoundAnalyzed = false;
        isDerived = false;
        isAlwaysCapped = false;

        //Ensure.ensure(s);
        init();
        set(s);
    }

    static void resetStrings() {
        stringBuf = "";
    }

    public int nSuffixes() {
        return nSuffixes;
    }

    public WordTag suffix(int n) {
        return (WordTag) suffix[n];
    }

    public boolean isCompound() {
        return nSuffixes() > 0;
    }

    public boolean isAnalyzed() {
        return isAnalyzed;
    }

    public boolean isCompoundAnalyzed() {
        return isCompoundAnalyzed;
    }

    public boolean isDerived() {
        return isDerived;
    }
    // 21 bits free

    /*std::ostream&operator<<(std::ostream&os,NewWord &w)

    {
        os << w.String();
        return os;
    }*/

    public boolean isAlwaysCapped() {
        return isAlwaysCapped;
    }

    void init() {
        super.init();
        // PN. The instruction below should be useless
        // WordTag.init(this, true);
        nSuffixes = 0;
        newWord = true;
        isForeign = true;
        isSpellOK = false;
        isAnalyzed = false;
        isCompoundAnalyzed = false;
        isDerived = false;
        isAlwaysCapped = false;
        mayBeCapped = true;
        compoundBeginOK = compoundEndOK = true;
    }

    public void reset() {
        tagIndex = (char) Tag.TAG_INDEX_NONE;
        lemma = null;
        inflectRule = (short) InflectRule.INFLECT_NO_RULE;
        tagFreq = 0;
        lexProb = 0;
        addedByTagger = false;
        verbtype = 0;
        // PN Added above to replace instr. below
        //WordTag.Reset();
        next = this;
        isAnalyzed = false;
        isCompoundAnalyzed = false;
        isDerived = false;
        nSuffixes = 0;
    }

    boolean addSuffixes(Word w) {
        for (WordTag wt = w; wt != null; wt = wt.next()) {
            if (!wt.getTag().isCompoundStop()) {
                if (nSuffixes() >= MAX_WORD_TAG_SUFFIXES) {
                    Message.invoke(MessageType.MSG_MINOR_WARNING, "too many suffixes for word", getString());
                    return false;
                }
                suffix[nSuffixes++] = wt;
            }
        }
        return true;
    }

    void computeLexProbs() {
        if (isAmbiguous()) {
            Ensure.ensure(this == Tag.getProbsWord());
            //    tagger.TagUnknownWord(this, true, false, token);
            for (WordTag wt = this; wt != null; wt = wt.next()) {
                if (wt.getTag().isContent()) {
                    wt.lexProb = (float) (wt.getTag().getLexProb());
                    Ensure.ensure(wt.lexProb > 0);
                } else
                    Message.invoke(MessageType.MSG_ERROR, "new-word", getString(), "has non-content tag",
                            wt.getTag().getString());
            }
        } else
            lexProb = 1;
    }

    void printInfo(PrintStream out) throws IOException {
        super.printInfo(out);
        out.print('*');
        if (isDerived())
            out.print('d');
        if (nSuffixes() > 0) {
            out.print(" -");
            out.print(suffix(0).String());
        }
    }

    int keyNewWord(NewWord w) {
        return w.getString().hashCode();
    }

    int compareNewWords(NewWord w1, NewWord w2) {
        return w1.getString().compareTo(w2.getString());
    }

    int compareStringAndNewWord(String s, NewWord w2) {
        return s.compareTo(w2.getString());
    }

    public String toString() {
        return String();
    }
}
