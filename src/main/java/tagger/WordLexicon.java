package tagger;

import common.Ensure;
import common.Message;
import common.MessageType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/* wordlexicon.cc
 * author: Johan Carlberger
 * last change: 2000-05-08
 * comments: WordLexicon class
 */

/******************************************************************************

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 ******************************************************************************/


public class WordLexicon extends LinkedHashMap<String, Word> implements Serializable {
    public static int MAX_N_EXTRA_RULES = 300;
    public static int MAX_N_EXTRA_LEMMAS = 10000;
    //public Map<String, Word> wordMap;
    public List<Word> wordL;
    Map<String, Word> styleWordMap;
    List<Word> styleWordL;
    // The JSON files to load
    JSONArray cw, cwtl;
    private TagLexicon tags;
    private String lexiconDir;
    private WordTag[] more;
    private char[] strings;
    private int CL = 0, CW = 0, CWT = 0, CMW = 0;
    private int N_STYLEWORDS = 0;
    private Map<String, StyleWord> stylewords;
    private String commentBuf;
    private InflectLexicon inflects = new InflectLexicon();
    private ExtraRules[] extraRules = new ExtraRules[MAX_N_EXTRA_RULES];
    private int nExtraRules;
    private ExtraLemma[] extraLemmas = new ExtraLemma[MAX_N_EXTRA_LEMMAS];
    private int nExtraLemmas;
    private NewWordLexicon newWords;
    private Word[] wordsAlpha;


    public WordLexicon() {
        more = null;
        CW = 0;
        nExtraRules = 0;
        newWords = null;
        for (int i = 0; i < extraRules.length; i++) {
            extraRules[i] = new ExtraRules();
        }
        for (int i = 0; i < extraLemmas.length; i++) {
            extraLemmas[i] = new ExtraLemma();
        }
    }

    public static void main(String[] args) throws IOException {
        TagLexicon tags = new TagLexicon();
        MorfLexicon morfs = new MorfLexicon();
        WordLexicon words = new WordLexicon();
        NewWordLexicon newWords = new NewWordLexicon();
        tags.loadSlow("lex/tags");
        morfs.loadSlow("lex/morfs", tags);
        words.loadSlow("lex/words", tags, newWords);
        //words.testInflections(); // TODO crashes
        String result = words.getInflectionList("bil");
        System.out.println(result);
        words.analyzeWordAndPrintInflections("bilen");
    }

    public Word[] getWordsAlpha() {
        return wordsAlpha;
    }

    //  Word* Find(char *s) { Word w; w.string = (char*) s; return words.Find(w); }
    //  Word* Find2(char *s) { Word w; w.string = (char*) s; return Find(w); }
    //PN. Removed useless
    /*public StyleWord GetStyleWord(Word w) {
        return stylewords.Find(StyleWord(w));
    }*/

    void print() {
        for (int i = 0; i < CW; i++)
            System.out.println(wordL.get(i));
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

    public InflectLexicon getInflects() {
        return inflects;
    }

    void loadInfo() throws IOException {
        Message.invoke(MessageType.MSG_STATUS, "loading words info...");
        String cwJSONString = Files.readString(Paths.get(lexiconDir + "/cw.json"), StandardCharsets.UTF_8);
        JSONObject cwJSON = new JSONObject(cwJSONString);
        cw = cwJSON.getJSONArray("cw");
        CW = cw.length();

        CL = 0;
        for (int i = 0; i < CW; i++) {
            JSONArray item = (JSONArray) cw.get(i);
            CL += ((String) item.get(1)).length() + 1;
        }

        String cwtlJSONString = Files.readString(Paths.get(lexiconDir + "/cwtl.json"), StandardCharsets.UTF_8);
        JSONObject cwtlJSON = new JSONObject(cwtlJSONString);
        cwtl = cwtlJSON.getJSONArray("cwtl");
        CWT = cwtl.length();

        //System.out.println("CW " + CW + " CL " + CL + " CWT " + CWT);
        // N_STYLEWORDS
    }

    // PN. Removed not used
    /*Word FindAbbreviation(String abb) { //obscure stuff
        char *s = abb + 1;
        for (; *s;
        s++)
        if (isspace( * s))
        break;
        if (*s || *(s - 1) == '.'){
            char string[ Word.MAX_WORD_LENGTH],u = string;
            for (char *p = abb; *p;
            p++, u++)
            if (isspace( * p)){
                if (*(u - 1) != '.')
        *u = '.';
        else
                u--;
            } else
        *u = *p;
            if (*(u - 1) != '.')
        *u++ = '.';
        *u = '\0';
            Word w2 = Find(string);
            if (w2)
                return w2;
        *(u - 1) = '\0';
            return Find(string);
        }
        return null;
    }*/

    void allocateMemory() {
        Message.invoke(MessageType.MSG_STATUS, "allocating memory for words...");
        WordTag.words = this;
        CMW = CWT - CW;
        Ensure.ensure(CW);
        wordL = new ArrayList<>();
        //Init("words", CW, CompareWords, KeyWord, RankWords, CompareStringAndWord, KeyWordString);
        more = new WordTag[CMW];
        for (int i = 0; i < more.length; i++) {
            more[i] = new WordTag();
        }
        if (more == null) Message.invoke(MessageType.MSG_ERROR, "out of memory");
        strings = new char[CL]; // new OK
        if (strings == null) Message.invoke(MessageType.MSG_ERROR, "out of memory");
//        if (N_STYLEWORDS > 0) {
//            styleWordL = new ArrayList<>();
//            styleWordMap = new HashMap<>();
//            //stylewords.Init("stylewords", N_STYLEWORDS, CompareStyleWords, KeyStyleWord, null, null, null);
//        }
        wordsAlpha = new Word[CW]; // new OK
        if (wordsAlpha == null) Message.invoke(MessageType.MSG_ERROR, "out of memory");

    }

    WordTag getExtraLemma(WordTag wt) {
        for (int i = 0; i < nExtraLemmas; i++)
            if (extraLemmas[i].wt == wt)
                return extraLemmas[i].lemma;
        Message.invoke(MessageType.MSG_ERROR, "cannot find extra lemma for a word-tag");
        return null;
    }

    void loadLemmas() throws IOException {
        Message.invoke(MessageType.MSG_STATUS, "loading lemmas...");
        String wordString, lemmaString;
        Tag tag = new Tag();
        PrintStream out;
        int freq;
        nExtraLemmas = 0;
        for (int i = 0; i < CWT; i++) {
            JSONArray item = (JSONArray) cwtl.get(i);
            freq = (int) item.get(0);
            wordString = (String) item.get(1);
            tag.string = (String) item.get(2);
            lemmaString = (String) item.get(3);
            Word w = get(wordString);
            if (w == null)
                Message.invoke(MessageType.MSG_ERROR, "no such word in the lexicon:", wordString);
            Tag t = tags.get(tag.string);
            if (t == null)
                Message.invoke(MessageType.MSG_ERROR, "no such tag in the lexicon:", tag.getString());
            WordTag wt = w.getWordTag(t);
            if (wt == null)
                Message.invoke(MessageType.MSG_ERROR, "no such word tag in the lexicon:", wordString, tag.getString());
            Word lw = get(lemmaString);
            WordTag lwt = null;
            if (lw != null) {
                lwt = lw.getWordTag(t.lemmaTag());
                if (lwt == null && t.lemmaTag2() != null)
                    lwt = lw.getWordTag(t.lemmaTag2());
                if (lwt != null && t.isLemma() && lwt != wt && wt.getTagFreq() == 0) {
                    Message.invoke(MessageType.MSG_MINOR_WARNING, wt.String(), t.getString(), "has lemma", lwt.String());
                    //	std::cout << wt << ' ' << lwt << std::endl;
                }
            }
            if (lwt == null) {
                if (Settings.xPrintUnknownLemmas && !t.isSilly()) {
                    if (t.lemmaTag2() != null && lemmaString.charAt(lemmaString.length() - 1) == 's')
                        System.out.println(lemmaString + "\t" + t.lemmaTag2() + "\t" + lemmaString);
                    else
                        System.out.println(lemmaString + "\t" + t.lemmaTag() + "\t" + lemmaString);
                } else
                    Message.invoke(MessageType.MSG_MINOR_WARNING, "no such lemma in the lexicon:", lemmaString,
                            t.lemmaTag().getString());
            } else if (wt.lemma(0) != null) {
                if (nExtraLemmas >= MAX_N_EXTRA_LEMMAS)
                    Message.invoke(MessageType.MSG_ERROR, "number of multiple lemmas exceed",
                            Integer.toString(MAX_N_EXTRA_LEMMAS));
                if (wt.nLemmas() >= Word.MAX_LEMMAS_PER_WORDTAG)
                    Message.invoke(MessageType.MSG_MINOR_WARNING, w.getString(), "has more than",
                            Integer.toString(Word.MAX_LEMMAS_PER_WORDTAG), "lemmas");
                else {
                    extraLemmas[nExtraLemmas].wt = wt;
                    extraLemmas[nExtraLemmas].lemma = lwt;
                    nExtraLemmas++; // jonas ????? gdb says this line trashes memory
                    wt.nExtraLemmas++;
                }
                if (Settings.xListMultipleLemmas) {
                    System.out.println(w + "\t" + t + "\t" + w.lemma + "\t" + w.tagFreq);
                    System.out.println(w + "\t" + t + "\t" + lemmaString + "\t" + freq);
                }
                // find all other wordtags and fix tagFreqs:
                WordTag wt2;
                for (wt2 = wt.next(); wt2 != null; wt2 = wt2.next())
                    if (wt2.getTag() == t) {
                        if (wt2.isExtraLemma())
                            break;
                        wt.tagFreq += wt2.tagFreq;
                        wt2.extraLemma = true;
                    }
                for (wt2 = wt.next(); wt2 != null; wt2 = wt2.next())
                    if (wt2.getTag() == t) {
                        wt2.tagFreq = wt.tagFreq;
                        wt2.lemma = lwt;
                    }
            } else {
                lwt.string = lemmaString; //TODO PN. Added to be able to retrieve the string
                wt.lemma = lwt;
            }
        }
    }

    void addExtraRule(WordTag wt, short ruleIndex) {
        //std::cout << "Added '" << wt.String() << "'" << std::endl;
        if (wt.nInflectRules() == 0) {
            wt.inflectRule = ruleIndex;
            return;
        }
        if (wt.nExtraInflectRules >= InflectRule.MAX_INFLECTION_RULES_PER_WORDTAG - 1)
            Message.invoke(MessageType.MSG_ERROR, "too many inflection rules for",
                    wt.String(), wt.getTag().getString());
        ExtraRules s = null;
        if (nExtraRules > 0)
            if (extraRules[nExtraRules - 1].wt == wt)
                s = extraRules[nExtraRules - 1];
            else if (extraRules[nExtraRules - 1].wt.String().compareTo(wt.String()) > 0) {
                System.out.print("Before '" + wt.String() + "', '");
                System.out.println(extraRules[nExtraRules - 1].wt.String() + "'");
                //Message.invoke(MSG_ERROR, "word rules files must be sorted");
            }
        if (s == null) {
            s = extraRules[nExtraRules++];
            s.wt = wt;
        }
        Ensure.ensure(nExtraRules < MAX_N_EXTRA_RULES);
        s.rule[wt.nExtraInflectRules++] = ruleIndex;
    }

    // PN. No style words. Commented
/*    void LoadStyleWords() {
        // this has become a rather messy method. please improve it if you feel inclined to
        if (!N_STYLEWORDS)
            return;
        Message.invoke(MessageType.MSG_STATUS, "loading style words...");
        InputStream in;
        if (!File.FixIfstream(in, lexiconDir, "style")) {
            Message.invoke(MessageType.MSG_WARNING, "cannot load stylewords");
            N_STYLEWORDS = 0;
            return;
        }
        int i;
        for (i = 0; i < StyleWord.MAX_STYLES; i++)
            StyleWord::styles[i][0] ='\0';
        for (i = 0; i < CW; i++)
            ( * this)[i].style = 0;
        //  words[i].style=0;
        char wordString[ Word.MAX_WORD_LENGTH],lemmaString[Word.MAX_WORD_LENGTH];
        Tag tag;
        char style[ 100],paragraph[100];
        int k = 0;
        while (in >> wordString >> tag.string >> lemmaString) {
            Ensure.ensure(k < N_STYLEWORDS);
            if (wordString[0] == '#')
                continue;
            StyleWord sw = stylewords[k];
            Word w = Find(wordString);
            if (!w)
                Message.invoke(MessageType.MSG_ERROR, "style: no such word", wordString);
            Tag t = tags.Find(tag);
            if (!t)
                Message.invoke(MessageType.MSG_ERROR, "style: no such tag", tag.getString());
            WordTag wt = w.GetWordTag(t);
            if (!wt)
                Message.invoke(MessageType.MSG_ERROR, "style: no such word-tag", w.getString(), t.getString());
            wt.style = true;
            sw.word = w;
            sw.wordTag = wt;
            if (!wt.Lemma(0)) {
                Word lw = Find(lemmaString);
                if (lw)
                    wt.lemma = lw.GetWordTag(t.LemmaTag());
                if (!wt.lemma)
                    Message.invoke(MessageType.MSG_MINOR_WARNING, "style: no such lemma in the lexicon:",
                            lemmaString, t.LemmaTag().getString());
            }
            do {
                in >> style;
                if (strlen(style) >= StyleWord.MAX_PARAGRAPH)
                    Message.invoke(MessageType.MSG_WARNING, "style: too long style type:", style);
                for (i = 0; StyleWord.styles[i][0]; i++)
                    if (!strcmp(StyleWord.styles[i], style))
                        break;
                Ensure.ensure(i < StyleWord.MAX_STYLES);
                if (!StyleWord.styles[i][0])
                    strcpy(StyleWord.styles[i], style);
                sw.style |= ((int) 1 << i);
                SkipSpaceButNotNewLine(in);
            } while (in.peek() == ';' && in.get());
            in >> paragraph;
            if (strlen(paragraph) >= StyleWord.MAX_PARAGRAPH)
                Message.invoke(MessageType.MSG_WARNING, "style: too long paragraph name:", paragraph);
            strncpy(sw.paragraph, paragraph, StyleWord.MAX_PARAGRAPH - 1);
            int j = 0;
            char c;
            SkipSpaceButNotNewLine(in);
            while ((c = (char) in.peek()) != '#' && c != '\n') { // read alternatives
                if (j >= StyleWord.MAX_ALTERNATIVES)
                    Message.invoke(MessageType.MSG_ERROR, "style: too many alternatives for word on line", Integer.toString(k + 1));
                uint m;
                for (m = 0; (c = (char) in.peek()) != ';' && c != '\n' && c != '#'; m++) {
                    wordString[m] = (char) in.get();
                    if (m >= Word.MAX_WORD_LENGTH) {
                        wordString[m] = '\0';
                        Message.invoke(MessageType.MSG_WARNING, "style: too long alternative word", wordString);
                        break;
                    }
                }
                wordString[m] = '\0';
                while (IsSpace(wordString[m - 1])) m--;
                Ensure.ensure(m);
                wordString[m] = '\0';
                if (!strcmp(wordString, "-")) // means no alternative
                    break;
                if (c == ';') in.get();
                SkipSpaceButNotNewLine(in);
                Word * ww = Find(wordString);
                if (!ww) {
                    ww = new Word(wordString);  // jb: who is responsible for the memory?
                    sw.owns_mem[j] = true;
                }
                sw.alt[j] = ww;
                j++;
            }
            sw.nAlts = j;
            if (in.peek() == '#') {
                in.get();
                char comment[ 1000];
                in.getline(comment, 999);
                sw.comment = commentBuf.NewString(comment);
                //sw.comment = new char[strlen(comment)+1];
                //strcpy(sw.comment, comment);
            }
            k++;
        }
        if (k != N_STYLEWORDS)
            Message.invoke(MessageType.MSG_MINOR_WARNING, Integer.toString(k), "words in style words file");
        stylewords.Hashify(false);
    }*/

    void loadWordRules() throws IOException {
        String inflectJSONString = Files.readString(Paths.get(lexiconDir + "/inflection.lex.json"), StandardCharsets.UTF_8);
        JSONObject inflectJSON = new JSONObject(inflectJSONString);
        JSONArray inflect = inflectJSON.getJSONArray("inflection.lex");

        Message.invoke(MessageType.MSG_STATUS, "loading word inflection rules...");
        String string, ruleName;
        //  while(in >> string >> ruleName) { // jonas, error for words like id'ehistoria, split into "id" "historia" with gcc 3.0 (not with gcc 2.95)
        for (int i = 0; i < inflect.length(); i++) {
            JSONArray item = (JSONArray) inflect.get(i);
            string = (String) item.get(0);
            ruleName = (String) item.get(1);

            Word w2 = get(string);
            if (w2 != null) {
                boolean ok = false;
                for (WordTag wt = w2; wt != null; wt = wt.next()) {
                    Tag t = wt.getTag();
                    if (t.originalTag().isRuleBase() ||
                            (t.isRuleBase2() && wt.lemma(0) != null &&
                                    w2.getString().compareTo(wt.lemma(0).String()) == 0)) {
                        /*if (string.equals("regel")) {
                            System.out.println(string + "\t" + ruleName + "\t" + t.originalTag().getIndex());
                        }*/
                        int index = inflects.findRuleIndex(string, ruleName, (char) t.originalTag().getIndex()) ;
                        /*if (string.equals("regel"))
                            System.out.println("Inx: " + index);*/
                        if (index != InflectRule.INFLECT_NO_RULE) {
                            //System.out.println(string + "\t" + ruleName + "\t" + index);
                            addExtraRule(wt, (short) index);
                            ok = true;
                        }
                    }
                }
                /*if (string.equals("regel"))
                    System.out.println(ok);*/
                if (!ok)
                    Message.invoke(MessageType.MSG_MINOR_WARNING, "inflection.lex, rule not applicable for word", w2.getString());
            } else // just to mark the matching rule as used:
                inflects.findRuleIndex(string, ruleName, (char) Tag.TAG_INDEX_NONE);
        }
    }

    void loadSlow(String dir, TagLexicon tgs, NewWordLexicon n) throws IOException {
        tags = tgs;
        lexiconDir = dir;
        newWords = n;
        loadInfo();
        allocateMemory();
        inflects.loadSlow(lexiconDir, tags);
        Message.invoke(MessageType.MSG_STATUS, "loading word lexicon slow...");
        //char[] buff = strings;

        for (int i = 0; i < CW; i++) {
            JSONArray item = (JSONArray) cw.get(i);
            Word w = new Word();
            w.init();
            w.next = null;
            w.freq = (int) item.get(0);
            w.string = (String) item.get(1);
            int len = w.string.length();
            if (!Letter.containsVowel(w.string)) {
                w.compoundEndOK = false;
                w.isForeign = true;
                w.isSpellOK = false;
            }
            if (len < 5)
                w.isSpellOK = false;
            if (len < 3)
                w.compoundEndOK = false;
            if (w.string.contains("-"))
                w.mayBeCapped = true;
            //buff += len + 1;
            w.strLen = (char) len;
            if (w.freq == 0)
                w.extra = true;
            wordsAlpha[i] = w;
            wordL.add(w);
            put(w.string, w);
        }
        // PN. Useless
        //if (buff.equals(strings[CL]))
        //    Message.invoke(MessageType.MSG_WARNING, Integer.toString(buff - strings), "characters read from cw file");
        //  words.Hashify();
        for (int i = 0; i < CW; i++) {
            // TODO PN. Changed this line. This does not work with String(). Understand why
            // Now solved with a change in getWord
            String string = (wordL.get(i)).String();
            //    char *string = (char*) words[i].String();
            if (string.contains(" ")) {
                String str = string;
                String[] s = str.split(" +");
                for (int j = 0; j < s.length; j++) {
                    Word w = get(s[j]);
                    if (w != null)
                        if (s[j].equals(str)) w.collocation1 = true;
                        else w.collocation23 = true;
                    else
                        Message.invoke(MessageType.MSG_MINOR_WARNING, s.toString(), ", collocation part of", str, "not in lexicon");
                }
            }
        }
        Arrays.sort(wordsAlpha, new CompareWordPointers());
        //CompressStrings();
        String cwtlJSONString = Files.readString(Paths.get(lexiconDir + "/cwtl.json"), StandardCharsets.UTF_8);
        JSONObject cwtlJSON = new JSONObject(cwtlJSONString);
        JSONArray cwtl = cwtlJSON.getJSONArray("cwtl");

        String wordString, lemmaString;
        Tag tag = new Tag();
        int freq;
        int j = 0;
        int unknownTags = 0;
        for (int i = 0; i < CWT; i++) {
            JSONArray item = (JSONArray) cwtl.get(i);
            freq = (int) item.get(0);
            wordString = (String) item.get(1);
            tag.string = (String) item.get(2);
            lemmaString = (String) item.get(3);

            Word w = get(wordString);
            if (w == null)
                Message.invoke(MessageType.MSG_ERROR, wordString, "in cwtl is an unknown word");
            if (w.getString().compareTo(wordString) != 0)
                Message.invoke(MessageType.MSG_ERROR, wordString, "main lexicon must not contain capped words");
            Tag t = tags.get(tag.string);
            if (t == null) {
                Message.invoke(MessageType.MSG_WARNING, tag.getString(), "in cwtl is an unknown tag");
                unknownTags++;
                //PN. Not sure about the instr. below
                //t = tags.get("0");
                t = tags.tagL.get(0);
            }
            Ensure.ensure(t != null);
            WordTag wt;
            if (w.getTagIndex() != Tag.TAG_INDEX_NONE) {
                int k = 0;
                for (wt = w; wt.next() != null; wt = wt.next()) {
                    //PN. Here BUG with the creation of an infinite list somewhere.
                    //System.out.print(k + "\t" + wt.lemma + " " + wt);
                    k++;
                }
                // PN. Remove the comments below for the final program
                //System.out.println("Tag number per word (ambiguity):" + k);
                // PN. Original instruction
                //wt = wt.next = more[j];
                //wt.Init(w, false);
                // PN. Begin Replacement
                // TODO If we replace WordTag() with Word(), the lemmatization works for known words
                wt = wt.next = more[j];
                wt.init(w, false);
                //wt = wt.next;
                //wt.isWord = false;
                //more[j] = wt;
                // PN. End replacement
                j++;
                if (j > CMW)
                    Message.invoke(MessageType.MSG_ERROR, "too many word-tags in cwtl at line", Integer.toString(i + 1));
            } else {
                wt = w;
            }
            wt.tagIndex = (char) t.getIndex();
            wt.tagFreq = freq;
            if (t.isProperNoun())
                w.mayBeCapped = true;
            if (t.isProperNoun() && t.isRuleBase())
                wt.inflectRule = (short) inflects.findRuleIndex(w.getString(), "p1", (char) t.getIndex());
            if (freq == 0) {
                wt.extraWordTag = true;
                w.hasExtraWordTag = true;
            }
        }
        Ensure.ensure(j == CMW);
        for (int i = 0; i < CW; i++) {
            int f = 0;
            for (WordTag q = wordL.get(i); q != null; q = q.next())
                //    for (WordTag *q = &words[i]; q; q=q.Next())
                f += q.tagFreq;
            if (f != wordL.get(i).getFreq())
                //    if (f != words[i].Freq())
                Message.invoke(MessageType.MSG_ERROR, "sum of word-tag freqs wrong for word", wordL.get(i).string)
                        ;
            //  Message.invoke(MSG_ERROR, "sum of word-tag freqs wrong for word", words[i].String());
        }
        if (unknownTags != 0)
            Message.invoke(MessageType.MSG_WARNING, Integer.toString(unknownTags), "unknown tags in cwtl");
        loadLemmas();
        loadWordRules();
        //LoadStyleWords();
        loadCompoundLists();
        loadForeign();
        loadVerbtypes();
    }

    void loadVerbtypes() {
        JSONArray intransitivaverb;
        try {
            String ivJSONString = Files.readString(Paths.get(lexiconDir + "/intransitivaverb.json"), StandardCharsets.UTF_8);
            JSONObject ivJSON = new JSONObject(ivJSONString);
            intransitivaverb = ivJSON.getJSONArray("intransitivaverb");
        } catch (IOException e) {
            Message.invoke(MessageType.MSG_WARNING, "cannot load intransitiva verb");
            return;
        }
        Message.invoke(MessageType.MSG_STATUS, "loading intransitiva verb...");
        String string;
        for (int l = 0; l < intransitivaverb.length(); l++) {
            string = (String) intransitivaverb.get(l);
            Word w = get(string);
            if (w != null) {
                w.verbtype = 1;
                for (int k = 0; k < w.nLemmas(); k++)
                    for (int j = 0; j < w.lemma(k).nInflectRules(); j++)
                        for (int i = 0; i < tags.getCT(); i++)
                            if (tags.tagL.get(i).isVerb()) {
                                WordTag wt2 = w.getForm(tags.tagL.get(i), k, j);
                                if (wt2 != null)
                                    wt2.verbtype = 1;
                            }
            }
        }

        JSONArray btvVerbs;
        try {
            String btvJSONString = Files.readString(Paths.get(lexiconDir + "/bitransitivaverb.json"), StandardCharsets.UTF_8);
            JSONObject btvJSON = new JSONObject(btvJSONString);
            btvVerbs = btvJSON.getJSONArray("bitransitivaverb");
        } catch (IOException e) {
            Message.invoke(MessageType.MSG_WARNING, "cannot load bitransitiva verb");
            return;
        }
        Message.invoke(MessageType.MSG_STATUS, "loading bitransitiva verb...");

        for (int l = 0; l < btvVerbs.length(); l++) {
            string = (String) btvVerbs.get(l);
            Word w = get(string);
            if (w != null) {
                w.verbtype = 2;
                for (int k = 0; k < w.nLemmas(); k++)
                    for (int j = 0; j < w.lemma(k).nInflectRules(); j++)
                        for (int i = 0; i < tags.getCT(); i++)
                            if (tags.tagL.get(i).isVerb()) {
                                WordTag wt2 = w.getForm(tags.tagL.get(i), k, j);
                                if (wt2 != null)
                                    wt2.verbtype = 2;
                            }
            }
        }
        JSONArray feminina;
        try {
            String femininaJSONString = Files.readString(Paths.get(lexiconDir + "/feminina.json"), StandardCharsets.UTF_8);
            JSONObject femininaJSON = new JSONObject(femininaJSONString);
            feminina = femininaJSON.getJSONArray("feminina");
        } catch (IOException e) {
            Message.invoke(MessageType.MSG_WARNING, "cannot load feminin nouns");
            return;
        }

        Message.invoke(MessageType.MSG_STATUS, "loading feminin nouns...");
        for (int l = 0; l < feminina.length(); l++) {
            string = (String) feminina.get(l);

            Word w = get(string);
            if (w != null) {
                w.verbtype = 3;
                for (int k = 0; k < w.nLemmas(); k++)
                    for (int j = 0; j < w.lemma(k).nInflectRules(); j++)
                        for (int i = 0; i < tags.getCT(); i++)
                            if (tags.tagL.get(i).isNoun()) {
                                WordTag wt2 = w.getForm(tags.tagL.get(i), k, j);
                                if (wt2 != null)
                                    wt2.verbtype = 3;
                            }
            }
        }
        JSONArray opt_space_words;
        try {
            String opt_space_wordsJSONString = Files.readString(Paths.get(lexiconDir + "/opt_space_words.json"), StandardCharsets.UTF_8);
            JSONObject opt_space_wordsJSON = new JSONObject(opt_space_wordsJSONString);
            opt_space_words = opt_space_wordsJSON.getJSONArray("opt_space_words");
        } catch (IOException e) {
            Message.invoke(MessageType.MSG_WARNING, "cannot load opt-space-words nouns");
            return;
        }
        Message.invoke(MessageType.MSG_STATUS, "loading opt-space-words nouns...");
        for (int i = 0; i < opt_space_words.length(); i++) {
            string = (String) opt_space_words.get(i);
            Word w = get(string);
            if (w != null) {
                w.optSpace = true;
            }
        }
    }

    void loadForeign() {
        String[] lines;
        JSONArray foreign_w;
        try {
            String foreign_wJSONString = Files.readString(Paths.get(lexiconDir + "/foreign.w.json"), StandardCharsets.UTF_8);
            JSONObject foreign_wJSON = new JSONObject(foreign_wJSONString);
            foreign_w = foreign_wJSON.getJSONArray("foreign.w");
        } catch (IOException e) {
            Message.invoke(MessageType.MSG_WARNING, "cannot load foreign.w");
            return;
        }
        Message.invoke(MessageType.MSG_STATUS, "loading foreign words...");
        String string;
        for (int i = 0; i < foreign_w.length(); i++) {
            string = (String) foreign_w.get(i);
            Word w = get(string);
            if (w != null)
                w.isForeign = true;
            string += "s";
            w = get(string);
            if (w != null)
                w.isForeign = true;
        }

        JSONArray spellNotOK;
        try {
            String spellNotOKJSONString = Files.readString(Paths.get(lexiconDir + "/spellNotOK.json"), StandardCharsets.UTF_8);
            JSONObject spellNotOKJSON = new JSONObject(spellNotOKJSONString);
            spellNotOK = spellNotOKJSON.getJSONArray("spellNotOK");
        } catch (IOException e) {
            Message.invoke(MessageType.MSG_WARNING, "cannot load spellNotOK");
            return;
        }

        Message.invoke(MessageType.MSG_STATUS, "loading spellNotOK words...");
        for (int i = 0; i < spellNotOK.length(); i++) {
            string = (String) spellNotOK.get(i);
            Word w = get(string);
            if (w != null)
                w.isSpellOK = false;
        }

        JSONArray spellOK;
        try {
            String spellOKJSONString = Files.readString(Paths.get(lexiconDir + "/spellOK.json"), StandardCharsets.UTF_8);
            JSONObject spellOKJSON = new JSONObject(spellOKJSONString);
            spellOK = spellOKJSON.getJSONArray("spellOK");
        } catch (IOException e) {
            Message.invoke(MessageType.MSG_WARNING, "cannot load spellOK");
            return;
        }

        Message.invoke(MessageType.MSG_STATUS, "loading spellOK words...");
        for (int i = 0; i < spellOK.length(); i++) {
            string = (String) spellOK.get(i);

            Word w = get(string);
            if (w != null)
                w.isSpellOK = true;
        }
    }

    void loadCompoundLists() {
        JSONArray compound_end;
        try {
            String compound_endJSONString = Files.readString(Paths.get(lexiconDir + "/compound-end-stop.w.json"), StandardCharsets.UTF_8);
            JSONObject compound_endJSON = new JSONObject(compound_endJSONString);
            compound_end = compound_endJSON.getJSONArray("compound-end-stop.w");
        } catch (IOException e) {
            Message.invoke(MessageType.MSG_ERROR, "cannot load compound-end-stop.w");
            return;
        }
        String string;
        for (int i = 0; i < compound_end.length(); i++) {
            string = compound_end.getString(i);
            Word w = get(string);
            if (w != null) w.compoundEndOK = false;
        }

        JSONArray compound_begin;
        try {
            String compound_beginJSONString = Files.readString(Paths.get(lexiconDir + "/compound-begin-ok.w.json"), StandardCharsets.UTF_8);
            JSONObject compound_beginJSON = new JSONObject(compound_beginJSONString);
            compound_begin = compound_beginJSON.getJSONArray("compound-begin-ok.w");

        } catch (IOException e) {
            Message.invoke(MessageType.MSG_ERROR, "cannot load compound-begin-ok.w");
            return;
        }
        for (int i = 0; i < compound_begin.length(); i++) {
            string = compound_begin.getString(i);
            Word w = get(string);
            if (w != null) w.compoundBeginOK = true;
        }
    }

/*    void SetPointersFromIndices() {
        Message.invoke(MessageType.MSG_STATUS, "setting word pointers...");
        int i;
        for (i = 0; i < CW; i++) {
            Word w = &( * this)[i];
            //    Word *w = &words[i];
            w.string = strings + (size_t) w.string;
            WordTag wt;
            for (wt = w; wt; wt = wt.Next()) {
                if (wt.lemma == (WordTag *) - 1)
                    wt.lemma = null;
                else if (((size_t) wt.lemma) < CW)
                    wt.lemma = &( * this)[(size_t) wt.lemma];
                //	wt.lemma = &words[(int)wt.lemma];
        else
                wt.lemma = &more[((size_t) wt.lemma) - CW];
                if (wt.next == (WordTag *) - 1)
                    wt.next = w;
                else
                    wt.next = &more[(size_t) wt.next];
            }
            wordsAlpha[i] = &( * this)[(size_t) wordsAlpha[i]];
            //    wordsAlpha[i] = &words[(int)wordsAlpha[i]];
        }
        for (i = 0; i < nExtraLemmas; i++) {
            ExtraLemma & wtal = extraLemmas[i];
            if (((size_t) wtal.wt) < CW)
                wtal.wt = &( * this)[(size_t) wtal.wt];
            //      wtal.wt = &words[(int)wtal.wt];
        else
            wtal.wt = &more[((size_t) wtal.wt) - CW];
            if (((size_t) wtal.lemma) < CW)
                wtal.lemma = &( * this)[(size_t) wtal.lemma];
            //      wtal.lemma = &words[(int)wtal.lemma];
        else
            wtal.lemma = &more[((size_t) wtal.lemma) - CW];
        }
        for (i = 0; i < nExtraRules; i++) {
            ExtraRules & wtar = extraRules[i];
            if (((size_t) wtar.wt) < CW)
                wtar.wt = &( * this)[(size_t) wtar.wt];
            //      wtar.wt = &words[(int)wtar.wt];
        else
            wtar.wt = &more[((size_t) wtar.wt) - CW];
        }
    }*/

    int getWordsInRange(String s, int[] n) {
        int min = 0;
        int max, max2;
        max = max2 = CW - 1;
        int len = s.length();
        while (min <= max) {
            int mid = (max + min) / 2;
            // TODO Understand why this change corrected the bug
            // PN. Changed String() to string and seems to work. Should understand why
            int cmp = s.substring(0, len).compareTo(wordsAlpha[mid].string);
            //int cmp = s.substring(0, len).compareTo(wordsAlpha[mid].String());
            if (cmp > 0)
                min = mid + 1;
            else {
                max = mid - 1;
                if (cmp < 0)
                    max2 = max;
            }
        }
        int min2 = max;
        if (min2 < 0)
            min2 = 0;
        while (min2 <= max2) {
            int mid = (max2 + min2) / 2;
            if (s.substring(0, len).compareTo(wordsAlpha[mid].string) >= 0)
                //if (s.substring(0, len).compareTo(wordsAlpha[mid].String()) >= 0)
                min2 = mid + 1;
            else
                max2 = mid - 1;
        }
        n[0] = max2 - min + 1;
        return min;
    }

    boolean loadFast(String dir, TagLexicon tgs, NewWordLexicon n, boolean warn) throws IOException, ClassNotFoundException {
        tags = tgs;
        lexiconDir = dir;
        newWords = n;
        lexiconDir = dir;
        if (!Files.exists(Paths.get(lexiconDir + "/fast")))
            return false;

        File fastFile = new File(lexiconDir, "/fast");
        FileInputStream fis = new FileInputStream(fastFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        /*if (!File.CheckVersion(in, Settings.xVersion)) {
            Message.invoke(MessageType.MSG_WARNING, "fast word lexicon file obsolete");
            return false;
        }
        Message.invoke(MessageType.MSG_STATUS, "loading word lexicon fast...");
        File.ReadVar(in, CW);
        File.ReadVar(in, CWT);
        File.ReadVar(in, CL);
        File.ReadVar(in, N_STYLEWORDS);
        File.ReadVar(in, nExtraRules);*/
        allocateMemory();
        ois.readObject();
        wordL = (List<Word>) ois.readObject();
        more = (WordTag[]) ois.readObject();
        strings = (char[]) ois.readObject();
        nExtraLemmas = (int) ois.readObject();
        inflects.loadFast(ois);
        extraRules = (ExtraRules[]) ois.readObject();
        wordsAlpha = (Word[]) ois.readObject();
        ois.close();
        fis.close();

        //SetPointersFromIndices();
        //LoadStyleWords();
        if (Settings.xWarnAll)
            for (int i = 0; i < CW - 1; i++)
                if (wordsAlpha[i].String().compareTo(wordsAlpha[i + 1].String()) > 0)
                    System.out.println(wordsAlpha[i] + " " + wordsAlpha[i + 1]);
        return true;
    }

    //PN. Not sure what it means. May be restore
    /*
    void CompressStrings() {
        Message.invoke(MessageType.MSG_STATUS, "compressing word strings...");
        // assuming newWord-bit is 0 for all words
        // mark words that are are subwords:
        int i;
        for (i = 0; i < CW; i++)
            for (char s = ( this)[i].string + 1; s;
        s++){
            //    for (char *s = words[i].string+1; *s; s++) {
            Word w = Find(s);
            if (w) w.newWord = 1;
        }
        String strings2, s = strings2 = new char[CL]; // new OK // a new place to put the strings
        if (!strings2) {
            Message.invoke(MessageType.MSG_WARNING, "not enough memory to compress strings");
            return;
        }
        // move all words that are not subwords:
        for (i = 0; i < CW; i++) {
            //    if (!words[i].newWord) {
            //  strcpy(s, words[i].string);
            //  words[i].string = s;
            //  s += words[i].strLen + 1;
            // }
            if (!( * this)[i].newWord){
                strcpy(s, ( this)[i].string);
                ( this)[i].string = s;
                s += ( this)[i].strLen + 1;
            }
        }
        // move all subwords:
        for (i = 0; i < CW; i++)
            if (!( this)[i].newWord)
        for (char ss = ( * this)[i].string + 1; *ss;
        ss++){
            //    if (!words[i].newWord)
            //      for (char *ss = words[i].string+1; *ss; ss++) {
            Word w = Find(ss);
            if (w && w.newWord) {
                w.string = ( char*)ss;
                w.newWord = 0;
            }
        }
        // std::cerr<<" (saved "<<CL-(s-strings2)<<" of "<<CL<<" bytes, "<<100.0*(CL-(s-strings2))/CL<<"%)";
        ExtByt(-CL);
        CL = s - strings2 + 1; // means a smaller string buffer when fast loading
        ExtByt(CL);
        delete strings;
        strings = strings2;
    }*/

    boolean save() throws IOException {
        if (Files.exists(Paths.get(lexiconDir + "/fast")))
            return false;
        Message.invoke(MessageType.MSG_STATUS, "saving fast word lexicon...");
        // PN. Not sure what the code below means
        // Check after te code compiles in Java
        /*
        for (int i = 0; i < CW; i++) {
            WordTag wt, next;
            ( this)[i].string = ( this)[i].string - (size_t) strings;
            //    words[i].string = words[i].string - (uint)strings;
            for (wt =&( * this)[i];
            wt;
            wt = next){
                //    for (wt=&words[i]; wt; wt=next) {
                next = wt.Next();
                if (next)
                    wt.next = (WordTag) (wt.next -  more[0]); // i.e. index of wt.next in more
                else
                    wt.next = (WordTag) - 1;
                if (wt.lemma)
                    if (wt.lemma.IsWord()) {
                        Ensure.ensure((int) ((Word) wt.lemma -  ( this)[0]) < CW);
                        //	  Ensure.ensure((int)((Word*)wt.lemma - &words[0]) < CW);
                        wt.lemma = (WordTag) ((Word ) wt.lemma - & ( this)[0]);
                        //	  wt.lemma = (WordTag*) ((Word*)wt.lemma - &words[0]);
                    } else
                        wt.lemma = (WordTag * (CW + (wt.lemma - & more[0]));
                else
                    wt.lemma = (WordTag ) - 1;
            }
            wordsAlpha[i] = (Word ) (wordsAlpha[i] -  ( this)[0]);
            //    wordsAlpha[i] = (Word*) (wordsAlpha[i] - &words[0]);
        }
        for (int i = 0; i < nExtraLemmas; i++) {
            ExtraLemma wtal = extraLemmas[i];
            if (wtal.lemma.IsWord()) {
                Ensure.ensure((int) ((Word) wtal.lemma -  ( this)[0]) < CW);
                //      Ensure.ensure((int)((Word*)wtal.lemma - &words[0]) < CW);
                wtal.lemma = (WordTag) ((Word) wtal.lemma - ( this)[0]);
                //      wtal.lemma = (WordTag*) ((Word*)wtal.lemma - &words[0]);
            } else
                wtal.lemma = (WordTag) (CW + (wtal.lemma - more[0]));
            if (wtal.wt.IsWord()) {
                Ensure.ensure((int) ((Word) wtal.wt -  ( this)[0]) < CW);
                //      Ensure.ensure((int)((Word*)wtal.wt - &words[0]) < CW);
                wtal.wt = (WordTag) ((Word) wtal.wt - (  this)[0]);
                //      wtal.wt = (WordTag*) ((Word*)wtal.wt - &words[0]);
            } else
                wtal.wt = (WordTag ) (CW + (wtal.wt -  more[0]));
        }
        for (int i = 0; i < nExtraRules; i++) {
            ExtraRules wtar = extraRules[i];
            if (wtar.wt.IsWord()) {
                Ensure.ensure((int) ((Word) wtar.wt -  ( this)[0]) < CW);
                //      Ensure.ensure((int)((Word*)wtar.wt - &words[0]) < CW);
                wtar.wt = (WordTag) ((Word *) wtar.wt -  (  this)[0]);
                //      wtar.wt = (WordTag*) ((Word*)wtar.wt - &words[0]);
            } else
                wtar.wt = (WordTag) (CW + (wtar.wt -  more[0]));
        }*/
        // PN. may be restore after all compiles
        /*File.SetVersion(out, xVersion);
        File.WriteVar(out, CW);
        File.WriteVar(out, CWT);
        File.WriteVar(out, CL);
        File.WriteVar(out, N_STYLEWORDS);
        File.WriteVar(out, nExtraRules);*/

        File fastFile = new File(lexiconDir, "fast");
        FileOutputStream fos = new FileOutputStream(fastFile);
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(fos));
        // PN. To restore when I undestand how to do it
        //oos.writeObject(this);
        oos.writeObject(wordL);
        oos.writeObject(more);
        oos.writeObject(strings);
        oos.writeObject(nExtraLemmas);
        inflects.save(oos);
        oos.writeObject(extraRules);
        oos.writeObject(wordsAlpha);
        oos.flush();
        oos.close();
        fos.close();
        //SetPointersFromIndices();
        return true;
    }

    void printStatistics() {
        System.out.println("word lexicon statistics: ");
        //  words.Statistics();
        //Statistics();
    }

    int compoundAnalyze(NewWord w) {
        // returns number of suffixes added to w
        w.isCompoundAnalyzed = true;
        int len = w.getStringLen();
        if (len < Settings.xCompoundMinLength)
            return 0;
        Word bestSuffix = null;
        String suffString, prefString;
        prefString = w.getString();
        for (int sufLen = len - Settings.xCompoundPrefixMinLength;
             sufLen >= Settings.xCompoundSuffixMinLength; sufLen--) {
            int preLen = len - sufLen;
            suffString = w.string.substring(preLen);
            Word s = get(suffString);
            if (s != null && s.isCompoundEndOK()) {
                prefString = w.getString().substring(0, preLen);
                Word p = get(prefString);
                if (bestSuffix == null)
                    if (p == null || p.isCompoundBeginOK())
                        bestSuffix = s;
                if (p != null && p.isCompoundBeginOK()) {
                    w.addSuffixes(s);
                    if (suffString.charAt(0) != 's')
                        return w.nSuffixes();
                } else if (w.string.charAt(preLen - 1) == 's') {
                    prefString = prefString.substring(0, preLen - 1);
                    p = get(prefString);
                    if (p != null && p.isCompoundBeginOK()) {
                        w.addSuffixes(s);
                        return w.nSuffixes();
                    }
                } else if (w.string.charAt(preLen - 1) == '-') {
                    prefString = prefString.substring(0, preLen - 1);
                    p = get(prefString);
                    if (p != null) {
                        w.addSuffixes(s);
                        return w.nSuffixes();
                    }
                }
            }
        }
        if (!Settings.xCompoundRequirePrefix && bestSuffix != null)
            w.addSuffixes(bestSuffix);
        return w.nSuffixes();
    }

/*
int WordLexicon::CheckForWordTags(NewWord *nw, Word *w) {
  static NewWord *prev = null;
  static Word *checked[200];
  static int n = 0;
  static int ok = 0;
  if (nw != prev) {
    prev = nw;
    ok = 0;
    for (int i=0; i<n; i++)
      checked[i].suggested = 0;
    n = 0;
  }
  for (WordTag *wt = w; wt; wt=wt.Next())
    if (wt.IsLemma())
      if (AddTagsDerivedFromLemma(nw, wt))
	ok++;
  return ok;
}
*/

    boolean addTagsDerivedFromLemma(NewWord w, WordTag lemma) {
        boolean ok = false;
        //System.out.println("adding word-tags to " + w + " derivable from " + lemma);
        //System.out.println(lemma.nInflectRules());
        for (int j = 0; j < lemma.nInflectRules(); j++) {
            //System.out.println("passe2");
            // jbfix: if no rule found, exit gracefully
            int ir = lemma.inflectRule(j);
            if (ir == InflectRule.INFLECT_NO_RULE)
                return false;
            InflectRule r = inflects.rule(ir);
            for (int i = 0; i < r.getNForms(); i++) {
                String ss = r.apply(lemma.String(), i);
                if (ss != null && ss.compareTo(w.getString()) == 0 &&
                        r.tagIndex(i) != Tag.TAG_INDEX_NONE &&
                        !tags.tagL.get(r.tagIndex(i)).isSms()) {
                    WordTag wt = newWords.addWordTag(w, tags.tagL.get(r.tagIndex(i)));
                    //	std::cout << wt << " from " << lemma << std::endl;
                    wt.lemma = lemma;
                    w.isDerived = true;
                    ok = true;
                    if (i == 0) // i.e. lemma
                        wt.inflectRule = (short) lemma.inflectRule(j);
                }
            }
        }
        //  std::cout << std::endl;
        return ok;
    }

    void analyzeNewWord(NewWord w, boolean tryHard) {
        // tries to determine w:s lemma and inflection rule
        if (w.getString() == null) return; // line added by Viggo 1999-09-27
        compoundAnalyze(w);
        String string;
        Word[] checkedWords = new Word[Word.MAX_WORD_LENGTH];
        int nChecked = 0;
        int oks = 0;
        int i;
        //  std::cout << "check suffix" << std::endl;
        for (i = 0; i < w.nSuffixes(); i++) {
            WordTag suffix = w.suffix(i);
            Ensure.ensure(suffix != null);
            int len = w.getStringLen() - suffix.getWord().getStringLen();
            string = w.getString().substring(0, len);
            for (int j = 0; j < suffix.nLemmas(); j++) {
                WordTag suffixLemma = suffix.lemma(j);
                string = string.substring(0, len) + suffixLemma.String(); // string is the wanted lemma-string now
                Tag lemmaTag = suffixLemma.getTag().originalTag();
                //System.out.println("SL: " + lemmaTag);
                Word w2 = get(string);
                if (w2 != null) {     // main lexicon lemma word exists
                    checkedWords[nChecked++] = w2;
                    w2.suggested = true;
                    WordTag lemma = w2.getWordTag(lemmaTag);
                    if (lemma != null)
                        if (addTagsDerivedFromLemma(w, lemma))
                            oks++;
                } else {
                    NewWord w3 = newWords.get(string);
                    if (w3 == null) {
                        w3 = newWords.addWord(string, null);
                        w3.isAnalyzed = true;
                    }
                    checkedWords[nChecked++] = w3;
                    w3.suggested = true;
                    WordTag lemma = w3.getWordTag(lemmaTag);
                    if (lemma == null)
                        lemma = newWords.addWordTag(w3, lemmaTag);
                    lemma.lemma = lemma; // funny
                    //System.out.println("Str: " + suffixLemma.nInflectRules());
                    //System.out.println(suffixLemma.inflectRule);
                    //System.out.println(suffixLemma.nExtraInflectRules);
                    for (int k = 0; k < suffixLemma.nInflectRules(); k++) {
                        lemma.inflectRule = (short) suffixLemma.inflectRule(k);
                        if (addTagsDerivedFromLemma(w, lemma))
                            oks++;
                    }
                }
            }
            if (suffix.nLemmas() <= 0) {
                newWords.addWordTag(w, suffix.getTag());
                Message.invoke(MessageType.MSG_MINOR_WARNING, suffix.String(),
                        suffix.getTag().getString(), "has no lemma");
            }
        }
        if (oks < 4) {
            //std::cout << "check prefix" << std::endl;
            string = w.getString();
            int start = w.getStringLen() - 1;
            int stop = start - Word.MAX_INFLECTION_CHARS_ON_NORMAL_WORD + 1;
            if (stop < Word.MIN_PREFIX_CHARS_ON_NORMAL_WORD)
                stop = Word.MIN_PREFIX_CHARS_ON_NORMAL_WORD;
            for (i = start; i >= stop; i--) {
                string = string.substring(0, i);
                //      std::cout << " test " << string << std::endl;
                Word w2 = get(string);
                if (w2 == null) {
                    NewWord w3 = newWords.get(string);
                    if (w3 != null)
                        if (!w3.isSuggested()) {
                            if (!w3.isAnalyzed())
                                analyzeNewWord(w3, false);
                            w2 = w3;
                        }
                } else {
                    if (w2.isSuggested()) continue;
                    checkedWords[nChecked++] = w2;
                    w2.suggested = true;
                }
                for (WordTag wt2 = w2; wt2 != null; wt2 = wt2.next())
                    if (wt2.isLemma())
                        if (addTagsDerivedFromLemma(w, wt2))
                            oks++;
            }
            int min_range = 0;
            int n = 0;
            if (oks < 4) {
                //std::cout << "check range" << std::endl;
                string = w.getString();
                int min = w.getStringLen() + 2;
                for (i = start; i >= stop && oks < 2; i--) {
                    string = string.substring(0, i);
                    // PN. Beware n is a variable
                    // Used this trick to change the value
                    int[] temp = {n};
                    min_range = getWordsInRange(string, temp);
                    n = temp[0];
                    //    std::cout << "range on " << string << " = " << n << std::endl;
                    if (n > 20) break;
                    for (int j = 0; j < n; j++) {
                        Word w2 = wordsAlpha[min_range + j];
                        if (w2.isSuggested()) continue;
                        w2.suggested = true;
                        if (w2.getStringLen() <= min)
                            for (WordTag wt2 = w2; wt2 != null; wt2 = wt2.next()) {
                                //	  std::cout << w2 << ' ' << wt2.IsLemma() << "len: " << w2.StringLen() << std::endl;
                                if (wt2.isLemma())
                                    if (addTagsDerivedFromLemma(w, wt2)) {
                                        oks++;
                                        if (oks >= 2)
                                            break;
                                    }
                            }
                    }
                }
            }
            for (i = 0; i < n; i++)
                wordsAlpha[min_range + i].suggested = false;
        }
        for (i = 0; i < nChecked; i++)
            checkedWords[i].suggested = false;
        if (!w.isCompound() && !w.isDerived()) {
            if (w.getTextFreq() > 3 && w.isAlwaysCapped()) {
                if (w.getString().charAt(w.getStringLen() - 1) == 's')
                    newWords.addWordTag(w, tags.specialTag(Yytoken.TOKEN_PROPER_NOUN_GENITIVE));
                else {
                    WordTag wt2 = newWords.addWordTag(w, tags.specialTag(Yytoken.TOKEN_PROPER_NOUN));
                    //	std::cerr << wt2 << std::endl;
                    guessWordTagRule(wt2);
                }
            }
            int best = -1;
            InflectRule bestRule = null;
            for (int k = 0; k < getInflects().getNRules(); k++) {
                InflectRule r = getInflects().rule(k);
                if (w.isAlwaysCapped() && r.name().compareTo("p1") != 0)
                    continue;
                if (r.IsApplicable(w.getString(), Tag.TAG_INDEX_NONE)) {
                    int nFormsFound = 0;
                    for (int j = 1; j < r.getNForms(); j++) {
                        String s = r.apply(w.getString(), j);
                        if (s != null && w.getString().compareTo(s) != 0) {
                            NewWord w2 = newWords.get(s);
                            if (w2 != null)
                                nFormsFound++;
                        }
                    }
                    if (nFormsFound > best) {
                        if (tryHard) {
                            newWords.addWordTag(w, tags.tagL.get(r.tagIndex(0)));
                            w.lemma = w;
                            w.inflectRule = (short) k;
                        }
                        bestRule = r;
                        best = nFormsFound;
                    }
                }
            }
            if (best > 0) {
                //std::cerr << w << ' ' << bestRule << std::endl;
                for (int j = 1; j < bestRule.getNForms(); j++) {
                    String s = bestRule.apply(w.getString(), j);
                    if (s != null && w.getString().compareTo(s) != 0 && bestRule.tagIndex(j) != Tag.TAG_INDEX_NONE) {
                        NewWord w2 = newWords.get(s);
                        if (w2 != null)
                            newWords.addWordTag(w2, tags.tagL.get(bestRule.tagIndex(j)));
                    }
                }
            }
        }
        //  if (w.IsLemma() && w.NInflectRules() == 0)
        //    std::cerr << "no rule for " << w << std::endl;
        w.isAnalyzed = true;
    }

    void analyzeNewWords() {
        Message.invoke(MessageType.MSG_STATUS, "analyzing new words...");
        NewWord w;
        for (String key : newWords.keySet()) {
            w = newWords.get(key);
            if (!w.isAnalyzed())
                analyzeNewWord(w, false);
        }
    }

    void guessWordTagRule(WordTag wt) {
        //  Message.invoke(MSG_STATUS, "guessing rule for word", wt.String());
        Tag t = wt.getTag();
        NewWord nw = wt.getWord().isNewWord() ? (NewWord) wt.getWord() : null;
        if (nw != null) {
            if (!nw.isCompoundAnalyzed())
                Message.invoke(MessageType.MSG_WARNING, nw.getString(), "not compound analyzed");
            for (int i = 0; i < nw.nSuffixes(); i++) {
                WordTag suffix = nw.suffix(i);
                if (suffix.getTag() == t && suffix.inflectRule != InflectRule.INFLECT_NO_RULE) {
                    wt.inflectRule = suffix.inflectRule;
                    return;
                }
            }
        }
        int bestRule = InflectRule.INFLECT_NO_RULE;
        float best = -1;
        String prevName = "";
        for (int i = 0; i < getInflects().getNRules(); i++) {
            InflectRule r = getInflects().rule(i);
            if (r.name().compareTo(prevName) != 0)
                if (r.IsApplicable(wt.String(), t.getIndex())) {
                    prevName = r.name();
                    float nFormsFound = 0.0f;
                    for (int j = 1; j < r.getNForms(); j++) {
                        String string = r.apply(wt.String(), j);
                        if (string != null) {
                            Word w3 = get(string);
                            if (w3 != null) {
                                WordTag wt3 = w3.getWordTag(r.tagIndex(j));
                                if (wt3 != null) {
                                    int k;
                                    for (k = 0; k < wt3.nLemmas(); k++)
                                        if (wt3.lemma(k).getWord() == wt.getWord()) {
                                            nFormsFound += 1.0f;
                                            break;
                                        }
                                    if (k >= wt3.nLemmas())
                                        nFormsFound += 0.1f;
                                }// else
                                //	nFormsFound -= 0.4f;
                            }
                        }
                    }
                    if (nFormsFound > best) {
                        bestRule = i;
                        best = nFormsFound;
                    }
                }
        }
        //  if (bestRule != INFLECT_NO_RULE)
        //    std::cout << wt << tab << Inflects().Rule(bestRule).Name() << std::endl;
        wt.inflectRule = (short) bestRule;
    }

    int extraInflectRule(WordTag wt, int n) {
        if (n < 1 || n >= InflectRule.MAX_INFLECTION_RULES_PER_WORDTAG)
            Message.invoke(MessageType.MSG_ERROR, "ExtraInflectRule called with", Integer.toString(n));
        if (nExtraRules > 0) {
            int mid, min = 0, max = nExtraRules - 1, cmp;
            do {
                mid = (min + max) / 2;
                Ensure.ensure(extraRules[mid].wt != null);
                cmp = extraRules[mid].wt.String().compareTo(wt.String());
                if (cmp < 0) min = mid + 1;
                else if (cmp > 0) max = mid - 1;
                else break;
            } while (min <= max);
            if (cmp != 0) {
                Message.invoke(MessageType.MSG_WARNING, "no extra inflect rule for", wt.String());
                return InflectRule.INFLECT_NO_RULE;
            }
            if (extraRules[mid].wt == wt)
                return extraRules[mid].rule[n - 1];
            else if (mid > 0 && extraRules[mid - 1].wt == wt)
                return extraRules[mid - 1].rule[n - 1];
            else if (mid < nExtraRules - 1 && extraRules[mid + 1].wt == wt)
                return extraRules[mid + 1].rule[n - 1];
            //Message.invoke(MSG_ERROR, "problem in ExtraInflectRule(), johan must fix this", wt.String());
            Message.invoke(MessageType.MSG_WARNING, "problem in ExtraInflectRule(), johan must fix this", wt.String());
            return InflectRule.INFLECT_NO_RULE;
        }
        Message.invoke(MessageType.MSG_ERROR, "cannot find extra inflect rule for", wt.String());
        return InflectRule.INFLECT_NO_RULE;
    }

    WordTag getInflectedForm(WordTag wt, int ruleIndex, Tag t) {
        if (!wt.isLemma())
            Message.invoke(MessageType.MSG_MINOR_WARNING, "GetInflectedForm():", wt.String(), "is not a lemma");
        String string = inflects.getInflectedForm(wt.String(), ruleIndex, t.getIndex());
        if (string != null) {
            Word w = get(string);
            if (w != null) {
                WordTag wt2 = w.getWordTag(t);
                if (wt2 != null)  // if wanted form exists, it is returned
                    return wt2;
            }
            // or else a new word is used:
            NewWord nw = newWords.get(string);
            if (nw == null) {
                nw = newWords.addWord(string, t);
                nw.lemma = wt.getWord();
                nw.isAnalyzed = true;
                return nw;
            }
            WordTag wt2 = nw.getWordTag(t);
            if (wt2 != null)
                return wt2;
            wt2 = newWords.addWordTag(nw, t);
            wt2.lemma = wt;
            return wt2;
        }
        return null;
    }

    void generateInflections(boolean onlyUnknownWordTags) {
        Message.invoke(MessageType.MSG_STATUS, "generating inflections...");
        for (int i = 0; i < CW; i++) {
            Word w = wordL.get(i);
            //    Word* w = &words[i];
            for (WordTag wt = w; wt != null; wt = wt.next())
                if (!wt.getTag().isProperNoun())
                    for (int j = 0; j < wt.nInflectRules(); j++) {
                        int ruleIndex = wt.inflectRule(j);
                        boolean found = false;
                        int nOK = 0;
                        for (int k = 0; k < tags.getCT(); k++) {
                            String string = inflects.getInflectedForm(w.getString(), ruleIndex, tags.tagL.get(k).getIndex());
                            if (string != null) {
                                found = true;
                                if (!onlyUnknownWordTags)
                                    System.out.print(w + "\t" + string + "\t" + tags.tagL.get(k));
                                Word w3;
                                if ((w3 = get(string)) != null) {
                                    WordTag wt3 = w3.getWordTag(tags.tagL.get(k));
                                    if (wt3 != null) {
                                        int m;
                                        for (m = 0; m < wt3.nLemmas(); m++)
                                            if (wt3.lemma(m).getWord() == w) {
                                                nOK++;
                                                if (!onlyUnknownWordTags)
                                                    System.out.print("\t*");
                                                break;
                                            }
                                        if (m >= wt3.nLemmas()) {
                                            if (!onlyUnknownWordTags)
                                                System.out.print("\t& " + wt3.lemma(0));
                                        }
                                    } else {
                                        WordTag u;
                                        for (u = w3; u != null; u = u.next())
                                            if (u.getTag().originalTag() == tags.tagL.get(k)) {
                                                nOK++;
                                                if (!onlyUnknownWordTags)
                                                    System.out.print("\t*");
                                                break;
                                            }
                                        if (u == null) {
                                            if (onlyUnknownWordTags)
                                                System.out.println(string + "\t" + tags.tagL.get(k) + "\t" + w + "\t" + "GI");
                                            else {
                                                System.out.print("\t~");
                                                for (u = w3; u != null; u = u.next())
                                                    System.out.print(u.getTag() + "\t");
                                            }
                                        }
                                    }
                                }
                                if (!onlyUnknownWordTags)
                                    System.out.println();
                            }
                        }
                        if (!onlyUnknownWordTags) {
                            if (nOK < 2)
                                System.out.println(w + "\t§");
                            if (!found)
                                System.out.println(w + " no inflections found");
                        }
                    }
        }
    }

    void testInflections() throws IOException {
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        Settings.xPrintWordInfo = true;
        boolean showApplicable = false;
        String string;
        System.out.print("\nenter word (or '-' to toggle show applicable rules):");
        while ((string = br.readLine()) != null) {
            if (string.charAt(0) == '-') {
                showApplicable = !showApplicable;
                continue;
            }
            analyzeWordAndPrintInflections(string);
            if (showApplicable) {
                System.out.println("\tall applicable rules:");
                String prevName = "";
                for (int i = 0; i < inflects.getNRules(); i++) {
                    InflectRule r = inflects.rules[i];
                    for (int k = 0; k < tags.getCT(); k++)
                        if (r.name().compareTo(prevName) != 0)
                            if (r.IsApplicable(string, tags.tagL.get(k).getIndex())) {
                                prevName = r.name();
                                System.out.print(r.name() + " (" + i + ')');
                                for (int j = 1; j < r.getNForms(); j++) {
                                    String s = r.apply(string, j);
                                    if (s != null)
                                        System.out.print(" " + s);
                                }
                                System.out.println();
                            }
                }
            }
            System.out.print("\nenter word (or '-' to toggle show applicable rules):");
        }
    }

    void analyzeWordAndPrintInflections(String string) throws IOException {
        Word w = get(string);
        if (w != null) {
            System.out.println("\t\"all forms found in lexicon:");
            for (WordTag wt = w; wt != null; wt = wt.next())
                wt.print(System.out);
        } else {
            System.out.println(string + " is not in main lexicon, analyzing...");
            NewWord nw = newWords.addWord(string, null);
            analyzeNewWord(nw, true);
            nw.print(System.out);
            for (WordTag wt = nw; wt != null; wt = wt.next())
                if (wt.nLemmas() == 0) {
                    wt.print(System.out);
                    System.out.println();
                }
            w = nw;
        }
        for (WordTag wt = w; wt != null; wt = wt.next()) {
            System.out.println("\tall forms that can be generated from " + wt + ":");
            for (int k = 0; k < wt.nLemmas(); k++)
                for (int j = 0; j < wt.lemma(k).nInflectRules(); j++)
                    for (int i = 0; i < tags.getCT(); i++) {
                        WordTag wt2 = wt.getForm(tags.tagL.get(i), k, j);
                        if (wt2 != null) {
                            System.out.print(wt2);
                            if (wt2.getWord().isNewWord())
                                System.out.print(" *");
                            System.out.println();
                        }
                    }
        }
    }

    String getInflectionList(String string) {
        Word w = get(string);
        if (w == null) {
            NewWord nw = newWords.addWord(string, null);
            analyzeNewWord(nw, true);
            w = nw;
        }
        String result = "";
        String res;
        for (WordTag wt = w; wt != null; wt = wt.next()) {
            for (int k = 0; k < wt.nLemmas(); k++) {
                for (int j = 0; j < wt.lemma(k).nInflectRules(); j++) {
                    for (int i = 0; i < tags.getCT(); i++) {
                        WordTag wt2 = wt.getForm(tags.tagL.get(i), k, j);
                        if (wt2 != null) {
                            res = wt2.String();
                            res += " ";
                            if (!result.contains(res))
                                result += res;
                        }
                    }
                }
            }
        }
        return result;
    }
}
