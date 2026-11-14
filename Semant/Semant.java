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
    throw new Error("Semant.transExp");
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
    throw new Error("Semant.transDec");
  }

  // new method created to turn struct members into RECORD chain
  Types.RECORD transFields(Absyn.DecList l) {
    if (l == null) return null;
    // used ai to figure out recursive structure for struct members
    Absyn.VarDec m = (Absyn.VarDec) l.head;
    return new Types.RECORD(m.name, transTy(m.type), transFields(l.tail));
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

