package Types;

public class ARRAY extends Type {
   public Type element;
   public int size;

   
   public ARRAY(Type e, int s) {element = e; size = s;}
   public boolean coerceTo(Type t) {
      Type actual = t.actual();
      if (actual instanceof ARRAY) {
         ARRAY other = (ARRAY) actual;
         return sameType(element, other.element) && size == other.size;
      }

      if (actual instanceof POINTER) {
         POINTER other = (POINTER) actual;
         return sameType(element, other.element);
      }
      return false;
   }
}
