package Absyn;

public class BinOpExp extends Exp {
    public final static int MUL=0, DIV=1, MOD=2, PLUS=3, MINUS=4, SHL=5, SHR=6, LT=7, GT=8, LE=9, GE=10, EQ=11, NE=12,
BITAND=13, BITXOR=14, BITOR=15, AND=16, OR=17;

    public Exp left;
    public Exp right;
    public int operator;
    public BinOpExp(int p, Exp left, int operator, Exp right) {pos=p; this.left=left; this.operator=operator; this.right=right;}

    
}
