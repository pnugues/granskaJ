package tagger;

import common.Ensure;
import common.Message;
import common.MessageType;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringWriter;

public class WordTag implements Serializable {

    protected static WordLexicon words;
    public String string; // TODO PN. Added to be able to get the string
    protected float lexProb;            // lexical prob, C(w, t) / C(t) right now
    protected int tagFreq;
    protected WordTag next;
    protected WordTag lemma;
    protected int tagIndex;
    protected short inflectRule;
    protected boolean extraWordTag;
    protected boolean extraLemma;
    protected boolean style;
    protected byte nExtraLemmas;
    protected byte nExtraInflectRules;
    protected boolean isWord;
    protected boolean addedByTagger;
    protected byte verbtype;

    // jbfix: bit field initiated in init list to silent purify
    WordTag() {
        lexProb = 0;
        tagFreq = 0;
        next = null;
        lemma = null;
        tagIndex = Tag.TAG_INDEX_NONE;
        inflectRule = (short) InflectRule.INFLECT_NO_RULE;
        extraWordTag = false;
        extraLemma = false;
        style = false;
        nExtraLemmas = 0;
        nExtraInflectRules = 0;
        isWord = false;
        addedByTagger = false;
        verbtype = 0;
    }

    public float Cwt_Ct() {
        return (float) getTagFreq() * getTag().getFreqInv();
    }

    public WordTag next() {
        if (next == null) return null;
        return (next.isWord() ? null : next);
    }

    public Tag getTag() {
        return Tag.tags[tagIndex];
    }

    public float getLexProb() {
        return lexProb;
    }

    public int getTagIndex() {
        return tagIndex;
    }

    public boolean hasStyleRemark() {
        return style;
    }

    public int nLemmas() {
        return lemma != null ? nExtraLemmas + 1 : 0;
    }

    public boolean isLemma() {
        return lemma == this;
    }

    public boolean isExtraLemma() {
        return extraLemma;
    }

    public boolean isExtraWordTag() {
        return extraWordTag;
    }

    public boolean isWord() {
        return isWord;
    }

    public boolean isAddedByTagger() {
        return addedByTagger;
    }

    public int getVerbType() {
        return verbtype;
    }

    public int getTagFreq() {
        return tagFreq;
    }

    public int nInflectRules() {
        return (inflectRule == InflectRule.INFLECT_NO_RULE) ? 0 : nExtraInflectRules + 1;
    }

    String String() {
        return getWord().getString();
    }

    int stemLen() {
        if (nInflectRules() == 0) return -1;
        return lemma(0).getWord().getStringLen() - words.getInflects().rule(inflectRule).getNCharsToRemove();
    }

    WordTag lemma(int n) {
        if (n == 0)
            return lemma;
        return words.getExtraLemma(this);
    }

    void guessInflectRule() {
        Ensure.ensure(inflectRule == InflectRule.INFLECT_NO_RULE);
        words.guessWordTagRule(this);
    }

    int inflectRule(int n) {
        if (n == 0)
            return inflectRule;
        return words.extraInflectRule(this, n);
    }

    WordTag getForm(Tag t, int lemmaNumber, int ruleNumber) {
        if (lemmaNumber == 0 && t == getTag())
            return (WordTag) this;
        Ensure.ensure(lemmaNumber < nLemmas());
        WordTag lemma = lemma(lemmaNumber);
        //Ensure.ensure(lemma);
        Ensure.ensure(ruleNumber < lemma.nInflectRules());
        if (t.getIndex() == lemma.getTagIndex())
            return (WordTag) lemma;
        int ruleIndex = lemma.inflectRule(ruleNumber);
        WordTag wt2 = words.getInflectedForm(lemma.getWord(), ruleIndex, getTag());
        if (wt2 == this || (wt2 != null && wt2.getWord().isNewWord()))
            return words.getInflectedForm(lemma.getWord(), ruleIndex, t);
        if (wt2 != null)
            Message.invoke(MessageType.MSG_MINOR_WARNING, wt2.String(), " does not seem to be the correct form");
        if (wt2 == null && getTag().originalTag() != getTag()) {
            wt2 = words.getInflectedForm(lemma.getWord(), ruleIndex, getTag().originalTag());
            if (wt2 != null)
                return words.getInflectedForm(lemma.getWord(), ruleIndex, t);
        }
        return null;
    }

//std::ostream& operator<<(std::ostream&, Tag*);

    void printInfo(PrintStream out) throws IOException {

        if (isExtraLemma() || isAddedByTagger() || isExtraWordTag())
            out.print(" -");
        if (isExtraLemma())
            out.print('x');
        if (isAddedByTagger())
            out.print('t');
        if (isExtraWordTag())
            out.print('e');
    }

    void printTag(StringWriter out) {
        out.write(getTag().toString());
    }

    void printTag(PrintStream out) throws IOException {
        out.print(getTag());
    }

    void print(PrintStream out) throws IOException {
        out.print('<');
        out.print(getTag());
        out.print("> ");
        out.print(getTagFreq());
        out.print(' ');
        if (Settings.xPrintWordInfo) {
            for (int i = 0; i < nInflectRules(); i++) {
                out.print(words.getInflects().rule(inflectRule(i)).toString());
                out.print(' ');
            }
            printInfo(out);
        }
        if (Settings.xPrintLemma) {
            out.print('(');
            for (int i = 0; i < nLemmas(); i++) {
                if (i > 0) {
                    out.print(' ');
                }
                out.print(lemma(i).toString());
            }
            out.print(')');
        }
        out.println();
    }


    void init(WordTag n, boolean isW) {
        next = n;
        isWord = isW;
    }

    void reset() {
        tagIndex = (char) Tag.TAG_INDEX_NONE;
        lemma = null;
        inflectRule = (short) InflectRule.INFLECT_NO_RULE;
        tagFreq = 0;
        lexProb = 0;
        addedByTagger = false;
        verbtype = 0;
    }

    Word getWord() {
        WordTag wt = this;
        while (!wt.isWord()) {
            if (wt.next == null)
                break;
            wt = wt.next;
            if (wt == this)
                Message.invoke(MessageType.MSG_ERROR, "infinite loop in WordTag::GetWord()");
        }
        return (Word) wt;
    }

    public String toString() {
        return getWord() + " " + getTag();
    }
}

/*

    inline std::ostream& operator<<(std::ostream& os, WordTag &wt) {
        os << wt.GetWord() << ' ' << wt.GetTag(); return os;
    }

    inline std::ostream& operator<<(std::ostream& os, WordTag *wt) {
        if (wt) os << *wt; else os << "(null WordTag)";
        return os;
    }

 */