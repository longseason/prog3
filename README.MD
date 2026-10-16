# this project was done alone alongside the help of Claude, an llm

## what didnt work
the scanner does not catch certains mistakes in the scanner. for example, empty characters and ones with too many characters might be accepted instead of erroring out. if a scanner enters a comment but does not receive the end slash, it will end and print eof instead of throwing an error.

## comments, ints, strings, control characters
i implemented comments using a comments state, which allowed for no other rules applied.
this made it so that nothing inside of the comment would produce tokens.
intlits have separate patterns for decimals, octals, and hex's with optional suffixes.
here, the longest match decides which one get's chosen.
similar to comments, strings also have their own class that has a buffer.  
the buffer reads/grabs decoded characters until the quote ends string.
control characters are ignored and skipped 

## ai usage
i used claude to help me debug my code and also for pasting boilerplate code.
by boilerplate code, i specifically mean that I worked on sym.java myself, and then had claude copy and paste
the token constants into main and c.lex to save me time copying and pasting myself.
on c.lex and implementing the above, i had claude debug my initial comments solution (claude debugged most of my work).
the issue there was that my keywords and operators had no state prefix, so their tokens were counted whiile they should have been commented out.
i also used claude for the other implementation (ints, strings, control characters) by first asking for understanding, and then modifying the code I was given.
additionally, i also had claude give me test cases to test while i implemented each thing.
