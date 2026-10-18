package Absyn;
import Symbol.Symbol;

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

  void say(long i) { // intexp is a long, so changing here to reflect that
    out.print(i);
  }

  void say(boolean b) {
    out.print(b);
  }

  void sayln(String s) {
    out.println(s);
  }

  // returns a string rep of symbol or null if symbol is null
  String sym(Symbol s) {
    return s == null ? "null" : s.toString();
  }

// this code was done using ai to save time
  static final String[] binaryOps = {
    "MUL", "DIV", "MOD", "PLUS", "MINUS", "SHL", "SHR", "LT", "GT", "LE", "GE",
    "EQ", "NE", "BITAND", "BITXOR", "BITOR", "AND", "OR"
  };
  static final String[] unOps = {
    "NEG", "POS", "NOT", "BITNOT", "DEREF", "ADDR", "PREINC", "PREDEC", "POSTINC", "POSTDEC" 
  };
  static final String[] assignOps = {
    "ASSIGN", "MUL", "DIV", "MOD", "PLUS", "MINUS", "SHL", "SHR", "AND", "XOR", "OR" 
  };

  void prExp(IdExp e, int d) {
    say("IdExp(");
    say(sym(e.name));
    say(")");
  }
  
  void prExp(MemberExp e, int d) {
    if (e.arrow) {
      sayln("ArrowExp(");
    } else {
      sayln("DotExp(");
    }
    prExp(e.record, d+1);
    sayln(",");
    indent(d+1);
    say(sym(e.field));
    say(")");
  }

  void prExp(SubscriptExp e, int d) {
    sayln("SubscriptExp(");
    prExp(e.array, d+1);
    sayln(",");
    prExp(e.index, d+1);
    say(")");
  } 
  
// modified so that instead of having 18 switch cases, it uses a lookup instead
  void prExp(BinOpExp e, int d) {
    sayln("BinOpExp(");
    indent(d+1);
    say(binaryOps[e.operator]);
    sayln(",");
    prExp(e.left, d+1);
    sayln(",");
    prExp(e.right, d+1);
    say(")");
  }

  void prExp(IntExp e, int d) {
    say("IntExp("); say(e.value); say(")");
  }

  void prExp(StringExp e, int d) {
    say("StringExp("); say(e.value); say(")");
  }

  void prExp(CallExp e, int d) {
    say("CallExp("); say(e.func.toString()); sayln(",");
    prExplist(e.args, d+1); say(")");
  }

  void prExp(AssignExp e, int d) {
    sayln("AssignExp(");
    indent(d+1);
    say(assignOps[e.operator]);
    sayln(",");
    prExp(e.left, d+1);
    sayln(",");
    prExp(e.right, d+1);
    say(")");
  }

  // for c, this is for char literals
  void prExp(CharExp e, int d) {
    say("CharExp(");
    say(e.value);
    say(")");
  }

  // for c, one operand operators
  void prExp(UnOpExp e, int d) {
    sayln("UnOpExp(");
    indent(d+1);
    say(unOps[e.operator]);
    sayln(",");
    prExp(e.exp, d+1);
    say(")");
  }

// ternary operator
  void prExp(CondExp e, int d) {
    sayln("CondExp(");
    prExp(e.test, d+1);
    sayln(",");
    prExp(e.thenExp, d+1);
    sayln(",");
    prExp(e.elseExp, d+1);
    say(")");
  }

  void prExp(CastExp e, int d) {
    sayln("CastExp(");
    prType(e.type, d+1);
    sayln(",");
    prExp(e.exp, d+1);
    say(")");
  }

  void prExp(SizeofExp e, int d) {
    sayln("SizeofExp(");
    prExp(e.exp, d+1);
    say(")");
  }

  void prExp(SizeofTypeExp e, int d) {
    sayln("SizeofTypeExp(");
    prType(e.type, d+1);
    say(")");
  }

  // parenthesized expression, kept so the tree shows the source's parens
  void prExp(ParenExp e, int d) {
    sayln("ParenExp(");
    prExp(e.exp, d+1);
    say(")");
  }

  void prExp(CommaExp e, int d) {
    sayln("CommaExp(");
    prExp(e.left, d+1);
    sayln(",");
    prExp(e.right, d+1);
    say(")");
  }

  /* Print Exp class types. Indent d spaces. */
  public void prExp(Exp e, int d) {
    indent(d);
    if (e == null) say("null");
    // kept
    else if (e instanceof IntExp) prExp((IntExp) e, d);
    else if (e instanceof StringExp) prExp((StringExp) e, d);
    else if (e instanceof CallExp) prExp((CallExp) e, d);
    else if (e instanceof AssignExp) prExp((AssignExp) e, d);

    // new 
    else if (e instanceof BinOpExp) prExp((BinOpExp) e, d);
    else if (e instanceof IdExp) prExp((IdExp) e, d);
    else if (e instanceof MemberExp) prExp((MemberExp) e, d);
    else if (e instanceof SubscriptExp) prExp((SubscriptExp) e, d);
    else if (e instanceof CharExp) prExp((CharExp) e, d);
    else if (e instanceof UnOpExp) prExp((UnOpExp) e, d);
    else if (e instanceof CondExp) prExp((CondExp) e, d);
    else if (e instanceof CastExp) prExp((CastExp) e, d);
    else if (e instanceof SizeofExp) prExp((SizeofExp) e, d);
    else if (e instanceof SizeofTypeExp) prExp((SizeofTypeExp) e, d);
    else if (e instanceof CommaExp) prExp((CommaExp) e, d);
    else if (e instanceof ParenExp) prExp((ParenExp) e, d);
    else throw new Error("Print.prExp");
  }

  // adds desc + full type 
  void prDec(FunDec f, int i) {
    sayln("FunDec(");
    prDescList(f.desc, i+1);
    sayln(",");
    prType(f.type, i+1);
    sayln(",");
    indent(i+1);
    say(sym(f.name));
    sayln(",");
    prParamList(f.params, i+1);
    sayln(",");
    prStm((Statement) f.body, i+1);  // cast so the dispatcher indents it
    say(")");
  }

  // adds desc + full type
  void prDec(VarDec v, int i) {
    sayln("VarDec(");
    prDescList(v.desc, i+1);
    sayln(",");
    prType(v.type, i+1);
    sayln(",");
    indent(i+1);
    say(sym(v.name));
    sayln(",");
    prInit(v.init, i+1);
    say(")");
  }

  // type and a name
  void prDec(TypedefDec t, int i) {
    sayln("TypedefDec(");
    prType(t.type, i+1);
    sayln(",");
    indent(i+1);
    say(sym(t.name));
    say(")");
  }

  // for function protyping in c
  void prDec(FunProtoDec f, int i) {
    sayln("FunProtoDec(");
    prDescList(f.desc, i+1);
    sayln(",");
    prType(f.type, i+1);
    sayln(",");
    indent(i+1);
    say(sym(f.name));
    sayln(",");
    prParamList(f.params, i+1);
    say(")");
  }

  //. for stucts and union in c
  void prDec(StructUnionDec s, int i) {
    if (s.isUnion) {
      sayln("UnionDec(");
    } else {
      sayln("StructDec(");
    }
    prDescList(s.desc, i+1);
    sayln(",");
    indent(i+1);
    say(sym(s.name));
    sayln(",");
    if (s.members == null) {
      indent(i+1);
      say("null");
    } else {
      prDecList(s.members, i+1);
    }
    say(")");
  }

  // for enum in c with list of enumerators
  void prDec(EnumDec e, int i) {
    sayln("EnumDec(");
    indent(i+1);
    say(sym(e.name));
    sayln(",");
    prEnumList(e.list, i+1);
    say(")");
  }

  // modified for c instead of tiger 
  void prDec(Dec d, int i) {
    indent(i);
    if (d == null) say("null");
    else if (d instanceof VarDec) prDec((VarDec) d, i);
    else if (d instanceof FunDec) prDec((FunDec) d, i);
    else if (d instanceof TypedefDec) prDec((TypedefDec) d, i);
    // new
    else if (d instanceof FunProtoDec) prDec((FunProtoDec) d, i);
    else if (d instanceof StructUnionDec) prDec((StructUnionDec) d, i);
    else if (d instanceof EnumDec) prDec((EnumDec) d, i);
    else throw new Error("Print.prDec");
  }

  void prType(NameType t, int i) {
    say("NameType(");
    say(sym(t.name));
    say(")");
  }

  void prType(ArrayType t, int i) {
    sayln("ArrayType(");
    prType(t.base, i+1);
    sayln(",");
    prExp(t.size, i+1);
    say(")");
  }

  // pointer wraps the base type for C
  void prType(PointerType t, int i) {
    sayln("PointerType(");
    prType(t.base, i+1);
    say(")");
  }

  // stuct and union foo used as type
  void prType(StructType t, int i) {
    if (t.isUnion) {
      say("UnionType(");
    } else {
      say("StructType(");
    }
    say(sym(t.name));
    say(")");
  }

  void prType(EnumType t, int i) {
    say("EnumType(");
    say(sym(t.name));
    say(")");
  }

// dispatcher with 3 new types for C
  void prType(Type t, int i) {
    indent(i);
    if (t==null) say("null");
      if (t instanceof NameType) prType((NameType) t, i);
      else if (t instanceof NameType) prType((NameType) t, i);
      else if (t instanceof ArrayType) prType((ArrayType) t, i);
      else if (t instanceof PointerType) prType((PointerType) t, i);
      else if (t instanceof StructType) prType((StructType) t, i);
      else if (t instanceof EnumType) prType((EnumType) t, i);
      else throw new Error("Print.prTy");
  }


  void prExplist(ExpList e, int d) {
    indent(d);
    say("ExpList("); 
    if (e!=null) {
      sayln("");
      prExp(e.head, d+1); 
      if (e.tail != null) {
	sayln(","); prExplist(e.tail, d+1);
      }
    }
    say(")");
  }

  // changed to public so it can be called outside
  public void prDecList(DecList v, int d) {
    indent(d);
    say("DecList("); 
    if (v!=null) {
      sayln("");
      prDec(v.head, d+1); sayln(",");
      prDecList(v.tail, d+1);
    }
    say(")");
  }

  //  statement list inside {} for C
  void prStmList(StatementList s, int d) {
    indent(d);
    say("StatementList(");
    if (s != null) {
      sayln("");
      prStm(s.head, d+1);
      if (s.tail != null) {
        sayln(",");
        prStmList(s.tail, d+1);
      }
    }
    say(")");
  }

  // one line descriptors
  void prDescList(DescriptorList l, int d) {
    indent(d);
    say("Descriptors(");
    while (l != null) {
      say(sym(l.head));
      if (l.tail != null) {
        say(", ");
      }
      l = l.tail;
    }
    say(")");
  }

  // parameter list for FunDec
  void prParamList(ParamList p, int d) {
    indent(d);
    say("ParamList(");
    if (p != null) {
      sayln("");
      prParam(p.head, d+1);
      if (p.tail != null) {
        sayln(",");
        prParamList(p.tail, d+1);
      }
    }
    say(")");
  }

  void prParam(Param p, int d) {
    indent(d);
    if (p.ellipsis) {
      say("Ellipsis");
    } else {
      sayln("Param(");
      prType(p.type, d+1);
      sayln(",");
      indent(d+1);
      say(sym(p.name));
      say(")");
    }
  }

  void prInit(Initializer init, int d) {
    indent(d);
    if (init == null) {
      say("null");
    } else if (init instanceof ExpInit) {
      ExpInit e = (ExpInit) init;
      sayln("ExpInit(");
      prExp(e.exp, d+1);
      say(")");
    } else if (init instanceof ListInit) {
      ListInit l = (ListInit) init;
      sayln("ListInit(");
      prInitList(l.list, d+1);
      say(")");
    } else {
      throw new Error("Print.prInit");
    }
  }

  void prInitList(InitializerList l, int d) {
    indent(d);
    say("InitializerList(");
    if (l != null) {
      sayln("");
      prInit(l.head, d+1);
      if (l.tail != null) {
        sayln(",");
        prInitList(l.tail, d+1);
      }
    }
    say(")");
  }

  void prEnumList(EnumeratorList l, int d) {
    indent(d);
    say("EnumeratorList(");
    if (l != null) {
      sayln("");
      prEnumerator(l.head, d+1);
      if (l.tail != null) {
        sayln(",");
        prEnumList(l.tail, d+1);
      }
    }
    say(")");
  }

  void prEnumerator(Enumerator e, int d) {
    indent(d);
    say("Enumerator(");
    say(sym(e.name));
    if (e.value != null) {
      sayln(",");
      prExp(e.value, d+1);
    }
    say(")");
  }

// statements now
  void prStm(ExpStatement s, int d) {
    sayln("ExpStatement(");
    prExp(s.exp, d+1);
    say(")");
  }

  void prStm(ReturnStatement s, int d) {
    sayln("ReturnStatement(");
    prExp(s.exp, d+1);
    say(")");
  }

  void prStm(BreakStatement s, int d) {
    say("BreakStatement()");
  }

  void prStm(ContinueStatement s, int d) {
    say("ContinueStatement()");
  }

  void prStm(GotoStatement s, int d) {
    say("GotoStatement(");
    say(sym(s.label));
    say(")");
  }

  void prStm(LabeledStatement s, int d) {
    sayln("LabeledStatement(");
    indent(d+1);
    say(sym(s.label));
    sayln(",");
    prStm(s.statement, d+1);
    say(")");
  }
  // declaration statements
  void prStm(CompoundStatement s, int d) {
    sayln("CompoundStatement(");
    prDecList(s.decs, d+1);
    sayln(",");
    prStmList(s.stms, d+1);
    say(")");
  }

  // if else, prints null if else is empty
  void prStm(IfElseStatement s, int d) {
    sayln("IfElseStatement(");
    prExp(s.test, d+1);
    sayln(",");
    prStm(s.thenClause, d+1);
    sayln(",");
    prStm(s.elseClause, d+1);
    say(")");
  }

  void prStm(WhileStatement s, int d) {
    sayln("WhileStatement(");
    prExp(s.test, d+1);
    sayln(",");
    prStm(s.body, d+1);
    say(")");
  }

  void prStm(DoWhileStatement s, int d) {
    sayln("DoWhileStatement(");
    prStm(s.body, d+1);
    sayln(",");
    prExp(s.test, d+1);
    say(")");
  }

  void prStm(ForStatement s, int d) {
    sayln("ForStatement(");
    prExp(s.init, d+1);
    sayln(",");
    prExp(s.test, d+1);
    sayln(",");
    prExp(s.incr, d+1);
    sayln(",");
    prStm(s.body, d+1);
    say(")");
  }

// statement dispatcher
  public void prStm(Statement s, int d) {
    indent(d);
    if (s == null) say("null");
    else if (s instanceof ExpStatement) prStm((ExpStatement) s, d);
    else if (s instanceof CompoundStatement) prStm((CompoundStatement) s, d);
    else if (s instanceof IfElseStatement) prStm((IfElseStatement) s, d);
    else if (s instanceof WhileStatement) prStm((WhileStatement) s, d);
    else if (s instanceof DoWhileStatement) prStm((DoWhileStatement) s, d);
    else if (s instanceof ForStatement) prStm((ForStatement) s, d);
    else if (s instanceof ReturnStatement) prStm((ReturnStatement) s, d);
    else if (s instanceof BreakStatement) prStm((BreakStatement) s, d);
    else if (s instanceof ContinueStatement) prStm((ContinueStatement) s, d);
    else if (s instanceof GotoStatement) prStm((GotoStatement) s, d);
    else if (s instanceof LabeledStatement) prStm((LabeledStatement) s, d);
    else throw new Error("Print.prStm");
  }
}