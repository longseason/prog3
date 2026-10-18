/* expressions.c - every C89 operator the parser supports
   (no casts, no sizeof expr, no floats, no function pointers) */

fun int expressions(int a, int b, int* p, Node* n) {
  var int x;
  var int y;

  /* primary: identifiers, integer bases and suffixes, chars, strings */
  x = a;
  x = 42; x = 0x1F; x = 017; x = 42UL;
  x = 'c';
  x = "string";
  x = (a);

  /* postfix */
  x = arr[3][a + 1];
  x = f();
  x = g(a, b + 1, h(2));
  x = n->value;
  x = n->next->value;
  x = rec.field;
  x = a++;
  x = a--;

  /* unary */
  x = ++a;
  x = --a;
  x = &a;
  x = *p;
  x = +a;
  x = -a;
  x = ~a;
  x = !a;
  x = - - a;
  x = sizeof(int);
  x = sizeof(char**);
  x = sizeof(int[4]);

  /* binary, one per precedence level */
  x = a * b / 2 % 3;
  x = a + b - 1;
  x = a << 2 >> 1;
  x = a < b; x = a > b; x = a <= b; x = a >= b;
  x = a == b; x = a != b;
  x = a & b;
  x = a ^ b;
  x = a | b;
  x = a && b;
  x = a || b;

  /* precedence and associativity checks */
  x = a + b * 2;           /* a + (b * 2)          */
  x = (a + b) * 2;         /* parens kept: ParenExp */
  x = a - b - 1;           /* (a - b) - 1, left    */
  x = -a * b;              /* (-a) * b, unary first */
  x = a || b && x;         /* a || (b && x)        */
  x = a & b == 1;          /* a & (b == 1), as in C */

  /* conditional, nested to the right */
  x = a ? b : y ? 1 : 2;

  /* assignment operators, right-associative */
  x = y = 0;
  x *= 2; x /= 2; x %= 2; x += 1; x -= 1;
  x <<= 1; x >>= 1; x &= 1; x ^= 1; x |= 1;

  /* comma */
  x = (a, b);
  return x;
}
