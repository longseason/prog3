typedef int myint;
typedef char** strings;
struct Node { int value; Node* next; }
union Value { int i; char c; }
struct myStruct { int[][] x; int y; }

/* should pass */
var int a = 5;
var char b = 'c';
var char******* p = 8;
var char* m = "hello";
var myint count = 0;
var strings argv;
var int[3][2] t = {{1,2},{1,2},{1,2}};
var int[][] u = {{1,2},{1,2}};
var char[] cs = {'a', 'A'};
var myStruct s1 = {{{1,2},{1,2}},1};
var Value v = 5;

/* should fail, one error each */
var int e1 = 'c';
var char e2 = 90;
var int[3] e3 = {1,2};
var int[][] e4 = {{1,2},{1,2,3}};
var char[] e5 = {'a', 65};
var myStruct e6 = {1,2};
var int[2] e7 = 5;
var char* e8 = 'a';
var void e9;
var Value e10 = "hi";
var undefined_t e11;