package tagger;

import common.Ensure;
import common.Message;
import common.MessageType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MorfLexicon extends LinkedHashMap<String, Word> {// extends HashArray<Word> {
    public static int MIN_PREFIX_LENGTH = 2;
    public static int MAX_LAST_CHARS = 5;
    public static int MIN_LAST_CHARS = 1;
    // PN. Temporary variable before I understand how the whole stuff works
    List<String> morfL;
    private String lexiconDir;
    private WordTag[] more;
    private char[] strings;
    private int CL = 0, CW = 0, CWT = 0, CMW = 0;
    // The JSON arrays to load in this program
    private JSONArray cw, cwt;

    public MorfLexicon() {
        strings = null;
        morfL = new ArrayList<>();
    }

    public static void main(String[] args) throws IOException {
        TagLexicon tl = new TagLexicon();
        MorfLexicon ml = new MorfLexicon();
        tl.loadSlow("lex/tags/");
        ml.loadSlow("lex/morfs/", tl);
    }

    public int getCWT() {
        return CWT;
    }

    public int getCW() {
        return CW;
    }

    public boolean isLoaded() {
        return CW > 0;
    }

    void loadInfo() throws IOException {
        Message.invoke(MessageType.MSG_STATUS, "loading morf words...");
        String cwJSONString = Files.readString(Paths.get(lexiconDir + "/cw.json"), StandardCharsets.UTF_8);
        JSONObject cwJSON = new JSONObject(cwJSONString);
        cw = cwJSON.getJSONArray("cw");
        CW = cw.length();

        CL = 0;
        for (int i = 0; i < CW; i++) {
            JSONArray item = (JSONArray) cw.get(i);
            CL += ((String) item.get(1)).length() + 1;
        }

        String cwtJSONString = Files.readString(Paths.get(lexiconDir + "/cwt.json"), StandardCharsets.UTF_8);
        JSONObject cwtJSON = new JSONObject(cwtJSONString);
        cwt = cwtJSON.getJSONArray("cwt");
        CWT = cwt.length();
    }

    void allocateMemory() {
        Message.invoke(MessageType.MSG_STATUS, "allocating memory for morfs...");
        CMW = CWT - CW;
        //Init("morfs", CW, CompareWords, KeyWord, RankWords, CompareStringAndWord, KeyWordString);
        strings = new char[CL]; // new OK
        Ensure.ensure(strings != null);
        // TODO PN. I had to add 1. Undestand why
        more = new WordTag[CMW + 1];  // why word, and not wordtag?
        for (int i = 0; i < more.length; i++) {
            more[i] = new WordTag();
        }
        Ensure.ensure(more != null);
    }

    void loadSlow(String dir, TagLexicon tags) throws IOException {
        lexiconDir = dir;
        loadInfo();
        allocateMemory();
        char[] buff = strings;

        long wordLen = 0;
        for (int i = 0; i < CW; i++) {
            JSONArray item = (JSONArray) cw.get(i);
            Word w = new Word();
            w.init();
            w.freq = item.getInt(0);
            w.string = item.getString(1);
            put(w.string, w);
            morfL.add((String) item.get(1));
            wordLen += ((String) item.get(1)).length() + 1;
        }

        int freq;
        String string;
        Tag tag = new Tag();
        int j = 0;
        for (int i = 0; i < CWT; i++) {
            JSONArray item = (JSONArray) cwt.get(i);
            freq = (int) item.get(0);
            string = (String) item.get(1);
            tag.string = (String) item.get(2);

            Word w = get(string);
            if (w == null)
                Message.invoke(MessageType.MSG_ERROR, "unknown word in morfs/cwt:", string);
            Tag t = tags.get(tag.string);
            if (t == null)
                Message.invoke(MessageType.MSG_ERROR, "unknown tag in morfs/cwt:", tag.string);

            // TODO
            // PN. This is the C++ version.
            if (w.getTagIndex() != Tag.TAG_INDEX_NONE) {
                WordTag q;
                for (q = w; q.next() != null; q = q.next()) ;
                q.next = more[j];
                more[j].init(w, false);
                more[j].tagIndex = t.getIndex();
                more[j].tagFreq = freq;
                j++;
                if (j > CMW)
                    Message.invoke(MessageType.MSG_ERROR, "too many word-tags in morfs/cwt at line:", Integer.toString(i + 1));
            } else {
                w.tagIndex = t.getIndex();
                w.tagFreq = freq;
            }
            // TODO
            // PN. Clarify this and align with C++ version
            /*Word temp = new Word();
            temp.tagFreq = freq;
            temp.tagIndex = (char) t.getIndex();
            temp.next = null;
            // Is the word in the map?
            if (get(string) != null) {
                WordTag q;
                for (q = w; q.next() != null; q = q.next())
                    ;
                q.next = temp; // TODO PN. Why do I need this?
                more[j] = q.next;
                more[j].init(w, false);
                more[j].tagIndex = t.getIndex();
                more[j].tagFreq = freq;
                //j++;
                if (j > CMW)
                    Message.invoke(MessageType.MSG_ERROR, "too many word-tags in morfs/cwt at line:", Integer.toString(i + 1));
            } else {
                // The word is not in the map
                put(string, temp);
            }*/
        }
        for (int i = 0; i < CW; i++) {
            int f = 0;
            for (WordTag q = get(morfL.get(i)); q != null; q = q.next())
                f += q.getTagFreq();
            if (f != get(morfL.get(i)).freq) {
                Message.invoke(MessageType.MSG_WARNING, "sum of word-tag-freqs for word", get(morfL.get(i)).String(),
                        "does not sum up to word-freq");
                Message.invoke(MessageType.MSG_CONTINUE, "sum:", Integer.toString(f), "word-freq:", Integer.toString(get(morfL.get(i)).freq));
            }
        }
    }

    /*
        void SetPointersFromIndices() {
            Message.invoke(MessageType.MSG_STATUS, "setting morf pointers...");
            for (int i = 0; i < CW; i++) {
                Word w = &( * this)[i];
                w.string = strings + (size_t) w.string;
                WordTag wt;
                for (wt = w; wt; wt = wt.Next())
                    if (wt.next == (WordTag *) - 1)
                        wt.next = w;
                    else
                        wt.next = &more[(size_t) wt.next];
            }
        }
    */

    /*
        void CompressStrings() {
            Message.invoke(MessageType.MSG_STATUS, "compressing morf strings...");
            // assuming suggested-bit is 0 for all words
            // mark words that are are subwords:
            int i;
            for (i = 0; i < CW; i++)
                for (char *s = ( * this)[i].string + 1; *s;
            s++){
                Word * w;
                if ((w = Find(s)) != null)
                    w.suggested = 1;
            }
            char *s, *strings2;
            s = strings2 = new char[CL];      // new OK // a new place to put the strings
            Ensure.ensure(s);
            // move all words that are not subwords:
            for (i = 0; i < CW; i++) {
                if (!( * this)[i].suggested){
                    strcpy(s, ( * this)[i].string);
                    ( * this)[i].string = s;
                    s += ( * this)[i].strLen + 1;
                }
            }
            // move all subwords:
            for (i = 0; i < CW; i++)
                if (!( * this)[i].suggested)
            for (char *ss = ( * this)[i].string + 1; *ss;
            ss++){
                Word * w;
                if ((w = Find(ss)) != null)
                    if (w.suggested) {
                        w.string = ( char*)ss;
                        w.suggested = 0;
                    }
            }
            //  for (i=0; i<CW; i++)
            //    ensure(!words[i].suggested);
            //  std::cout<<" (saved "<<CL-(s-strings2)<<" of "<<CL<<" bytes, "<<100.0*(CL-(s-strings2))/CL<<"%)";
            ExtByt(-CL);
            CL = s - strings2 + 1;
            ExtByt(CL);
            strings = strings2;
        }
    */
    /* jonas */
    void printStatistics() {
        System.out.println("morf lexicon statistics: ");
        // PN Below HashArray
        // Statistics();
    }

}
