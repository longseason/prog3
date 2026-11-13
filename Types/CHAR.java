package Types;

public class CHAR extends Type {
	public CHAR () {}
	public boolean coerceTo(Type t) {
		Type actual = t.actual();
		return actual instanceof CHAR;
	}
}