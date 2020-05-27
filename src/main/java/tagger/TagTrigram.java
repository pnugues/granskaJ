package tagger;

import java.io.Serializable;

public class TagTrigram implements Serializable {
    //  uint Key() { return tag1 ^ (tag2<<4) ^ (tag3<<9); }
    public char tag1;
    public char tag2;
    public char tag3;
    public char nothing;
    public float pf_prob;
    public int pf_freq;

    public TagTrigram() {
    }

    public TagTrigram(char t1, char t2, char t3) {
        tag1 = t1;
        tag2 = t2;
        tag3 = t3;
    }

/*

 std::ostream& operator<<(std::ostream& os, TagTrigram &t) {
        os << (int)t.tag1 << ' ' << (int)t.tag2 << ' ' << (int)t.tag3 << ' ' << t.pf.prob;
        return os;
        }
        inline std::ostream& operator<<(std::ostream& os, TagTrigram *t) {
        if (t) os << *t;
        else os << "(null)";
        return os;
        }
        inline int CompareTagTrigrams(TagTrigram &t1, TagTrigram &t2) {
        return t1.tag1 != t2.tag1 ||
        t1.tag2 != t2.tag2 ||
        t1.tag3 != t2.tag3;
        }
        inline int RankTagTrigrams(TagTrigram &t1, TagTrigram &t2) {
        return t1.pf.freq - t2.pf.freq;
        }

        inline uint KeyTagTrigram(TagTrigram &t) {
        //  return t.key;   not faster
        return t.tag3 ^ (t.tag2<<4) ^ (t.tag1<<10);
        }*/
}
