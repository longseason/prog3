/* declarations.c - the course's simplified declaration syntax */

/* var: modifiers (bitfield_list), types, initializers */
var int plain;
var extern static int x = 1;
var static extern int y = 1;
var const volatile register auto int all_modifiers;
var char letter = 'A';
var char* message = "hello";
var short s;
var long l;
var float f;
var double d;
var void* anything;

/* pointer stars, then array brackets: all empty or all sized */
var int*** ptr3;
var int******[][][][][][][] deep;
var int[][] open2;
var int[10] ten;
var int****[22][3] grid;
var int[N * 2] sized_by_constant_expression;
var int[a] sized_by_identifier;

/* initializers, including nested lists */
var int[2] pair = {1, 2};
var int[3][2] table = {{1,2},{1,2},{1,2}};
var int computed = 2 * 3 + 1;

/* typedef: the new name is used as a type afterwards */
typedef int myint;
typedef char** strings;
typedef int*[10] ptr_array;
typedef enum Color ColorT;
var myint count = 0;
var strings argv;
var ColorT shade;

/* enum: no trailing semicolon; values are constant expressions */
enum Color { RED, GREEN = 2, BLUE = GREEN + 1 }
var enum Color c = RED;

/* struct / union: members are "type name;", no trailing semicolon */
struct Node { int value; Node* next; }
static union Value { int i; char c; char*[4] names; }
var Node head;
var Node* tail;

/* prototypes list types only; definitions name every parameter */
fun int printf(char*, ...);
fun static int**[10][33] exampleFunction(int*, int******);
fun void noargs();

fun static int**[10][33] example(int* x, int****** y) {
  return 0;
}

fun int sum(int count, ...) {
  return count;
}

fun void nothing() { }

/* declarations inside a block come before the statements */
fun int locals(int a) {
  var int i = a;
  var int[3] v = {1, 2, 3};
  typedef int local_t;
  var local_t t;
  enum Inner { A, B }
  struct Pair { int first; int second; }
  t = i;
  return t;
}
