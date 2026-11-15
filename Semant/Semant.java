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
  // second semant constructor, where frame isnt used til activation records
  public Semant(Frame.Frame frame, ErrorMsg.ErrorMsg err) {
    this(new Env(err));
  }

  public void transProg(Absyn.DecList decs) {
    for (Absyn.DecList d = decs; d != null; d = d.tail) {
      transDec(d.head);
    }
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
      if (!et.ty.coerceTo(formal.fieldType))
        error(pos(arg.head, e.pos), "argument " + n + " of " + e.func + " has the wrong type");
      formal = formal.tail;
      arg = arg.tail;
      n++;
    }
    if (formal != null)
      error(e.pos, "too few arguments in call to " + e.func);
    if (arg != null) {
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

  Exp transDec(Absyn.Dec d) {
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

  Exp transDec(Absyn.FunProtoDec d) {
    Types.RECORD formals = transParams(d.params);
    Type resullts = transTy(d.type);
    env.venv.put(d.name, new FunEntry(formals, resullts));
    return null;
  }

  Exp transDec(Absyn.FunDec d) {
    // taken from earlier function prototype handling
    Types.RECORD formals = transParams(d.params);
    Type results = transTy(d.type);
    env.venv.put(d.name, new FunEntry(formals, results));

    env.venv.beginScope();
    env.tenv.beginScope();

    for (Types.RECORD f = formals; f != null; f = f.tail) {
      env.venv.put(f.fieldName, new VarEntry(f.fieldType));
    }

    Type saved = returnType;
    returnType = results;

    for (Absyn.DecList l = d.body.decs; l != null; l = l.tail) {
      transDec(l.head);
    }

    // statements come here

    returnType = saved;

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
    env.venv.put(d.name, new VarEntry(type));
    return null;
  } 
}

