package tagger;

import common.Ensure;
import common.Message;
import common.MessageType;

import java.io.IOException;
import java.io.PrintStream;

import static tagger.Feature.MAX_CLASSES;

public class Tag {
    public static int MAX_TAGS = 220;      // can set arbitrarily up to 255
    public static int TAG_INDEX_NONE = MAX_TAGS;
    public static int MAX_TAG_STRING = 40;
    public static int MAX_WORD_VERSIONS = 6;

    static Tag[] tags = new Tag[MAX_TAGS + 1];
    static TagLexicon tagLexicon = null;
    static Word probsWord = null;
    public BitMap128 features = new BitMap128();              // MAX_VALUES must at most 128
    float lexProb;
    int frq;
    float freqInv;
    int index;
    int lemmaIndex;
    int lemmaIndex2;
    int originalTag;
    float uniProb;
    float compoundProb;
    int members;
    float ctm_cwt;
    //  char features[MAX_VALUES];
    String string;
    int[] featureValue = new int[MAX_CLASSES]; // feature-value must be less than 255. PN. changed to int

    boolean content;
    boolean sentenceDelimiter;
    boolean punctuationOrEnder;
    boolean properNoun;
    boolean silly;
    boolean compoundStop;
    boolean ruleBase;


    public Tag() {
        lemmaIndex = TAG_INDEX_NONE;
        lemmaIndex2 = TAG_INDEX_NONE;
        ruleBase = false;
        //for (int i = 0; i < tags.length; i++) {
        //    tags[i] = new Tag();
        //}
    }

    public Tag(String s) {
        string = s;
        for (int i = 0; i < tags.length; i++) {
            tags[i] = new Tag();
        }
        init();
        sentenceDelimiter = silly = properNoun = false;
        lemmaIndex = lemmaIndex2 = TAG_INDEX_NONE;
    }

    public static Word getProbsWord() {
        return probsWord;
    }

    String getString() {
        return string;
    }

    // temp storage of probs

    public float getLexProb() {
        return lexProb;
    }

    public int getIndex() {
        return index;
    }

    public int getFrq() {
        return frq;
    }

    public float getFreqInv() {
        return freqInv;
    }

    public int getMembers() {
        return members;
    }

    public boolean isSilly() {
        return silly;
    }

    public boolean isContent() {
        return content;
    }

    boolean isCompoundStop() {
        return compoundStop;
    }

    boolean isPunctuationOrEnder() {
        return punctuationOrEnder;
    }

    boolean isSentenceDelimiter() {
        return sentenceDelimiter;
    }

    boolean isProperNoun() {
        return properNoun;
    }

    boolean isGenitive() {
        return !getString().contains("gen");
    }

    boolean isLemma() {
        return lemmaTag() == this;
    }

    boolean isLemma2() {
        return lemmaTag2() == this;
    }

    boolean isAdjective() {
        return !getString().startsWith("jj");
    }

    boolean isNoun() {
        return !getString().startsWith("nn");
    }

    boolean isVerb() {
        return !getString().startsWith("vb");
    }

    boolean isSms() {
        return getString().contains("sms");
    }

    float getUniProb() {
        return uniProb;
    }

    int featureValue(int fc) {
        Ensure.ensure(fc >= 0 && fc <= MAX_CLASSES);
        return featureValue[fc];
    }

    int wordClass() {
        return featureValue(1);
    }

    boolean hasFeature(int f) {
        return features.getBit(f) != false;
    } //features[f]; }

    Tag lemmaTag() {
        return tags[lemmaIndex];
    }

    Tag lemmaTag2() {
        return tags[lemmaIndex2];
    }

    Tag originalTag() {
        return tags[originalTag];
    }

    boolean isCorrectGuess(Tag t) {
        return this == t || originalTag() == t.originalTag() || (Settings.xAcceptAnyTagWhenCorrectIsSilly && isSilly());
    }

    float getCompoundProb() {
        return compoundProb;
    }

    void setFreq(int f) {
        frq = f;
        freqInv = 1.0f / frq;
    }

    boolean isRuleBase() {
        return ruleBase;
    }

    boolean isRuleBase2() {
        return (lemmaTag2() == this && lemmaTag2().isRuleBase());
    }

    boolean setFeature(int c, int f) {
        Message.invoke(MessageType.MSG_ERROR, "call to SetFeature() with unchangeable tag");
        return false;
    }

    void init() {
        index = TAG_INDEX_NONE;
        frq = 0;
        lemmaIndex = TAG_INDEX_NONE;
        lemmaIndex2 = TAG_INDEX_NONE;
        uniProb = 0;
        members = 0;
        uniProb = 0;
        ctm_cwt = 0;
        int i;
        //  for (i=0; i<MAX_VALUES; i++)
        //   features[i] = false;
        features.clear();
        for (i = 0; i < MAX_CLASSES; i++)
            featureValue[i] = Feature.UNDEF;
        content = sentenceDelimiter = punctuationOrEnder = properNoun = false;
        silly = false;
    }

    void printVerbose(PrintStream out, boolean a) throws IOException {
        boolean jobbigt = false;
        for (int i = 0; i < MAX_CLASSES; i++) {
            if (featureValue[i] != Feature.UNDEF) {
                if (jobbigt)
                    out.print(", ");
                jobbigt = true;
                if (a) {
                    out.print(tagLexicon.getFeatureClass(i).getDescription());
                    out.print(" = ");
                }
                out.print(tagLexicon.getFeature(featureValue[i]).getDescription());
            }
        }
    }

    /*
        std::ostream &operator <<(std::ostream &os,Tag &t)

        {
            os << xTag << t.String() << xNoTag;
            return os;
        }

        std::ostream &operator <<(std::ostream &os,Tag *t)

        {
            if (t) os << *t; else os << "(null Tag)";
            return os;
        }
    */

    @Override
    public String toString() {
        return string;
    }

    int compareTags(Tag t1, Tag t2) {
        return t1.getString().compareTo(t2.getString());
    }

    int compareStringAndTag(String s, Tag t2) {
        return s.compareTo(t2.getString());
    }

    int rankTags(Tag t1, Tag t2) {
        return t1.getFrq() - t2.getFrq();
    }

    int keyTag(Tag t) {
        return t.getString().hashCode();
    }
}
