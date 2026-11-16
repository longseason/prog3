package FindEscape;

// Escape analysis for C. In Tiger a variable escapes when a nested function
// uses it. C has no nested functions; instead a variable escapes (must be
// kept in memory instead of a register) when
//   - its address is taken with &, or
//   - it is declared volatile.
// The result is stored in the escape field of Absyn.VarDec and Absyn.Param.
// Leaf functions are not tracked, since there is no static link to save.
public class FindEscape {
  Symbol.Table escEnv = new Symbol.Table(); // escEnv maps Symbol to Escape

  static final Symbol.Symbol VOLATILE = Symbol.Symbol.symbol("volatile");

  public FindEscape(Absyn.DecList decs) {
    for (Absyn.DecList d = decs; d != null; d = d.tail)
      traverseDec(0, d.head);
  }

  private boolean isVolatile(Absyn.DescriptorList l) {
    for (; l != null; l = l.tail)
      if (l.head == VOLATILE) return true;
    return false;
  }

  // ---- declarations ----

  void traverseDec(int depth, Absyn.Dec d) {
    if (d instanceof Absyn.VarDec)
      traverseDec(depth, (Absyn.VarDec)d);
    else if (d instanceof Absyn.FunDec)
      traverseDec(depth, (Absyn.FunDec)d);
    // structs, typedefs, enums and prototypes declare no variables
  }

  void traverseDec(int depth, Absyn.VarDec d) {
    // the initializer is traversed first: it cannot refer to the new variable
    traverseInit(depth, d.init);
    Escape esc = new VarEscape(depth, d);
    escEnv.put(d.name, esc);
    if (isVolatile(d.desc))
      esc.setEscape();
  }

  void traverseDec(int depth, Absyn.FunDec d) {
    escEnv.beginScope();
    for (Absyn.ParamList p = d.params; p != null; p = p.tail)
      if (!p.head.ellipsis)
        escEnv.put(p.head.name, new FormalEscape(depth + 1, p.head));
    if (d.body != null) {
      for (Absyn.DecList l = d.body.decs; l != null; l = l.tail)
        traverseDec(depth + 1, l.head);
      for (Absyn.StatementList l = d.body.stms; l != null; l = l.tail)
        traverseStm(depth + 1, l.head);
    }
    escEnv.endScope();
  }

  void traverseInit(int depth, Absyn.Initializer i) {
    if (i instanceof Absyn.ExpInit)
      traverseExp(depth, ((Absyn.ExpInit)i).exp);
    else if (i instanceof Absyn.ListInit)
      for (Absyn.InitializerList l = ((Absyn.ListInit)i).list; l != null; l = l.tail)
        traverseInit(depth, l.head);
  }

  // ---- statements: nothing interesting, just visit the children ----

  void traverseStm(int depth, Absyn.Statement s) {
    if (s instanceof Absyn.CompoundStatement) {
      Absyn.CompoundStatement c = (Absyn.CompoundStatement)s;
      escEnv.beginScope();
      for (Absyn.DecList l = c.decs; l != null; l = l.tail)
        traverseDec(depth, l.head);
      for (Absyn.StatementList l = c.stms; l != null; l = l.tail)
        traverseStm(depth, l.head);
      escEnv.endScope();
    } else if (s instanceof Absyn.ExpStatement) {
      traverseExp(depth, ((Absyn.ExpStatement)s).exp);
    } else if (s instanceof Absyn.IfElseStatement) {
      Absyn.IfElseStatement i = (Absyn.IfElseStatement)s;
      traverseExp(depth, i.test);
      traverseStm(depth, i.thenClause);
      traverseStm(depth, i.elseClause);
    } else if (s instanceof Absyn.WhileStatement) {
      traverseExp(depth, ((Absyn.WhileStatement)s).test);
      traverseStm(depth, ((Absyn.WhileStatement)s).body);
    } else if (s instanceof Absyn.DoWhileStatement) {
      traverseStm(depth, ((Absyn.DoWhileStatement)s).body);
      traverseExp(depth, ((Absyn.DoWhileStatement)s).test);
    } else if (s instanceof Absyn.ForStatement) {
      Absyn.ForStatement f = (Absyn.ForStatement)s;
      traverseExp(depth, f.init);
      traverseExp(depth, f.test);
      traverseExp(depth, f.incr);
      traverseStm(depth, f.body);
    } else if (s instanceof Absyn.ReturnStatement) {
      traverseExp(depth, ((Absyn.ReturnStatement)s).exp);
    } else if (s instanceof Absyn.LabeledStatement) {
      traverseStm(depth, ((Absyn.LabeledStatement)s).statement);
    }
    // break, continue, goto and null have no children
  }

  // ---- expressions ----

  void traverseExp(int depth, Absyn.Exp e) {
    if (e instanceof Absyn.UnOpExp) {
      Absyn.UnOpExp u = (Absyn.UnOpExp)e;
      // the interesting case: &x means x must live in memory
      if (u.operator == Absyn.UnOpExp.ADDR)
        setEscape(u.exp);
      traverseExp(depth, u.exp);
    } else if (e instanceof Absyn.BinOpExp) {
      traverseExp(depth, ((Absyn.BinOpExp)e).left);
      traverseExp(depth, ((Absyn.BinOpExp)e).right);
    } else if (e instanceof Absyn.AssignExp) {
      traverseExp(depth, ((Absyn.AssignExp)e).left);
      traverseExp(depth, ((Absyn.AssignExp)e).right);
    } else if (e instanceof Absyn.CommaExp) {
      traverseExp(depth, ((Absyn.CommaExp)e).left);
      traverseExp(depth, ((Absyn.CommaExp)e).right);
    } else if (e instanceof Absyn.CondExp) {
      traverseExp(depth, ((Absyn.CondExp)e).test);
      traverseExp(depth, ((Absyn.CondExp)e).thenExp);
      traverseExp(depth, ((Absyn.CondExp)e).elseExp);
    } else if (e instanceof Absyn.CallExp) {
      for (Absyn.ExpList a = ((Absyn.CallExp)e).args; a != null; a = a.tail)
        traverseExp(depth, a.head);
    } else if (e instanceof Absyn.SubscriptExp) {
      traverseExp(depth, ((Absyn.SubscriptExp)e).array);
      traverseExp(depth, ((Absyn.SubscriptExp)e).index);
    } else if (e instanceof Absyn.MemberExp) {
      traverseExp(depth, ((Absyn.MemberExp)e).record);
    } else if (e instanceof Absyn.ParenExp) {
      traverseExp(depth, ((Absyn.ParenExp)e).exp);
    } else if (e instanceof Absyn.CastExp) {
      traverseExp(depth, ((Absyn.CastExp)e).exp);
    } else if (e instanceof Absyn.SizeofExp) {
      traverseExp(depth, ((Absyn.SizeofExp)e).exp);
    }
    // identifiers, literals and sizeof(type) have no children
  }

  // Mark the variable whose address is taken by & as escaping.
  // &x, &(x), &x[i] and &x.f all take the address of (part of) x.
  // &p->f does not: it only uses the value of the pointer p.
  void setEscape(Absyn.Exp e) {
    if (e instanceof Absyn.ParenExp) {
      setEscape(((Absyn.ParenExp)e).exp);
    } else if (e instanceof Absyn.SubscriptExp) {
      setEscape(((Absyn.SubscriptExp)e).array);
    } else if (e instanceof Absyn.MemberExp) {
      if (!((Absyn.MemberExp)e).arrow)
        setEscape(((Absyn.MemberExp)e).record);
    } else if (e instanceof Absyn.IdExp) {
      Escape esc = (Escape) escEnv.get(((Absyn.IdExp)e).name);
      if (esc != null)
        esc.setEscape();
    }
  }
}
