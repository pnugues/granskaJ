/* settings.cc
 * author: Johan Carlberger
 * last change: 2000-05-05
 * comments:
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

package tagger;

import common.Message;
import common.MessageType;

public class Settings {

    //static AbstractSentence xCurrSentence = null;
    //static RuleTerm xCurrRuleTerm = null;
    static public final int clusterLimit = 500;
    static public final boolean xIgnoreCitation = true;
    static public final boolean xAddPeriodIfMissing = true;
    static public final boolean xPrintCorrectTag = false;
    static public final boolean xCountFaults = false;
    static public final boolean xPrintUnknownLemmas = false;
    static public final int xCompoundMinLength = 3;
    static public final int xCompoundPrefixMinLength = 1;
    static public final int xCompoundSuffixMinLength = 2;
    static public final double MAX_PROB = Double.MAX_VALUE * 0.01;
    static public final double MIN_PROB = Double.MIN_VALUE * 10000000;
    static public final boolean xAlwaysNormalizeNewWords = false;
    static public final boolean xTagTrigramsUsed = true;
    static public final boolean xMorfCommonSuffix = true;
    //jonas optConst boolean xMorfCapital = true;
    //jonas optConst boolean xMorfNonCapital = true;
    //jonas boolean xAmbiguousNewWords = false;
    static public final boolean xMorfCapital = true; // jonas
    static public final boolean xMorfNonCapital = true; // jonas
    static public final boolean xNewWordsMemberTaggingOnly = false;
    static public final int xMaxLastChars = 5;
    static public final int xMinLastChars = 1;
    static public final int xTaggingEquation = 19;
    static public final float xLambda19 = (float) 0.00000333;
    static public final float xLambdaUni = (float) 0.059549;
    static public final float xLambdaBi = (float) 5.260;
    static public final float xLambdaTri = (float) 2.218;
    static public final float xLambdaTriExp = (float) 0.17452;
    static public final float xEpsilonTri = (float) 0.006042;
    static public final float xLambdaExtra = (float) 0.0001041;
    static public final float xAlphaExtra = (float) 0.00010;  // was 0.466 before 2000-02-07
    static public final float xEpsilonExtra = (float) 0.00000059014; // was  0.0000000059014  before 2000-02-07
    static public final int xNWordVersions = 4;
    static public final int xNNewWordVersions = 6;
    static public final float xAlphaLastChar[] = {0.0f, 0.03227f, 0.08662f, 0.2241f, 0.8162f, 4.373f};
    static public final float xAlphaSuffix = (float) 40;   // was 658000 before 2001-06-28
    static public final float xAlphaMember = (float) 0.0000391;
    static public final float xAlphaCapital = (float) 23.15;
    static public final float xAlphaNonCapital = (float) 0.02243;
    static public final float xAlphaUnknownCapital = (float) 27.06;
    static public final float xAlphaUnknownNonCapital = (float) 0.02050;
    // statistics:
    static public final boolean xAcceptAnyTagWhenCorrectIsSilly = true;
    static public final boolean xCountPunctuationMarksAsWords = true;

    // tagging:
    static public final boolean xOptimize = false;
    static public final boolean xOptimizeImportantParameters = true;
    static public final boolean xGoldenRatio = false;
    static public final boolean xComputeLexProbs = false;
    public static final boolean xCheckLexicons = false;
    static public final boolean xCheckLetters = false;
    static public final boolean xCheckNumbers = false;
    static public String xEndl = "\n";
    // diagnostics:
    static public boolean xVerbose = false;
    static public boolean xWarnAll = false;
    //debugging & developping:
    static boolean xTryLatestFeature = false;
    // input:
    static boolean xTaggedText = true;
    static boolean xNewlineMeansNewSentence = false;
    static boolean xNoCollocations = false; //jonas  // johnny removed optConst
    static boolean xNofast = true;
    // output:
    static boolean xOutputWTL;
    static String xTab = "\t";
    static String xBlue = "", xRed = "", xGreen = "", xNoColor = "";
    static String xItalic = "", xNoItalic = "";
    static String xHeading = "", xNoHeading = "";
    static String xBold = "", xNoBold = "";
    static String xSmall = "", xNoSmall = "";
    static String xTag = "", xNoTag = "";
    static boolean xPrintAllWords = false;
    static boolean xPrintAllWordTags = false;
    static boolean xPrintLemma = false;
    static boolean xPrintLexicalProbs = false;
    static boolean xPrintWordInfo = true;
    static boolean xPrintSelectedTag = false;
    static boolean xPrintProbs = false;
    static boolean xPrintOneWordPerLine = true;
    static boolean xPrintSentenceProbs = false;
    static boolean xPrintHTML = false;
    static boolean xMarkSuspiciousSentences = false;
    static boolean xPrintParameters = true;
    static boolean xListMultipleLemmas = false;
    // lexicon:
    static String xSettingsFile = "settings";
    // static String xVersion = "version 0.990913 PC   (c) 1999 KTH, NADA, Johan Carlberger";
    static String xVersion = "version 0.9 Java   (c) 2020 Pierre Nugues from code by Johan Carlberger";
    // new words analysis:
    static boolean xAnalyzeNewWords = true;
    static boolean xCompoundRequirePrefix = true;
    static boolean xTaggingExtraWords = false;
    static boolean xAmbiguousNewWords = false; // jonas
    static boolean xEvaluateTagging = true;
    // optimization:
    static boolean xOptimizeMatchings = true;
    static float xNewParameter = (float) 0;
    static float xNewParameter2 = (float) 0;
    static float xScope = (float) 2;
    static boolean xRandomize = false;
    static boolean xRepeatTest = false;
    static boolean xTestFeatures = false;
    // timings:
    static boolean xTakeTime;
    // correcting:
    static int xNSuggestionsWanted = 5;
    static boolean xSuspectAllUnknownWords = true;
    static boolean xSuggestSuspicious = false;
    static int xEditDistanceLimit = 200;
    static boolean xProbChange = true;
    static boolean xProbConcatenate = true;
    static boolean xProbDelete = true;
    static boolean xProbSwap = true;
    static float xChangeFactor = (float) 40;
    static float xConcatenateFactor = (float) 20;
    static float xDeleteFactor = (float) 1;
    static float xSwapFactor = (float) 1;
    // predictor:
    static float xLexProbExp = (float) 1.02;

    public static void SetFormatSettings(OutputMode mode) {
        switch (mode) {
            case OUTPUT_MODE_UNIX:
                xPrintHTML = false;
                xBlue = xGreen = xRed = "[";
                xNoColor = "]";
                xItalic = "[";
                xNoItalic = "]";
                xHeading = xNoHeading = "";
                xBold = xNoBold = "";
                xSmall = xNoSmall = "";
                xTag = "<";
                xNoTag = ">";
                xEndl = "\n";
                xTab = "\t";
                break;
            case OUTPUT_MODE_HTML:
                xPrintHTML = true;
                xPrintOneWordPerLine = false;
                xBlue = "<FONT COLOR=\"#3366FF\">";
                xRed = "<FONT COLOR=\"#FF0000\">";
                xGreen = "<FONT COLOR=\"#009900\">";
                xNoColor = "</FONT>";
                xItalic = "<I>";
                xNoItalic = "</I>";
                xHeading = "<H1>";
                xNoHeading = "</H1>";
                xBold = "<B>";
                xNoBold = "</B>";
                xSmall = "<FONT SIZE=\"-2\">";
                xNoSmall = "</FONT>";
                xTag = "<FONT SIZE=\"-1\">&lt;";
                xNoTag = "&gt;</FONT>";
                xEndl = "<BR>\n";
                xTab = "&nbsp;&nbsp;&nbsp;&nbsp;";
                break;
            case OUTPUT_MODE_PC:
                xPrintHTML = false;
                xBlue = xGreen = xRed = xNoColor = "";
                xItalic = "\"";
                xNoItalic = "\"";
                xHeading = xNoHeading = "";
                xBold = xNoBold = "";
                xSmall = xNoSmall = "";
                xTag = xNoTag = "";
                xEndl = "\n";
                xTab = "\t";
                break;
            case OUTPUT_MODE_XML:
                xPrintHTML = false;
                xBlue = "<emph type=\"blue\">";
                xGreen = "<emph type=\"green\">";
                xRed = "<emph type=\"red\">";
                xNoColor = "</emph>";
                xItalic = "<emph type=\"italic\">";
                xNoItalic = "</emph>";
                xHeading = xNoHeading = "";
                xBold = xNoBold = "";
                xSmall = xNoSmall = "";
                xTag = "<";
                xNoTag = ">";
                xEndl = "\n";
                xTab = "\t";
                break;
            default:
                Message.invoke(MessageType.MSG_ERROR, "unknown output mode");
        }
    }

}
