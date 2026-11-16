package FindEscape;

class FormalEscape extends Escape {
  Absyn.Param fl;
  FormalEscape(int d, Absyn.Param f) {
    depth = d;
    fl = f;
    fl.escape = false;
  }

  void setEscape() {
    fl.escape = true;
  }
}
