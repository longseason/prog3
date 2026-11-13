package Types;

public class INT extends Type {
	public INT () {}
	public boolean coerceTo(Type t) {
		Type actual = t.actual();
		return (actual instanceof INT) || (actual instanceof POINTER);
	}
	public boolean isNumber() {return true;}
}
