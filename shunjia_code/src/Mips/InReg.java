package Mips;

/**
 * Created by zhushunjia on 2016/12/3.
 */


public class InReg extends Frame.Access
{
    private Temp.Temp reg;
    public InReg() {reg = new Temp.Temp();}

    public Tree.Exp exp(Tree.Exp framePtr)
    {	return new Tree.TEMP(reg);	}
    public Tree.Exp expFromStack(Tree.Exp stackPtr)
    {	return new Tree.TEMP(reg);	}
}