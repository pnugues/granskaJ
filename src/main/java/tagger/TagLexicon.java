package tagger;

import common.Ensure;
import common.Message;
import common.MessageType;
import org.json.JSONArray;
import org.json.JSONObject;
import token.Tokenizer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

//import common.File;


public class TagLexicon extends LinkedHashMap<String, Tag> {

    static float[][] bigramProbs = new float[Tag.MAX_TAGS][Tag.MAX_TAGS];
    static int[][] ttOff = new int[Tag.MAX_TAGS][Tag.MAX_TAGS];
    static int CT = 0, CTT = 0, CTTT = 0, CWT = 0;
    public List<Tag> tagL; //PN. This list could certainly be removed
    //  HashArray<TagTrigram> tagTrigrams;
    TagTrigram[] ttt;
    Tag dummyTag; // jonas
    String lexiconDir;
    int[][] bigramFreqs;
    Tag[] specialTags = new Tag[Tag.MAX_TAGS + 1];
    int[] specialTagIndices = new int[Tag.MAX_TAGS + 1];
    int nFeatures;
    int nFeatureClasses;
    FeatureValue[] features = new FeatureValue[Feature.MAX_VALUES + 1];
    FeatureClass[] featureClasses = new FeatureClass[Feature.MAX_CLASSES + 1];
    BitMap65536 equalFeaturesBitMap = new BitMap65536();
    Word probsWord;
    // The JSON files to load
    private JSONArray ct, ctt, cttt, ctm, taginfo;
    private JSONObject featuresJSON;

    TagLexicon() {
        dummyTag = null;
        bigramFreqs = null;
        Ensure.ensure(Feature.MAX_VALUES < 256);
        for (int i = 0; i < features.length; i++) {
            features[i] = new FeatureValue();
        }
        for (int i = 0; i < featureClasses.length; i++) {
            featureClasses[i] = new FeatureClass();
        }
        for (int i = 0; i < specialTags.length; i++) {
            specialTags[i] = new Tag();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        TagLexicon tl = new TagLexicon();
        tl.loadSlow("/Users/pierre/Projets/Granska/granskaJ/lex/tags/");
    }

    static boolean isLoaded() {
        return CT > 0;
    }

    float Pt3_t1t2(int t1, int t2, int t3) {   // P(t3 | t1, t2)
        if (ttOff[t1][t2] >= 0) {
            int index = ttOff[t1][t2];
            TagTrigram t = ttt[index];
            for (; t.tag3 < t3; t = ttt[index++]);
            if (t.tag3 == t3 && t.tag2 == t2)
                return t.pf_prob;
        }
        return bigramProbs[t2][t3];
        //    TagTrigram *t = tagTrigrams.Find(TagTrigram(t1, t2, t3));
        //    return t ? t.pf_prob : bigramProbs[t2][t3];
    }

    int ttt_freq(char t1, char t2, char t3) {
        if (ttOff[t1][t2] >= 0) {
            TagTrigram t = ttt[ttOff[t1][t2]];
            for (int i = 0; ttt[ttOff[t1][t2] + i].tag3 < t3; i++)
                t = ttt[ttOff[t1][t2] + i];
            if (t.tag3 == t3 && t.tag2 == t2)
                return t.pf_freq;
        }
        return 0; //bigramFreqs[t2][t3];
    }

    float Pt1t2(char t1, char t2) {               // P(t1, t2) = P(t1)*P(t2 | t1)
        //Ensure.ensure(bigramFreqs);
        return tagL.get(t1).getUniProb() * bigramFreqs[t1][t2] / tagL.get(t1).getFrq();
    }

    int t_freq(char t1) {
        return tagL.get(t1).getFrq();
    }

    Tag findTag(String s) {
        Tag t = new Tag(s);
        return get(t);
    }

    int tt_freq(char t1, char t2) {
        return bigramFreqs[t1][t2];
    }

    int getCT() {
        return CT;
    }

    Tag specialTag(Yytoken t) {
        return specialTags[t.ordinal()];
    }

    boolean isOKFeatureIndex(int f) {
        return f >= 0 && f < nFeatures;
    }

    int getNFeatures() {
        return nFeatures;
    }

    int getNFeatureClasses() {
        return nFeatureClasses;
    }

    Tag dummyTag() {
        if (dummyTag != null) dummyTag = new Tag("DummyTag");
        return dummyTag;
    } // jonas

    float Pt3_t1t2(Tag t1, Tag t2, Tag t3) {
        return Pt3_t1t2(t1.index, t2.index, t3.index);
    }

    int featureValueIndex(String s) {
        for (int i = 0; i < nFeatures; i++)
            if (s.compareTo(features[i].getName()) == 0)
                return i;
        return Feature.UNDEF;
    }

    int featureClassIndex(String s) {
        for (int i = 0; i < nFeatures; i++)
            if (s.compareTo(featureClasses[i].getName()) == 0)
                return i;
        return Feature.UNDEF;
    }

    boolean isCompatible(int f1, int f2) {
        //  ensure(IsOKFeatureIndex(f1));
        //  ensure(IsOKFeatureIndex(f2));
        return equalFeaturesBitMap.getBit(f1 | (f2 << 8));
    }

    void setCompatible(int f1, int f2) {
        Ensure.ensure(isOKFeatureIndex(f1));
        Ensure.ensure(isOKFeatureIndex(f2));
        equalFeaturesBitMap.setBit(f1 | (f2 << 8));
        equalFeaturesBitMap.setBit(f2 | (f1 << 8));
    }

//float TagLexicon::Pt3_t1t2(uchar t1, uchar t2, uchar t3) {}

    FeatureValue getFeature(int f) {
        Ensure.ensure(isOKFeatureIndex(f));
        return features[f];
    }

    FeatureClass getFeatureClass(int f) {
        Ensure.ensure(f >= 0);
        Ensure.ensure(f < nFeatureClasses);
        return featureClasses[f];
    }

    void computeProbs() {
        Ensure.ensure(bigramFreqs != null);
        float prevUni = -1;
        float prevBi = -1;
        float prevTri = -1;
        float prevExp = -1;
        float prevEps = -1;
        int i;
        if (Settings.xLambdaUni != prevUni || Settings.xLambdaBi != prevBi ||
                Settings.xEpsilonTri != prevEps) {
            for (int j = 0; j < CT; j++)
                for (i = 0; i < CT; i++) {
                    bigramProbs[i][j] = Settings.xLambdaUni * tagL.get(j).getUniProb();
                    if (bigramFreqs[i][j] > 0)
                        bigramProbs[i][j] += Settings.xLambdaBi * bigramFreqs[i][j] * tagL.get(i).getFreqInv() + Settings.xEpsilonTri;
                }
        }
        if (Settings.xLambdaUni != prevUni || Settings.xLambdaBi != prevBi ||
                Settings.xLambdaTri != prevTri || Settings.xLambdaTriExp != prevExp)
            if (Settings.xLambdaTriExp < 0.6) {
                for (i = 0; i < CTTT; i++) {
                    TagTrigram t = ttt[i];
                    Ensure.ensure(bigramFreqs[t.tag1][t.tag2] > 0);
                    t.pf_prob = Settings.xLambdaUni * tagL.get(t.tag3).getUniProb() +
                            Settings.xLambdaBi * bigramFreqs[t.tag2][t.tag3] * tagL.get(t.tag2).getFreqInv() +
                            Settings.xLambdaTri * t.pf_freq * (float) Math.pow(bigramFreqs[t.tag1][t.tag2], Settings.xLambdaTriExp - 1);
                    Ensure.ensure(t.pf_prob > 0);
                    /* PN UNCOMMENT this to see the bug
                    if ((tagL.get(t.tag1).toString().equals("nn.utr.plu.ind.nom") &&
                            tagL.get(t.tag2).toString().equals("mad") &&
                            tagL.get(t.tag3).toString().equals("sen.que"))
                            ||
                            (tagL.get(t.tag1).toString().equals("mad") &&
                                    tagL.get(t.tag2).toString().equals("sen.que") &&
                                    tagL.get(t.tag3).toString().equals("sen.que"))
                            || (tagL.get(t.tag1).toString().equals("pn.utr.sin.ind.sub") &&
                            tagL.get(t.tag2).toString().equals("mad") &&
                            tagL.get(t.tag3).toString().equals("sen.que"))
                            || (tagL.get(t.tag1).toString().equals("pm.nom") &&
                            tagL.get(t.tag2).toString().equals("mad") &&
                            tagL.get(t.tag3).toString().equals("sen.que"))) {
                        System.out.println("!\t" + i + "\t" +
                                tagL.get(t.tag1) + "\t" + (int) t.tag1 + "\t" +
                                tagL.get(t.tag2) + "\t" + (int) t.tag2 + "\t" +
                                tagL.get(t.tag3) + "\t" + (int) t.tag3 + "\t" +
                                +t.pf_prob);
                        System.out.println((int) t.tag1 + "\t" + (int) t.tag2 + "\t" +  (int) t.tag3);
                        System.out.println(t.pf_prob);
                        System.out.println("Trigrams: " + Pt3_t1t2(46, 118, 118));
                    }*/
                }
            } else
                Message.invoke(MessageType.MSG_MINOR_WARNING, "xLambdaTriExp cannot exceed 0.6 (not tested)");
        prevUni = Settings.xLambdaUni;
        prevBi = Settings.xLambdaBi;
        prevTri = Settings.xLambdaTri;
        prevExp = Settings.xLambdaTriExp;
        prevEps = Settings.xEpsilonTri;
    }

    //PN. This function now loads the files instead of the metainfo
    void loadInfo() throws IOException {
        String ctJSONString = Files.readString(Paths.get(lexiconDir + "/ct.json"), StandardCharsets.UTF_8);
        JSONObject ctJSON = new JSONObject(ctJSONString);
        ct = ctJSON.getJSONArray("ct");
        CT = ct.length();

        String taginfoJSONString = Files.readString(Paths.get(lexiconDir + "/taginfo.json"), StandardCharsets.UTF_8);
        JSONObject taginfoJSON = new JSONObject(taginfoJSONString);
        taginfo = taginfoJSON.getJSONArray("taginfo");

        String featuresJSONString = Files.readString(Paths.get(lexiconDir + "/features.json"), StandardCharsets.UTF_8);
        if (featuresJSONString == null) {
            Message.invoke(MessageType.MSG_WARNING, "cannot open file tags/features, no tag features loaded");
            return;
        }
        featuresJSON = new JSONObject(featuresJSONString);

        String cttJSONString = Files.readString(Paths.get(lexiconDir + "/ctt.json"), StandardCharsets.UTF_8);
        JSONObject cttJSON = new JSONObject(cttJSONString);
        ctt = cttJSON.getJSONArray("ctt");
        CTT = ctt.length();

        String ctttJSONString = Files.readString(Paths.get(lexiconDir + "/cttt.json"), StandardCharsets.UTF_8);
        JSONObject ctttJSON = new JSONObject(ctttJSONString);
        cttt = ctttJSON.getJSONArray("cttt");
        CTTT = cttt.length();

        String ctmJSONString = Files.readString(Paths.get(lexiconDir + "/ctm.json"), StandardCharsets.UTF_8);
        JSONObject ctmJSON = new JSONObject(ctmJSONString);
        ctm = ctmJSON.getJSONArray("ctm");

        CWT = 0;
        for (int i = 0; i < CT; i++) {
            JSONArray item = (JSONArray) ctm.get(i);
            CWT += (int) item.get(0);
        }
    }

    void allocateMemory(boolean fastMode) {
        //Init("tags", CT, CompareTags, KeyTag, RankTags, CompareStringAndTag, KeyWordString);
        ttt = new TagTrigram[CTTT + 1];
        for (int i = 0; i < ttt.length; i++) {
            ttt[i] = new TagTrigram();
        }
        if (fastMode && !Settings.xOptimizeMatchings) return;
        bigramFreqs = new int[CT][CT]; // new OK
    }

    void loadSlow(String dir) throws IOException {
        Ensure.ensure(Feature.MAX_VALUES <= 128);
        lexiconDir = dir;
        Message.invoke(MessageType.MSG_STATUS, "loading tag lexicon slow...");
        loadInfo();
        allocateMemory(false);
        loadTags();
        Message.invoke(MessageType.MSG_STATUS, "loading tag info...");
        loadTagInfo();
        Message.invoke(MessageType.MSG_STATUS, "loading tag features...");
        loadFeatures();
        Message.invoke(MessageType.MSG_STATUS, "loading tag bigrams...");
        loadTagBigrams();
        if (Settings.xTagTrigramsUsed) {
            Message.invoke(MessageType.MSG_STATUS, "loading tag trigrams...");
            loadTagTrigrams();
        }
        Message.invoke(MessageType.MSG_STATUS, "loading tag member...");
        loadTagMember();
        computeProbs();
        setTags();
        testFeatures();
    }

    void loadFeatures() throws IOException {
        Ensure.ensure(Feature.UNDEF == 0);
        features[0].name = "undef";
        features[0].description = "undefined feature value";
        features[0].index = 0;
        featureClasses[0].name = "undef";
        featureClasses[0].description = "undefined feature class";
        featureClasses[0].index = 0;
        nFeatures = 1;
        nFeatureClasses = 1;
        equalFeaturesBitMap.clear();
        setCompatible(0, 0);
        Ensure.ensure(isCompatible(0, 0));
        int i = 0;
        FeatureClass fc = null;
        Iterator<String> keys = featuresJSON.keys();
        List<String> feats = new ArrayList<>();
        while (keys.hasNext()) {
            feats.add(keys.next());
        }
        // We need wordcl as the first item
        feats.remove("wordcl");
        feats.add(0, "wordcl");
        for (int k = 0; k < feats.size(); k++) {
            String key = feats.get(k);
            Ensure.ensure(nFeatureClasses < Feature.MAX_CLASSES);
            fc = featureClasses[nFeatureClasses];
            fc.name = key;
            Ensure.ensure(nFeatures != 1 || fc.name.compareTo("wordcl") != 0);
            fc.description = (String) ((JSONObject) featuresJSON.get(key)).get("swedish_transl");
            i = 0;
            Iterator<String> value_keys = ((JSONObject) ((JSONObject) featuresJSON.get(key)).get("values")).keys();
            while (value_keys.hasNext()) {
                Ensure.ensure(nFeatures < Feature.MAX_VALUES);
                Ensure.ensure(i < Feature.MAX_FEATURES_PER_CLASS);
                FeatureValue fv = features[nFeatures];
                String value_key = value_keys.next();
                fv.name = value_key;
                fv.index = nFeatures;
                nFeatures++;
                fv.description = (String) ((JSONObject) ((JSONObject) featuresJSON.get(key)).get("values")).get(value_key);
                fv.featureClass = nFeatureClasses;
                fc.features[i] = fv.index;
                setCompatible(fv.getIndex(), fv.getIndex());
                Ensure.ensure(isCompatible(fv.getIndex(), fv.getIndex()));
                if (fv.name.contains("/")) {
                    String s = fv.name;
                    String[] ff = s.split("/");
                    for (int l = 0; l < ff.length; l++) {
                        int index2 = featureValueIndex(ff[l]);
                        Ensure.ensure(index2 != Feature.UNDEF);
                        setCompatible(fv.index, index2);
                        Ensure.ensure(isCompatible(fv.index, index2));
                    }
                }
                i++;
            }
            fc.nFeatures = i;
            nFeatureClasses++;
        }

        for (int k = 0; k < CT; k++) {
            Tag t = tagL.get(k);
            String s = t.string;
            String[] f = s.split("\\.");
            for (int l = 0; l < f.length; l++) {
                int j = featureValueIndex(f[l]);
                if (j == Feature.UNDEF)
                    Message.invoke(MessageType.MSG_WARNING, "unknown feature in tags/features:", f[l]);
                t.features.setBit(j);
                //      t.features[j] = true;
                t.featureValue[features[j].featureClass] = (char) j;
            }
            s = t.string;
            String[] ff = s.split("\\./");
            for (int l = 0; l < ff.length; l++) {
                int j = featureValueIndex(ff[l]);
                if (j == Feature.UNDEF)
                    Message.invoke(MessageType.MSG_WARNING, "unknown feature in tags/features:", ff[l]);
                t.features.setBit(j); //[j] = true;
                //      t.features[j] = true;
            }
        }
        //  SetCompatible(UNDEF, FeatureValueIndex("set"));
        //  SetCompatible(FeatureValueIndex("set"), UNDEF);
    }

    void testFeatures() {
        if (!Settings.xTestFeatures)
            return;
        System.out.println("testing tag methods:");
        System.out.println("tags:");
        int i;
        for (i = 0; i < CT; i++) {
            Tag t = tagL.get(i);
            System.out.println(i + "\t" + t.toString());
        }
        System.out.println("features:");
        for (i = 0; i < CT; i++) {
            Tag t = tagL.get(i);
            System.out.print(t.toString() + " (");
            int j;
            for (j = 0; j < nFeatures; j++)
                if (t.hasFeature(j))
                    System.out.print(features[j] + " ");
            System.out.print(") ");
            for (j = 0; j < nFeatureClasses; j++)
                if (t.featureValue(j) != Feature.UNDEF)
                    System.out.print("(" + featureClasses[j] + " " + features[t.featureValue(j)] + ")");
            System.out.println();
        }
        System.out.println("testing compatible features:");
        for (i = 0; i < nFeatures; i++) {
            Ensure.ensure(isCompatible(i, i));
            for (int j = 0; j < nFeatures; j++)
                if (i != j && isCompatible(i, j))
                    System.out.println(features[i] + " and " + features[j] + " are compatible");
        }
        System.out.println("testing features classes:");
        for (i = 0; i < nFeatureClasses; i++) {
            FeatureClass fc = featureClasses[i];
            System.out.print(fc + " can be either of: ");
            for (int j = 0; j < fc.getNFeatures(); j++)
                System.out.print(getFeature(fc.getFeature(j)) + " ");
            System.out.println();
        }
    }

    void loadTags() throws IOException {
        tagL = new ArrayList<>();
        int i, freq;
        for (i = 0; i < CT; i++) {
            Tag t = new Tag();
            int j;
            for (j = 0; j < Feature.MAX_VALUES; j++)
                t.features.unsetBit(j); //[j] = false;
            //  t.features[j] = false;
            for (j = 0; j < Feature.MAX_CLASSES; j++)
                t.featureValue[j] = (char) Feature.UNDEF;
            JSONArray item = (JSONArray) ct.get(i);
            freq = (int) item.get(0);
            t.string = (String) item.get(1);
            t.setFreq(freq);
            Ensure.ensure(t.string.length() + 4 < (int) Tag.MAX_TAG_STRING);
            t.uniProb = (float) freq / (float) CWT;
            if (t.string.contains("sms"))
                t.uniProb *= 0.000001f;
            if (t.string.startsWith("vb"))
                t.compoundProb = (float) 0.1;
            else
                t.compoundProb = (float) 1.0;
            tagL.add(t);
            put(t.string, t);
        }
        // PN. Find a way to remove this below
        for (char j = 0; j < CT; j++) {
            tagL.get(j).index = j;
        }
    }

    void loadTagInfo() throws IOException {
        for (int i = 0; i < CT; i++) {
            specialTags[i] = null;
            specialTagIndices[i] = -1;
            tagL.get(i).lexProb = 0;
        }
        char minusOrPlus;
        Tag tag = new Tag();
        Tag base = new Tag();
        int k = 0;
        for (int i = 0; i < taginfo.length(); i++) {
            k++;
            JSONArray item = (JSONArray) taginfo.get(i);
            minusOrPlus = ((String) item.get(0)).charAt(0);
            tag.string = (String) item.get(1);
            base.string = (String) item.get(2);
            Tag t = get(tag.string);
            if (t == null) {
                Message.invoke(MessageType.MSG_MINOR_WARNING, "unknown tag in tags/taginfo:", tag.string);
                continue;
            }
            Tag b = get(base.string);
            if (b == null) {
                Message.invoke(MessageType.MSG_ERROR, "unknown lemma tag in tags/taginfo:", base.string);
            }
            t.lemmaIndex = b.getIndex();
            t.lexProb = 1;
            t.content = t.properNoun = t.punctuationOrEnder = t.silly = t.sentenceDelimiter = false;
            if (minusOrPlus == '+')
                t.content = true;
            else if (minusOrPlus == '-')
                t.content = false;
            else
                Message.invoke(MessageType.MSG_ERROR, "bad format in tags/taginfo at line", Integer.toString(k));
            for (int j = 2; j < item.length(); j++) {
                base.string = (String) item.get(j);
                b = get(base.string);
                if (b != null)
                    t.lemmaIndex2 = b.getIndex();
                else {
                    Yytoken token = Tokenizer.string2Token(base.string);
                    switch (token) {
                        case TOKEN_ERROR:
                            Message.invoke(MessageType.MSG_WARNING, "token = ERROR in tags/taginfo at line", Integer.toString(k));
                            continue;
                        case TOKEN_SILLY:
                            t.silly = true;
                            continue;
                        case TOKEN_DELIMITER_PERIOD:
                        case TOKEN_DELIMITER_QUESTION:
                        case TOKEN_DELIMITER_EXCLAMATION:
                        case TOKEN_DELIMITER_HEADING:
                        case TOKEN_DELIMITER_OTHER:
                            t.sentenceDelimiter = true;
                            break;
                        case TOKEN_PROPER_NOUN:
                        case TOKEN_PROPER_NOUN_GENITIVE:
                            t.properNoun = true;
                            break;
                        case TOKEN_PERIOD:
                        case TOKEN_QUESTION_MARK:
                        case TOKEN_EXCLAMATION_MARK:
                        case TOKEN_PUNCTUATION:
                            t.punctuationOrEnder = true;
                            break;
                        default:
                            break;
                    }
                    if (specialTags[token.ordinal()] != null)
                        Message.invoke(MessageType.MSG_WARNING, "tags/taginfo: only one tag allowed for each token, line", Integer.toString(k));
                    else {
                        specialTags[token.ordinal()] = t;
                        specialTagIndices[token.ordinal()] = t.getIndex();
                    }
                }
            }
        }
        for (int m = 0; m < CT; m++)
            tagL.get(m).compoundStop = (!tagL.get(m).isContent() || tagL.get(m).isSilly());
        if (k < CT)
            Message.invoke(MessageType.MSG_WARNING, "tags/taginfo contains too few tags, only", Integer.toString(k));
        if (k != 0)
            for (int j = 0; j < CT; j++)
                if (tagL.get(j).lexProb == 0)
                    Message.invoke(MessageType.MSG_ERROR, "tags/taginfo, no info for tag", tagL.get(j).toString());
    }

    void loadTagBigrams() throws IOException {
        //Ensure.ensure(bigramFreqs);
        int freq;
        for (int j = 0; j < CT; j++)
            for (int k = 0; k < CT; k++) {
                bigramFreqs[j][k] = 0;
            }
        Tag tag1 = new Tag(), tag2 = new Tag();
        for (int i = 0; i < ctt.length(); i++) {
            JSONArray item = (JSONArray) ctt.get(i);
            freq = (int) item.get(0);
            tag1.string = (String) item.get(1);
            tag2.string = (String) item.get(2);
            Tag t1 = get(tag1.string);
            Tag t2 = get(tag2.string);
            if (t1 == null)
                Message.invoke(MessageType.MSG_ERROR, "unknown tag in tags/ctt", tag1.string);
            if (t2 == null)
                Message.invoke(MessageType.MSG_ERROR, "unknown tag in tags/ctt", tag2.string);
            bigramFreqs[t1.index][t2.index] = freq;
        }
    }

    void loadTagTrigrams() throws IOException {
        Tag tag1 = new Tag(), tag2 = new Tag(), tag3 = new Tag();
        int i = 0;
        int freq = 0;
        for (i = 0; i < cttt.length(); i++) {
            JSONArray item = (JSONArray) cttt.get(i);
            freq = (int) item.get(0);
            Ensure.ensure(freq > 0);
            tag1.string = (String) item.get(1);
            tag2.string = (String) item.get(2);
            tag3.string = (String) item.get(3);
            Tag t1 = get(tag1.string);
            Tag t2 = get(tag2.string);
            Tag t3 = get(tag3.string);

            if (t1 == null)
                Message.invoke(MessageType.MSG_ERROR, "unknown tag in tags/ctt", tag1.string);
            if (t2 == null)
                Message.invoke(MessageType.MSG_ERROR, "unknown tag in tags/ctt", tag2.string);
            if (t3 == null)
                Message.invoke(MessageType.MSG_ERROR, "unknown tag in tags/ctt", tag3.string);
            ttt[i].tag1 = (char) t1.index;
            ttt[i].tag2 = (char) t2.index;
            ttt[i].tag3 = (char) t3.index;
            ttt[i].pf_freq = freq;

    /*    tagTrigrams[i].tag1 = t1.index;
    tagTrigrams[i].tag2 = t2.index;
    tagTrigrams[i].tag3 = t3.index;
    tagTrigrams[i].pf_freq = freq; */
        }
        Ensure.ensure(i == CTTT);
        ttt[CTTT].tag1 = ttt[CTTT].tag2 = ttt[CTTT].tag3 = (char) CT;
        Arrays.sort(ttt, new TagTriComp());
        //qsort(ttt, CTTT, sizeof(TagTrigram), TagTriComp);
        int a1 = -1, a2 = -1;
        for (i = 0; i < CT; i++)
            for (int j = 0; j < CT; j++)
                ttOff[i][j] = -1;
        for (i = 0; i < CTTT; i++) {
            boolean c = false;
            if (ttt[i].tag1 != a1) {
                a1 = ttt[i].tag1;
                c = true;
            }
            if (ttt[i].tag2 != a2) {
                a2 = ttt[i].tag2;
                c = true;
            }
            if (c) ttOff[a1][a2] = i;
        }
        //  tagTrigrams.Hashify();
    }

    void printStatistics() {
        System.out.println("tag lexicon statistics: ");
        //Statistics();
  /*  if (xTagTrigramsUsed) {
    std::cout << "tag trigrams: ";
    tagTrigrams.Statistics();
    } */
    }

    void loadTagMember() throws IOException {
        int freq;
        Tag tag = new Tag();
        int sum = 0;
        for (int i = 0; i < CT; i++) {
            JSONArray item = (JSONArray) ctm.get(i);
            freq = (int) item.get(0);
            tag.string = (String) item.get(1);
            Tag t = get(tag.string);
            if (t == null) {
                Message.invoke(MessageType.MSG_WARNING, "unknown tag in tags/ctm:", tag.string);
                continue;
            }
            Ensure.ensure(t != null);
            t.members = freq;
            t.ctm_cwt = (float) freq / (float) CWT;
            sum += freq;
        }
        if (sum != CWT)
            Message.invoke(MessageType.MSG_MINOR_WARNING, "sum of tag members =", Integer.toString(sum),
                    "; should be", Integer.toString(CWT));
    }

    void setTags() {
        Tag.tagLexicon = this;
        for (int i = 0; i < Tag.tags.length; i++) {
            Tag.tags[i] = new Tag();
        }
        for (int i = 0; i < getCT(); i++) {
            Tag.tags[i] = tagL.get(i);
            specialTags[i] = specialTagIndices[i] < 0 ? null : tagL.get(specialTagIndices[i]);
        }
        for (int i = getCT(); i <= Tag.MAX_TAGS; i++) {
            Tag.tags[i] = null;
            specialTags[i] = null;
        }
        Tag tag = new Tag();
        for (int i = 0; i < getCT(); i++) {
            Tag t = tagL.get(i);
            if (t.getString().contains("set") || t.getString().contains("dat") ||
                    t.getString().contains("mod") || t.getString().contains("aux") ||
                    t.getString().contains("kop")) {
                tag.string = t.getString();
                if (t.getString().contains("imp.akt"))
                    tag.string = tag.string.substring(0, t.getString().length() - 8);
                else
                    tag.string = tag.string.substring(0, t.getString().length() - 4);
                Tag tg = get(tag.string);
                Ensure.ensure(tg != null);
                t.originalTag = tg.getIndex();
            } else
                t.originalTag = (char) i;
        }
    }
}
