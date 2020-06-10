package tagger;

import common.Ensure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InflectRule implements Serializable {
    public static List<String> strings;
    static int MAX_INFLECTION_RULES = 450;
    static int MAX_INFLECTION_FORMS = 22;
    static int MAX_INFLECTION_ENDINGS = 10;
    static int INFLECT_NO_RULE = 511;
    static int INFLECTION_FORM_NONE = Short.MAX_VALUE;
    static int MAX_INFLECTION_RULES_PER_WORDTAG = 3; //cannot exceed 4 without more bits for nExtraInflectRules in Word
    short nameIndex;
    short[] endingIndex = new short[MAX_INFLECTION_ENDINGS];
    int[] tagIndex = new int[MAX_INFLECTION_FORMS];
    short[] formIndex = new short[MAX_INFLECTION_FORMS];
    int nCharsToRemove;
    int nEndings;
    int nForms;
    boolean used;

    InflectRule() {
        used = false;
        strings = new ArrayList<>();
    }

    String name() {
        return strings.get(nameIndex);
    }

    String ending(int n) {
        return strings.get(endingIndex[n]);
    }

    String form(int f) {
        return strings.get(f);
    }

    int getNCharsToRemove() {
        return nCharsToRemove;
    }

    int tagIndex(int n) {
        return tagIndex[n];
    }

    int getNForms() {
        return nForms;
    }

    String apply(String string, int form) {
        if (form == 0)
            return string;
        Ensure.ensure(form < nForms);
        int q = formIndex[form - 1];
        if (q == INFLECTION_FORM_NONE)
            return null;
        int stemLength = Math.max(0, string.length() - nCharsToRemove);
        String s = string.substring(0, stemLength);
        s += form(q);
        return s;
    }

    boolean IsApplicable(String string) {
        for (int i = 0; i < nEndings; i++) {
            int n = ending(i).length();
            String ending, stringEnd;
            if (ending(i).charAt(0) == '^') {
                ending = ending(i) + 1;
                stringEnd = string;
            } else {
                ending = ending(i);
                int index = string.length() - n;
                if (index < 0)
                    index = 0;
                stringEnd = string.substring(index);
            }
            int bound = Math.min(stringEnd.length(), n);
            if (stringEnd.substring(0, bound).compareTo(ending) == 0 ||
                    (ending.charAt(0) == 'V' &&
                            Letter.isVowel(stringEnd.charAt(0)) &&
                            stringEnd.substring(1, bound).compareTo(ending.substring(1)) == 0) ||
                    (ending.charAt(0) == 'C' &&
                            Letter.isConsonant(stringEnd.charAt(0)) &&
                            stringEnd.substring(1, bound).compareTo(ending.substring(1)) == 0))
                return true;
        }
        return false;
    }

    boolean IsApplicable(String string, int baseFormTagIndex) {
        return (baseFormTagIndex == tagIndex[0] || baseFormTagIndex == Tag.TAG_INDEX_NONE)
                && IsApplicable(string);
    }

    public String toString() {
        return name();
    }
/*
    inline std::ostream&operator<<(std::ostream&os,InflectRule&r)

    {
        os << r.Name();
        return os;
    }

    inline std::ostream&operator<<(std::ostream&os,InflectRule*r)

    {
        if (r) os <<*r;else os << "(null InflectRule)";
        return os;
    }
 */
}