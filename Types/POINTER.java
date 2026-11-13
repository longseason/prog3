package Types;

public class POINTER extends Type {
    public Type element;
    public POINTER(Type e) {element = e;}
    public boolean coerceTo(Type t) {
        Type actual = t.actual();

        if(!(actual instanceof POINTER)) {
            return false;
        }
        
        POINTER other = (POINTER) actual;
        return element.coerceTo(other.element);
    }
}
