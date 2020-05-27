package tagger;

import java.util.Comparator;

public class TagTriComp implements Comparator<TagTrigram> {
    public int compare(TagTrigram a1, TagTrigram a2) {
        TagTrigram t1 = a1;
        TagTrigram t2 = a2;
        if (t1.tag1 > t2.tag1) return 1;
        if (t1.tag1 < t2.tag1) return -1;
        if (t1.tag2 > t2.tag2) return 1;
        if (t1.tag2 < t2.tag2) return -1;
        if (t1.tag3 > t2.tag3) return 1;
        if (t1.tag3 < t2.tag3) return -1;
        return 0;
    }
}