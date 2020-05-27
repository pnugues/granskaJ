package tagger;

import common.Ensure;
import common.File;
import common.Message;
import common.MessageType;

import java.io.IOException;
import java.nio.file.Paths;

public class Lexicon {
    WordLexicon words;
    MorfLexicon morfs;
    TagLexicon tags;
    NewWordLexicon newWords;
    String lexiconDir;

    //HashTable<WordRuleTerms> wordRuleTerms;

    public Lexicon() {
        /*wordRuleTerms = new HashTable<WordRuleTerms>(KeyWordRuleTerms, CompareWordRuleTerms,
                null, null);*/
        tags = new TagLexicon();
        morfs = new MorfLexicon();
        words = new WordLexicon();
        newWords = new NewWordLexicon();
    }

    public boolean loadTags(String dir) throws IOException, ClassNotFoundException {
        Message.invoke(MessageType.MSG_STATUS, "loading tags...");
        Ensure.ensure(tags.isLoaded() == false);
        String wDir = "";
        wDir = Paths.get(dir, "tags").toString();
        if (Settings.xNofast) {
            tags.loadSlow(wDir);
        } else if (!tags.loadFast(wDir, false)) {
            tags.loadSlow(wDir);
            tags.save();
        }
        return TagLexicon.isLoaded();
    }

    public String lexiconDir() {
        return lexiconDir;
    }

    public boolean isLoaded() {
        return tags.isLoaded() && words.isLoaded() && morfs.isLoaded();
    }

    public WordLexicon getWords() {
        return words;
    }

    public TagLexicon getTags() {
        return tags;
    }

    /*NewWord AddNewWord(String s) {
        return newWords.AddWord(s);
    }*/

    public NewWordLexicon getNewWords() {
        return newWords;
    }

    public MorfLexicon getMorfs() {
        return morfs;
    }


    /*WordRuleTerms FindWordRuleTerms(Word w) {
        if (!w.IsRuleAnchor())
            return null;
        WordRuleTerms wrt = new WordRuleTerms((Word) w);
        return wordRuleTerms.Find(wrt);
    }*/

    /*boolean AddWordRuleTerm(Word w, RuleTerm r) {
        boolean check = true;
        WordRuleTerms wrt = (WordRuleTerms) FindWordRuleTerms(w);
        //  std::cout << "add: " << w << ' ' << r << ' ' << wrt << std::endl;
        if (wrt) {
            for (RuleTermList l = wrt; l; l = l.Next()) {
                if (l.GetRuleTerm() == r) {
                    Message.invoke(MessageType.MSG_WARNING,
                            "AddWordRuleTerm() detected something Viggo promised would never happen, word = ",
                            w.getString());
                    check = false;
                    return false;
                }
                Ensure.ensure(l.Next() != wrt);
            }
            wrt.AddRuleTerm(r);
            return check;
        }
        wrt = new WordRuleTerms(w, r); // new OK
        wordRuleTerms.Insert(wrt);
        return check;
    }*/

    void setWordProbs(Tagger tagger) {
        for (int i = 0; i < words.getCW(); i++) {
            if (words.wordL.get(i).hasExtraWordTag())
                tagger.tagUnknownWord(words.wordL.get(i), true, false, null);
            words.wordL.get(i).computeLexProbs();
        }
    }

    void setMorfProbs() {
        float[] prevAlpha = {0, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        Ensure.ensure(morfs.isLoaded());
        int i;
        for (i = 0; i < 10; i++)
            if (prevAlpha[i] != Settings.xAlphaLastChar[i])
                break;
        if (i != 10)
            for (i = 0; i < morfs.getCW(); i++) {
                int len = morfs.morfL.get(i).length();
                if (prevAlpha[len] != Settings.xAlphaLastChar[len]) {
                    for (WordTag w = morfs.get(morfs.morfL.get(i)); w != null; w = w.next()) {
                        w.lexProb = Settings.xAlphaLastChar[len] * w.getTagFreq() / morfs.get(morfs.morfL.get(i)).freq;
                        Ensure.ensure(w.lexProb > 0);
                    }
                }
            }
        for (i = 0; i < 10; i++)
            if (i < Settings.xAlphaLastChar.length) {
                prevAlpha[i] = Settings.xAlphaLastChar[i];
            } else {
                prevAlpha[i] = 0.0f;
            }
    }

    void loadWords(String dir, Tagger tagger) throws IOException, ClassNotFoundException {
        if (Settings.xNofast) {
            words.loadSlow(dir, tags, newWords);
            setWordProbs(tagger);
            return;
        }
        boolean fastOK = words.loadFast(dir, tags, newWords, false);
        if (!fastOK) {
            words.loadSlow(dir, tags, newWords);
            setWordProbs(tagger);
        }
        if (!fastOK)
            words.save();
    }

    void loadMorfs(String dir) throws IOException, ClassNotFoundException {
        if (Settings.xNofast) {
            morfs.loadSlow(dir, tags);
            setMorfProbs();
            return;
        }
        boolean fastOK = morfs.loadFast(dir, false);
        if (!fastOK) {
            morfs.loadSlow(dir, tags);
            setMorfProbs();
        }
        if (!fastOK)
            morfs.save();
    }

    public boolean loadWordsAndMorfs(Tagger tagger, String dir) throws IOException, ClassNotFoundException {
        Ensure.ensure(tags.isLoaded());
        Ensure.ensure(!morfs.isLoaded());
        Ensure.ensure(!words.isLoaded());
        lexiconDir = dir;
        Message.invoke(MessageType.MSG_STATUS, "loading morfs...");
        loadMorfs(File.addFileName(lexiconDir, "morfs"));
        if (!morfs.isLoaded())
            Message.invoke(MessageType.MSG_ERROR, "cannot load morf lexicon");
        Message.invoke(MessageType.MSG_STATUS, "loading words...");
        loadWords(File.addFileName(lexiconDir, "words"), tagger);
        if (!words.isLoaded())
            Message.invoke(MessageType.MSG_ERROR, "cannot load word lexicon");
        return true;
    }

    Word findMainWord(String s) {
        //char[] s2 = new char[Word.MAX_WORD_LENGTH];
        String s2 = s;
        s2 = s2.toLowerCase();
        return words.get(s2);
    }

    Word findMainOrNewWordNoCaps(String s) {
        Word w = words.get(s);
        return w != null ? w : newWords.get(s);
    }

    Word findMainOrNewWord(String s) {
        Word w = findMainOrNewWordNoCaps(s);
        if (w != null) return w;
        //char[] s2 = new char[Word.MAX_WORD_LENGTH];
        String s2 = s;
        s2 = s2.toLowerCase();
        return findMainOrNewWordNoCaps(s2);
    }

    Word findMainOrNewWordAndAddIfNotPresent(String s) {
        Word w = findMainOrNewWordNoCaps(s);
        if (w != null) return w;
        //char[] s2 = new char[Word.MAX_WORD_LENGTH];
        String s2 = s.toLowerCase();
        w = findMainOrNewWordNoCaps(s2);
        if (w != null) return w;
        NewWord nw = newWords.addWord(s2, null);
        getWords().analyzeNewWord(nw, false);
        return nw;
    }

}
