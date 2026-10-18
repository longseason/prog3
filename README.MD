# Group F: Abdul Chattha, Jonathan Martinez, and Jalen White

## Team contribution
### Absyn - Abdul 
I handled the absyn portion of the project, with the assistance of Claude. I found ai tools to be very helpful, especially in understanding the differences between tiger and C. In print.java, I chose to modify the existing code method by method and then used it to help me code the C only structs afterwards.

### Exp-Stm - Jonathan
I handled the expression and statement portion of `Parse/Grm.cup`, with the assistance of Claude, and wrote the tests in `tests/expressions.c` and `tests/statements.c`. I found AI tools to be helpful, especially for understanding the shift/reduce conflicts CUP reported and how precedence declarations resolve them. I built and tested the grammar and checked the printed trees for precedence, associativity, and the dangling else.

### Decls - Jonathan
I handled the declaration portion of `Parse/Grm.cup`, with the assistance of Claude, following the professor's simplified declaration grammar, and wrote `tests/declarations.c` and `tests/errors.c`. I also added the error recovery, updated `Parse/Parse.java`, `Parse/Main.java` and the `Makefile`, and made two small additions to the Absyn package: a `ParenExp` class (the handout asks that parentheses show up in the tree) and a fix so function bodies print indented.

## Build and run

    make
    java Parse.Main tests/expressions.c

`make` runs JLex on `Parse/C.lex`, runs CUP on `Parse/Grm.cup` (with `-expect 1`), shrinks the generated tables (see *Build notes*), and compiles `ErrorMsg`, `Symbol`, `Absyn` and `Parse`. `make clean` removes every generated file, including `Parse/sym.java`, which CUP generates from the terminal list in `Grm.cup`.

## What works

- All C89 expressions (section 3.3) except the two rules the handout removes (casts and `sizeof` of an expression) and anything involving floats or function pointers. `sizeof ( type )` works.
- All C89 statements (section 3.6) except `switch`, `case` and `default`.
- The professor's simplified declaration grammar: `var`, `fun` definitions and prototypes, `typedef`, struct/union, enum, and nested initializers.
- Error recovery for bad declarations, statements and parenthesized expressions.
- 1 shift/reduce conflict (the dangling else, harmless), 0 reduce/reduce conflicts.

## How expressions are parsed

The grammar follows C89 section 3.3 with one simplification:

- **Unary, postfix, conditional (`?:`), assignment and comma** keep the C89 layered rules. So unary operators always bind tighter than binary ones (`-a * b` is `(-a) * b`), the left side of an assignment must be a `unary_expression` (`a + b = c` is a syntax error, as in C), and assignment is right-associative (`x = y = 0`).
- **The ten binary levels** (`* / %`, `+ -`, `<< >>`, `< > <= >=`, `== !=`, `&`, `^`, `|`, `&&`, `||`) are folded into one ambiguous rule, `binary_expression ::= binary_expression OP binary_expression`, with one alternative per operator. The `precedence` declarations put them back in C's order.
- Parentheses are kept as `ParenExp`. Function calls are by name only (`CallExp` holds a `Symbol`), since function pointers are excluded.
- Integer constants come from the lexer as text (`INTLIT`, e.g. `0x1F` or `42UL`); the grammar strips the suffix and converts hex/octal/decimal into `IntExp`.

Every operator is tested in `tests/expressions.c`, including precedence checks such as `a + b * 2`, `a - b - 1`, `a || b && x`, and `a & b == 1` (which is `a & (b == 1)` in C).

## How statements are parsed

Straight from C89 section 3.6 without `switch`/`case`/`default`: compound statements (declarations first, then statements, both optional), expression statements, the empty statement `;` (an `ExpStatement` with a null expression), labels (`ID : statement`), `if`/`else`, `while`, `do`/`while`, `for` (each of the three parts optional), `goto`, `continue`, `break`, and `return` with or without a value. `tests/statements.c` covers all of them.

## Parsing conflicts and how we resolved them

Without any precedence declarations CUP reports **325** shift/reduce conflicts:

- **324 from the binary operators.** For `a OP1 b OP2 c` the parser can reduce `a OP1 b` or shift `OP2`. That is 18 operators × 18 operators = 324 conflicts. The ten `precedence left ...` declarations in `Grm.cup` resolve all of them: higher precedence wins, and `left` makes equal-precedence operators group to the left (`(a - b) - 1`).
- **1 from the dangling else**, which we leave in the grammar (`-expect 1` in the Makefile). In `if (a) if (b) x = 1; else x = 2;`, after `x = 1;` the parser can reduce the inner `if` (giving the `else` to the outer one) or shift `ELSE` (giving it to the inner one). CUP resolves it by shifting, which is exactly C's rule that an `else` belongs to the nearest `if`. It is harmless because the default choice is the correct one; `tests/statements.c` checks it and the tree shows the `else` on the inner `IfElseStatement`.

There are **no reduce/reduce conflicts**. Two places look like they might have one but don't:

- `parameters` and `type_parameters` both allow `( )`. After `( )` a definition is always followed by `{` and a prototype by `;`, so one token of lookahead picks the right reduction.
- A statement can start with `ID :` (a label) or with an identifier expression. The label rule only exists at the start of a statement, where `:` can't follow an expression, so LALR(1) keeps the two apart.

## How declarations are parsed

`Grm.cup` implements the professor's "Changes to C Declarations" grammar rule for rule, with the same nonterminal names (`bitfield_list`, `type_augments`, `brackets_list`, `empty_array_type_list`, `expression_array_type_list`, `type_parameters`, ...). A program is a list of these declarations.

- **Types** are a `type_name`, then pointer stars, then array brackets. The brackets are either all empty or all sized, so `int[][]` and `int****[22][3]` are valid but `int**[1][]` and `int[][10]` are not. Sizes are constant expressions, so `int[a]` is valid syntax.
- **Type nodes** nest like C: `int**[10][33]` becomes `ArrayType(ArrayType(PointerType(PointerType(NameType(int))), 33), 10)`, an array of 10 arrays of 33 `int**`. An empty bracket has a null size.
- `bitfield_list` (`const volatile extern static auto register`) becomes a `DescriptorList`.
- **Function definitions** name every parameter; **prototypes** list types only (`fun int printf(char*, ...);`). `...` becomes a final `Param` with `ellipsis = true`.
- **Struct/union** members are `type name;`. Struct, union and enum declarations have no trailing `;`, as in the professor's grammar.
- Inside a compound statement any declaration is allowed, so a nested function definition is accepted by the parser; rejecting it is left to semantic analysis.
- The handout writes most list rules left-recursively; we kept that and append to Abdul's head/tail lists so they come out in source order. `bitfield_list` is right-recursive in the handout and in our grammar.

`tests/declarations.c` covers every declaration form, including all the examples from the handout and slides.

## Typedef handling

`type_name` includes a bare `ID`, so a typedef name (or a struct/union name) can be used anywhere a type is expected: `typedef char** strings;` then `var strings argv;`. The parser does not check that the name was actually defined; that is semantic analysis.

In real C this is the famous typedef ambiguity: `(T) * x` could be a cast or a multiplication, so compilers need the symbol table while parsing. Our version of C avoids it. Declarations always start with a keyword (`var`, `fun`, `typedef`, `enum`, `struct`/`union` or a modifier), and casts are excluded, so an `ID` in type position can never be confused with an expression. The one consequence is `sizeof(x)`: because `sizeof expression` is excluded, `x` is read as a type name.

## Error recovery

Error productions, each resynchronizing on a token that ends a construct:

| Rule | Recovers from |
|---|---|
| `top_declaration ::= error SEMICOLON` and `error RBRACE` | any bad top-level declaration or function |
| `declaration ::= VAR error SEMICOLON` and `TYPEDEF error SEMICOLON` | a bad `var` or `typedef`, at top level or inside a function |
| `statement ::= error SEMICOLON` | a bad statement |
| `primary_expression ::= LPAREN error RPAREN` | a bad expression inside parentheses |

The top-level rules sit on a wrapper (`top_declaration`) rather than on `declaration` itself. `declaration` is also used inside compound statements, and `declaration ::= error SEMICOLON` would be a reduce/reduce conflict there with `statement ::= error SEMICOLON`.

A recovered construct becomes `null` in the tree and parsing continues. `tests/errors.c` has 13 deliberate errors; all 13 are reported with a line and column, and every valid declaration and function around them is still in the tree. Errors closer together than three tokens are merged into one report by CUP.

Two fixes were needed for this to work with the CUP version we were given (v0.10g):

1. **Dropped token after recovery.** The CUP runtime threw away one input token after every recovered error, so the declaration after an error was lost. `lr_parser.read_lookahead()` leaves `cur_token` one token past the lookahead buffer, but the rest of the runtime assumes it is the last buffered token and calls `scan()` again. `Grm.cup` overrides `read_lookahead()` in its `parser code` to fill the buffer the way the runtime expects.
2. **Default reductions.** Without them, a valid declaration followed by a line that starts with an illegal token (e.g. `int x;` without `var`) was popped off the stack during recovery and lost. The Makefile passes `-compact_red` to CUP, which makes the most common reduction in each state the default (as yacc does), so the valid declaration is finished before the error is detected.

## Build notes

- **`Tools/PackTables.java`.** CUP v0.10g writes its parse tables as huge `short[][]` array literals. For a grammar the size of C, the code that builds them is over Java's 64 KB-per-method limit and `javac` fails with "code too large". After CUP runs, the Makefile runs this small tool, which stores the same tables as strings and decodes them when the class loads. The tables themselves are unchanged.
- The Makefile compiles `ErrorMsg`, `Symbol` and `Absyn` too, so the Tiger classes in the course classpath are never picked up by mistake.
- Token names come from our lexer (`Parse/C.lex`): `&` is `AND`, `|` is `OR`, `&&` is `ANDAND`, `||` is `OROR`, `%` is `MOD`, `^` is `XOR`. `SWITCH`, `CASE`, `DEFAULT`, `SIGNED`, `UNSIGNED`, `HASH` and `HASHHASH` are returned by the lexer but unused, so CUP prints 7 "declared but never used" warnings.

## Not implemented (excluded by the assignment)

Casts, `sizeof` of an expression, the `switch` statement, function pointers, floating-point constants, and preprocessing directives. `float` and `double` are accepted as type names because the declaration grammar includes them. Adjacent string literals are not concatenated.

## Testing

    java Parse.Main tests/expressions.c
    java Parse.Main tests/statements.c
    java Parse.Main tests/declarations.c
    java Parse.Main tests/errors.c

The first three parse with no errors. `tests/errors.c` should print 13 `Syntax error` lines and still show `good1` through `good8`, `f` and `g` in the tree.
