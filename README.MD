# Group F: Abdul Chattha, Jonathan Martinez, and Jalen White

## Team contribution
### Absyn - Abdul 

### Expressions - Jonathan
I handled the expression type checking in `Semant/Semant.java`, with the assistance of Claude. I added a `transExp` function for every expression node from project 2 (identifiers, binary and unary operators, assignment, `?:`, comma, function calls, subscripts, `.` and `->`, `sizeof`) and added each one to the dispatch function. I found AI tools helpful for working out which nodes need type checking beyond calling translate on their children.

Design decisions:
- Math operations (arithmetic, shifts, bitwise, unary `-`/`+`/`~`, `++`/`--`, and compound assignment like `+=`) only accept numbers, so `1 + 'a'` is an error. This also makes pointer arithmetic (`p + 1`) an error.
- Comparisons need both sides to be the same kind of type, so `'a' < 100` is an error. A number can be compared with a pointer, since numbers can be assigned to pointers.
- `&&`, `||`, `!` and the condition of `?:` accept a number, char, or pointer.
- Assignment needs an assignable left side (variable, subscript, member, or `*p`) and a right side that matches its type.
- Calls check the number of arguments and the type of each argument against the function's entry in the symbol table.

### ??? - Jalen