package tagger;

import common.Ensure;
import common.Message;
import common.MessageType;
import common.Timer;
import token.Tokenizer;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

/*
@Author: Pierre Nugues
Translated and adapted to Java
from a program by Johan Carlberger
*/

public class Tagger extends Lexicon {

    static int TOKEN_BUF_CHUNK = 13000;
    Tokenizer tokenizer;
    Text theText = new Text();

    TrigramGadget[] gadget = new TrigramGadget[AbstractSentence.MAX_SENTENCE_LENGTH];
    InputStream inputStream;
    Tag[] contentTags = new Tag[Tag.MAX_TAGS];
    int CT_CONTENT;
    boolean xSetNewWordTagFreqs;
    WordToken[] specialWordToken = new WordToken[Tokenizer.N_TOKENS];
    int input_size; // jonas
    Word[] specialWord = new Word[Tokenizer.N_TOKENS];
    WordToken[] theTokens;
    int tokensBufSize;
    int nTokens;
    //std::ostringstream theOriginalText; //Oscar
    // PN/MK Line above replaced by this:
    StringWriter theOriginalText = new StringWriter();

    long loadTime;
    long tokenizeTime;
    long sentenceTime;
    long tagTime;
    long analyzeTime;

    Tagger() {
        theTokens = null;
        tokensBufSize = 0;
        //theOriginalText() ;
        //  std::cout << "Word size: " << sizeof(Word) << std::endl;
        //  std::cout << "NewWord size: " << sizeof(NewWord) << std::endl;
        Ensure.ensure(Settings.xMinLastChars >= MorfLexicon.MIN_LAST_CHARS);
        Ensure.ensure(Settings.xMaxLastChars <= MorfLexicon.MAX_LAST_CHARS);
        Ensure.ensure(Settings.xCompoundMinLength == Settings.xCompoundPrefixMinLength + Settings.xCompoundSuffixMinLength);
        if (Settings.xNNewWordVersions > Tag.MAX_WORD_VERSIONS)
            Message.invoke(MessageType.MSG_ERROR, "xNNewWordVersions cannot exceed MAX_WORD_VERSIONS, program must be recompiled");
        if (Settings.xNWordVersions > Tag.MAX_WORD_VERSIONS)
            Message.invoke(MessageType.MSG_ERROR, "xNWordVersions cannot exceed MAX_WORD_VERSIONS, program must be recompiled");
        if (Settings.xTaggingEquation < 19 || Settings.xTaggingEquation > 21)
            Message.invoke(MessageType.MSG_ERROR, "unknown tagging equation selected, program must be recompiled");
        xSetNewWordTagFreqs = true;
        AbstractSentence.tagger = this;
        nTokens = 0;
        for (int i = 0; i < gadget.length; i++) {
            gadget[i] = new TrigramGadget();
        }
        for (int i = 0; i < specialWordToken.length; i++) {
            specialWordToken[i] = new WordToken();
        }
        for (int i = 0; i < specialWord.length; i++) {
            specialWord[i] = new Word();
        }
    }

    void tagSentence(AbstractSentence s) {
        tagSentenceInterval(s, 0, s.getNTokens() - 1);
    }

    void reset() {
        Message.invoke(MessageType.MSG_STATUS, "resetting tagger...");
        theText.reset();
        WordToken.reset();
        for (int i = 0; i < nTokens; i++)
            theTokens[i].clear();
        nTokens = 0;
        getNewWords().Reset();
    }

    public boolean load(String dir) throws IOException, ClassNotFoundException {
        long timer = 0;
        if (Settings.xTakeTime)
            timer = System.currentTimeMillis();
        if (super.loadTags(dir)) {
            CT_CONTENT = 0;
            for (int i = 0; i < getTags().getCT(); i++)
                if (getTags().tagL.get(i).isContent()) {
                    contentTags[CT_CONTENT] = (Tag) getTags().tagL.get(i);
                    CT_CONTENT++;
                }
        } else {
            Message.invoke(MessageType.MSG_ERROR, "tag lexicon not loaded");
            return false;
        }
        if (super.loadWordsAndMorfs(this, dir)) {
            int i;
            for (i = 0; i < Tokenizer.N_TOKENS; i++) {
                specialWord[i] = null;
                specialWordToken[i].word = null;
            }
            Word w = getWords().get("$.");
            Ensure.ensure(w != null);
            specialWord[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()] = w;
            specialWordToken[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()].word = w;
            specialWordToken[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()].string = "$.";
            specialWordToken[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()].setSelectedTag(w.getTag(), false);
            specialWordToken[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()].trailingSpace = true;
            w = getWords().get("$!");
            Ensure.ensure(w != null);
            specialWord[Yytoken.TOKEN_DELIMITER_EXCLAMATION.ordinal()] = w;
            specialWordToken[Yytoken.TOKEN_DELIMITER_EXCLAMATION.ordinal()].word = w;
            specialWordToken[Yytoken.TOKEN_DELIMITER_EXCLAMATION.ordinal()].string = "$!";
            specialWordToken[Yytoken.TOKEN_DELIMITER_EXCLAMATION.ordinal()].setSelectedTag(w.getTag(), false);
            specialWordToken[Yytoken.TOKEN_DELIMITER_EXCLAMATION.ordinal()].trailingSpace = true;
            w = getWords().get("$?");
            Ensure.ensure(w != null);
            specialWord[Yytoken.TOKEN_DELIMITER_QUESTION.ordinal()] = w;
            specialWordToken[Yytoken.TOKEN_DELIMITER_QUESTION.ordinal()].word = w;
            specialWordToken[Yytoken.TOKEN_DELIMITER_QUESTION.ordinal()].string = "$?";
            specialWordToken[Yytoken.TOKEN_DELIMITER_QUESTION.ordinal()].setSelectedTag(w.getTag(), false);
            specialWordToken[Yytoken.TOKEN_DELIMITER_QUESTION.ordinal()].trailingSpace = true;
            w = getWords().get("$h");
            Ensure.ensure(w != null);
            specialWord[Yytoken.TOKEN_DELIMITER_HEADING.ordinal()] = w;
            specialWordToken[Yytoken.TOKEN_DELIMITER_HEADING.ordinal()].word = w;
            specialWordToken[Yytoken.TOKEN_DELIMITER_HEADING.ordinal()].string = "$h";
            specialWordToken[Yytoken.TOKEN_DELIMITER_HEADING.ordinal()].setSelectedTag(w.getTag(), false);
            specialWordToken[Yytoken.TOKEN_DELIMITER_HEADING.ordinal()].trailingSpace = true;
            w = getWords().get("$*");
            Ensure.ensure(w != null);
            specialWord[Yytoken.TOKEN_DELIMITER_OTHER.ordinal()] = w;
            specialWordToken[Yytoken.TOKEN_DELIMITER_OTHER.ordinal()].word = w;
            specialWordToken[Yytoken.TOKEN_DELIMITER_OTHER.ordinal()].string = "$*";
            specialWordToken[Yytoken.TOKEN_DELIMITER_OTHER.ordinal()].setSelectedTag(w.getTag(), false);
            specialWordToken[Yytoken.TOKEN_DELIMITER_OTHER.ordinal()].trailingSpace = true;
            specialWord[Yytoken.TOKEN_CARDINAL_SIN.ordinal()] = getWords().get("1");
            Ensure.ensure(specialWord[Yytoken.TOKEN_CARDINAL_SIN.ordinal()] != null);
            specialWord[Yytoken.TOKEN_CARDINAL.ordinal()] = specialWord[Yytoken.TOKEN_MATH.ordinal()] = specialWord[Yytoken.TOKEN_E_MAIL.ordinal()] =
                    specialWord[Yytoken.TOKEN_URL.ordinal()] = getWords().get("5");
            Ensure.ensure(specialWord[Yytoken.TOKEN_CARDINAL.ordinal()] != null);
            specialWord[Yytoken.TOKEN_BAD_CARDINAL.ordinal()] = getWords().get("4711");
            Ensure.ensure(specialWord[Yytoken.TOKEN_BAD_CARDINAL.ordinal()] != null);
            specialWord[Yytoken.TOKEN_ORDINAL.ordinal()] = getWords().get("femte");
            Ensure.ensure(specialWord[Yytoken.TOKEN_ORDINAL.ordinal()] != null);
            specialWord[Yytoken.TOKEN_YEAR.ordinal()] = specialWord[Yytoken.TOKEN_DATE.ordinal()] =
                    specialWord[Yytoken.TOKEN_TIME.ordinal()] = getWords().get("1998");
            Ensure.ensure(specialWord[Yytoken.TOKEN_YEAR.ordinal()] != null);
            specialWord[Yytoken.TOKEN_PARAGRAPH.ordinal()] = getWords().get("7 §");
            Ensure.ensure(specialWord[Yytoken.TOKEN_PARAGRAPH.ordinal()] != null);
            specialWord[Yytoken.TOKEN_PERIOD.ordinal()] = getWords().get(".");
            Ensure.ensure(specialWord[Yytoken.TOKEN_PERIOD.ordinal()] != null);
            specialWord[Yytoken.TOKEN_QUESTION_MARK.ordinal()] = getWords().get("?");
            Ensure.ensure(specialWord[Yytoken.TOKEN_QUESTION_MARK.ordinal()] != null);
            specialWord[Yytoken.TOKEN_EXCLAMATION_MARK.ordinal()] = getWords().get("!");
            Ensure.ensure(specialWord[Yytoken.TOKEN_EXCLAMATION_MARK.ordinal()] != null);
            specialWord[Yytoken.TOKEN_LEFT_PAR.ordinal()] = getWords().get("(");
            Ensure.ensure(specialWord[Yytoken.TOKEN_LEFT_PAR.ordinal()] != null);
            specialWord[Yytoken.TOKEN_RIGHT_PAR.ordinal()] = getWords().get(")");
            Ensure.ensure(specialWord[Yytoken.TOKEN_RIGHT_PAR.ordinal()] != null);
            specialWord[Yytoken.TOKEN_CITATION.ordinal()] = getWords().get("\"");
            Ensure.ensure(specialWord[Yytoken.TOKEN_CITATION.ordinal()] != null);
            specialWord[Yytoken.TOKEN_PERCENTAGE.ordinal()] = getWords().get("17 %");
            Ensure.ensure(specialWord[Yytoken.TOKEN_PERCENTAGE.ordinal()] != null);
            //    for (i=0; i<N_TOKENS; i++)
            //      if (!specialWord[i])
            //	std::cout << "no word for " << (Token)i << std::endl;
            gadget[0].setTags(specialWord[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()]);
            gadget[1].setTags(specialWord[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()]);
            if (Settings.xTakeTime)
                loadTime = System.currentTimeMillis() - timer;
            Message.invoke(MessageType.MSG_STATUS, "lexicon loaded successfully");
            return true;
        } else {
            Message.invoke(MessageType.MSG_ERROR, "lexicon not loaded");
            return false;
        }
    }

    void tagUnknownWord(Word w, boolean normalize, boolean suffixCheck, WordToken token) {
        // computes lexProbs for all content tags based on the morphology of w
        // if token not null capped-weighths are used
        Tag.probsWord = w;
        int i;
        int len = w.getStringLen();
        if (Settings.xNewWordsMemberTaggingOnly) {
            for (i = 0; i < CT_CONTENT; i++)
                contentTags[i].lexProb = contentTags[i].ctm_cwt;
        } else {
            for (i = 0; i < CT_CONTENT; i++)
                contentTags[i].lexProb = (float) 1e-10;
            if (Settings.xMinLastChars < Settings.xMaxLastChars) {
                if (len > MorfLexicon.MIN_PREFIX_LENGTH) {
                    int max = (len < (Settings.xMaxLastChars + MorfLexicon.MIN_PREFIX_LENGTH)) ?
                            len - MorfLexicon.MIN_PREFIX_LENGTH : Settings.xMaxLastChars;
                    int inx = len;
                    String string = w.getString().substring(inx, len);
                    for (int j = Settings.xMinLastChars; j <= max; j++) {
                        for (WordTag q = getMorfs().get(string); q != null; q = q.next()) {
                            Tag t = q.getTag();
                            t.lexProb += q.getLexProb();
                        }
                        inx--;
                        string = w.getString().substring(inx, len);
                    }
                }
            }
            if (suffixCheck) {
                NewWord nw = w.isNewWord() ? (NewWord) w : null;
                Ensure.ensure(nw != null);
                // added 2001-06-25:
                for (WordTag wt = w; wt != null; wt = wt.next()) {
                    if (wt.getTagIndex() != Tag.TAG_INDEX_NONE) {
                        wt.getTag().lexProb *= Settings.xAlphaSuffix;
                        if (Settings.xPrintLexicalProbs) {
                            System.out.println(w + ": Derived or Compound: " + wt.getTag() + " factored by " + Settings.xAlphaSuffix);
                        }
                    }
                }
            /* removed 2001-06-35
               Words().CompoundAnalyze(nw);
               for (int i=0; i<nw.NSuffixes(); i++) {
               WordTag *wt = nw.Suffix(i);
               wt.GetTag().lexProb += xAlphaSuffix*wt.LexProb()*wt.GetTag().CompoundProb();
               }
            */
                // terrible ad-hoc fix for words like 10b to avoid false scrutinizing alarms:
                if (nw.nSuffixes() == 0 && Letter.containsDigit(w.getString()) &&
                        !w.getString().contains("-")) {
                    getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN).lexProb *= 1000;
                    if (Settings.xPrintLexicalProbs)
                        System.out.println(w + ": ContainsDigit, prob of " +
                                getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN)
                                + " factored by 1000");
                }
            }
        }
        if (token != null) {
            if (Settings.xMorfCapital)
                if (token.isFirstCapped() && !token.isFirstInSentence()) {
                    getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN).lexProb *= Settings.xAlphaUnknownCapital;
                    getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN_GENITIVE).lexProb *= Settings.xAlphaUnknownCapital;
                    if (Settings.xPrintLexicalProbs) {
                        System.out.print(w + ": FirstCapped, pm-tags ");
                        System.out.print(getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN) + " and ");
                        System.out.print(getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN_GENITIVE));
                        System.out.println(" factored by " + Settings.xAlphaUnknownCapital);
                    }
                } else if (token.isAllCapped() && len < 5) {
                    getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN).lexProb *= 5000 * Settings.xAlphaUnknownCapital;
                    getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN_GENITIVE).lexProb *= 5000 * Settings.xAlphaUnknownCapital;
                    if (Settings.xPrintLexicalProbs) {
                        System.out.println(w + ": AllCapped, len<5, pm-tags factored by " + Settings.xAlphaUnknownCapital);
                    }
                }
            if (Settings.xMorfNonCapital)
                if (!token.isFirstCapped()) {
                    getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN).lexProb *= Settings.xAlphaUnknownNonCapital;
                    getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN_GENITIVE).lexProb *= Settings.xAlphaUnknownNonCapital;
                    if (Settings.xPrintLexicalProbs) {
                        System.out.println(w + ": NOT FirstCapped, pm-tags factored by " + Settings.xAlphaUnknownNonCapital);
                    }
                }
        }
        if (normalize) {
            float sum = 0;
            for (i = 0; i < CT_CONTENT; i++) {
                if (contentTags[i].lexProb <= 0)
                    Message.invoke(MessageType.MSG_ERROR, "negative prob for", w.getString());
                else
                    sum += contentTags[i].lexProb;
            }
            for (i = 0; i < CT_CONTENT; i++)
                contentTags[i].lexProb /= sum;
        }
    }

    //PN. Not use removed
    //float tagLexProb(TagPointer t) {
    //    return t.LexProb();
    //}

    void addTagsToUnknownWord(NewWord w) {
        // SelectBest((TagPointer*) contentTags, CT_CONTENT, (TagPointer*) g.tag,
        // g.n, tagLexProb);
        Tag[] best = new Tag[Tag.MAX_WORD_VERSIONS];
        int i, worst = -1;
        for (i = 0; i < Settings.xNNewWordVersions; i++)
            best[i] = contentTags[i];
        for (; i < CT_CONTENT; i++) {
            if (worst < 0) {
                worst = 0;
                for (int j = 1; j < Settings.xNNewWordVersions; j++)
                    if (best[worst].lexProb > best[j].lexProb)
                        worst = j;
            }
            if (best[worst].lexProb < contentTags[i].lexProb) {
                best[worst] = contentTags[i];
                worst = -1;
            }
        }
        for (i = 0; i < Settings.xNNewWordVersions; i++) {
            Ensure.ensure(best[i].lexProb > 0);
            getNewWords().addWordTagUnsafe(w, best[i]).lexProb = best[i].lexProb;
        }
    }

    void setLexicalProbs(Word w, WordToken t, TrigramGadget g) {
        Ensure.ensure(w != null);
        if (t.getSelectedTag() != null) {
            g.n = 1;
            g.tag[0] = t.getSelectedTag();
            g.lexProb[0] = 1;
            if (Settings.xPrintLexicalProbs) {
                System.out.println("lex-probs of " + w + ": " + g.tag[0] + " 1 (selected)");
            }
            return;
        }
        if (w.isNewWord()) {
            NewWord nw = (NewWord) w;
            if (Settings.xAnalyzeNewWords && !nw.isAnalyzed()) {
                Message.invoke(MessageType.MSG_MINOR_WARNING, "new-word", nw.getString(), "not analyzed before tagging");
                getWords().analyzeNewWord(nw, false);
            }
            tagUnknownWord(nw, false || Settings.xAlwaysNormalizeNewWords, true, t);
            g.n = Settings.xNNewWordVersions;
            //     SelectBest((TagPointer*) contentTags, CT_CONTENT, (TagPointer*) g.tag, g.n, tagLexProb);
            int i;
            for (i = 0; i < g.n; i++)
                g.tag[i] = contentTags[i];
            int worst = -1;
            for (; i < CT_CONTENT; i++) {
                if (worst < 0) {
                    worst = 0;
                    for (int j = 1; j < g.n; j++)
                        if (g.tag[worst].lexProb > g.tag[j].lexProb)
                            worst = j;
                }
                if (g.tag[worst].lexProb < contentTags[i].lexProb) {
                    g.tag[worst] = contentTags[i];
                    worst = -1;
                }
            }
            for (i = 0; i < g.n; i++)
                g.lexProb[i] = g.tag[i].lexProb;
            if (Settings.xPrintLexicalProbs) {
                System.out.println("lex-probs of new word " + w + ':');
                for (i = 0; i < g.n; i++)
                    System.out.println("\t" + g.tag[i] + ' ' + g.lexProb[i]);
                System.out.println();
            }
            return;
        }
        int i = 0, max = w.isNewWord() ? Settings.xNNewWordVersions : Settings.xNWordVersions;
        for (WordTag q = w; q != null && i < max; q = q.next()) {
            if (!q.isExtraLemma()) {
                Tag tg = q.getTag();
                Ensure.ensure(tg != null);
                g.tag[i] = tg;
                g.lexProb[i] = q.getLexProb();
                if (!w.isNewWord() && tg.isProperNoun())
                    if (t.isFirstCapped()) {
                        if (!t.isFirstInSentence() && Settings.xMorfCapital) {
                            if (Settings.xPrintLexicalProbs) {
                                System.out.print(w + ": FirstCapped, prob of " + tg + " factored by ");
                                System.out.println(Settings.xAlphaCapital);
                            }
                            g.lexProb[i] *= Settings.xAlphaCapital;
                        }
                    } else if (Settings.xMorfNonCapital) {
                        g.lexProb[i] *= Settings.xAlphaNonCapital;
                        if (Settings.xPrintLexicalProbs) {
                            System.out.println(w + ": NOT First Capped, prob of " + tg + " factored by ");
                            System.out.println(Settings.xAlphaNonCapital);
                        }
                    }
                if (g.lexProb[i] <= 0)
                    Message.invoke(MessageType.MSG_WARNING, w.getString(), "has non-positive prob for tag",
                            q.getTag().getString());
                //      std::cout << q << ' ' << g.lexProb[i] << std::endl;
                i++;
            }
        }
        Ensure.ensure(i > 0);
        if (t.isAllCapped() && w.getStringLen() < 6 && i < max &&
                w.getWordTag(getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN)) == null) {
            if (Settings.xPrintLexicalProbs) {
                System.out.println("(AllCapped . pm added)");
            }
            g.tag[i] = getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN);
            g.lexProb[i] = 0.1f * getTags().specialTag(Yytoken.TOKEN_PROPER_NOUN).getFreqInv();
            i++;
        }
        g.n = i;

        if (Settings.xPrintLexicalProbs) {
            System.out.println("lex-probs of known word " + w + ':');
            for (i = 0; i < g.n; i++) {
                System.out.println("\t" + g.tag[i] + ' ' + g.lexProb[i]);
            }
            System.out.println();
        }
        // test Åström exponent:
        //  for (i=0; i<g.n; i++) g.lexProb[i] = pow(g.lexProb[i], 0.9);
    }

    // PN. Not used
    /*
    void TagSentenceForPredictor(Sentence s, Word ws, int n, WordSuggestions sugg, int minLength, float factor) {
        int endPos = s.NWords();
        int lastPos = s.NWords() - 1;
        Ensure.ensure(lastPos >= 0);
        Ensure.ensure(n > 0);
        s.tokens[lastPos].GetToken() = WORD;
        s.tokens[lastPos].word = ws[0];
        TagSentenceInterval(s, Math.max(0, lastPos - 1), endPos);
        for (int i = 0; i < n; i++) {
            Word w = ws[i];
            if (!w.IsSuggested()) {
                s.tokens[lastPos].word = w;
                TagSentenceInterval(s, lastPos, endPos);
                double p = 0;
                for (int j = 0; j < gadget[endPos + 1].n; j++)
                    for (int k = 0; k < gadget[endPos].n; k++)
                        p += gadget[endPos + 1].prob[j][k];
                if (factor > 0)
                    p *= factor;
                if (xRecency)
                    if (w.TextFreq() != 0)
                        p *= (1 + xRecencyFactor * w.TextFreq() / w.Freq());
                    else {
                        Word b = w.LemmaWord();
                        if (b && b.SomeFormOccursInText())
                            p *= (1 + xRecencyFactorBase / w.Freq());
                    }
                if (xWordBigramsUsed) {
                    Word prev = (lastPos ? s.GetWord(lastPos - 1) : sentenceDelimiter);
                    Bigram b = Words().FindBigram(prev, w);
                    if (b)
                        p *= (1 + xLambdaWordBi * b.prob);
                    if (!b && prev.Freq() >= 80)
                        p *= (1 + xLambdaWordBi * xNewParameter / prev.Freq());
                }
                if (p > sugg.WorstProb())
                    if (w.StringLen() >= minLength)
                        sugg.AddCandidate(w, p);
            }
        }
    }*/


    void tagSentenceInterval(AbstractSentence s, int startPos, int endPos) {
        Ensure.ensure(startPos >= 0);
        Ensure.ensure(endPos < s.getNTokens());
        int i = startPos;
        TrigramGadget g0 = gadget[i]; //i-2 == startpos
        i++;
        TrigramGadget g1 = gadget[i]; //i-1
        //  Word *w1 = s.GetWord(i);
        i++;
        TrigramGadget g2 = gadget[i]; //i
        for (; i <= endPos; ) {
            WordToken t = s.getWordToken(i);
            //    g2.Reset();                   // make sure this has no effect on the tagging // jonas, this crashes the tagger, why?
            setLexicalProbs(t.getWord(), t, g2);
            // computes the lexical probabilities of w2 and assigns the possible tags of w2 to g2.
            for (int u = 0; u < g2.n; u++) {
                Tag tag2 = g2.tag[u];
                for (int v = 0; v < g1.n; v++) {
                    Tag tag1 = g1.tag[v];
                    //std::cout << "Pt1t2(" << tag1 << ',' << tag2 << ") = " << Tags().Pt1t2(tag1.Index(), tag2.Index()) << std::endl;
                    double best = 0.0;
                    for (int z = 0; z < g0.n; z++) {
                        Tag tag0 = g0.tag[z];
                        double prob = g1.prob[v][z] * (getTags().Pt3_t1t2(tag0, tag1, tag2)); //+biProb);//jonas, this sometimes gives UMR-error
                        //System.out.println(tag0 + "\t" + tag1 + "\t" + tag2);
                        //System.out.println(g1.prob[v][z] + "\t" + getTags().Pt3_t1t2(tag0, tag1, tag2) +"\t" + prob);
                        if (prob > best) {
                            best = prob;
                            g2.prev[u][v] = (char) z; // remember which tag in position 0 that gave the best probability
                        }
                    }
                    g2.prob[u][v] = best * g2.lexProb[u];
                }
            }
            if (g2.prob[0][0] < Settings.MIN_PROB) {
                if (g2.prob[0][0] <= 0) {
                    //s.Print();
                    Message.invoke(MessageType.MSG_WARNING, "prob is zero during tagging at", t.getWord().getString(),
                            g2.tag[0].getString());
                }
                Message.invoke(MessageType.MSG_MINOR_WARNING, "prob too small, normalizing");
                //std::cerr << "normalize, too small, index " << i << std::endl;
                g2.normalize(g2.n, g1.n);
            } else if (g2.prob[0][0] > Settings.MAX_PROB) {
                Message.invoke(MessageType.MSG_MINOR_WARNING, "prob too big, normalizing");
                //std::cerr << "normalize, too large, index " << i << std::endl;
                g2.normalize(g2.n, g1.n);
            }
            //    w1 = t.GetWord();
            i++;
            g0 = gadget[i - 2];
            g1 = gadget[i - 1];
            g2 = gadget[i];
        }
        if (endPos >= s.getNWords() + 2)
            rewind(s, endPos);
    }

    void rewind(AbstractSentence s, int endPos) {
        s.prob = gadget[endPos + 1].prob[0][0];
        TrigramGadget[] g = gadget;
        int x = 0;
        int y = 0;
        int prevY;
        for (int i = endPos - 2; i >= 2; i--) {
            WordToken t = s.getWordToken(i);
            Word old_word = t.getWord(); // jonas
            prevY = y;
            y = g[i + 2].prev[x][y];
        /*
          if(std::string("bye-bye") == t.GetWord().string) {
          std::cerr << t << std::endl;
          }
        */
            //    std::cerr << t.GetWord().GetWordTag(g[i].tag[y]) << "\t" << g[i].prob[x][y] / g[i-1].prob[y][g[i+1].prev[x][y]] << std::endl; // jonas, test of output probabilities

            g[i].selected = y;
            Ensure.ensure(y >= 0);
            x = prevY;
            Tag tag = g[i].tag[y];
            Ensure.ensure(tag != null);
            WordTag wt = t.getWord().getWordTag(tag);
            if (wt == null) {
                if (!t.getWord().isNewWord()) {
                    Message.invoke(MessageType.MSG_MINOR_WARNING, "the tagger selected unknown tag",
                            tag.getString(), "for main lexicon word", t.getWord().getString());
                    wt = t.word = getNewWords().addWord(t.getWord().getString(), tag);
                } else {
                    wt = getNewWords().addWordTag((NewWord) t.getWord(), tag);
                    getWords().guessWordTagRule(wt);
                    if (tag.isLemma())
                        wt.lemma = wt;
                    wt.addedByTagger = true;
                }
            }
            if (t.getWord().isNewWord() && (xSetNewWordTagFreqs || wt.tagFreq == 0))
                wt.tagFreq++;
            t.setSelectedTag(tag, true);
            if (t.getWord().isNewWord() // jonas
                    && t.getSelectedTag() != tag) // jonas, tagger chose unallowed tag, use old word instead
                t.word = old_word; // jonas
        /*
          if(std::string("bye-bye") == t.GetWord().string) {
          std::cerr << t.SelectedTag() << std::endl;
          }
        */
        }
        if (Settings.xPrintProbs) {
        /* estimate probability of chosen tag. count how much of the
           total Markov-probability would give this choice.
        */
            for (int i = 2; i <= endPos - 2; ++i) {
                // PN. Removed useless and crashed the program
                //double current = g[i].prob[g[i].selected][g[i - 1].selected];
                //double previous = g[i - 1].prob[g[i - 1].selected][g[i - 2].selected];
                //double a = current / previous / Math.max(s.getWordToken(i).getWord().getFreq(), 1);
                double sumProbs = 0, sumU = 0;
                for (int vv = 0; vv < g[i - 1].n; ++vv) {
                    for (int uu = 0; uu < g[i].n; ++uu)
                        sumProbs += g[i].prob[uu][vv] / g[i - 1].prob[vv][g[i].prev[uu][vv]];
                    sumU += g[i].prob[g[i].selected][vv] / g[i - 1].prob[vv][g[i].prev[g[i].selected][vv]];
                }
                double a = sumU / sumProbs;
                System.out.println(String.format(Locale.UK, "%1.06f", a) + "\t" + s.getWordToken(i));
            }
        }
    }

    void tagText() {
        if (!Settings.xOptimize) Message.invoke(MessageType.MSG_STATUS, "tagging text...");
        Timer timer = new Timer();
        if (Settings.xTakeTime)
            timer.start();
        if (Settings.xAnalyzeNewWords)
            getWords().analyzeNewWords();
        if (Settings.xTakeTime)
            analyzeTime = timer.restart();
        int i;
        for (Sentence s : theText.sentences)
            tagSentence(s);
        if (!Settings.xAmbiguousNewWords)
            for (Sentence s : theText.sentences) {
                for (i = 2; i < s.getNWords() + 2; i++) {
                    WordToken wt = s.getWordToken(i);
                    Word w = wt.getWord();
                    if (w.isNewWord()) {
                        WordTag wm = w.getWordTag(wt.getSelectedTag());
                        if (wm != null) {
                            int best = wm.getTagFreq();
                            for (WordTag q = w; q != null; q = q.next())
                                if (q.getTagFreq() > best) {
                                    wt.setSelectedTag(q.getTag(), true);
                                    best = q.getTagFreq();
                                }
                        } else {
                            if (wt.getSelectedTag() == null)
                                Message.invoke(MessageType.MSG_ERROR, "TagText(): no selected tag for", w.getString());
                            Message.invoke(MessageType.MSG_ERROR, "TagText(): unexpected tag", wt.getSelectedTag().getString(),
                                    "for", w.getString());
                        }
                    }
                }
            }
        if (Settings.xTakeTime)
            tagTime = timer.get();
    }

/*
  jonas,
  moved ifdef CORRECT_TAG_KNOWN . Tagger::EvaluateTagging()
  to developer-tagger.cpp
*/

    void readText() throws IOException {
        reset();
        Message.invoke(MessageType.MSG_STATUS, "reading tokens...");
        Timer timer = new Timer();
        if (Settings.xTakeTime)
            timer.start();
        //  char string[MAX_WORD_LENGTH];
        String lookUpString;
        int offset = 0, prevOffset;
        if (tokensBufSize == 0) {
            if (input_size > 0) { // jonas, this part is new
                tokensBufSize = 1000 + input_size / 4; // guess words approx 4 chars
                System.err.println("allocated space for " + tokensBufSize + " tokens ");
                theTokens = new WordToken[tokensBufSize];
            } else { // jonas, this is the old stuff
                theTokens = new WordToken[TOKEN_BUF_CHUNK]; // new OK
                tokensBufSize = TOKEN_BUF_CHUNK;
            }
        }
        for (int i = 0; i < theTokens.length; i++) {
            theTokens[i] = new WordToken();
        }
        theTokens[0].setWord(specialWord[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()], "$.", Yytoken.TOKEN_END);
        nTokens = 1;
        tokenizer = new Tokenizer(new InputStreamReader(inputStream));
        endText:
        for (; ; ) {
            //if(nTokens % 100000 == 1)
            //  std::cerr << "tokens read: " << nTokens << std::endl;
            //System.out.println("#Tokens: " + nTokens + "\t" + tokensBufSize);
            if (nTokens >= tokensBufSize) {
                tokensBufSize *= 2;      //jonas      tokensBufSize += TOKEN_BUF_CHUNK;
                WordToken[] tok = new WordToken[tokensBufSize]; // new OK
                for (int i = 0; i < tok.length; i++)
                    tok[i] = new WordToken();

                if (nTokens >= 0) System.arraycopy(theTokens, 0, tok, 0, nTokens);
                theTokens = tok;
                System.err.println("REallocated space for " + tokensBufSize + " tokens");
            }
            Word w = null;
            String lookUp = null;
            // PN. Removed from here. The stream should be redirected. To be corrected.
            //tokenizer = new Tokenizer(new InputStreamReader(inputStream));
            Yytoken token = tokenizer.parse();
            theOriginalText.write(tokenizer.tokenString());
            prevOffset = offset;
            offset += tokenizer.tokenLength();
            switch (token) {
                case TOKEN_END:
                    break endText;
                case TOKEN_SPLIT_WORD: { // "hälso- och sjukvård"
                    String string = tokenizer.tokenString();
                    for (int j = tokenizer.tokenLength() - 1; j > 0; j--)
                        if (Letter.isSpace(string.charAt(j))) {
                            //lookUp = string + j + 1;
                            //moves the pointer ahead j+1 steps
                            lookUp = string.substring(j + 1);
                            break;
                        }

                    Ensure.ensure(lookUp != null);
                    break;
                }
                case TOKEN_ABBREVIATION:
                case TOKEN_WORD: {
                    String string = tokenizer.tokenString();
                    w = findMainOrNewWord(string);
                    if (w == null) {
                        lookUpString = string;
                        lookUpString = Letter.spaceFix(lookUpString);
                        w = findMainOrNewWord(lookUpString);
                        if (w == null) {
                            if (token == Yytoken.TOKEN_ABBREVIATION)
                                lookUpString = Letter.space2Punct(lookUpString);
                            else
                                lookUpString = Letter.punctFix(lookUpString);
                            w = findMainOrNewWord(lookUpString);
                        }
                    }
                    break;
                }
                case TOKEN_PUNCTUATION:
                case TOKEN_SIMPLE_WORD:
                    break;
                case TOKEN_LEFT_PAR:
                case TOKEN_RIGHT_PAR:
                case TOKEN_CITATION:
                case TOKEN_CARDINAL_SIN:
                case TOKEN_CARDINAL:
                case TOKEN_BAD_CARDINAL:
                case TOKEN_ORDINAL:
                case TOKEN_YEAR:
                case TOKEN_DATE:
                case TOKEN_TIME:
                case TOKEN_PARAGRAPH:
                case TOKEN_PERIOD:
                case TOKEN_QUESTION_MARK:
                case TOKEN_DELIMITER_PERIOD:
                case TOKEN_DELIMITER_QUESTION:
                case TOKEN_DELIMITER_EXCLAMATION:
                case TOKEN_DELIMITER_HEADING:
                case TOKEN_DELIMITER_OTHER:
                case TOKEN_PERCENTAGE:
                case TOKEN_MATH:
                case TOKEN_E_MAIL:
                case TOKEN_URL:
                    w = specialWord[token.ordinal()];
                    if (getTags().specialTag(token) == null) {
                        System.err.println(token);
                    }
                    Ensure.ensure(getTags().specialTag(token) != null);
                    theTokens[nTokens].setSelectedTag(getTags().specialTag(token), false);
                    Ensure.ensure(w != null);
                    break;
                case TOKEN_EXCLAMATION_MARK:
                    w = specialWord[token.ordinal()];
                    break;
                case TOKEN_NEWLINE:
                    if (!Settings.xNewlineMeansNewSentence) {
                        theTokens[nTokens - 1].trailingSpace = true;
                        continue;
                    }
                case TOKEN_BEGIN_PARAGRAPH:
                case TOKEN_BEGIN_HEADING:
                case TOKEN_END_HEADING:
                case TOKEN_BEGIN_TITLE:
                case TOKEN_END_TITLE:
                    if (theTokens[nTokens - 1].token == Yytoken.TOKEN_NEWLINE)
                        nTokens--;
                    theTokens[nTokens++].setWord(specialWord[Yytoken.TOKEN_DELIMITER_HEADING.ordinal()], null, token);
                    continue;
                case TOKEN_BEGIN_TABLE:
                case TOKEN_TABLE_TAB:
                case TOKEN_END_TABLE:
                    continue;
                case TOKEN_SPACE:
                    theTokens[nTokens - 1].trailingSpace = true;
                    continue;
                case TOKEN_PROPER_NOUN:
                case TOKEN_PROPER_NOUN_GENITIVE:
                case TOKEN_UNKNOWN:
                case TOKEN_SILLY:
                case TOKEN_ERROR:
                    Message.invoke(MessageType.MSG_WARNING, tokenizer.tokenString(), "was recognized as",
                            Tokenizer.token2String(token), "by the tokenizer");
                    break;
            }
            String string = tokenizer.tokenString();
            if (w == null) {
                w = findMainOrNewWordAndAddIfNotPresent(lookUp != null ? lookUp : string);
                if (token == Yytoken.TOKEN_PUNCTUATION)
                    theTokens[nTokens].setSelectedTag(getTags().specialTag(token), false);
            }
            if (w.isNewWord()) {
                NewWord nw = (NewWord) w;
                nw.freq++;
            }
            w.textFreq++;
            WordToken t = theTokens[nTokens++];
            t.offset = prevOffset;
            t.setWord(w, string, token);
        }
        theTokens[nTokens].setWord(specialWord[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()], "$.", Yytoken.TOKEN_END);
        theTokens[nTokens + 1].setWord(specialWord[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()], "$.", Yytoken.TOKEN_END);
        //  for(int i = 0; i < nTokens; ++i)  std::cout<< theTokens[i] << std::endl;  // jonas, debug
        if (Settings.xTakeTime)
            tokenizeTime = timer.restart();
        if (nTokens > 1) // jonas
            buildSentences(theTokens);
        if (Settings.xTakeTime)
            sentenceTime = timer.restart();
    }

    void buildSentences(WordToken[] tokens) {
        String theOriginalString = theOriginalText.toString();
        Message.invoke(MessageType.MSG_STATUS, "building sentences...");
        int start = 1;
        int end = 0;
        int nCit = 0;
        int nOpenPar = 0;
        int firstPeriodPos = 0;
        int periods = 0;
        int checkedUntil = 0;
        boolean punktLista = false;
        //  int nChecked = 0, nFound = 0;
        Yytoken delToken = Yytoken.TOKEN_DELIMITER_OTHER;
        theText.sentences = null;
        Sentence s = null;
        String string;
        String lookUpString;
        for (int j = 1; ; j++) {
            //    std::cout << tokens[j] << std::endl;
            if (j - end >= AbstractSentence.MAX_SENTENCE_LENGTH - 5) { // this if-statement fixes bug for long sentences
                // sentence is too long, what to do...
                end = j; //jonas, otherwise one word disappears... end = j-1;

            } else {
                //   else
                switch (tokens[j].token) {
                    case TOKEN_DELIMITER_HEADING:
                    case TOKEN_END_HEADING:
                    case TOKEN_BEGIN_TITLE:
                    case TOKEN_END_TITLE:
                        delToken = Yytoken.TOKEN_DELIMITER_HEADING;
                    case TOKEN_BEGIN_HEADING:
                    case TOKEN_NEWLINE:
                    case TOKEN_BEGIN_PARAGRAPH:
                        if (s != null)
                            s.endsParagraph = true;
                        end = j - 1;
                        break;
                    case TOKEN_WORD:
                    case TOKEN_SIMPLE_WORD:
                        if (!Settings.xNoCollocations) { // jonas
                            if (j > checkedUntil) {
                                Ensure.ensure(tokens[j].getWord() != null && tokens[j + 1].getWord() != null && tokens[j + 2].getWord() != null);
                                if (tokens[j].getWord().isCollocation1() &&
                                        tokens[j + 1].getWord().isCollocation23()) {
                                    //	  nChecked++;
                                    if (tokens[j + 2].getWord().isCollocation23()) {
                                        string = String.format("%s %s %s", tokens[j].realString(), tokens[j + 1].realString(),
                                                tokens[j + 2].realString());
                                        Word w = findMainWord(string);
                                        if (w != null) {
                                            if (w.isOptSpace()) {
                                                lookUpString = String.format("%s%s%s", tokens[j].realString(),
                                                        tokens[j + 1].realString(),
                                                        tokens[j + 2].realString());
                                                w = findMainWord(lookUpString);
                                                Ensure.ensure(w != null);
                                            }
                                            tokens[j].setWord(w, string, Yytoken.TOKEN_WORD);
                                            tokens[j + 1].word = tokens[j + 2].word = null;
                                            j += 2;
                                            checkedUntil = j;
                                            //	      nFound++;
                                            continue;
                                        }
                                    }
                                    string = String.format("%s %s", tokens[j].realString(), tokens[j + 1].realString());
                                    Word w = findMainWord(string);
                                    if (w != null) {
                                        if (w.isOptSpace()) {
                                            lookUpString = String.format("%s%s", tokens[j].realString(),
                                                    tokens[j + 1].realString());
                                            w = findMainWord(lookUpString);
                                            Ensure.ensure(w != null);
                                        }
                                        tokens[j].setWord(w, string, Yytoken.TOKEN_WORD);
                                        tokens[++j].word = null;
                                        //	    nFound++;
                                    }
                                }
                                checkedUntil = j;
                            }
                        }
                        continue;
                    case TOKEN_QUESTION_MARK:
                        delToken = Yytoken.TOKEN_DELIMITER_QUESTION;
                        if (nCit % 2 == 0 && nOpenPar == 0) {
                            end = j;
                            break;
                        }
                        if (periods != 0) {
                            firstPeriodPos = j;
                            if (nOpenPar == 1 && tokens[start].token == Yytoken.TOKEN_LEFT_PAR
                                    && tokens[j + 1].token == Yytoken.TOKEN_RIGHT_PAR) {
                                j = end = start;
                                break;
                            }
                            if (nCit % 2 != 0 && tokens[start].token == Yytoken.TOKEN_CITATION
                                    && tokens[j + 1].token == Yytoken.TOKEN_CITATION) {
                                j = end = start;
                                break;
                            }
                        }
                        if (++periods > 1) {
                            if (tokens[start].token == Yytoken.TOKEN_CITATION ||
                                    tokens[start].token == Yytoken.TOKEN_LEFT_PAR)
                                j = end = start;
                            else
                                j = end = firstPeriodPos;
                            break;
                        }
                        continue;
                    case TOKEN_EXCLAMATION_MARK:
                        delToken = Yytoken.TOKEN_DELIMITER_EXCLAMATION;
                        if (nCit % 2 == 0 && nOpenPar == 0) {
                            end = j;
                            break;
                        }
                        if (periods != 0) {
                            firstPeriodPos = j;
                            if (nOpenPar == 1 && tokens[start].token == Yytoken.TOKEN_LEFT_PAR
                                    && tokens[j + 1].token == Yytoken.TOKEN_RIGHT_PAR) {
                                j = end = start;
                                break;
                            }
                            if (nCit % 2 != 0 && tokens[start].token == Yytoken.TOKEN_CITATION
                                    && tokens[j + 1].token == Yytoken.TOKEN_CITATION) {
                                j = end = start;
                                break;
                            }
                        }
                        if (++periods > 1) {
                            if (tokens[start].token == Yytoken.TOKEN_CITATION ||
                                    tokens[start].token == Yytoken.TOKEN_LEFT_PAR)
                                j = end = start;
                            else
                                j = end = firstPeriodPos;
                            break;
                        }
                        continue;
                    case TOKEN_PERIOD:
                        if (tokens[j - 1].getWord() != null &&
                                tokens[j - 1].token == Yytoken.TOKEN_SIMPLE_WORD &&
                                tokens[j + 1].token != Yytoken.TOKEN_BEGIN_PARAGRAPH &&
                                tokens[j + 1].token != Yytoken.TOKEN_END &&
                                tokens[j].realString().length() == 1) {
                            string = String.format("%s.", tokens[j - 1].lexString());
                            Word w1 = tokens[j - 1].getWord();
                            Word w2 = getWords().get(string);
                            int check = 0;
                            if (w2 != null) {
                                check = AbbrCheck.W_DOT;    // e.g. "etc." is a word in lexicon
                                //std::cout << "W_DOT " << w2 << ' ';
                            }
                            if (!tokens[j - 1].getWord().isNewWord()) {
                                check |= AbbrCheck.W_NO_DOT;     // e.g. "sak" is not a known word in lexicon
                                //std::cout << "W_NO_DOT ";
                            }
                            if (tokens[j + 1].isFirstCapped()) {
                                check |= AbbrCheck.CAPPED;
                                //std::cout << "CAPPED ";
                            } else if (Letter.isDigit(tokens[j + 1].realString().charAt(0))) {
                                check |= AbbrCheck.DIGIT;
                                //std::cout << "DIGIT ";
                            }
                            if (tokens[j + 1].getWord().mayBeCapped()) {
                                check |= AbbrCheck.MAY_CAP;
                                //std::cout << "MAY_CAP ";
                            }
                            //std::cout << std::endl;
                            boolean abb = false, period = true;
                            switch (check) {
                                case 0:
                                    if (!Letter.isLetter(tokens[j + 1].realString().charAt(0))) break;
                                case AbbrCheck.DIGIT:
                                case AbbrCheck.W_DOT | AbbrCheck.MAY_CAP:
                                case AbbrCheck.W_DOT | AbbrCheck.W_NO_DOT: // check this case
                                case AbbrCheck.W_DOT | AbbrCheck.W_NO_DOT | AbbrCheck.MAY_CAP:
                                case AbbrCheck.W_DOT | AbbrCheck.W_NO_DOT | AbbrCheck.CAPPED | AbbrCheck.MAY_CAP: // added 2001-06-27, OK?
                                case AbbrCheck.W_DOT | AbbrCheck.W_NO_DOT | AbbrCheck.DIGIT:
                                    abb = true;
                                    period = false;
                                    break;
                                case AbbrCheck.W_DOT | AbbrCheck.W_NO_DOT | AbbrCheck.CAPPED:
                                    if (w2.getFreq() > w1.getFreq()) abb = true;
                                    break;
                            }
                            //std::cout << tokens[j-1] << tokens[j] << tokens[j+1]
                            //     << check << ' ' << (abb ? "ABB " : "") << (period ? "PERIOD " : "") << std::endl << std::endl;
                            if (abb)
                                tokens[j - 1].setWord(w2 != null ? w2 : w1,
                                        period ? tokens[j - 1].realString() : string,
                                        Yytoken.TOKEN_ABBREVIATION);
                            if (!period) {
                                tokens[j].word = null;
                                continue;
                            }
                        }
                        if (tokens[j].realString().length() > 1 && tokens[j].realString().charAt(1) == '.' && !tokens[j + 1].isFirstCapped())
                            continue;
                        delToken = Yytoken.TOKEN_DELIMITER_PERIOD;
                        if (nCit % 2 == 0 && nOpenPar == 0) {
                            end = j;
                            break;
                        }
                        if (periods != 0) {
                            firstPeriodPos = j;
                            if (nOpenPar == 1 && tokens[start].token == Yytoken.TOKEN_LEFT_PAR
                                    && tokens[j + 1].token == Yytoken.TOKEN_RIGHT_PAR) {
                                j = end = start;
                                break;
                            }
                            if (nCit % 2 != 0 && tokens[start].token == Yytoken.TOKEN_CITATION
                                    && tokens[j + 1].token == Yytoken.TOKEN_CITATION) {
                                j = end = start;
                                break;
                            }
                        }
                        if (++periods > 1) {
                            if (tokens[start].token == Yytoken.TOKEN_CITATION ||
                                    tokens[start].token == Yytoken.TOKEN_LEFT_PAR)
                                j = end = start;
                            else
                                j = end = firstPeriodPos;
                            break;
                        }
                        continue;
                    case TOKEN_END:
                        end = j - 1;
                        break;
                    case TOKEN_CITATION:
                        nCit++;
                        continue;
                    case TOKEN_LEFT_PAR:
                        nOpenPar++;
                        continue;
                    case TOKEN_RIGHT_PAR:
                        if (nOpenPar > 0) nOpenPar--;
                        else if (j == start) {
                            end = j;
                            break;
                        }
                        continue;
                    case TOKEN_ABBREVIATION:
                        if (tokens[j + 1].isFirstCapped() &&
                                !tokens[j + 1].getWord().mayBeCapped() &&
                                tokens[j].realString().charAt(tokens[j].realString().length() - 1) == '.') {
                            end = j;
                            break;
                        }
                    default:
                        continue;
                }
            }
            int n = end + 5 - start;
            if (n > 4) {
                if (s != null) {
                    s = new Sentence(n);
                    theText.sentences.add(s); // new OK
                } else {
                    theText.sentences = new ArrayList<>();
                    s = new Sentence(n);
                    theText.sentences.add(s); // new OK
                }
                Ensure.ensure(n < AbstractSentence.MAX_SENTENCE_LENGTH);
                Ensure.ensure(s != null);
                short mm = 0;
                for (int i = start; i <= end; i++)
                    if (tokens[i].word != null) {
                        Ensure.ensure(tokens[i].token != Yytoken.TOKEN_BEGIN_HEADING);
                        Ensure.ensure(tokens[i].token != Yytoken.TOKEN_END_HEADING);
                        s.tokens[2 + mm++] = tokens[i];
                    }
                Ensure.ensure(mm > 0);
                short m = mm;
                s.nTokens = (short) (m + 4);
                s.nWords = m;
            /* jonas, why is this here?
               if (m > 1 && s.tokens[3].IsFirstCapped() &&
               (s.tokens[2].token == TOKEN_BAD_CARDINAL ||
               s.tokens[2].token == TOKEN_CARDINAL ||
               s.tokens[2].token == TOKEN_CARDINAL_SIN)) {
               s.tokens[2].token = TOKEN_RIGHT_PAR;
               s.tokens[2].word = specialWord[TOKEN_RIGHT_PAR];
               }
            */
                int startOffset = 0;
                int endOffset = 0;
                for (int k = 2; k < m + 2; k++)
                    if (s.tokens[k].getToken() != Yytoken.TOKEN_PUNCTUATION &&
                            s.tokens[k].getToken() != Yytoken.TOKEN_CITATION &&
                            s.tokens[k].getToken() != Yytoken.TOKEN_RIGHT_PAR) {
                        s.tokens[k].firstInSentence = true;
                        startOffset = s.tokens[k].getOffset(); //Oscar
                        break;
                    }
                WordToken del = specialWordToken[delToken.ordinal()];
                if (s.tokens[2].getToken() == Yytoken.TOKEN_PUNCTUATION ||
                        s.tokens[2].getToken() == Yytoken.TOKEN_RIGHT_PAR) {
                    if (punktLista)
                        del = specialWordToken[Yytoken.TOKEN_DELIMITER_OTHER.ordinal()];
                } else {
                    if (s.tokens[m + 1].getToken() == Yytoken.TOKEN_PUNCTUATION &&
                            ":".compareTo(s.getWord(m + 1).getString()) == 0)
                        punktLista = true;
                    else
                        punktLista = false;
                }
                endOffset = s.tokens[m + 1].getOffset(); //Oscar
                s.setOriginalText(theOriginalString.substring(startOffset, endOffset + 1)); //Oscar
                Ensure.ensure(del.word != null);
                s.tokens[0] = s.tokens[1] =
                        s.tokens[m + 2] = s.tokens[m + 3] = del;
            }
            start = j + 1;
            if (start >= nTokens)
                break;
            nCit = 0;
            nOpenPar = 0;
            periods = 0;
            firstPeriodPos = 0;
            delToken = Yytoken.TOKEN_DELIMITER_OTHER;
        }
        //  std::cout << "nChecked: " << nChecked << " nFound: "<< nFound << std::endl;
    }

    void generateInflections() {
        Message.invoke(MessageType.MSG_STATUS, "generating inflections...");
        getWords().generateInflections(false);
        Message.invoke(MessageType.MSG_COUNTS, "during inflection generating");
    }

    void printTimes() {
        theText.countContents();
        System.out.println(theText);
        System.out.println("timings:");
        System.out.println("\t" + loadTime);
        System.out.println(" ms to load lexicon");
        System.out.println("\t" + (double) theText.getNWordTokens() / tokenizeTime);
        System.out.println(" words tokenized per second");
        System.out.println("\t" + (double) theText.getNWordTokens() / sentenceTime);
        System.out.println(" words sentenized per second");
        System.out.println("\t" + (double) theText.getNWordTokens() / analyzeTime);
        System.out.println("\t" + " words analyzed per second");
        System.out.println("\t" + (double) theText.getNWordTokens() / tagTime);
        System.out.println(" words tagged per second");
        System.out.println("\t" + (double) theText.getNWordTokens() / tokenizeTime + tagTime + analyzeTime + sentenceTime);
        System.out.println(" words tokenized and tagged per second");
    }

// read to two buffers ?
// use tokenizer, use every odd as tag ?

    int my_tolower(Character i) {
        return Character.toLowerCase(i);
    }

    int nextIndex(Vector<String> v, String s) {
        // very naive sync, find next index were word is 's'
        String tt = "";
        int start = 0;

        int i;
        for (i = 0; i < (int) s.length(); i++)
            if (s.charAt(i) != ' ')
                tt += s.charAt(i);
        String temp = "";
        for (i = start; i < (int) v.size() && temp.length() < tt.length(); i++)
            temp += v.get(i);
        tt = tt.toLowerCase();
        if (temp.equals(tt)) {
            tt = "";
            start = i;
            return i - 1;
        } else
            return -1;
    }

//handles tokenization errors badly, but sometimes succeds

    void readTaggedTextQnD() throws IOException {
        PrintStream oss;
        Vector<Tag> tagvec = new Vector<>();
        Vector<String> wordvec = new Vector<>();
        while (inputStream != null) {
            String l;
            l = new String(System.in.readAllBytes());
            System.err.println("found '" + l + "'");
            if (l != null) {
                String w = null, t = null;
                // PN. To be corrected. Needs more understanding
                //std::istringstream isl(l);
                //isl >> w >> t;
                //oss << w << "\n";
                Tag ct = getTags().get(t);
                if (ct == null) {
                    Message.invoke(MessageType.MSG_WARNING, "unknown tag for", w, t);
                    ct = getTags().dummyTag(); // this is not very good
                }
                w = w.toLowerCase();
                wordvec.add(w);
                tagvec.add(ct);
            } else {
                System.out.println();
                // PN. Correct this
                //oss << "\n"; // keep double newlines
            }
        }
        //std::istringstream iss(oss.str());
        //tokenizer.SetStream(iss);

        // now do the usual, but add tags... harder than one might think

        reset();
        Message.invoke(MessageType.MSG_STATUS, "reading tokens...");
        Timer timer = new Timer();
        if (Settings.xTakeTime)
            timer.start();
        String lookUpString;
        int offset = 0, prevOffset;
        if (tokensBufSize == 0) {
            if (input_size > 0) { // jonas, this part is new
                tokensBufSize = 1000 + input_size / 10; // guess words approx 4 chars, tags approx 6
                System.out.println("allocated space for " + tokensBufSize + " tokens ");
                theTokens = new WordToken[tokensBufSize];
            } else { // jonas, this is the old stuff
                theTokens = new WordToken[TOKEN_BUF_CHUNK];
                tokensBufSize = TOKEN_BUF_CHUNK;
            }
        }
        theTokens[0].setWord(specialWord[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()], "$.", Yytoken.TOKEN_END);
        nTokens = 1;
        endText:
        for (; ; ) {
            if (nTokens >= tokensBufSize) {
                tokensBufSize *= 2;
                WordToken[] tok = new WordToken[tokensBufSize];
                if (nTokens >= 0) System.arraycopy(theTokens, 0, tok, 0, nTokens);
                theTokens = tok;
                System.err.println("REallocated space for " + tokensBufSize + " tokens");
            }
            Word w = null;
            String lookUp = null;
            tokenizer = new Tokenizer(new InputStreamReader(inputStream));
            Yytoken token = tokenizer.parse();
            prevOffset = offset;
            offset += tokenizer.tokenLength();
            switch (token) {
                case TOKEN_END:
                    break endText;
                case TOKEN_SPLIT_WORD: { // "hälso- och sjukvård"
                    String string = tokenizer.tokenString();
                    for (int j = tokenizer.tokenLength() - 1; j > 0; j--)
                        if (Letter.isSpace(string.charAt(j))) {
                            lookUp = string + j + 1;
                            break;
                        }
                    Ensure.ensure(lookUp != null);
                    break;
                }
                case TOKEN_ABBREVIATION: // these don't work ok....
                case TOKEN_WORD: {
                    String string = tokenizer.tokenString();
                    w = findMainOrNewWord(string);
                    if (w == null) {
                        lookUpString = string;
                        lookUpString = Letter.spaceFix(lookUpString);
                        w = findMainOrNewWord(lookUpString);
                        if (w == null) {
                            if (token == Yytoken.TOKEN_ABBREVIATION)
                                lookUpString = Letter.space2Punct(lookUpString);
                            else
                                lookUpString = Letter.punctFix(lookUpString);
                            w = findMainOrNewWord(lookUpString);
                        }
                    }
                    break;
                }
                case TOKEN_PUNCTUATION:
                case TOKEN_SIMPLE_WORD:
                    break;
                case TOKEN_LEFT_PAR:
                case TOKEN_RIGHT_PAR:
                case TOKEN_CITATION:
                case TOKEN_CARDINAL_SIN:
                case TOKEN_CARDINAL:
                case TOKEN_BAD_CARDINAL:
                case TOKEN_ORDINAL:
                case TOKEN_YEAR:
                case TOKEN_DATE:
                case TOKEN_TIME:
                case TOKEN_PARAGRAPH:
                case TOKEN_PERIOD:
                case TOKEN_QUESTION_MARK:
                case TOKEN_DELIMITER_PERIOD:
                case TOKEN_DELIMITER_QUESTION:
                case TOKEN_DELIMITER_EXCLAMATION:
                case TOKEN_DELIMITER_HEADING:
                case TOKEN_DELIMITER_OTHER:
                case TOKEN_PERCENTAGE:
                case TOKEN_MATH:
                case TOKEN_E_MAIL:
                case TOKEN_URL:
                    w = specialWord[token.ordinal()];
                    if (getTags().specialTag(token) == null) {
                        System.err.println(token);
                    }
                    Ensure.ensure(getTags().specialTag(token) != null);
                    theTokens[nTokens].setSelectedTag(getTags().specialTag(token), true);
                    Ensure.ensure(w != null);
                    break;
                case TOKEN_EXCLAMATION_MARK:
                    w = specialWord[token.ordinal()];
                    break;
                case TOKEN_NEWLINE:
                    if (!Settings.xNewlineMeansNewSentence) {
                        theTokens[nTokens - 1].trailingSpace = true;
                        continue;
                    }
                case TOKEN_BEGIN_PARAGRAPH:
                case TOKEN_BEGIN_HEADING:
                case TOKEN_END_HEADING:
                case TOKEN_BEGIN_TITLE:
                case TOKEN_END_TITLE:
                    if (theTokens[nTokens - 1].token == Yytoken.TOKEN_NEWLINE)
                        nTokens--;
                    theTokens[nTokens++].setWord(specialWord[Yytoken.TOKEN_DELIMITER_HEADING.ordinal()], null, token);
                    continue;
                case TOKEN_BEGIN_TABLE:
                case TOKEN_TABLE_TAB:
                case TOKEN_END_TABLE:
                    continue;
                case TOKEN_SPACE:
                    theTokens[nTokens - 1].trailingSpace = true;
                    continue;
                case TOKEN_PROPER_NOUN:
                case TOKEN_PROPER_NOUN_GENITIVE:
                case TOKEN_UNKNOWN:
                case TOKEN_SILLY:
                case TOKEN_ERROR:
                    Message.invoke(MessageType.MSG_WARNING, tokenizer.tokenString(), "was recognized as",
                            Tokenizer.token2String(token), "by the tokenizer");
                    break;
            }
            String string = tokenizer.tokenString();
            if (w == null) {
                w = findMainOrNewWordAndAddIfNotPresent(lookUp != null ? lookUp : string);
                if (token == Yytoken.TOKEN_PUNCTUATION)
                    theTokens[nTokens].setSelectedTag(getTags().specialTag(token), true);
            }
            if (w.isNewWord()) {
                NewWord nw = (NewWord) w;
                nw.freq++;
            }
            w.textFreq++;
            WordToken t = theTokens[nTokens++];
            t.offset = prevOffset;
            t.setWord(w, string, token);
            // don't use this strategy, it is bad...
            int temp = nextIndex(wordvec, string);
            if (temp >= 0) {
                t.setSelectedTag(tagvec.get(temp), false);
            }
        }
        theTokens[nTokens].setWord(specialWord[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()], "$.", Yytoken.TOKEN_END);
        theTokens[nTokens + 1].setWord(specialWord[Yytoken.TOKEN_DELIMITER_PERIOD.ordinal()], "$.", Yytoken.TOKEN_END);
        if (Settings.xTakeTime)
            tokenizeTime = timer.restart();
        if (nTokens > 1) // jonas
            buildSentences(theTokens);
        if (Settings.xTakeTime)
            sentenceTime = timer.restart();
        tagText(); // fix the ones were tokenization was different

    }

    Text getText() {
        return theText;
    }

    void setStream(InputStream instream, int size_hint) {
        inputStream = instream;
        // PN. I can't understand what the function below does. Check later
        //tokenizer.SetStream(inputStream);
        input_size = size_hint;
    }
}
