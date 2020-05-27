package tagger;

import common.Message;
import common.MessageType;

public class ChangeableTag extends Tag {

    public ChangeableTag() {
        super("");
        index = TAG_INDEX_NONE;
    }

    public ChangeableTag(String t) {
        super(t);
        index = TAG_INDEX_NONE;
        string = "";
    }

    public boolean setFeature(int c, int f) {
        if (c < 0 || c >= Feature.MAX_CLASSES) {
            Message.invoke(MessageType.MSG_WARNING, "no such feature class", String.valueOf(c));
            return false;
        }
        if (f < 0 || f >= Feature.MAX_VALUES) {
            Message.invoke(MessageType.MSG_WARNING, "no such feature value", String.valueOf(f));
            return false;
        }
        FeatureClass fc = tagLexicon.getFeatureClass(c);
        for (int i = 0; i < fc.getNFeatures(); i++)
            features.unsetBit(fc.getFeature(i));
        //    features[fc.GetFeature(i)] = false;
        features.setBit(f);
        // features[f] = true;
        featureValue[c] = (char) f;
        string = "";
        return true;
    }

    public String getString() {
        if (string != "") {
            return string;
        }
        for (int j = 0; j < Feature.MAX_CLASSES; j++)
            if (featureValue[j] != Feature.UNDEF) {
                if (string != "") {
                    string += ".";
                }
                string += tagLexicon.getFeature(featureValue[j]).getName();
            }
        return string;
    }

    void reset() {
        string = "";
        int i;
        features.clear();
        //  for (i=0; i<MAX_VALUES; i++)
        //    features[i] = false;
        for (i = 0; i < Feature.MAX_CLASSES; i++)
            featureValue[i] = (char) Feature.UNDEF;
    }

    Tag findMatchingTag(int n) {
        Tag t = tagLexicon.findTag(getString());
        if (t != null) {
            if (n < 0) {
                n = 0;
                return t;
            }
        }
        if (n < 0)
            n = 0;
        // the try to find a compatible tag:
        int fvset = tagLexicon.featureValueIndex("set"); // messy fix
        next:
        for (; n < tagLexicon.getCT(); n++) {
            Tag t2 = tags[n];
            if (t != t2)
                for (int j = 0; j < Feature.MAX_CLASSES; j++)
                    if (featureValue[j] != Feature.UNDEF &&
                            !tagLexicon.isCompatible(featureValue[j], t2.featureValue(j))
                            && featureValue[j] != fvset)
                        continue next;
            n++;
            return t2;
        }
        return null;
    }

}
