# granskaJ: Granska in Java

Granska is a part-of-speech tagger for Swedish. It was developed by a team at KTH at the turn of the 2000s 
and its original source code is available from this link: https://github.com/viggokann/granska

Granska was a very good tagger, both accurate and fast, with lemmatization included. Many people, in the Swedish NLP 
community, used it in their projects.

As the program was written in a nonevolvable C++, it got more and more complicated to compile. Every time I changed my computer,
I had to customize the Makefile. With the UTF-8 and C++ evolutions, I thought that one day, I could no longer compile it and 
I would have to dump all the programs that depended on it.

I first tried to find a version of Granska in Java, a language that I believe makes it easier to maintain programs. I could not find any. So with the Corona crisis, 
I translated it myself. I also did it for the lex files that are now in JSON. And here is the result.

So, for the people, who loved Granska, granskaJ is an equivalent in Java.

This new version is far from being optimized and may still have translation bugs. Nonetheless, I hope some people will find this translation useful and, possibly, decide to improve it.
