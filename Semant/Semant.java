package Semant;
import Translate.Exp;
import Types.Type;

public class Semant {
  Env env;
  public Semant(ErrorMsg.ErrorMsg err) {
    this(new Env(err));
  }
  Semant(Env e) {
    env = e;
  }
  // second semant constructor, the frame is used for activation records
  public Semant(Frame.Frame frame, ErrorMsg.ErrorMsg err) {
    this(new Env(err));
    this.frame = frame;
  }

  // ---- activation records ----
  Frame.Frame frame;
  Translate.Level root;      // parent of every function level
  Translate.Level globals;   // where global variables are allocated
  Translate.Level level;     // the level we are currently allocating in

  private boolean isAggregate(Type t) {
    Type a = t.actual();
    return a instanceof Types.ARRAY || a instanceof Types.STRUCT;
  }

  // size of a type in words. ints, chars and pointers all take one word.
  private java.util.HashSet<Type> sizing = new java.util.HashSet<Type>();
  private int words(Type t) {
    Type a = t.actual();
    if (a instanceof Types.ARRAY) {
      Types.ARRAY arr = (Types.ARRAY) a;
      // an array with no size and no initializer is just one word
      return arr.size <= 0 ? 1 : arr.size * words(arr.element);
    }
    if (a instanceof Types.STRUCT) {
      Types.STRUCT st = (Types.STRUCT) a;
      if (!sizing.add(st))               // a struct that contains itself
        return 1;
      int n = 0;
      for (Types.RECORD f = st.fields; f != null; f = f.tail) {
        int w = words(f.fieldType);
        n = st.isUnion ? Math.max(n, w) : n + w;   // union members overlap
      }
      sizing.remove(st);
      return Math.max(n, 1);
    }
    return 1;
  }

  // allocate a variable in the current level (null if we have no frame)
  private Translate.Access alloc(boolean escape, Type t) {
    if (level == null)
      return null;
    return level.allocLocal(escape, words(t));
  }

  // one boolean per parameter: does it escape?
  private Util.BoolList escapes(Absyn.ParamList l) {
    if (l == null) return null;
    if (l.head.ellipsis) return escapes(l.tail);
    return new Util.BoolList(l.head.escape, escapes(l.tail));
  }

  private boolean isTypeDec(Absyn.Dec d) {
    return d instanceof Absyn.StructUnionDec || d instanceof Absyn.TypedefDec
        || d instanceof Absyn.EnumDec;
  }

  public void transProg(Absyn.DecList decs) {
    // find the variables that escape before anything is allocated
    new FindEscape.FindEscape(decs);

    // globals get a level of their own; every function level hangs off root
    if (frame != null) {
      root = new Translate.Level(frame);
      globals = new Translate.Level(root, Symbol.Symbol.symbol(".globals"), null);
      level = globals;
    }

    // C allows a function to be called before it is defined (forward reference),
    // so the top level is done in three passes:
    // 1. types, so that function headers can use them
    for (Absyn.DecList d = decs; d != null; d = d.tail)
      if (isTypeDec(d.head))
        transDec(d.head);

    // 2. function headers
    for (Absyn.DecList d = decs; d != null; d = d.tail) {
      if (d.head instanceof Absyn.FunProtoDec) {
        transDec(d.head);
      } else if (d.head instanceof Absyn.FunDec) {
        Absyn.FunDec f = (Absyn.FunDec) d.head;
        FunEntry entry = new FunEntry(transParams(f.params), transTy(f.type));
        entry.varargs = hasEllipsis(f.params);
        env.venv.put(f.name, entry);
      }
    }

    // 3. variables and function bodies
    for (Absyn.DecList d = decs; d != null; d = d.tail)
      if (!isTypeDec(d.head) && !(d.head instanceof Absyn.FunProtoDec))
        transDec(d.head);
  }

  private void error(int pos, String msg) {
    env.errorMsg.error(pos, msg);
  }

  static final Types.VOID   VOID   = new Types.VOID();
  static final Types.INT    INT    = new Types.INT();

  // replace STRING and NIL with CHAR
  static final Types.CHAR   CHAR   = new Types.CHAR();

  // return type of the current function being checked 
  Type returnType = VOID;

  // how many loops we are inside of, for break and continue
  int loopDepth = 0;
  // labels defined in the current function, and the gotos that still need checking
  java.util.HashSet<Symbol.Symbol> labels = new java.util.HashSet<Symbol.Symbol>();
  java.util.ArrayList<Absyn.GotoStatement> gotos = new java.util.ArrayList<Absyn.GotoStatement>();

  private Exp checkInt(ExpTy et, int pos) {
    if (!INT.coerceTo(et.ty))
      error(pos, "integer required");
    return et.exp;
  }

  Type transTy(Absyn.Type t) {
    if (t instanceof Absyn.NameType) {
      Symbol.Symbol name = ((Absyn.NameType)t).name;
      String s = name.toString();

      // map onto shared objects 
      if (s.equals("int") || s.equals("short") || s.equals("long")) {
        return INT;
      }
      if (s.equals("char")) {
        return CHAR;
      }
      if (s.equals("void")) {
        return VOID;
      }

      // used ai here to figure what the error was, needed cast to fix it
      Type found = (Type) env.tenv.get(name);
      if (found == null) {
        error(t.pos, "undefined type " + s);
        return INT;
    }
      return found;
    }

    if (t instanceof Absyn.EnumType) {
      return INT;
    }
    if (t instanceof Absyn.PointerType) {
      Type base = transTy(((Absyn.PointerType) t).base);
      return new Types.POINTER(base);
    }
    // used ai for this explicity
    if (t instanceof Absyn.ArrayType) {
      Absyn.ArrayType a = (Absyn.ArrayType) t;
      Type element = transTy(a.base);
      int size = -1;
      if (a.size instanceof Absyn.IntExp)
        size = (int) ((Absyn.IntExp) a.size).value;
      return new Types.ARRAY(element, size);
    }

    throw new Error("Semant.transTy");
  }


  ExpTy transExp(Absyn.Exp e) {
    // explicitly used ai here to deal with the reptitive pattern 
    if (e == null)
      return new ExpTy(null, VOID);
    else if (e instanceof Absyn.OpExp)
      return transExp((Absyn.OpExp)e);
    else if (e instanceof Absyn.LetExp)
      return transExp((Absyn.LetExp)e);
    else if (e instanceof Absyn.IntExp)
      return transExp((Absyn.IntExp)e);
    else if (e instanceof Absyn.CharExp)
      return transExp((Absyn.CharExp)e);
    else if (e instanceof Absyn.StringExp)
      return transExp((Absyn.StringExp)e);
    else if (e instanceof Absyn.IdExp)
      return transExp((Absyn.IdExp)e);
    else if (e instanceof Absyn.ParenExp)
      return transExp(((Absyn.ParenExp)e).exp);
    else if (e instanceof Absyn.BinOpExp)
      return transExp((Absyn.BinOpExp)e);
    else if (e instanceof Absyn.UnOpExp)
      return transExp((Absyn.UnOpExp)e);
    else if (e instanceof Absyn.AssignExp)
      return transExp((Absyn.AssignExp)e);
    else if (e instanceof Absyn.CondExp)
      return transExp((Absyn.CondExp)e);
    else if (e instanceof Absyn.CommaExp)
      return transExp((Absyn.CommaExp)e);
    else if (e instanceof Absyn.CallExp)
      return transExp((Absyn.CallExp)e);
    else if (e instanceof Absyn.SubscriptExp)
      return transExp((Absyn.SubscriptExp)e);
    else if (e instanceof Absyn.MemberExp)
      return transExp((Absyn.MemberExp)e);
    else if (e instanceof Absyn.CastExp)
      return transExp((Absyn.CastExp)e);
    else if (e instanceof Absyn.SizeofExp)
      return transExp((Absyn.SizeofExp)e);
    else if (e instanceof Absyn.SizeofTypeExp)
      return transExp((Absyn.SizeofTypeExp)e);
    throw new Error("Semant.transExp");
  }

  // ---- helpers for expressions ----

  // math only accepts numbers (rule 6), so chars and pointers are rejected
  private void checkNumber(ExpTy et, int pos, String what) {
    if (!et.ty.isNumber())
      error(pos, what + " requires a number");
  }

  // things that can be tested for true/false: numbers, chars, pointers
  private boolean isScalar(Type t) {
    Type a = t.actual();
    return a instanceof Types.INT || a instanceof Types.CHAR || a instanceof Types.POINTER;
  }

  private void checkScalar(ExpTy et, int pos, String what) {
    if (!isScalar(et.ty))
      error(pos, what + " requires a number, char, or pointer");
  }

  // an lvalue is something with a location: x, a[i], s.f, p->f, *p
  private boolean isLvalue(Absyn.Exp e) {
    if (e instanceof Absyn.ParenExp)
      return isLvalue(((Absyn.ParenExp) e).exp);
    if (e instanceof Absyn.IdExp || e instanceof Absyn.SubscriptExp || e instanceof Absyn.MemberExp)
      return true;
    return e instanceof Absyn.UnOpExp && ((Absyn.UnOpExp) e).operator == Absyn.UnOpExp.DEREF;
  }

  private int pos(Absyn.Exp e, int fallback) {
    return e == null ? fallback : e.pos;
  }

  // ---- expressions ----

  ExpTy transExp(Absyn.IdExp e) {
    Object x = env.venv.get(e.name);
    if (x instanceof VarEntry)
      return new ExpTy(null, ((VarEntry) x).ty);
    if (x instanceof FunEntry)
      error(e.pos, e.name + " is a function, not a variable");
    else
      error(e.pos, "undefined variable " + e.name);
    return new ExpTy(null, INT);
  }

  ExpTy transExp(Absyn.BinOpExp e) {
    ExpTy left = transExp(e.left);
    ExpTy right = transExp(e.right);
    int lpos = pos(e.left, e.pos), rpos = pos(e.right, e.pos);

    switch (e.operator) {
    // arithmetic, shifts and bitwise: numbers only
    case Absyn.BinOpExp.MUL: case Absyn.BinOpExp.DIV: case Absyn.BinOpExp.MOD:
    case Absyn.BinOpExp.PLUS: case Absyn.BinOpExp.MINUS:
    case Absyn.BinOpExp.SHL: case Absyn.BinOpExp.SHR:
    case Absyn.BinOpExp.BITAND: case Absyn.BinOpExp.BITXOR: case Absyn.BinOpExp.BITOR:
      checkNumber(left, lpos, "arithmetic operator");
      checkNumber(right, rpos, "arithmetic operator");
      return new ExpTy(null, INT);

    // comparisons: both sides must be the same kind of thing, so 'a' < 100 fails
    case Absyn.BinOpExp.LT: case Absyn.BinOpExp.GT: case Absyn.BinOpExp.LE:
    case Absyn.BinOpExp.GE: case Absyn.BinOpExp.EQ: case Absyn.BinOpExp.NE:
      if (!isScalar(left.ty) || !isScalar(right.ty))
        error(e.pos, "comparison requires numbers, chars, or pointers");
      else if (!left.ty.coerceTo(right.ty) && !right.ty.coerceTo(left.ty))
        error(e.pos, "comparison of incompatible types");
      return new ExpTy(null, INT);

    // && and ||
    case Absyn.BinOpExp.AND: case Absyn.BinOpExp.OR:
      checkScalar(left, lpos, "logical operator");
      checkScalar(right, rpos, "logical operator");
      return new ExpTy(null, INT);

    default:
      throw new Error("Semant.transExp: unknown binary operator");
    }
  }

  ExpTy transExp(Absyn.UnOpExp e) {
    ExpTy et = transExp(e.exp);
    int p = pos(e.exp, e.pos);
    Type a = et.ty.actual();

    switch (e.operator) {
    case Absyn.UnOpExp.NEG: case Absyn.UnOpExp.POS: case Absyn.UnOpExp.BITNOT:
      checkNumber(et, p, "unary operator");
      return new ExpTy(null, INT);

    case Absyn.UnOpExp.NOT:
      checkScalar(et, p, "operator !");
      return new ExpTy(null, INT);

    case Absyn.UnOpExp.DEREF:
      if (a instanceof Types.POINTER) {
        Type elem = ((Types.POINTER) a).element;
        if (elem.actual() instanceof Types.VOID)
          error(e.pos, "cannot dereference a void pointer");
        return new ExpTy(null, elem);
      }
      if (a instanceof Types.ARRAY)
        return new ExpTy(null, ((Types.ARRAY) a).element);
      error(e.pos, "cannot dereference a non-pointer");
      return new ExpTy(null, INT);

    case Absyn.UnOpExp.ADDR:
      if (!isLvalue(e.exp))
        error(e.pos, "operator & requires a variable");
      return new ExpTy(null, new Types.POINTER(et.ty));

    case Absyn.UnOpExp.PREINC: case Absyn.UnOpExp.PREDEC:
    case Absyn.UnOpExp.POSTINC: case Absyn.UnOpExp.POSTDEC:
      if (!isLvalue(e.exp))
        error(e.pos, "++/-- requires a variable");
      checkNumber(et, p, "++/--");
      return new ExpTy(null, INT);

    default:
      throw new Error("Semant.transExp: unknown unary operator");
    }
  }

  ExpTy transExp(Absyn.AssignExp e) {
    ExpTy left = transExp(e.left);
    ExpTy right = transExp(e.right);

    if (!isLvalue(e.left))
      error(e.pos, "left side of assignment is not assignable");

    if (e.operator == Absyn.AssignExp.ASSIGN) {
      if (left.ty.actual() instanceof Types.ARRAY)
        error(e.pos, "cannot assign to an array");
      else if (!right.ty.coerceTo(left.ty))
        error(pos(e.right, e.pos), "assigned value does not match the type of the left side");
    } else {
      // +=, -=, <<= ... are math, so both sides must be numbers
      checkNumber(left, e.pos, "compound assignment");
      checkNumber(right, pos(e.right, e.pos), "compound assignment");
    }
    return new ExpTy(null, left.ty);
  }

  ExpTy transExp(Absyn.CondExp e) {
    ExpTy test = transExp(e.test);
    ExpTy a = transExp(e.thenExp);
    ExpTy b = transExp(e.elseExp);

    checkScalar(test, pos(e.test, e.pos), "condition of ?:");
    if (b.ty.coerceTo(a.ty))
      return new ExpTy(null, a.ty);
    if (a.ty.coerceTo(b.ty))
      return new ExpTy(null, b.ty);
    error(e.pos, "branches of ?: have different types");
    return new ExpTy(null, a.ty);
  }

  ExpTy transExp(Absyn.CommaExp e) {
    transExp(e.left);
    return transExp(e.right);
  }

  ExpTy transExp(Absyn.CallExp e) {
    Object x = env.venv.get(e.func);
    if (!(x instanceof FunEntry)) {
      error(e.pos, x == null ? "undefined function " + e.func : e.func + " is not a function");
      // still type check the arguments
      for (Absyn.ExpList a = e.args; a != null; a = a.tail)
        transExp(a.head);
      return new ExpTy(null, INT);
    }

    FunEntry f = (FunEntry) x;
    Types.RECORD formal = f.formals;
    Absyn.ExpList arg = e.args;
    int n = 1;
    while (formal != null && arg != null) {
      ExpTy et = transExp(arg.head);
      // an array parameter is really a pointer to its elements
      Type want = formal.fieldType;
      if (want.actual() instanceof Types.ARRAY)
        want = new Types.POINTER(((Types.ARRAY) want.actual()).element);
      if (!et.ty.coerceTo(want))
        error(pos(arg.head, e.pos), "argument " + n + " of " + e.func + " has the wrong type");
      formal = formal.tail;
      arg = arg.tail;
      n++;
    }
    if (formal != null)
      error(e.pos, "too few arguments in call to " + e.func);
    if (arg != null) {
      // extra arguments are only allowed if the function was declared with ...
      if (!f.varargs)
        error(e.pos, "too many arguments in call to " + e.func);
      for (; arg != null; arg = arg.tail)
        transExp(arg.head);
    }
    return new ExpTy(null, f.result);
  }

  ExpTy transExp(Absyn.SubscriptExp e) {
    ExpTy array = transExp(e.array);
    ExpTy index = transExp(e.index);
    checkNumber(index, pos(e.index, e.pos), "array index");

    Type a = array.ty.actual();
    if (a instanceof Types.ARRAY)
      return new ExpTy(null, ((Types.ARRAY) a).element);
    if (a instanceof Types.POINTER)
      return new ExpTy(null, ((Types.POINTER) a).element);
    error(e.pos, "subscripted value is not an array or pointer");
    return new ExpTy(null, INT);
  }

  ExpTy transExp(Absyn.MemberExp e) {
    ExpTy record = transExp(e.record);
    Type t = record.ty.actual();

    if (e.arrow) {
      if (!(t instanceof Types.POINTER)) {
        error(e.pos, "-> requires a pointer to a struct or union");
        return new ExpTy(null, INT);
      }
      t = ((Types.POINTER) t).element.actual();
    }
    if (!(t instanceof Types.STRUCT)) {
      error(e.pos, (e.arrow ? "->" : ".") + " requires a struct or union");
      return new ExpTy(null, INT);
    }

    for (Types.RECORD f = ((Types.STRUCT) t).fields; f != null; f = f.tail)
      if (f.fieldName == e.field)
        return new ExpTy(null, f.fieldType);
    error(e.pos, "no member named " + e.field + " in " + ((Types.STRUCT) t).name);
    return new ExpTy(null, INT);
  }

  // the grammar does not produce casts or sizeof exp, but the Absyn nodes exist
  ExpTy transExp(Absyn.CastExp e) {
    transExp(e.exp);
    return new ExpTy(null, transTy(e.type));
  }

  ExpTy transExp(Absyn.SizeofExp e) {
    transExp(e.exp);
    return new ExpTy(null, INT);
  }

  ExpTy transExp(Absyn.SizeofTypeExp e) {
    transTy(e.type);
    return new ExpTy(null, INT);
  }

  // the following three methods are for literals, numbers = int, characters = char, strings = pointer to char
  ExpTy transExp(Absyn.IntExp e) {
    return new ExpTy(null, INT);
  }

  ExpTy transExp(Absyn.CharExp e) {
    return new ExpTy(null, CHAR);
  }

  ExpTy transExp(Absyn.StringExp e) {
    return new ExpTy(null, new Types.POINTER(CHAR));
  }


  ExpTy transExp(Absyn.OpExp e) {
    ExpTy left = transExp(e.left);
    ExpTy right = transExp(e.right);

    switch (e.oper) {
    case Absyn.OpExp.PLUS:
      checkInt(left, e.left.pos);
      checkInt(right, e.right.pos);
      return new ExpTy(null, INT);
    default:
      throw new Error("unknown operator");
    }
  }

  ExpTy transExp(Absyn.LetExp e) {
    env.venv.beginScope();
    env.tenv.beginScope();
    for (Absyn.DecList d = e.decs; d != null; d = d.tail) {
      transDec(d.head);
    }
    ExpTy body = transExp(e.body);
    env.venv.endScope();
    env.tenv.endScope();
    return new ExpTy(null, body.ty);
  }

  // ---- statements ----
  // statements have no type, so these return nothing

  void transStm(Absyn.Statement s) {
    if (s == null)                       // left behind by a syntax error
      return;
    else if (s instanceof Absyn.CompoundStatement)
      transStm((Absyn.CompoundStatement)s);
    else if (s instanceof Absyn.ExpStatement)
      transStm((Absyn.ExpStatement)s);
    else if (s instanceof Absyn.IfElseStatement)
      transStm((Absyn.IfElseStatement)s);
    else if (s instanceof Absyn.WhileStatement)
      transStm((Absyn.WhileStatement)s);
    else if (s instanceof Absyn.DoWhileStatement)
      transStm((Absyn.DoWhileStatement)s);
    else if (s instanceof Absyn.ForStatement)
      transStm((Absyn.ForStatement)s);
    else if (s instanceof Absyn.ReturnStatement)
      transStm((Absyn.ReturnStatement)s);
    else if (s instanceof Absyn.BreakStatement)
      transStm((Absyn.BreakStatement)s);
    else if (s instanceof Absyn.ContinueStatement)
      transStm((Absyn.ContinueStatement)s);
    else if (s instanceof Absyn.GotoStatement)
      transStm((Absyn.GotoStatement)s);
    else if (s instanceof Absyn.LabeledStatement)
      transStm((Absyn.LabeledStatement)s);
    else throw new Error("Semant.transStm");
  }

  // { declarations statements } gets its own scope
  void transStm(Absyn.CompoundStatement s) {
    env.venv.beginScope();
    env.tenv.beginScope();
    for (Absyn.DecList d = s.decs; d != null; d = d.tail)
      transDec(d.head);
    for (Absyn.StatementList l = s.stms; l != null; l = l.tail)
      transStm(l.head);
    env.venv.endScope();
    env.tenv.endScope();
  }

  // nothing to check here other than the expression itself ( ; has no expression)
  void transStm(Absyn.ExpStatement s) {
    transExp(s.exp);
  }

  // the test of an if or a loop must be something that can be true or false
  private void checkTest(Absyn.Exp test, int pos, String what) {
    ExpTy et = transExp(test);
    checkScalar(et, pos(test, pos), "condition of " + what);
  }

  void transStm(Absyn.IfElseStatement s) {
    checkTest(s.test, s.pos, "if");
    transStm(s.thenClause);
    transStm(s.elseClause);
  }

  void transStm(Absyn.WhileStatement s) {
    checkTest(s.test, s.pos, "while");
    loopDepth++;
    transStm(s.body);
    loopDepth--;
  }

  void transStm(Absyn.DoWhileStatement s) {
    loopDepth++;
    transStm(s.body);
    loopDepth--;
    checkTest(s.test, s.pos, "do while");
  }

  // all three parts of a for are optional
  void transStm(Absyn.ForStatement s) {
    transExp(s.init);
    if (s.test != null)
      checkTest(s.test, s.pos, "for");
    transExp(s.incr);
    loopDepth++;
    transStm(s.body);
    loopDepth--;
  }

  // the returned value must match the return type of the function we are in
  void transStm(Absyn.ReturnStatement s) {
    boolean isVoid = returnType.actual() instanceof Types.VOID;
    if (s.exp == null) {
      if (!isVoid)
        error(s.pos, "return needs a value in a non-void function");
      return;
    }
    ExpTy et = transExp(s.exp);
    if (isVoid && !(et.ty.actual() instanceof Types.VOID))
      error(s.pos, "void function cannot return a value");
    else if (!et.ty.coerceTo(returnType))
      error(s.exp.pos, "returned value does not match the function's return type");
  }

  void transStm(Absyn.BreakStatement s) {
    if (loopDepth == 0)
      error(s.pos, "break outside of a loop");
  }

  void transStm(Absyn.ContinueStatement s) {
    if (loopDepth == 0)
      error(s.pos, "continue outside of a loop");
  }

  // checked at the end of the function, since the label may come later
  void transStm(Absyn.GotoStatement s) {
    gotos.add(s);
  }

  void transStm(Absyn.LabeledStatement s) {
    if (!labels.add(s.label))
      error(s.pos, "duplicate label " + s.label);
    transStm(s.statement);
  }

  Exp transDec(Absyn.Dec d) {
    if (d == null) return null;

    if (d instanceof Absyn.VarDec)
      return transDec((Absyn.VarDec)d);

    // type def line
    if (d instanceof Absyn.TypedefDec)
      return transDec((Absyn.TypedefDec)d);

    // struct def line
    if (d instanceof Absyn.StructUnionDec)
      return transDec((Absyn.StructUnionDec)d);

    // function prototype line
    if (d instanceof Absyn.FunProtoDec)
      return transDec((Absyn.FunProtoDec)d);

    // functign definition line
    if (d instanceof Absyn.FunDec)
      return transDec((Absyn.FunDec)d);

    // enum line
    if (d instanceof Absyn.EnumDec)
      return transDec((Absyn.EnumDec)d);

    throw new Error("Semant.transDec");
  }

  // new method created to turn struct members into RECORD chain
  Types.RECORD transFields(Absyn.DecList l) {
    if (l == null) return null;
    // used ai to figure out recursive structure for struct members
    Absyn.VarDec m = (Absyn.VarDec) l.head;
    return new Types.RECORD(m.name, transTy(m.type), transFields(l.tail));
  }

  // makes a function parameter list into a RECORD chain
  Types.RECORD transParams(Absyn.ParamList l) {
    if (l == null) return null;
    if (l.head.ellipsis) {
      return transParams(l.tail);
    }
    return new Types.RECORD(l.head.name, transTy(l.head.type), transParams(l.tail));
  }

  boolean hasEllipsis(Absyn.ParamList l) {
    for (; l != null; l = l.tail)
      if (l.head.ellipsis) return true;
    return false;
  }

  Exp transDec(Absyn.FunProtoDec d) {
    Types.RECORD formals = transParams(d.params);
    Type resullts = transTy(d.type);
    FunEntry entry = new FunEntry(formals, resullts);
    entry.varargs = hasEllipsis(d.params);
    env.venv.put(d.name, entry);
    return null;
  }

  Exp transDec(Absyn.FunDec d) {
    // taken from earlier function prototype handling
    Types.RECORD formals = transParams(d.params);
    Type results = transTy(d.type);
    FunEntry entry = new FunEntry(formals, results);
    entry.varargs = hasEllipsis(d.params);
    env.venv.put(d.name, entry);

    // new level for this function, built from which parameters escape
    Translate.Level savedLevel = level;
    Translate.AccessList access = null;
    if (root != null) {
      level = new Translate.Level(root, d.name, escapes(d.params));
      entry.level = level;
      access = level.formals;
    }
    d.entry = entry;

    env.venv.beginScope();
    env.tenv.beginScope();

    // each parameter gets the access the frame allocated for it
    for (Types.RECORD f = formals; f != null; f = f.tail) {
      env.venv.put(f.fieldName, new VarEntry(access == null ? null : access.head, f.fieldType));
      if (access != null) access = access.tail;
    }

    Type saved = returnType;
    returnType = results;

    for (Absyn.DecList l = d.body.decs; l != null; l = l.tail) {
      transDec(l.head);
    }

    // each function has its own labels, and starts outside of any loop
    java.util.HashSet<Symbol.Symbol> savedLabels = labels;
    java.util.ArrayList<Absyn.GotoStatement> savedGotos = gotos;
    int savedDepth = loopDepth;
    labels = new java.util.HashSet<Symbol.Symbol>();
    gotos = new java.util.ArrayList<Absyn.GotoStatement>();
    loopDepth = 0;

    for (Absyn.StatementList l = d.body.stms; l != null; l = l.tail) {
      transStm(l.head);
    }

    // a goto can jump forward, so labels are checked once the whole body is done
    for (Absyn.GotoStatement g : gotos)
      if (!labels.contains(g.label))
        error(g.pos, "undefined label " + g.label);

    labels = savedLabels;
    gotos = savedGotos;
    loopDepth = savedDepth;
    returnType = saved;
    level = savedLevel;

    env.venv.endScope();
    env.tenv.endScope();

    return null;
  }

  // i originally missed the need for a enum handling method, and this was caught with ai assistance
  Exp transDec(Absyn.EnumDec d) {
    for (Absyn.EnumeratorList l = d.list; l != null; l = l.tail) {
      if (l.head.value != null) {
        ExpTy et = transExp(l.head.value);
        if (!et.ty.isNumber()) {
          error(l.head.pos, "enum value for " + l.head.name + " must be a number");
        }
      }
      env.venv.put(l.head.name, new VarEntry(INT));
    }
    return null;
  }


  // binds new name to trnaslated type to store in tenv
  Exp transDec(Absyn.TypedefDec d) {
    Types.NAME n = new Types.NAME(d.name);
    n.bind(transTy(d.type));
    env.tenv.put(d.name, n);
    return null;
  }

  // puts struct into teno before translating memebers to let self reference work correctly
  Exp transDec(Absyn.StructUnionDec d) {
    Types.STRUCT s = new Types.STRUCT(d.name, d.isUnion);
    env.tenv.put(d.name, s);
    s.fields = transFields(d.members);
    return null;
  }

  Type checkInit(Type expected, Absyn.Initializer init) {
    Type t = expected.actual();

    // the next three blocks were done using ai assistance explicitly
    if (t instanceof Types.ARRAY) {
      Types.ARRAY a = (Types.ARRAY) t;
      if (!(init instanceof Absyn.ListInit)) {
        error(init.pos, "array must be initialized with a list");
        return expected;
      }
      Type elem = a.element;
      int count = 0;
      for (Absyn.InitializerList l = ((Absyn.ListInit) init).list; l != null; l = l.tail) {
        elem = checkInit(elem, l.head);  
        count++;
      }
      if (a.size != -1 && count != a.size)
        error(init.pos, "array initializer has " + count + " elements, expected " + a.size);
      if (a.size != -1 && elem == a.element)
        return expected;                 
      return new Types.ARRAY(elem, a.size == -1 ? count : a.size);
    }

    if (t instanceof Types.STRUCT && !((Types.STRUCT) t).isUnion && init instanceof Absyn.ListInit) {
      Types.RECORD f = ((Types.STRUCT) t).fields;
      Absyn.InitializerList l = ((Absyn.ListInit) init).list;
      while (f != null && l != null) {
        checkInit(f.fieldType, l.head);
        f = f.tail;
        l = l.tail;
      }
      if (f != null || l != null)
        error(init.pos, "struct initializer does not match the number of members");
      return expected;
    }

    if (t instanceof Types.STRUCT && ((Types.STRUCT) t).isUnion && init instanceof Absyn.ExpInit) {
      ExpTy et = transExp(((Absyn.ExpInit) init).exp);
      if (et.ty.coerceTo(expected))
        return expected;
      for (Types.RECORD f = ((Types.STRUCT) t).fields; f != null; f = f.tail)
        if (et.ty.coerceTo(f.fieldType))
          return expected;
      error(init.pos, "value does not match any union member");
      return expected;
    }

    // a single value may be wrapped in braces: var int x = {1};  (not for unions)
    if (init instanceof Absyn.ListInit && !(t instanceof Types.STRUCT)) {
      Absyn.InitializerList l = ((Absyn.ListInit) init).list;
      if (l != null && l.tail == null)
        return checkInit(expected, l.head);
    }

    if (init instanceof Absyn.ListInit) {
      error(init.pos, "list initializer not supported for this type"); // used the autocorrect for msg
      return expected;
    } 

    ExpTy et = transExp(((Absyn.ExpInit) init).exp);

    if (!et.ty.coerceTo(expected)) {
      error(init.pos, "initializer type does not match expected type"); // used the autocorrect for msg
    }
    return expected;
  }

  Exp transDec(Absyn.VarDec d) {
    Type type = transTy(d.type);

    if (type.actual() instanceof Types.VOID) {
      error(d.pos, "variable " + d.name + " cannot be void");
    }

    if (d.init != null) {
      type = checkInit(type, d.init);
    }
    // arrays, structs and unions do not fit in a register, and globals
    // always live in memory
    if (isAggregate(type) || level == globals)
      d.escape = true;

    // allocate the variable before making its entry
    VarEntry entry = new VarEntry(alloc(d.escape, type), type);
    d.entry = entry;
    env.venv.put(d.name, entry);
    return null;
  } 
}

