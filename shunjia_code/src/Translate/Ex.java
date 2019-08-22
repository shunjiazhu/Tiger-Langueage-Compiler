package Translate;

/**
 * Created by zhushunjia on 2016/12/3.
 */
import Temp.*;

public class Ex extends Exp{

    Tree.Exp exp;
    public Ex(Tree.Exp e){	exp = e;	}
    Tree.Exp unEx(){	return exp;	}
    Tree.Stm unNx(){	return new Tree.Exp(exp);	} //无返回值表达式
    Tree.Stm unCx(Label t, Label f)
    {	return new Tree.CJUMP(Tree.CJUMP.NE, exp, new Tree.CONST(0), t, f);	}
}
