package tagger;

import common.Ensure;
import common.Message;
import common.MessageType;

import java.io.IOException;
import java.io.PrintStream;

public class DynamicSentence extends AbstractSentence {
    private DynamicSentence next;
    private short size;
    private SentenceStatus status;
    private WordToken[] actualTokens;

    public DynamicSentence() {
        next = null;
        size = 0;
        actualTokens = null;
    }

    DynamicSentence(AbstractSentence s) {

        size = nTokens = (short) s.getNTokens();
        nWords = (short) s.getNWords();
        actualTokens = new WordToken[size]; // new OK
        tokens = new WordToken[size]; // new OK
        for (int i = 0; i < nTokens; i++) {
            actualTokens[i] = s.getWordToken(i);
            actualTokens[i].orgPos = (char) i;
            actualTokens[i].marked = false;
            actualTokens[i].marked2 = false;
            tokens[i] = actualTokens[i];
        }
        prob = s.getProb();
        next = null;
        status = SentenceStatus.SEEMS_OK;
    }

    public DynamicSentence getNext() {
        return next;
    }

    public DynamicSentence setNext(DynamicSentence s) {
        return next = s;
    }

    public SentenceStatus status() {
        return status;
    }

    public void printOrgRange(int from, int to, PrintStream out) throws IOException {
        for (int i = from; i < getNTokens(); i++)
            if (tokens[i].getOrgPos() == to + 1) {
                print(from, i - 1, out);
                return;
            }
        Message.invoke(MessageType.MSG_WARNING, "PrintOrgRange() failed");
    }

    void delete(int from, int to) {
        Ensure.ensure(size > 0);
        Ensure.ensure(from <= to);
        Ensure.ensure(from >= 1);
        Ensure.ensure(to < nTokens - 1);
        short n = (short) (to - from + 1);
        for (int i = to + 1; i < nTokens; i++)
            tokens[from++] = tokens[i];
        nTokens -= n;
        nWords -= n;
        Ensure.ensure(nTokens > 1);
    }

    boolean delete(int orgPos) {
        int pos = 0;
        for (int i = 2; i < getNTokens() - 1; i++)
            if (tokens[i].orgPos == orgPos) {
                pos = i;
                break;
            }
        if (pos == 0) return false;
        delete(pos, pos);
        return true;
    }

    boolean replace(Word w, String string, int orgPos) {
        int pos = 0;
        for (int i = 2; i < getNTokens() - 1; i++)
            if (tokens[i].orgPos == orgPos) {
                pos = i;
                break;
            }
        if (pos == 0) return false;
        if (w == getWord(pos) && string.compareTo(tokens[pos].realString()) == 0)
            return true;
        tokens[pos].setWord(w, string, Yytoken.TOKEN_WORD);
        tokens[pos].changed = true;
        return true;
    }

    boolean insert2(int orgPos, Word w, String string) {
        int pos = 0;
        for (int i = 2; i < getNTokens() - 1; i++)
            if (tokens[i].orgPos == orgPos) {
                pos = i;
                break;
            }
        if (pos == 0) return false;
        insert(pos, w, string);
        return true;
    }

    void insert(int pos, Word w, String string) {
        Ensure.ensure(pos < nTokens);
        Ensure.ensure(pos > 0);
        Ensure.ensure(size > 0);
        if (size <= nTokens) {
            size += 5;
            WordToken[] t = new WordToken[size]; // new OK
            for (int i = 0; i < nTokens; i++)
                t[i] = tokens[i];
            tokens = t;
        }
        Ensure.ensure(pos < size);
        for (int i = nTokens; i > pos; i--)
            tokens[i] = tokens[i - 1];
        tokens[pos] = new WordToken(); // new OK
        tokens[pos].setWord(w, string, Yytoken.TOKEN_WORD);
        tokens[pos].selectedTagErasable = true;
        tokens[pos].setSelectedTag(null, true);
        tokens[pos].changed = tokens[pos].inserted = true;
        nTokens++;
        nWords++;
    }
}
