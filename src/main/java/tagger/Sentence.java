package tagger;

import common.Ensure;

public class Sentence extends AbstractSentence {

    public Sentence(int n) {
        Ensure.ensure(n < MAX_SENTENCE_LENGTH);
        tokens = new WordToken[n];
    }

}
/*
     std::ostream& operator<<(std::ostream& os, Sentence &s) {
        os << &s; return os;
    }
     std::ostream& operator<<(std::ostream& os, AbstractSentence *s) {
        if (s) s.Print(os); else os << "(null Sentence)"; return os;
    }
*/

