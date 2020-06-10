package tagger;

import java.io.Serializable;

public class ExtraRules implements Serializable {
    public WordTag wt;
    public short[] rule = new short[InflectRule.MAX_INFLECTION_RULES_PER_WORDTAG - 1];

    public ExtraRules() {
        wt = null;
        for (int i = 0; i < rule.length; i++)
            rule[i] = (short) InflectRule.INFLECT_NO_RULE;
    }
}