package tagger;

public enum Yytoken {                 // examples:
    TOKEN_END,
    TOKEN_WORD,                // IT-användning
    TOKEN_SIMPLE_WORD,         // hej
    TOKEN_SPLIT_WORD,          // hälso- och sjukvård, a-, b- och c-uppgiften
    TOKEN_ABBREVIATION,        // t.ex., p
    TOKEN_CARDINAL,            // 675 000, -1,5
    TOKEN_BAD_CARDINAL,        // 6750 000, 45.4
    TOKEN_CARDINAL_SIN,        // 1, 1,0000
    TOKEN_ORDINAL,             // 6:e
    TOKEN_PERCENTAGE,          // 22 %
    TOKEN_MATH,                // 1+1 = 3
    TOKEN_YEAR,                // 1927, år 2000
    TOKEN_DATE,                // den 4 november 1927, 4 dec.
    TOKEN_TIME,                // klockan 9, kl. 14.37
    TOKEN_PARAGRAPH,           // $ 7, 7 $, $7, $$ 7-9, $
    TOKEN_PUNCTUATION,         // ,
    TOKEN_PERIOD,              // .
    TOKEN_QUESTION_MARK,       // ?
    TOKEN_EXCLAMATION_MARK,    // !
    TOKEN_LEFT_PAR,            // (
    TOKEN_RIGHT_PAR,           // )
    TOKEN_CITATION,            // "\""
    TOKEN_E_MAIL,              // jfc@nada.kth.se
    TOKEN_URL,                 // http://www.nada.kth.se/~jfc/
    TOKEN_SPACE,               // \t
    TOKEN_NEWLINE,             // \n
    TOKEN_BEGIN_TITLE,
    TOKEN_END_TITLE,
    TOKEN_BEGIN_HEADING,
    TOKEN_END_HEADING,
    TOKEN_BEGIN_PARAGRAPH,
    TOKEN_BEGIN_TABLE,
    TOKEN_TABLE_TAB,
    TOKEN_END_TABLE,
    TOKEN_PROPER_NOUN,
    TOKEN_PROPER_NOUN_GENITIVE,
    TOKEN_DELIMITER_PERIOD,
    TOKEN_DELIMITER_QUESTION,
    TOKEN_DELIMITER_EXCLAMATION,
    TOKEN_DELIMITER_HEADING,
    TOKEN_DELIMITER_OTHER,
    TOKEN_SILLY,
    TOKEN_ERROR,
    TOKEN_UNKNOWN
}
