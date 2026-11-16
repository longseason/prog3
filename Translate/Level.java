package Translate;
import Temp.Label;
import Symbol.Symbol;
import Util.BoolList;

public class Level {
  Level parent;
  Frame.Frame frame;		// not public!
  public AccessList formals;
  public AccessList frameFormals;
  // C has no nested functions, so there is no static link: the frame's
  // formals are exactly the function's parameters
  public Level(Level parent, Symbol name, BoolList formals) {
    this.parent = parent;
    this.frame = parent.frame.newFrame(name, formals);
    frameFormals = allocFormals(this.frame.formals);
    this.formals = frameFormals;
  }
  private AccessList allocFormals(Frame.AccessList formals) {
    if (formals == null)
      return null;
    return new AccessList(new Access(this, formals.head),
			  allocFormals(formals.tail));
  }

  public Level(Frame.Frame f) {
    frame = f;
  }

  public Label name() {
    return frame.name;
  }

  public Access allocLocal(boolean escape) {
    return new Access(this, frame.allocLocal(escape));
  }

  public Access allocLocal(boolean escape, int words) {
    return new Access(this, frame.allocLocal(escape, words));
  }
}
