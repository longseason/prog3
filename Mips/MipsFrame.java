package Mips;
import java.util.Hashtable;
import Symbol.Symbol;
import Temp.Temp;
import Temp.Label;
import Frame.Frame;
import Frame.Access;
import Frame.AccessList;

public class MipsFrame extends Frame {

  private int count = 0;
  public Frame newFrame(Symbol name, Util.BoolList formals) {
    Label label;
    if (name == null)
      label = new Label();
    else if (this.name != null)
      label = new Label(this.name + "." + name + "." + count++);
    else
      label = new Label(name);
    return new MipsFrame(label, formals);
  }

  public MipsFrame() {}
  private MipsFrame(Label n, Util.BoolList f) {
    name = n;
    formals = allocFormals(0, f);
  }

  // An escaping formal lives in the argument area that the caller set up,
  // at offsets 0, 4, 8, ... from the frame pointer. MIPS reserves a slot
  // there for every argument, even the ones passed in registers, so the
  // offset goes up for every formal. A formal that does not escape is
  // copied into a new temporary.
  private AccessList allocFormals(int offset, Util.BoolList f) {
    if (f == null)
      return null;
    Access a;
    if (f.head)
      a = new InFrame(offset);
    else
      a = new InReg(new Temp());
    return new AccessList(a, allocFormals(offset + wordSize, f.tail));
  }

  private static final int wordSize = 4;
  public int wordSize() { return wordSize; }

  // locals grow down from the frame pointer: -4, -8, ...
  private int localOffset = 0;

  public Access allocLocal(boolean escape) { return allocLocal(escape, 1); }

  public Access allocLocal(boolean escape, int words) {
    if (!escape)
      return new InReg(new Temp());
    localOffset -= words * wordSize;
    return new InFrame(localOffset);
  }
}
