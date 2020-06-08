package tagger;

import common.Ensure;
import common.Message;
import common.MessageType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class InflectLexicon implements Serializable {
    boolean isLoaded;
    String lexiconDir;
    InflectRule[] rules = new InflectRule[InflectRule.MAX_INFLECTION_RULES];
    int ruleStringLen;
    int nRules;

    InflectLexicon() {
        isLoaded = false;
        ruleStringLen = 0;
        for (int i = 0; i < rules.length; i++) {
            rules[i] = new InflectRule();
        }
    }

    boolean isLoaded() {
        return isLoaded;
    }

    int getNRules() {
        return nRules;
    }

    InflectRule rule(int n) {
        return rules[n];
    }

    int findRuleIndex(String string, String rule, char baseFormTagIndex) {
        for (int i = 0; i < nRules; i++)
            if (rule.compareTo(rules[i].name()) == 0 && rules[i].IsApplicable(string, baseFormTagIndex)) {
                rules[i].used = true;
                return i;
            }
        return InflectRule.INFLECT_NO_RULE;
    }

    String getInflectedForm(String string, int ruleIndex, int tagIndex) {
        InflectRule rule = rules[ruleIndex];
        for (int k = 0; k < rule.getNForms(); k++)
            if (rule.tagIndex[k] == tagIndex)
                return rule.apply(string, k);
        return null;
    }

    short addString(String s) {
        // Returns the index of the rule and not the index of the character
        for (short i = 0; i < InflectRule.strings.size(); i++)
            if (InflectRule.strings.get(i).compareTo(s) == 0)
                return i;
        int len = s.length() + 1;
        Ensure.ensure(ruleStringLen + len < Short.MAX_VALUE);
        InflectRule.strings.add(s);
        //PN. The next instruction is useless
        ruleStringLen += len;
        return (short) (InflectRule.strings.size() - 1);
    }

    void loadSlow(String dir, TagLexicon tags) throws IOException {
        // This is the new function with JSON data structured
        // as lists. It should be closer to the TSV data
        lexiconDir = dir;
        Message.invoke(MessageType.MSG_STATUS, "loading inflect lexicon slow...");

        String inflectJSONString = Files.readString(Paths.get(lexiconDir + "/inflection.rules.json"));
        JSONObject inflectJSON = new JSONObject(inflectJSONString);
        JSONArray inflect = inflectJSON.getJSONArray("inflection.rules");

        nRules = 0;
        for (int i = 0; i < inflect.length(); i++) {
            InflectRule r = rules[nRules];
            JSONObject infl_rules = (JSONObject) inflect.get(i);
            JSONArray tagStrings = infl_rules.getJSONArray("feat_infl");
            boolean ruleBase = true;

            int j;
            Tag t = new Tag();
            for (j = 0; j < tagStrings.length(); j++) {
                t.string = ((String) tagStrings.get(j));
                if (t.string.compareTo("-") != 0) {
                    Tag t2 = tags.get(t.string);
                    if (t2 == null) {
                        Message.invoke(MessageType.MSG_WARNING, "unknown tag in inflection.rules:",
                                t.string);//jonas 	    Message(MSG_ERROR, "unknown tag in inflection.rules:", t.String());
                    } else {
                        r.tagIndex[j] = t2.getIndex();
                        if (j == 0) {
                            t2.ruleBase = true;
                        }
                    }
                } else {
                    r.tagIndex[j] = Tag.TAG_INDEX_NONE;
                }
            }
            for (; j < InflectRule.MAX_INFLECTION_FORMS; j++) {
                r.tagIndex[j] = Tag.TAG_INDEX_NONE;
            }
            nRules++;
            JSONArray paradigms = infl_rules.getJSONArray("paradigm");
            for (int l = 0; l < paradigms.length(); l++) {
                //nRules++;
                JSONArray paradigm = (JSONArray) paradigms.get(l);
                r = rules[nRules];
                if (nRules != 0) {
                    for (j = 0; j < InflectRule.MAX_INFLECTION_FORMS; j++) {
                        r.tagIndex[j] = rules[nRules - 1].tagIndex[j];
                    }
                }
                r.nameIndex = addString((String) paradigm.get(0));
                String suffixes = (String) paradigm.get(1);
                List<String> infl_suffixes = new ArrayList<>();
                for (int m = 2; m < paradigm.length(); m++) {
                    infl_suffixes.add((String) paradigm.get(m));
                }
                r.nEndings = 0;
                String[] ss = suffixes.split(" *, *");
                for (int m = 0; m < ss.length; m++) {
                    if (r.nEndings >= InflectRule.MAX_INFLECTION_ENDINGS)
                        Message.invoke(MessageType.MSG_ERROR, "too many ending-alternatives for rule", r.name());
                    r.endingIndex[r.nEndings++] = addString(ss[m]);
                }
                if (infl_suffixes.get(0).startsWith("=")) {
                    r.nCharsToRemove = 0;
                } else {
                    r.nCharsToRemove = infl_suffixes.get(0).length();
                }
                for (r.nForms = 0; r.nForms < infl_suffixes.size() - 1; r.nForms++) {
                    String current_suffix = infl_suffixes.get(r.nForms + 1);
                    r.formIndex[r.nForms] = addString(current_suffix);
                    Ensure.ensure(r.nForms < InflectRule.MAX_INFLECTION_FORMS);
                    if (current_suffix.equals("-")) {
                        r.formIndex[r.nForms] = (short) InflectRule.INFLECTION_FORM_NONE;
                    } else {
                        if (infl_suffixes.get(r.nForms + 1).equals("=")) {
                            current_suffix = "";
                        }
                        r.formIndex[r.nForms] = addString(current_suffix);
                    }
                }
                for (int k = rules[nRules].nForms; k < InflectRule.MAX_INFLECTION_FORMS; k++)
                    r.formIndex[k] = (short) InflectRule.INFLECTION_FORM_NONE;
                r.nForms++;
                nRules++;
            }
        }
        isLoaded = true;
    }
}
