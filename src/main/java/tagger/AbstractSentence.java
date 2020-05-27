package tagger;

import common.Ensure;
import common.Message;
import common.MessageType;

import java.io.IOException;
import java.io.PrintStream;

public class AbstractSentence {
    public static int MAX_SENTENCE_LENGTH = 150;      // can be set arbitrarily
    static Tagger tagger = null;
    String originalText; //Oscar
    short nWords;                   // including punctuation marks
    short nTokens;                  // including 4 sentence-delimiters
    // jonas, both actually count the same thing
    WordToken[] tokens;
    double prob;
    boolean endsParagraph;
    boolean containsStyleWord;
    boolean containsRepeatedWords;
    boolean containsNewWord;
    boolean isHeading;
    boolean seemsForeign;
    boolean bitsSet;
    char nGramErrors;

    public AbstractSentence() {
        nWords = 0;
        nTokens = 0;
        tokens = null;
        prob = 0;
        endsParagraph = false;
        containsStyleWord = false;
        containsRepeatedWords = false;
        containsNewWord = false;
        isHeading = false;
        seemsForeign = false;
        bitsSet = false;
        nGramErrors = 0;
    }

    int getNWords() {
        return nWords;
    }

    int getNTokens() {
        return nTokens;
    }

    Word getWord(int n) {
        return tokens[n].getWord();
    }

    WordToken getWordToken(int n) {
        return tokens[n];
    }

    WordToken getWordTokensAt(int n) {
        return tokens[n];
    }

    int textPos() {
        return tokens[2].getOffset();
    }

    double getProb() {
        return prob;
    }

    boolean containsNewWord() {
        Ensure.ensure(bitsSet);
        return containsNewWord;
    }

    boolean containsStyleWord() {
        Ensure.ensure(bitsSet);
        return containsStyleWord;
    }

    boolean containsRepeatedWords() {
        Ensure.ensure(bitsSet);
        return containsRepeatedWords;
    }

    boolean seemsForeign() {
        Ensure.ensure(bitsSet);
        return seemsForeign;
    }

    boolean isHeading() {
        return isHeading;
    }

    boolean getEndsParagraph() {
        return endsParagraph;
    }

    int getNGramErrors() {
        return nGramErrors;
    }

    String getOriginalText() {
        return originalText;
    }

    void setOriginalText(String org) {
        //if(originalText)
        //    delete originalText;
        originalText = org;
    }

    boolean isEqual(AbstractSentence s) {
        if (getNTokens() != s.getNTokens())
            return false;
        for (int i = 2; i < nTokens - 2; i++)
            if (/*GetWord(i) != s.GetWord(i) ||*/ // jonas: if the surface form is equal, treat as equal
                    getWordToken(i).realString().compareTo(s.getWordToken(i).realString()) != 0)
                return false;
        return true;
    }

    void tagMe() {
        //Ensure.ensure(tagger);
        for (int i = 2; i < getNWords() + 2; i++)
            tokens[i].setSelectedTag(null, true);
        tagger.tagSentence(this);
        fixUp();
    }

    void setContentBits() {
        bitsSet = true;
        isHeading = tagger.getTags().specialTag(Yytoken.TOKEN_DELIMITER_HEADING) ==
                getWordToken(0).getSelectedTag();

        int nOK = 0, nForeign = 0;
        int i;
        for (i = 2; i < getNWords() + 2; i++) {
            WordToken t = getWordToken(i);
            if (t.getWord().hasStyleRemark())
                containsStyleWord = true;
            if (t.getWord().isForeign())
                nForeign++;
            else if (t.getWord().isNewWord())
                containsNewWord = true;
            else if (!t.getSelectedTag().isPunctuationOrEnder() &&
                    !t.getSelectedTag().isProperNoun())
                nOK++;
        }
        seemsForeign = getNWords() > 4 && ((nOK == 0 && nForeign > 0)
                || ((float) ((nOK) / getNWords()) < 0.15));

        for (i = 2; i < getNWords() - 1; i++) {
            if (getWord(i) == getWord(i + 1) && getWordToken(i) == getWordToken(i + 1) &&
                    getWordToken(i).getToken() != Yytoken.TOKEN_CARDINAL) {
                getWordToken(i + 1).repeated = true;
                containsRepeatedWords = true;
                break;
            }
            pip:
            for (int j = i + 2; (getNWords() + 2 - j) > (j - i); j++) {
                int k;
                for (k = 0; k < j - i; k++)
                    if (getWord(i + k) != getWord(j + k))
                        continue pip;
                for (k = 0; k < j - i; k++)
                    if (getWordToken(i + k) != getWordToken(j + k))
                        continue pip;
                // fix check strings
                for (k = 0; k < j - i; k++)
                    getWordToken(j + k).repeated = true;
                containsRepeatedWords = true;
                return;
            }
        }
    }

    void print(int from, int to, PrintStream out) throws IOException {
        Ensure.ensure(from >= 0);
        Ensure.ensure(to < getNTokens());
        if (from == 1) from = 2;
        if (to == nWords + 2) to--;
        boolean green = false, red = false;
        for (int i = from; i <= to; i++) {
            if (getWordToken(i).isChanged()) {
                if (!green) {
                    out.println(Settings.xGreen);
                    green = true;
                }
            } else {
                if (green) {
                    out.println(Settings.xNoColor);
                    green = false;
                }
            }
            if (getWordToken(i).isMarked2()) {
                if (!red) {
                    out.println(Settings.xRed);
                    red = true;
                }
            } else {
                if (red) {
                    out.println(Settings.xNoColor);
                    red = false;
                }
            }
            if (green && red) Message.invoke(MessageType.MSG_WARNING, "both green and red means purple");
            getWordToken(i).print(out, to != i);
        }
        if (green) out.println(Settings.xNoColor);
        if (red) out.println(Settings.xNoColor);
    }


    void print(PrintStream out) throws IOException {
        if (isHeading()) {
            out.println(Settings.xHeading);
        }
        if (Settings.xOutputWTL)
            print(0, getNTokens() - 1, out);
        else if (Settings.xPrintSelectedTag)
            print(2, getNWords() + 1, out);
        else
            print(2, getNWords() + 1, out);
        if (isHeading()) {
            out.println(Settings.xNoHeading);
        }
    }

    public void fixUp() {
        boolean setFirst = true;
        boolean changedFound = false;
        for (int i = 0; i < getNTokens(); i++) {
            if (tokens[i].getSelectedTag() == null)
                Message.invoke(MessageType.MSG_ERROR, "FixUp(): no selected tag in pos", String.valueOf(i));
            if ((i < 2 || i > getNTokens() - 3)) {
                if (!tokens[i].getSelectedTag().isSentenceDelimiter())
                    Message.invoke(MessageType.MSG_ERROR, "FixUp(): not OK tag in pos", String.valueOf(i));
            } else {
                if (tokens[i].getSelectedTag().isSentenceDelimiter())
                    Message.invoke(MessageType.MSG_WARNING, "FixUp() not OK tag in pos", String.valueOf(i));
                tokens[i].trailingSpace =
                        tokens[i + 1].getSelectedTag().isPunctuationOrEnder() ? false : true;
                if (setFirst && !tokens[i].getSelectedTag().isPunctuationOrEnder()) {
                    tokens[i].setFirstInSentence(true);
                    setFirst = false;
                } else if (tokens[i].isFirstInSentence())
                    tokens[i].setFirstInSentence(false);
                if (tokens[i].isChanged())
                    changedFound = true;
                else if (!changedFound && tokens[i].getOrgPos() != i)
                    tokens[i].changed = true;
            }
        }
    }

}