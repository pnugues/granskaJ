package tagger;

import common.Message;
import common.MessageType;
import token.Tokenizer;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class WordToken {

    static String stringBuf;
    Yytoken token;
    Word word;
    String string;
    Tag tagSelected;
    int offset;             // character position in text
    WordTag current; // for iterating
    List<WordTag> interpretations;
    char orgPos;            // used by DynamicSentence only

    WordToken next;
    boolean firstCapped;
    boolean allCapped;
    boolean firstInSentence;
    boolean trailingSpace;
    boolean hard;
    boolean selectedTagErasable;
    boolean repeated;
    boolean changed;
    boolean inserted;
    boolean marked;         // used by rules
    boolean marked2;         // used by rules
    boolean xAcceptSpellCapitalWords = true;
    boolean xAcceptSpellProperNouns = true;
    boolean xAcceptAllWordsInCompounds = true;

    public WordToken() {
        token = Yytoken.TOKEN_END;
        word = null;
        string = null;
        tagSelected = null;
        offset = 0;
        current = null;
        orgPos = 0;
        firstCapped = false;
        allCapped = false;
        firstInSentence = false;
        trailingSpace = false;
        hard = false;
        selectedTagErasable = true;
        repeated = false;
        changed = false;
        inserted = false;
        marked = false;
        marked2 = false;
        interpretations = new ArrayList<>();
    }

    static void reset() {
        stringBuf = "";
    }

    void clear() {
        word = null;
        string = null;
        tagSelected = null;
        selectedTagErasable = true;
        // next 2 lines by jonas 030321
        token = Yytoken.TOKEN_END;
        offset = 0;
        current = null;
        orgPos = 0;
        firstCapped = false;
        allCapped = false;
        firstInSentence = false;
        trailingSpace = false;
        hard = false;
        repeated = false;
        changed = false;
        inserted = false;
        marked = false;
        marked2 = false;
    }

    WordTag currentInterpretation() {
        return current;
    }

    String lexString() {
        return getWord().getString();
    }

    String realString() {
        return string;
    }

    Word getWord() {
        return word;
    }

    WordTag getWordTag() {
        return getWord().getWordTag(getSelectedTag());
    }

    int getOffset() {
        return offset;
    }

    int getOrgPos() {
        return orgPos;
    }

    Tag getSelectedTag() {
        return tagSelected;
    }

    boolean isSpellOK() {
        return getWord().isSpellOK() ||
                (isAllCapped() && xAcceptSpellCapitalWords) ||
                (isFirstCapped() && !isFirstInSentence() &&
                        xAcceptSpellProperNouns && getSelectedTag().isProperNoun());
    }

    boolean isFirstCapped() {
        return firstCapped;
    }

    boolean isAllCapped() {
        return allCapped;
    }

    boolean hasTrailingSpace() {
        return trailingSpace;
    }

    boolean isHard() {
        return hard;
    }

    boolean isRepeated() {
        return repeated;
    }

    boolean isBeginOK() {
        return xAcceptAllWordsInCompounds || getWord().isCompoundBeginOK();
    }

    boolean isEndOK() {
        return xAcceptAllWordsInCompounds || getWord().isCompoundEndOK();
    }

    boolean isChanged() {
        return changed;
    }
    // 13 bits free

    boolean isMarked() {
        return marked;
    }

    void setMarked(boolean a) {
        marked = a;
    }

    boolean isMarked2() {
        return marked2;
    }

    void setMarked2(boolean a) {
        marked2 = a;
    }

    Yytoken getToken() {
        return token;
    }

    boolean isFirstInSentence() {
        return firstInSentence;
    }

    void setFirstInSentence(boolean b) {
        //  std::cout << "sf " << string << std::endl;
        char[] st = new char[Word.MAX_WORD_LENGTH];
        firstInSentence = b;
        if (b) {
            if (Character.isUpperCase(string.charAt(0)))
                return;
            st[0] = Character.toUpperCase(string.charAt(0));
        } else {
            if (Character.isLowerCase(string.charAt(0)))
                return;
            st[0] = Character.toLowerCase(string.charAt(0));
        }
        for (int i = 1; i < string.length(); i++) {
            st[i] = string.charAt(i);
        }
        setWord(word, new String(st), token);
    }

    void setSelectedTag(Tag t, boolean erasable) {
        if (selectedTagErasable)
            tagSelected = t;
        if (!erasable) {
            selectedTagErasable = false;
            //ensure(tagSelected);
        }
    }

    WordTag firstInterpretation() {  // to iterate over all interpretations
        current = word;
        return word;
    }

    WordTag nextInterpretation() {  // next when iterating, null when finished
        if (current != null) {
            current = current.next();
            if (current != null && current.isExtraLemma())
                return nextInterpretation();
            return current;
        } else
            return null;
    }

    void setWord(Word w, String s, Yytoken t) {
        word = w;
        token = t;
        if (s != null && s.compareTo(w.getString()) != 0) {
            string = s;
            setCapped(s);
        } else
            string = w.getString();
    }

    void setCapped(String s) {
        if (Character.isUpperCase(s.charAt(0)))
            firstCapped = true;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isUpperCase(s.charAt(i))) {
                return;
            }
        }
        allCapped = true;
    }

    String lemmaString() {
        WordTag wt = getWordTag();
        if (wt != null && wt.lemma(0) != null) {
            return wt.lemma(0).String();
        }
        return getWord().getString();
    }

    void print(PrintStream out, boolean printSpace) throws IOException {
        if (Settings.xOutputWTL) {
            if (getWord() != null) {
                out.print(lexString());
                out.print('\t');
                out.print(getSelectedTag().toString());
                out.print('\t');
                out.print(lemmaString());
                out.print('\n');
            } else
                Message.invoke(MessageType.MSG_WARNING, "no word");
            return;
        }

        boolean printTag = Settings.xPrintSelectedTag;

        Word w = getWord();
        if (w != null) {
            if (Settings.xPrintHTML) {
                out.print(Letter.str2HTML(realString()));
            } else
                prettyPrintTextString(out);

            if (Settings.xPrintWordInfo) {
                out.print(" [");
                if (w.isNewWord()) {
                    ((NewWord) w).printInfo(out);
                } else {
                    w.printInfo(out);
                }
                out.print(' ');
                out.print(getToken().toString());
                out.print(']');
                if (isFirstInSentence()) {
                    out.print('~');
                }
            }
        } else {
            out.print("(null word-token)");
        }
        if (printTag) {
            if (Settings.xPrintAllWordTags) {
                w.printTags(out);
            } else {
                out.print('\t');
                out.print(getSelectedTag());
                out.print('\t');
            }
        }
        if (Settings.xPrintLemma) {
            out.print(lemmaString());
            out.print('\t');
        }
        if (Settings.xPrintOneWordPerLine || Settings.xPrintWordInfo)
            out.print('\n');
        else if (printSpace && hasTrailingSpace())
            out.print(' ');
    }

    void printVerbose(PrintStream out) throws IOException {
        out.print('[');
        prettyPrintTextString(out);
        out.print(']');
        out.print(getWord().isNewWord() ? "* " : " ");
        out.print(" [");
        out.print(lexString());
        out.print("] ");
        out.print(Tokenizer.token2String(token));
        out.print(' ');
        out.print(offset);
        out.print('\n');
    }

    void prettyPrintTextString(PrintStream out) throws IOException {
        if (realString() != null) {// jonas, program crashes when RealString() is null otherwise
            String s = realString();
            for (int i = 0; i < s.length(); i++) {
                if (Letter.isSpace(s.charAt(i))) {
                    out.print(' ');
                    while (Letter.isSpace(s.charAt(i + 1))) {
                        i++;
                    }
                } else {
                    out.print(s.charAt(i));
                }
            }
        }
    }

    @Override
    public String toString() {
        StringWriter out = new StringWriter();
        if (Settings.xOutputWTL) {
            if (getWord() != null) {
                out.write(lexString());
                out.write('\t');
                out.write(getSelectedTag().toString());
                out.write('\t');
                out.write(lemmaString());
                out.write('\n');
            } else
                Message.invoke(MessageType.MSG_WARNING, "no word");
            return out.toString();
        }

        boolean printTag = Settings.xPrintSelectedTag;

        Word w = getWord();
        if (w != null) {
            if (Settings.xPrintHTML) {
                out.write(Letter.str2HTML(realString()));
            } else {
                if (realString() != null) {// jonas, program crashes when RealString() is null otherwise
                    String s = realString();
                    for (int i = 0; i < s.length(); i++) {
                        if (Letter.isSpace(s.charAt(i))) {
                            out.write(' ');
                            while (Letter.isSpace(s.charAt(i + 1))) {
                                i++;
                            }
                        } else {
                            out.write(s.charAt(i));
                        }
                    }
                }
            }

            if (Settings.xPrintWordInfo) {
                out.write(" [");
                if (w.isNewWord()) {
                    ((NewWord) w).printInfo(out);
                } else {
                    w.printInfo(out);
                }
                out.write(' ');
                out.write(getToken().toString());
                out.write(']');
                if (isFirstInSentence()) {
                    out.write('~');
                }
            }
        } else {
            out.write("(null word-token)");
        }
        if (printTag) {
            if (Settings.xPrintAllWordTags) {
                w.printTags(out);
            } else {
                out.write('\t');
                out.write(getSelectedTag().toString());
                out.write('\t');
            }
        }
        if (Settings.xPrintLemma) {
            out.write(lemmaString());
            out.write('\t');
        }
        if (hasTrailingSpace())
            out.write(' ');

        out.flush();
        return out.toString();
    }
}

/*inline std::ostream& operator<<(std::ostream& os, const WordToken &w) {
    w.Print(os); return os;
}
inline std::ostream& operator<<(std::ostream& os, const WordToken *w) {
    if (w) os << *w; else os << "(null WordToken)"; return os;
}
*/
