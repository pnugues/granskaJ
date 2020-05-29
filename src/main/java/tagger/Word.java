package tagger;

import common.Ensure;
import common.Message;
import common.MessageType;

import java.io.IOException;
import java.io.PrintStream;

public class Word extends WordTag {
    public static int MAX_WORD_LENGTH = 80;      // can be set arbitrarily
    public static int MAX_LEMMAS_PER_WORDTAG = 2;
    public static int MAX_INFLECTION_CHARS_ON_NORMAL_WORD = 5;
    public static int MIN_PREFIX_CHARS_ON_NORMAL_WORD = 3;

    //protected static StringBuf stringBuf;
    // replaced with a string
    public static String stringBuf;
    public char strLen;
    public String string;
    public int freq;
    public char textFreq;
    public boolean extra;
    public boolean hasExtraWordTag;
    public boolean newWord;
    public boolean compoundEndOK;
    public boolean compoundBeginOK;
    public boolean suggested;
    public boolean someFormOccursInText;
    public boolean style;
    public boolean optSpace;
    public boolean isRuleAnchor;
    public boolean isForeign;
    public boolean collocation1;
    public boolean collocation23;
    public boolean tesaurus;
    public boolean isSpellOK;
    public boolean mayBeCapped;
    String Word;

    // jbfix: bit field initiated in init list to silent purify

    Word() {
        string = null;
        freq = 0;
        textFreq = 0;
        strLen = 0;
        extra = false;
        hasExtraWordTag = false;
        newWord = false;
        compoundEndOK = true;
        compoundBeginOK = false;
        suggested = false;
        someFormOccursInText = false;
        style = false;
        optSpace = false;
        isRuleAnchor = false;
        isForeign = false;
        collocation1 = false;
        collocation23 = false;
        tesaurus = false;
        isSpellOK = true;
        mayBeCapped = false;
    }

    // TODO PN. Implemented to avoid crashes. Does not work yet.
    // Remove if not necessary
    Word(WordTag wt) {
        this.words = wt.words;
        this.lexProb = wt.lexProb;            // lexical prob, C(w, t) / C(t) right now
        this.tagFreq = wt.tagFreq;
        this.next = wt.next;
        this.lemma = wt.lemma;
        this.tagIndex = wt.tagIndex;
        this.inflectRule = wt.inflectRule;
        this.extraWordTag = wt.extraWordTag;
        this.extraLemma = wt.extraLemma;
        this.style = wt.style;
        this.nExtraLemmas = wt.nExtraLemmas;
        this.nExtraInflectRules = wt.nExtraInflectRules;
        this.isWord = wt.isWord;
        this.addedByTagger = wt.addedByTagger;
        this.verbtype = wt.verbtype;
        this.string = wt.string;
    }

    Word(String s) {
        string = null;
        freq = 0;
        textFreq = 0;
        strLen = 0;
        extra = false;
        hasExtraWordTag = false;
        newWord = false;
        compoundEndOK = true;
        compoundBeginOK = false;
        suggested = false;
        someFormOccursInText = false;
        style = false;
        optSpace = false;
        isRuleAnchor = false;
        isForeign = false;
        collocation1 = false;
        collocation23 = false;
        tesaurus = false;
        isSpellOK = true;
        mayBeCapped = false;
        if (s != null) {
            init();
            set(s);
        }
    }

    public void reset() {
        textFreq = 0;
        someFormOccursInText = false;
    }

    public String getString() {
        return string;
    }

    public int getStringLen() {
        return strLen;
    }

    public boolean isAmbiguous() {
        return next() != null;
    }

    public boolean isNewWord() {
        return newWord;
    }

    public int getFreq() {
        return freq;
    }

    public int getTextFreq() {
        return textFreq % 256; // PN. To get the same as Granska
    }

    public boolean isExtra() {
        return extra;
    }

    public boolean hasExtraWordTag() {
        return hasExtraWordTag;
    }

    public boolean hasStyleRemark() {
        return style;
    }

    public boolean isCompoundEndOK() {
        return compoundEndOK;
    }

    public boolean isCompoundBeginOK() {
        return compoundBeginOK;
    }

    public boolean isSuggested() {
        return suggested;
    }

    public boolean isOptSpace() {
        return optSpace;
    }

    public boolean isRuleAnchor() {
        return isRuleAnchor;
    }

    public void setRuleAnchor(boolean b) {
        isRuleAnchor = b;
    }

    public boolean isForeign() {
        return isForeign;
    }

    public boolean isCollocation1() {
        return collocation1;
    }

    public boolean isCollocation23() {
        return collocation23;
    }

    public boolean someFormOccursInText() {
        return someFormOccursInText;
    }

    public boolean isTesaurus() {
        return tesaurus;
    }

    public boolean isSpellOK() {
        return isSpellOK;
    }

    public boolean mayBeCapped() {
        return mayBeCapped;
    }

    void init() {
        super.init(this, true);
    }

    WordTag getWordTag(Tag t) {
        for (WordTag q = this; q != null; q = q.next())
            if (q.getTag() == t)
                return q;
        return null;
    }

    WordTag getWordTag(int index) {
        for (WordTag q = this; q != null; q = q.next())
            if (q.getTagIndex() == index)
                return q;
        return null;
    }

    // jbfix: Word leaked its string memory
    void set(String s) {
        int len = s.length();
        string = s;
        strLen = (char) len;
    }

    int compareWordPointersByStringLength(Word w1, Word w2) {
        return w1.getStringLen() - w2.getStringLen();
    }

    int compareWords(Word w1, Word w2) {
        return w1.getString().compareTo(w2.getString());
    }

    int compareStringAndWord(String s, Word w2) {
        return s.compareTo(w2.getString());
    }

    int rankWords(Word w1, Word w2) {
        return w1.getFreq() - w2.getFreq();
    }

    int keyWord(Word w) {
        return w.getString().hashCode();
    }

    int keyWordString(String s) {
        return s.hashCode();
    }//.stringBuf;  // jbfix: NewWord:: => Word

    void computeLexProbs() {
        Ensure.ensure(!hasExtraWordTag() || this == Tag.getProbsWord());
//    tagger.TagUnknownWord(this, true, false, token);
        WordTag wt;
        if (isExtra()) {
            for (wt = this; wt != null; wt = wt.next()) {
                Ensure.ensure(wt.isExtraWordTag());
                if (!wt.getTag().isContent()) {
                    wt.lexProb = (float) Settings.xEpsilonExtra * wt.getTag().getFreqInv();
                } else {
                    wt.lexProb = (float) Settings.xEpsilonExtra * wt.getTag().getLexProb() * wt.getTag().getFreqInv();
                    Ensure.ensure(wt.lexProb > 0);
                }
            }
        } else {
            Ensure.ensure(getFreq() > 0);
            for (wt = this; wt != null; wt = wt.next()) {
                if (wt.isExtraWordTag()) {
                    if (!wt.getTag().isContent()) {
                        wt.lexProb = (float) (Settings.xEpsilonExtra * Math.pow(getFreq(), -Settings.xAlphaExtra) * Settings.xLambdaExtra);
                        Ensure.ensure(wt.lexProb > 0);
                        Ensure.ensure(wt.lexProb < 10000);
                    } else {
                        wt.lexProb = (float) (wt.getTag().getLexProb() *
                                Math.pow(getFreq(), -Settings.xAlphaExtra) * Settings.xLambdaExtra);
                        Ensure.ensure(wt.lexProb > 0);
                    }
                } else {
                    if (wt.getTagFreq() <= 0) {
                        Message.invoke(MessageType.MSG_WARNING, "ComputeLexProbs, word has negative tag-freq", getString());
                    }
                    Ensure.ensure(wt.getTagFreq() > 0);
                    if (Settings.xTaggingEquation == 19) {
                        wt.lexProb = ((float) wt.getTagFreq()) * wt.getTag().getFreqInv();
                    } else if (Settings.xTaggingEquation == 20) {
                        wt.lexProb = ((float) wt.getTagFreq()) / getFreq();
                    } else if (Settings.xTaggingEquation == 21) {
                        wt.lexProb = Settings.xLambda19 * ((float) wt.getTagFreq()) / getFreq() +
                                ((float) wt.getTagFreq()) * wt.getTag().getFreqInv();
                    } else {
                        Ensure.ensure(0);
                    }
                    //	if (wt.GetTag().IsSilly())
                    //	  wt.lexProb *= 1;
                    Ensure.ensure(wt.lexProb > 0);
                }
                Ensure.ensure(wt.lexProb > 0);
            }
        }
    }

    void print(PrintStream out) throws IOException {
        out.print(getString());
        out.print(' ');
        if (Settings.xPrintWordInfo) {
            if (isNewWord()) {
                ((NewWord) this).printInfo(out);
            } else {
                printInfo(out);
            }
        }
        if (Settings.xPrintAllWordTags) {
            out.print('\n');
            out.print('\t');
        }
        if (Settings.xPrintAllWordTags)
            for (WordTag wt = next(); wt != null; wt = wt.next()) {
                out.print('\t');
                wt.print(out);
            }
    }

    void printTags(PrintStream out) throws IOException {
        //  out << std::endl << tab;
        out.print('\t');
        for (WordTag wt = this; wt != null; wt = wt.next()) {
            out.print('\t');
            wt.printTag(out);
        }
    }

    void printInfo(PrintStream out) throws IOException {
        out.print(getFreq());
        out.print(' ');
        out.print(getTextFreq());
        out.print(' ');
        if (isExtra()) out.print('E');
        if (isForeign()) out.print("f");
        if (mayBeCapped()) out.print("m");
        if (isCompoundBeginOK()) out.print('b');
        if (isCompoundEndOK()) out.print('q');
        if (isSpellOK()) out.print('s');
    }
    /*
    std::ostream& operator<<(std::ostream& os, Word &w) {
        os << w.String(); return os;
    }

    std::ostream& operator<<(std::ostream& os, Word *w) {
        if (w) os << *w; else os << "(null Word)";
        return os;
    }*/

    public String toString() {
        return String();
    }
}
