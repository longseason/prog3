struct Node { int value; Node* next; }

fun int printf(char*, ...);
fun void nothing() { return; }

/* should pass */
fun int good(int n, char* s, Node* list, int[] nums) {
  var int i;
  var int total = 0;
  var int[3] arr = {1,2,3};
  for (i = 0; i < n; i++) {
    var int twice = i * 2;
    if (twice > 10) break;
    if (twice == 4) continue;
    total += twice;
  }
  for (;;) { break; }
  while (list) { total += list->value; list = list->next; }
  do { n--; } while (n > 0);
  if (*s) total = 1; else total = 2;
  ;
  goto done;
  total = later(arr, 1) + good(1, "x", list, nums);
  printf("%d %d", total, n);
  nothing();
done:
  return total;
}

fun int later(int[] a, int i) { return a[i]; }

/* should fail, one error each */
fun void bad(int n, Node node) {
  var int x;
  if (node) x = 1;
  while (node) x = 2;
  do x = 3; while (node);
  for (; node; ) x = 4;
  break;
  continue;
  goto nowhere;
  x = 'c';
  { var int inner; }
  inner = 1;
  return 5;
dup:
  x = 1;
dup:
  x = 2;
}

fun int bad2() { return; }
fun int bad3() { return 'c'; }
fun char* bad4() { var Node n; return n; }
