# Group F: Abdul Chattha, Jonathan Martinez, and Jalen White

## What is implemented
- Everything specificed in the document was implemented, and we also removed or skipped the stuff that wasn't necessary like static link, leaf function, and loopvar entry.

## Design decisions 
- We sepearate chars and ints as separate types.
- Ints can be assigned to any pointer tpye since we removed nil (because nil doesnt exist in C)
- Array init. must have same number of elements as declared size or else it'll fail
-  Char arrays have to be initilaized by a list of chars
- Unions can only be initialized with single value, not braces

## Team contribution
### Types, declarations, functions - Abdul 
- I did types, declarations, and functions for the project. For types, I added classes for chars, pointers, and structs while removing strings and nil since they're not used in C. For declarations, I added methods to make objects into a type object alongside methods to handle typedef, structs and unions, and variable declaration. For functions, they all store their parameters + return types to be checked while the function bodies have their own scope, possessing the parameters as variables. Like previously, I made use of ai to help break down parts I was unfamiliar with, to help me code alongside, and also help me with functions I struggled with, I made sure to mark functions/lines I used ai for.

### Expressions, Statements, Activation Records - Jonathan
- I did the expression, statement, and activation record parts. I added type checking for every expression and statement node in Semant, wrote the escape analysis for C, and finished the MIPS frame so each function gets its own level and variables are placed in the frame or a register. I also used AI for this project to help code side by side as I work through it.

### Testing - Jalen
- I tested to make sure everything was functional before submitting.