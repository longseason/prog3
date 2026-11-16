# Group F: Abdul Chattha, Jonathan Martinez, and Jalen White

## What is implemented
- Everything specificed in the document was implemented, and we also removed or skipped the stuff that wasn't necessary like static link, leaf function, and loopvar entry.

## Team contribution
### Types, declarations, functions - Abdul 
- I did types, declarations, and functions for the project. For types, I added classes for chars, pointers, and structs while removing strings and nil since they're not used in C. For declarations, I added methods to make objects into a type object alongside methods to handle typedef, structs and unions, and variable declaration. For functions, they all store their parameters + return types to be checked while the function bodies have their own scope, possessing the parameters as variables. Like previously, I made use of ai to help break down parts I was unfamiliar with, to help me code alongside, and also help me with functions I struggled with, I made sure to mark functions/lines I used ai for.

### Expressions, Statements, Activation Records - Jonathan
- I did the expression, statement, and activation record parts. I added type checking for every expression and statement node in Semant, wrote the escape analysis for C, and finished the MIPS frame so each function gets its own level and variables are placed in the frame or a register. I also used AI for this project to help code side by side as I work through it.

### Testing - Jalen
- I tested to make sure everything was functional before submitting.