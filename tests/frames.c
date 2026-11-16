struct Point { int x; int y; }

var int g = 1;
var int[4] table = {1,2,3,4};

fun int f(int a, int b, int c, char* s, Point p) {
  var int local = a + b;
  var int addr;
  var volatile int v = 0;
  var int[3] arr = {1,2,3};
  var Point pt = {1,2};
  var int* q = &addr;
  var int* r = &c;
  {
    var int inner = local;
    var int[2][2] m = {{1,2},{3,4}};
    q = &arr[inner];
  }
  return local + *q + *r;
}

fun int main() {
  var int x = 3;
  var Point p = {1,2};
  return f(x, 2, 3, "s", p);
}
