# Group F: Abdul Chattha, Jonathan Martinez, and Jalen White

## Team contribution
### Absyn - Abdul 
I handled the absyn portion of the project, with the assistance of Claude. I found ai tools to be very helpful, especially in understanding the differences between tiger and C. In print.java, I chose to modify the existing code method by method and then used it to help me code the C only structs afterwards.

### Exp-Stm and decls - Jonathan
I handled exressions and statements for this, and also wrote test cases. I used claude and it helpped me understand all of the shift/reduce conflicts in CUP and how to use precedence declarations to resolve them. For declarations, I used the professor's grammar and also added the error recovery. 

### Testing - Jalen
I was tasked with testing the actual code using Jonathan's test code.

## What works

- All C89 expressions (section 3.3) except the two rules the handout removes (casts and `sizeof` of an expression) and anything involving floats or function pointers. `sizeof ( type )` works.
- All C89 statements (section 3.6) except `switch`, `case` and `default`.
- The professor's simplified declaration grammar: `var`, `fun` definitions and prototypes, `typedef`, struct/union, enum, and nested initializers.
- Error recovery for bad declarations, statements and parenthesized expressions.
- 1 shift/reduce conflict (the dangling else, harmless), 0 reduce/reduce conflicts.

## Parsing conflicts and error recovery
### Parsing
Without any precedence declarations, Cup had over 300 shift/reduce conflicts, the majority of which came from binary operators and one from the dangling else. The precedence declarations resolves the binary operator errors. The dangling else is harmless since it attaches itself the closest if, which is how it's done in C, so it's fine. There are no reduce/reduce conflicts.
### Error recover
there were bad declarations, statements and expressions inside paranthesis, which were caught by error rules. The parser skips to the next ;, }, or ) and keeps parsing the file.  The errors in test/errors.c were reported, and the code around still showed up in the tree.