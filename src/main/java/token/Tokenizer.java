package token;

import tagger.Yytoken;

public class Tokenizer extends Yylex {
    public static int N_TOKENS = Yytoken.TOKEN_UNKNOWN.ordinal() + 1;
    java.io.Reader reader;


    public Tokenizer(java.io.Reader reader) {
        super(reader);
        this.reader = reader;
    }

    public static String token2String(Yytoken t) {
        switch (t) {
            case TOKEN_END:
                return "TOKEN_END";
            case TOKEN_WORD:
                return "TOKEN_WORD";
            case TOKEN_SIMPLE_WORD:
                return "TOKEN_SIMPLE_WORD";
            case TOKEN_SPLIT_WORD:
                return "TOKEN_SPLIT_WORD";
            case TOKEN_ABBREVIATION:
                return "TOKEN_ABBREVIATION";
            case TOKEN_LEFT_PAR:
                return "TOKEN_LEFT_PAR";
            case TOKEN_RIGHT_PAR:
                return "TOKEN_RIGHT_PAR";
            case TOKEN_CITATION:
                return "TOKEN_CITATION";
            case TOKEN_YEAR:
                return "TOKEN_YEAR";
            case TOKEN_DATE:
                return "TOKEN_DATE";
            case TOKEN_TIME:
                return "TOKEN_TIME";
            case TOKEN_PARAGRAPH:
                return "TOKEN_PARAGRAPH";
            case TOKEN_ORDINAL:
                return "TOKEN_ORDINAL";
            case TOKEN_CARDINAL:
                return "TOKEN_CARDINAL";
            case TOKEN_BAD_CARDINAL:
                return "TOKEN_BAD_CARDINAL";
            case TOKEN_CARDINAL_SIN:
                return "TOKEN_CARDINAL_SIN";
            case TOKEN_PERCENTAGE:
                return "TOKEN_PERCENTAGE";
            case TOKEN_MATH:
                return "TOKEN_MATH";
            case TOKEN_PERIOD:
                return "TOKEN_PERIOD";
            case TOKEN_QUESTION_MARK:
                return "TOKEN_QUESTION_MARK";
            case TOKEN_EXCLAMATION_MARK:
                return "TOKEN_EXCLAMATION_MARK";
            case TOKEN_BEGIN_TITLE:
                return "TOKEN_BEGIN_TITLE";
            case TOKEN_END_TITLE:
                return "TOKEN_END_TITLE";
            case TOKEN_BEGIN_HEADING:
                return "TOKEN_BEGIN_HEADING";
            case TOKEN_END_HEADING:
                return "TOKEN_END_HEADING";
            case TOKEN_BEGIN_PARAGRAPH:
                return "TOKEN_BEGIN_PARAGRAPH";
            case TOKEN_BEGIN_TABLE:
                return "TOKEN_BEGIN_TABLE";
            case TOKEN_TABLE_TAB:
                return "TOKEN_TABLE_TAB";
            case TOKEN_END_TABLE:
                return "TOKEN_END_TABLE";
            case TOKEN_PROPER_NOUN:
                return "TOKEN_PROPER_NOUN";
            case TOKEN_PROPER_NOUN_GENITIVE:
                return "TOKEN_PROPER_NOUN_GENITIVE";
            case TOKEN_DELIMITER_PERIOD:
                return "TOKEN_DELIMITER_PERIOD";
            case TOKEN_DELIMITER_QUESTION:
                return "TOKEN_DELIMITER_QUESTION";
            case TOKEN_DELIMITER_EXCLAMATION:
                return "TOKEN_DELIMITER_EXCLAMATION";
            case TOKEN_DELIMITER_HEADING:
                return "TOKEN_DELIMITER_HEADING";
            case TOKEN_DELIMITER_OTHER:
                return "TOKEN_DELIMITER_OTHER";
            case TOKEN_SILLY:
                return "TOKEN_SILLY";
            case TOKEN_PUNCTUATION:
                return "TOKEN_PUNCTUATION";
            case TOKEN_E_MAIL:
                return "TOKEN_E_MAIL";
            case TOKEN_URL:
                return "TOKEN_URL";
            case TOKEN_NEWLINE:
                return "TOKEN_NEWLINE";
            case TOKEN_SPACE:
                return "TOKEN_SPACE";
            case TOKEN_ERROR:
                return "TOKEN_ERROR";
            case TOKEN_UNKNOWN:
                return "TOKEN_UNKNOWN";
        }
        return "TOKEN_UNKNOWN";
    }

    public static Yytoken string2Token(String s) {
        if (s.equals("TOKEN_BEGIN_TITLE"))
            return Yytoken.TOKEN_BEGIN_TITLE;
        if (s.equals("TOKEN_END_TITLE"))
            return Yytoken.TOKEN_END_TITLE;
        if (s.equals("TOKEN_BEGIN_HEADING"))
            return Yytoken.TOKEN_BEGIN_HEADING;
        if (s.equals("TOKEN_END_HEADING"))
            return Yytoken.TOKEN_END_HEADING;
        if (s.equals("TOKEN_BEGIN_PARAGRAPH"))
            return Yytoken.TOKEN_BEGIN_PARAGRAPH;
        if (s.equals("TOKEN_BEGIN_TABLE"))
            return Yytoken.TOKEN_BEGIN_TABLE;
        if (s.equals("TOKEN_TABLE_TAB"))
            return Yytoken.TOKEN_TABLE_TAB;
        if (s.equals("TOKEN_END_TABLE"))
            return Yytoken.TOKEN_END_TABLE;
        if (s.equals("TOKEN_ERROR"))
            return Yytoken.TOKEN_ERROR;
        if (s.equals("TOKEN_END"))
            return Yytoken.TOKEN_END;
        if (s.equals("TOKEN_WORD"))
            return Yytoken.TOKEN_WORD;
        if (s.equals("TOKEN_LEFT_PAR"))
            return Yytoken.TOKEN_LEFT_PAR;
        if (s.equals("TOKEN_RIGHT_PAR"))
            return Yytoken.TOKEN_RIGHT_PAR;
        if (s.equals("TOKEN_CITATION"))
            return Yytoken.TOKEN_CITATION;
        if (s.equals("TOKEN_SIMPLE_WORD"))
            return Yytoken.TOKEN_SIMPLE_WORD;
        if (s.equals("TOKEN_SPLIT_WORD"))
            return Yytoken.TOKEN_SPLIT_WORD;
        if (s.equals("TOKEN_ABBREVIATION"))
            return Yytoken.TOKEN_ABBREVIATION;
        if (s.equals("TOKEN_YEAR"))
            return Yytoken.TOKEN_YEAR;
        if (s.equals("TOKEN_DATE"))
            return Yytoken.TOKEN_DATE;
        if (s.equals("TOKEN_TIME"))
            return Yytoken.TOKEN_TIME;
        if (s.equals("TOKEN_PARAGRAPH"))
            return Yytoken.TOKEN_PARAGRAPH;
        if (s.equals("TOKEN_ORDINAL"))
            return Yytoken.TOKEN_ORDINAL;
        if (s.equals("TOKEN_CARDINAL"))
            return Yytoken.TOKEN_CARDINAL;
        if (s.equals("TOKEN_BAD_CARDINAL"))
            return Yytoken.TOKEN_BAD_CARDINAL;
        if (s.equals("TOKEN_CARDINAL_SIN"))
            return Yytoken.TOKEN_CARDINAL_SIN;
        if (s.equals("TOKEN_PERCENTAGE"))
            return Yytoken.TOKEN_PERCENTAGE;
        if (s.equals("TOKEN_MATH"))
            return Yytoken.TOKEN_MATH;
        if (s.equals("TOKEN_PUNCTUATION"))
            return Yytoken.TOKEN_PUNCTUATION;
        if (s.equals("TOKEN_PERIOD"))
            return Yytoken.TOKEN_PERIOD;
        if (s.equals("TOKEN_QUESTION_MARK"))
            return Yytoken.TOKEN_QUESTION_MARK;
        if (s.equals("TOKEN_EXCLAMATION_MARK"))
            return Yytoken.TOKEN_EXCLAMATION_MARK;
        if (s.equals("TOKEN_PROPER_NOUN"))
            return Yytoken.TOKEN_PROPER_NOUN;
        if (s.equals("TOKEN_PROPER_NOUN_GENITIVE"))
            return Yytoken.TOKEN_PROPER_NOUN_GENITIVE;
        if (s.equals("TOKEN_E_MAIL"))
            return Yytoken.TOKEN_E_MAIL;
        if (s.equals("TOKEN_URL"))
            return Yytoken.TOKEN_URL;
        if (s.equals("TOKEN_NEWLINE"))
            return Yytoken.TOKEN_NEWLINE;
        if (s.equals("TOKEN_SPACE"))
            return Yytoken.TOKEN_SPACE;
        if (s.equals("TOKEN_SILLY"))
            return Yytoken.TOKEN_SILLY;
        if (s.equals("TOKEN_DELIMITER_PERIOD"))
            return Yytoken.TOKEN_DELIMITER_PERIOD;
        if (s.equals("TOKEN_DELIMITER_QUESTION"))
            return Yytoken.TOKEN_DELIMITER_QUESTION;
        if (s.equals("TOKEN_DELIMITER_EXCLAMATION"))
            return Yytoken.TOKEN_DELIMITER_EXCLAMATION;
        if (s.equals("TOKEN_DELIMITER_HEADING"))
            return Yytoken.TOKEN_DELIMITER_HEADING;
        if (s.equals("TOKEN_DELIMITER_OTHER"))
            return Yytoken.TOKEN_DELIMITER_OTHER;
        if (s.equals("TOKEN_ERROR"))
            return Yytoken.TOKEN_ERROR;
        if (s.equals("TOKEN_UNKNOWN"))
            return Yytoken.TOKEN_UNKNOWN;
        return Yytoken.TOKEN_ERROR;
    }

    public static void main(String argv[]) {
        if (argv.length == 0) {
            System.out.println("Usage : java Tokenizer [ --encoding <name> ] <inputfile(s)>");
        } else {
            int firstFilePos = 0;
            String encodingName = "UTF-8";
            if (argv[0].equals("--encoding")) {
                firstFilePos = 2;
                encodingName = argv[1];
                try {
                    // Side-effect: is encodingName valid?
                    java.nio.charset.Charset.forName(encodingName);
                } catch (Exception e) {
                    System.out.println("Invalid encoding '" + encodingName + "'");
                    return;
                }
            }
            for (int i = firstFilePos; i < argv.length; i++) {
                Tokenizer scanner = null;
                try {
                    java.io.FileInputStream stream = new java.io.FileInputStream(argv[i]);
                    java.io.Reader reader = new java.io.InputStreamReader(stream, encodingName);
                    scanner = new Tokenizer(reader);
                    Yytoken token;
                    //scanner.ParseText();
                    do {
                        token = scanner.parse();
                        System.out.print(scanner.tokenString() + '\t');
                        System.out.print(scanner.tokenLength());
                        System.out.print('\t');
                        System.out.println(token);
                    } while (token != Yytoken.TOKEN_END);

                } catch (java.io.FileNotFoundException e) {
                    System.out.println("File not found : \"" + argv[i] + "\"");
                } catch (java.io.IOException e) {
                    System.out.println("IO error scanning file \"" + argv[i] + "\"");
                    System.out.println(e);
                } catch (Exception e) {
                    System.out.println("Unexpected exception:");
                    e.printStackTrace();
                }
            }
        }
    }

    // PN. I could not understand what this does
    // Check later
    //void SetStream(InputStream instream) { switch_streams(instream, NULL); }

    public Yytoken parse() throws java.io.IOException {
        return this.yylex();
    }

    public int tokenLength() {
        return this.yylength();
    }

    public String tokenString() {
        return this.yytext();
    }

    void parseText() throws java.io.IOException {
        Yytoken token;
        while ((token = parse()) != Yytoken.TOKEN_END) {
            if (token != Yytoken.TOKEN_SPACE) {
                if (token != Yytoken.TOKEN_NEWLINE && token != Yytoken.TOKEN_BEGIN_PARAGRAPH) {
                    System.out.print(tokenString());
                }
                System.out.print('\t');
                System.out.println(token);
            }
        }
    }

    int checkTokens() {
        for (int i = 0; i < N_TOKENS; i++) {
            Yytoken t = Yytoken.values()[i];
            if (string2Token(token2String(t)) != t) {
                System.out.println("CheckTokens(): Token[" + i + "] = " + t + "doesn't work");
                return 0;
            }
        }
        return 1;
    }
}