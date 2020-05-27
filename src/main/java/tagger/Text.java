package tagger;

import common.Message;
import common.MessageType;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class Text {
    public List<Sentence> sentences;
    private int nSentences;
    private int nWordTokens;       // not including sentence-delimiters // jonas, actually, it is including ...
    private int nNewWords;

    public Text() {
        sentences = null;
    }

    public int getNSentences() {
        return nSentences;
    }

    public int getNWordTokens() {
        return nWordTokens;
    }

    public int getNNewWords() {
        return nNewWords;
    }

    public Sentence getFirstSentence() {
        return sentences.get(0);
    }

    public void print(PrintStream out) throws IOException {
        for (Sentence s : sentences)
            s.print(out);
    }

    public void countContents() {
        nSentences = 0;
        nWordTokens = 0;
        nNewWords = 0;
        for (Sentence s : sentences) {
            nSentences++;
            nWordTokens += s.getNWords();
            for (int i = 2; i < s.getNWords() + 2; i++) {
                if (s.getWord(i).isNewWord())
                    nNewWords++;
            }
        }
    }

    public WordToken getWordTokenInPos(int pos) {
        int i;
        for (i = 0; i < sentences.size(); i++) {
            if (sentences.get(i).getWordToken(2).getOffset() > pos)
                break;
        }
        if (i != sentences.size())
            for (int j = 2; j < sentences.get(i).getNWords() + 2; j++)
                if (sentences.get(i).getWordToken(j).getOffset() <= pos &&
                        sentences.get(i).getWordToken(j + 1).getOffset() > pos)
                    return sentences.get(i).getWordToken(j);
        return null;
    }

    public void reset() {
        Message.invoke(MessageType.MSG_STATUS, "resetting text...");
        if (sentences == null)
            return;
        for (Sentence s : sentences)
            for (int i = 2; i < s.getNTokens() - 2; i++)
                s.getWord(i).reset();
        //jonas  if (firstSentence) delete firstSentence;
        //jonas firstSentence = null;
    }
    /*
  inline std::ostream&operator<<(std::ostream&os,Text &t)

  {
    os << "text with " << t.NSentences() << " sentences and "
            << t.NWordTokens() << " word-tokens";
    return os;
  }

  inline std::ostream&operator<<(std::ostream&os,Text *t)

  {
    if (t) os << *t; else os << "(null Text)";
    return os;
  }

  // jonas, remove all sentences here, instead of using recursive delete

  }*/
}
