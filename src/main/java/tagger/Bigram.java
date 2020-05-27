package tagger;

public class Bigram {

    public Word word1;
    public Word word2;
    public float prob;

    public boolean compareBigrams(Bigram b1, Bigram b2) {
        return !(b1.word1 == b2.word1 && b1.word2 == b2.word2);
    }

    public int keyBigram(Bigram b) {
        return b.word1.getString().hashCode() ^ b.word2.getString().hashCode();
    }
/*
     std::ostream&operator<<(std::ostream&os,Bigram &b)

    {
        os << b.word1.String() << ' ' << b.word2.String();
        return os;
    }

     std::ostream&operator<<(std::ostream&os,Bigram *b)

    {
        if (b) os << *b; else os << "(null Bigram)";
        return os;
    }*/

}
