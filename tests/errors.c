/* errors.c - syntax errors. Each one should be reported with its line
   and column, and the parser should recover and keep going: every
   "good" declaration and function below still appears in the tree.
   (CUP merges errors that are closer together than three tokens, so a
   valid line separates each bad one.) */

/* ---- declaration errors ---- */
var int[][10] mixed_brackets;       /* empty [] before a sized one        */
var int good1;
var int**[1][] sized_then_empty;    /* sized then empty: also not allowed  */
var int good2;
var unsigned int u;                 /* unsigned is not a type_name         */
var int good3;
var int missing_init = ;            /* nothing after =                      */
var int good4 = 4;
fun int proto(int x);               /* prototypes list types, not names    */
var int good5;
fun int def(int) { return 1; }      /* definitions must name parameters    */
var int good6;
typedef int;                        /* typedef without a name              */
var int good7;
int no_var_keyword;                 /* C-style declaration without var      */
var int good8;

/* ---- statement and expression errors ---- */
fun int f(int x) {
  var int = 5;                      /* missing name in a local declaration */
  x = 1 +;                          /* incomplete expression               */
  if (x y) x = 2;                   /* missing operator in the condition   */
  x = (x +) * 2;                    /* error inside parentheses            */
  a + b = c;                        /* left side must be a unary expression */
  return x;                         /* still parsed                        */
}

fun int g() { return 2; }           /* parsed normally after the errors    */
