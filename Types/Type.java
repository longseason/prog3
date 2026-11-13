package Types;

public abstract class Type {
   public Type actual() {return this;}
         
   public boolean coerceTo(Type t) {return false;}

   // check if the type is a number
   public boolean isNumber() {return false;}

   public static boolean sameType(Type a, Type b) {
      a = a.actual();
      b = b.actual();

      if (a instanceof POINTER && b instanceof POINTER) {
		   return sameType(((POINTER) a).element, ((POINTER) b).element);
	   }

      if (a instanceof ARRAY && b instanceof ARRAY) {
         return ((ARRAY) a).size == ((ARRAY) b).size
             && sameType(((ARRAY) a).element, ((ARRAY) b).element);
      }
        
      if (a instanceof INT  && b instanceof INT) {
		   return true;
      }
      if (a instanceof CHAR && b instanceof CHAR) {
		   return true;
      }
      if (a instanceof VOID && b instanceof VOID) {
		   return true;
      }
      
      return a == b;
   }
}
