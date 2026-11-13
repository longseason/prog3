package Types;

public class STRUCT extends Type {
    public Symbol.Symbol name;
    public boolean isUnion;
    public RECORD fields;

    public STRUCT(Symbol.Symbol n, boolean u) {name = n; isUnion = u;}

    public boolean coerceTo(Type t) {
        return this == t.actual();
    }
}
