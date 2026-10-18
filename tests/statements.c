/* statements.c - every C89 statement except switch/case/default */

fun int statements(int n) {
  var int i = 0;
  var int total = 0;

  ;                                      /* empty statement     */
  total = n;                             /* expression statement */

  {                                      /* nested compound      */
    var int inner = 1;
    inner++;
    {
      ;
    }
  }

  if (n > 0) total = 1;
  if (n > 0) total = 1; else total = 2;
  if (n > 0) { total = 1; } else if (n < 0) { total = 2; } else { total = 3; }

  /* dangling else: the else belongs to the inner if */
  if (n) if (i) total = 1; else total = 2;

  while (i < n) i++;
  while (1) { break; }

  do { i--; } while (i > 0);

  for (i = 0; i < n; i++) total += i;
  for (i = 0, total = 0; i < n; i++, total++) { continue; }
  for (;;) break;
  for (; i < n;) i++;

  goto done;
done:
  i = 0;
start: loop:
  if (i < 10) { i++; goto loop; }

  return total;
}

fun void no_value() {
  return;
}
