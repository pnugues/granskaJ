/* tokenizer.l
 * last change: 2000-02-02
 * input: a normal text file
 * output: tokenized text
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


%%
%unicode

/*%debug*/

DIGIT=\p{N}
DIGIT3={DIGIT}{DIGIT}{DIGIT}
YE=(1[3-9]{DIGIT}{DIGIT})|(2[01]{DIGIT}{DIGIT})
YEA={YE}|{YE}{DWS}{YE}|{YE}(\/|{DWS}|\/{DWS}){DIGIT}{DIGIT}
YEAR=({YEA}|{YE})(\-talet)?|år{SPACE_NL}*({YEA}|{YE})
TIME1=((klockan|kl\.|kl){SPACE_NL}*)?([0-1]?{DIGIT}|2[0-4])\.[0-6]{DIGIT}
TIME2=(klockan|kl\.|kl){SPACE_NL}*([0-1]?{DIGIT}|2[0-4])({PUNC}?{SPACE}*[0-6]{DIGIT})?
DIGITS={DIGIT}+
DWS={SPACE_NL}*\-{SPACE_NL}*
PARAGRAPH=§+{SPACE}*{DIGITS}({DWS}{DIGITS})?|{DIGITS}({DWS}{DIGITS})?{SPACE}*§+|§+
CARDINALSIN=1(,0+)?
CARDINAL_OK=-?(0|[1-9]{DIGIT}?{DIGIT}?({SPACE}{DIGIT3})*)(,{DIGITS})?
CARDINAL=-?{DIGITS}(({SPACE}+|{PUNC}|:{SPACE}+){DIGITS})*
PERCENTAGE={CARDINAL}({DWS}{CARDINAL})?{SPACE_NL}*(\%(\-ig)?|procent|promille)
MATH={CARDINAL}({SPACE}*[+/*=-]{SPACE}*{CARDINAL})+
ORDINAL={CARDINAL}:?[eE]
LETTER=\p{L}
LD={LETTER}|{DIGIT}
ENDER=[\.!?]
DASH=\-\n?
LWORD={LETTER}+|({LD}*{LETTER}{LD}*)
DWORD={LWORD}({DASH}{LWORD})+
WORD={LWORD}|{DWORD}
WORD_DOTS={WORD}(\.{WORD})+
SPACE=[\ \t\r\225\240]
SPACE_NL=\n|{SPACE}
AMP=[&]
LT=[<]
GT=[>]
APOS=[']
QOUT=[\"]
PUNC=[\/\.\-,:+=;\(\)\[\]_%;@~\\´\*$§#£\{\}`\^\|·»¾º]
SOME_PUNC=[\/\-+=;\(\)\[\]_%<>@~\\´\*$§#£\{\}`\^\|·»¾º]
EMAIL=({WORD}|{WORD_DOTS})@{WORD_DOTS}
PROTOCOL=({WORD}:?\/\/)|www\.
URL={PROTOCOL}{WORD}([\.\/~]|{WORD})+{WORD}\/?
DOT=((\.|:){SPACE}*)|{SPACE}+
CON=[b-df-hj-np-tv-xzB-DF-HJ-NP-TV-XZ]
C={CON}+|[eouyäEOUYÄ]
CV={CON}|{LETTER}
NL=\P{L}
MONTH=maj|((jan|feb|mar|apr|jun|jul|aug|agu|sep|okt|nov|dec)\.?{LETTER}*)

%%
TOKEN_.*                     {return Yytoken.TOKEN_UNKNOWN;}

{TIME1}                      {return Yytoken.TOKEN_TIME;}
{TIME2}                      {return Yytoken.TOKEN_TIME;}
{PARAGRAPH}                  {return Yytoken.TOKEN_PARAGRAPH;}

{C}({DOT}{CV})*{DOT}{C}\.?/{NL}   {return Yytoken.TOKEN_PARAGRAPH;}

({LWORD}\.{SPACE}*)*dr\.?/{NL}              {return Yytoken.TOKEN_ABBREVIATION;}
[eo]{DOT}?dyl\.?/{NL}       {return Yytoken.TOKEN_ABBREVIATION;}
f{DOT}?(ö|Ö)\.?/{NL}     {return Yytoken.TOKEN_ABBREVIATION;}
i{DOT}?st{DOT}?f\.?/{NL} {return Yytoken.TOKEN_ABBREVIATION;}
kungl{DOT}maj:t/{NL}     {return Yytoken.TOKEN_ABBREVIATION;}
({LWORD}\.{SPACE}*)*lic\.?/{NL}              {return Yytoken.TOKEN_ABBREVIATION;}
t{DOT}ex\.?/{NL}         {return Yytoken.TOKEN_ABBREVIATION;}

{YEAR}                      {return Yytoken.TOKEN_YEAR;}
(den{SPACE_NL}*)?({DIGIT}{DIGIT}?|{ORDINAL}){SPACE_NL}*{MONTH}({SPACE_NL}*{YEAR})? {return Yytoken.TOKEN_DATE;}

{WORD}\-({SPACE_NL}*,{SPACE_NL}*{WORD}\-)*{SPACE_NL}+(och(\/eller)?|eller|samt|som|till|resp|resp\.|respektive){SPACE_NL}+{WORD}   {return Yytoken.TOKEN_SPLIT_WORD;}
{EMAIL}                     {return Yytoken.TOKEN_E_MAIL;}
{URL}                       {return Yytoken.TOKEN_URL;}
\<HREF[^>]+\>             {return Yytoken.TOKEN_URL;}

{PERCENTAGE}                {return Yytoken.TOKEN_PERCENTAGE;}
{LWORD}                     {return Yytoken.TOKEN_SIMPLE_WORD;}
\"{WORD}\"-?{WORD}          {return Yytoken.TOKEN_WORD;}
\({WORD}\)-?{WORD}          {return Yytoken.TOKEN_WORD;}
{WORD}-?\({WORD}\)          {return Yytoken.TOKEN_WORD;}
{WORD}([:']{C})?            {return Yytoken.TOKEN_WORD;}
{CARDINAL}-{WORD}           {return Yytoken.TOKEN_WORD;}

{MATH}                      {return Yytoken.TOKEN_MATH;}
{CARDINALSIN}               {return Yytoken.TOKEN_CARDINAL_SIN;}
{CARDINAL_OK}               {return Yytoken.TOKEN_CARDINAL;}
{CARDINAL}                  {return Yytoken.TOKEN_BAD_CARDINAL;}
{ORDINAL}                   {return Yytoken.TOKEN_ORDINAL;}

{SPACE}*\n{SPACE}*(\n{SPACE}*)+  {return Yytoken.TOKEN_BEGIN_PARAGRAPH;}
{SPACE}*(\n{SPACE}*)+       {return Yytoken.TOKEN_NEWLINE;}
{SPACE}+                    {return Yytoken.TOKEN_SPACE;}
\.{ENDER}*                  {return Yytoken.TOKEN_PERIOD;}
\?{ENDER}*                  {return Yytoken.TOKEN_QUESTION_MARK;}
\!{ENDER}*                  {return Yytoken.TOKEN_EXCLAMATION_MARK;}
\(                          {return Yytoken.TOKEN_LEFT_PAR;}
\)                          {return Yytoken.TOKEN_RIGHT_PAR;}
\"                          {return Yytoken.TOKEN_CITATION;}
{SOME_PUNC}+                {return Yytoken.TOKEN_PUNCTUATION;}
{PUNC}                      {return Yytoken.TOKEN_PUNCTUATION;}
.                           {return Yytoken.TOKEN_PUNCTUATION;}
{AMP}                       {return Yytoken.TOKEN_PUNCTUATION;}
{LT}                        {return Yytoken.TOKEN_PUNCTUATION;}
{GT}                        {return Yytoken.TOKEN_PUNCTUATION;}
{APOS}                      {return Yytoken.TOKEN_PUNCTUATION;}
{QOUT}                      {return Yytoken.TOKEN_PUNCTUATION;}
<<EOF>>                     {return Yytoken.TOKEN_END;}