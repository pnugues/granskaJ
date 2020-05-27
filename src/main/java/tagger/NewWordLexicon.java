package tagger;

import common.Ensure;
import common.Message;
import common.MessageType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class NewWordLexicon extends LinkedHashMap<String, NewWord> {
    public List<String> newWordL;
    //public Map<String, NewWord> newWordMap;

    public NewWordLexicon() {
        //PN check this below
        //Init(KeyNewWord, CompareNewWords, KeyWordString, CompareStringAndNewWord);
        newWordL = new ArrayList<>();
    }

    public NewWord addWord(String s, Tag t) {
        //  if (Find(s))
        //   Message(MSG_WARNING, "adding word twice", s);
        NewWord w = new NewWord(s); // new OK
        put(s, w);
        if (t != null)
            addWordTagUnsafe(w, t);
        else
            Ensure.ensure(w.tagIndex == Tag.TAG_INDEX_NONE);
        return w;
    }

    public WordTag addWordTag(NewWord w, Tag tag) {
        WordTag wt = w.getWordTag(tag);
        if (wt != null) return wt;
        return addWordTagUnsafe(w, tag);
    }

    public WordTag addWordTagUnsafe(NewWord w, Tag tag) {
        if (!tag.isContent()) {
            Message.invoke(MessageType.MSG_MINOR_WARNING, "adding non-content tag", tag.getString(),
                    "to new-word", w.getString());
        }
        WordTag wt;
        if (w.tagIndex == Tag.TAG_INDEX_NONE)
            wt = w;
        else {
            wt = new WordTag(); // new OK
            wt.init(w.next, false);
            w.next = wt;
        }
        wt.tagIndex = (char) tag.getIndex();
        return wt;
    }

    public void Reset() {
        Message.invoke(MessageType.MSG_STATUS, "resetting newwordlexicon...");
        newWordL = new ArrayList<>();
        clear();
        NewWord.resetStrings();
    }
}
