package Types;

public class Print {
  java.io.PrintWriter out;

  public Print(java.io.PrintWriter o) { out = o; }

  void indent(int d) {
    for(int i=0; i<d; i++) 
      out.print(' ');
  }

  void say(String s) {
    out.print(s);
  }

  void say(int i) {
    out.print(i);
  }

  void say(boolean b) {
    out.print(b);
  }

  void sayln(String s) {
    out.println(s);
  }

  public void prType(Type t, int d) {
    if (t == null) {
      say("()");
      return;
    }
    
    // we remove STRING and NIL, replace with CHAR, POINTER, and STRUCT
    if      (t instanceof INT)    prType((INT)   t, d);
    else if (t instanceof RECORD) prType((RECORD)t, d);
    else if (t instanceof ARRAY)  prType((ARRAY) t, d);
    else if (t instanceof VOID)   prType((VOID)  t, d);
    else if (t instanceof NAME)   prType((NAME)  t, d);
    else if (t instanceof CHAR)   prType((CHAR)   t, d);
    else if (t instanceof POINTER) prType((POINTER) t, d);
    else if (t instanceof STRUCT) prType((STRUCT) t, d);
    else throw new Error("Print.prType");
  }

  void prType(INT t, int d) {
    say("INT");
  }

  void prType(RECORD t, int d) {
    say("RECORD(");
    if (t != null) {
      sayln(""); indent(d+1); sayln(t.fieldName.toString());
      indent(d+1); say(":"); prType(t.fieldType, d+2); sayln(",");
      indent(d+1); prType(t.tail, d+1);
    }
    say(")");
  }


  void prType(ARRAY t, int d) {
    say("ARRAY["); say(t.size); sayln("]("); indent(d+1); prType(t.element, d+1); say(")");
  }

  void prType(VOID t, int d) {
    say("VOID");
  }

  void prType(NAME t, int d) {
    Type b = t.binding;
    say("NAME("); say(t.name.toString()); say(")");
    if (b == null)
      return;
    t.binding = null;
    sayln(""); indent(d); say("="); prType(b, d+1);
    t.binding = b;
  }

  // new functions 
  void prType(CHAR t, int d) {
    say("CHAR");
  }

  void prType(POINTER t, int d) {
    say("POINTER("); indent(d+1); prType(t.element, d+1); say(")");
  }

  // i explicitly used ai for this function to help me understand after i was stuck
  void prType(STRUCT t, int d) {
    say(t.isUnion ? "UNION(" : "STRUCT("); say(t.name.toString()); say(")");
    RECORD r = t.fields;
    if (r == null) {
      return;
    }

    t.fields = null;
    sayln(""); indent(d); say("="); prType(r, d+1);
    t.fields = r;
  }
}
