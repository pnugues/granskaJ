package tagger;

import common.Basics;
import common.Message;
import common.MessageType;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
@Author: Pierre Nugues
Translated and adapted to Java
from a program by Johan Carlberger
*/

/* main.cc
 * author: Johan Carlberger
 * last change: 2001-12-06
 * comments: main for Tagger only
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

public class Main {

    static public Tagger tagger;
    static boolean xGenerateInflections = false;
    static boolean xReadTaggedText = false;

    static void printUsage(String progName) {

        System.out.println("usage:");
        System.err.println(progName + '\t' + "[-ABDMNPUatuv] [-l lexicon-directory]");
        System.err.println('\t' + "[textFile]*");
        System.err.println("input:");
        System.err.println('\t' + "N:" + Basics.dont(Settings.xNewlineMeansNewSentence) + "interpret newline to indicate end of sentence/paragraph");
        System.err.println('\t' + "Q:" + Basics.dont(xReadTaggedText) + "read text that is already tagged");
        System.err.println('\t' + "R:" + Basics.dont(Settings.xNoCollocations) + "make collocations many tokens");
        System.err.println("output:");
        System.err.println('\t' + "A:" + Basics.dont(Settings.xPrintAllWords) + "print all words");
        System.err.println('\t' + "B:" + Basics.dont(Settings.xPrintLemma) + "print lemma");
        System.err.println('\t' + "D:" + Basics.dont(Settings.xListMultipleLemmas) + "list multiple lemma");
        System.err.println('\t' + "G:" + Basics.dont(xGenerateInflections) + "generate inflections");
        System.err.println('\t' + "I:" + Basics.dont(Settings.xPrintWordInfo) + "print words info");
        System.err.println('\t' + "L:" + Basics.dont(Settings.xPrintLexicalProbs) + "print lexical probs");
        System.err.println('\t' + "M:" + Basics.dont(Settings.xPrintAllWordTags) + "print all word-tags of words");
        System.err.println('\t' + "P:" + Basics.dont(Settings.xPrintProbs) + "print estimated probabilities of chosen tags");
        System.err.println('\t' + "S:" + Basics.dont(Settings.xPrintSelectedTag) + "print selected tag");
        System.err.println('\t' + "W:" + Basics.dont(Settings.xOutputWTL) + "print wtl-format");
        System.err.println("tagging mode:");
        System.err.println('\t' + "a:" + Basics.dont(!!Settings.xAmbiguousNewWords) + "tag new words ambiguously");
        System.err.println('\t' + "u:" + Basics.dont(Settings.xAnalyzeNewWords) + "analyze new words");
        System.err.println("diagnostics:");
        System.err.println('\t' + "F:" + Basics.dont(Settings.xTryLatestFeature) + "try latest feature");
        System.err.println('\t' + "f:" + Basics.dont(Settings.xTestFeatures) + "test features");
        System.err.println('\t' + "t:" + Basics.dont(Settings.xTakeTime) + "take time");
        System.err.println('\t' + "v:" + Basics.dont(Settings.xVerbose) + "verbose");
        System.err.println("if no lexicon directory is given with option -l, the program uses the path");

        String temp = System.getenv("TAGGER_LEXICON");
        if (temp == null)
            temp = "";  // seg fault on 'std::cerr << (char*) 0 << std::endl;'
        System.err.println("in environment variable TAGGER_LEXICON = " + temp);
        System.err.println("or the current working directory if TAGGER_LEXICON is null");
        temp = System.getenv("TAGGER_TEST_TEXT");
        if (temp == null)
            temp = "";
        System.err.println("if no test text is given, the program uses the path");
        System.err.println("in environment variable TAGGER_TEST_TEXT = " + temp);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        List<String> argv = new ArrayList<>();
        argv.add(new Object() {
        }.getClass().getEnclosingClass().toString());
        Collections.addAll(argv, args);
        int argc = argv.size();

        String lexiconDir = System.getenv("TAGGER_LEXICON");
        int i;
        Settings.xPrintAllWords = Settings.xPrintSelectedTag = true;
        for (i = 1; i < argc && argv.get(i).charAt(0) == '-'; i++)
            if (argv.get(i).charAt(1) == 'l') {
                if (++i < argc)
                    lexiconDir = argv.get(i);
                else {
                    printUsage(argv.get(0));
                    System.exit(1);
                    return;
                }
            } else for (int j = 1; j < argv.get(i).length(); j++) {
                switch (argv.get(i).charAt(j)) {
                    case 'A':
                        Settings.xPrintAllWords = !Settings.xPrintAllWords;
                        break;
                    case 'B':
                        Settings.xPrintLemma = !Settings.xPrintLemma;
                        break;
                    case 'D':
                        Settings.xListMultipleLemmas = !Settings.xListMultipleLemmas;
                        break;
                    case 'G':
                        xGenerateInflections = !xGenerateInflections;
                        break;
                    case 'I':
                        Settings.xPrintWordInfo = !Settings.xPrintWordInfo;
                        break;
                    case 'L':
                        Settings.xPrintLexicalProbs = !Settings.xPrintLexicalProbs;
                        break;
                    case 'M':
                        Settings.xPrintAllWordTags = !Settings.xPrintAllWordTags;
                        break;
                    case 'N':
                        Settings.xNewlineMeansNewSentence = !Settings.xNewlineMeansNewSentence;
                        break;
                    case 'P':
                        Settings.xPrintProbs = !Settings.xPrintProbs;
                        break;
                    case 'S':
                        break;
                    case 'W':
                        Settings.xOutputWTL = !Settings.xOutputWTL;
                        break;
                    case 'Q':
                        xReadTaggedText = !xReadTaggedText;
                        break;
                    case 'R':
                        Settings.xNoCollocations = !Settings.xNoCollocations;
                        break;
                    case 'a':
                        Settings.xAmbiguousNewWords = !Settings.xAmbiguousNewWords;
                        break;
                    case 'f':
                        Settings.xTestFeatures = !Settings.xTestFeatures;
                        break;
                    case 'F':
                        Settings.xTryLatestFeature = !Settings.xTryLatestFeature;
                        break;
                    case 't':
                        Settings.xTakeTime = !Settings.xTakeTime;
                        break;
                    case 'u':
                        Settings.xAnalyzeNewWords = !Settings.xAnalyzeNewWords;
                        break;
                    case 'v':
                        Settings.xVerbose = !Settings.xVerbose;
                        break;
                    default:
                        printUsage(new Object() {
                        }.getClass().getEnclosingClass().toString());
                        return;
                }
            }
        if (lexiconDir == null)
            lexiconDir = ".";
        if (i == argc) {
            argv.add(System.getenv("TAGGER_TEST_TEXT"));
            if (argv.get(i) == null) {
                printUsage(argv.get(0));
                return;
            }
            argc++;
        }
        if (Settings.xTakeTime) {
            Settings.xPrintProbs = Settings.xPrintAllWords = false;
        } else if (Settings.xPrintProbs) { // jonas
            Settings.xPrintAllWords = false; // jonas
            Settings.xPrintSelectedTag = true;
        } else if (!Settings.xPrintAllWords && !Settings.xPrintAllWordTags)
            Settings.xPrintAllWords = Settings.xPrintSelectedTag = true;

        //  std::cout.precision(0);
        //std::cout.setf(std::ios::fixed);
        // PN. Remove the fast files for now
        if (Files.exists(Paths.get("lex/morfs/fast"))) {
            Files.delete(Paths.get("lex/morfs/fast"));

        }
        if (Files.exists(Paths.get("lex/words/fast"))) {
            Files.delete(Paths.get("lex/words/fast"));
        }
        if (Files.exists(Paths.get("lex/tags/fast"))) {
            Files.delete(Paths.get("lex/tags/fast"));
        }
        tagger = new Tagger();
        if (!tagger.load(lexiconDir))
            return;
        Message.invoke(MessageType.MSG_COUNTS, "during loading");
        if (xGenerateInflections) {
            tagger.generateInflections();
            return;
        }
        if (Settings.xVerbose)
            System.out.println(argv.get(0) + ' ' + Settings.xVersion);
        for (; i < argc; i++) {
            String dir = "";
            String fileName = Paths.get(dir, argv.get(i)).toString();
            InputStream in = new BufferedInputStream(new FileInputStream(argv.get(i)));
            //in.seekg(0, std::ios::end); // jonas
            int inlength = -1; // jonas
            //int inlength = in.tellg(); // jonas
            //in.seekg(0, std::ios::beg); // jonas
            tagger.setStream(in, inlength);
            if (xReadTaggedText) {
                tagger.readTaggedTextQnD();
                //      tagger.TagText();
            } else {
                tagger.readText();
                tagger.tagText();
            }
            if (Settings.xTakeTime) tagger.printTimes();
            if (Settings.xPrintAllWords || Settings.xPrintCorrectTag || Settings.xOutputWTL)
                tagger.getText().print(System.out);
            in.close();
        }
        Message.invoke(MessageType.MSG_COUNTS, "during tagging");
    }

}