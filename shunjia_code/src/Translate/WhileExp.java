package Translate;

/**
 * Created by zhushunjia on 2016/12/3.
 */

import Temp.*;
import Tree.*;

public class WhileExp extends Exp{
    Exp test = null;
    Exp body = null;
    Label out = null; //出口

    WhileExp(Exp test, Exp body, Label out)
    {
        this.test = test;
        this.body = body;
        this.out = out;
    }

    Tree.Exp unEx()
    {
        return null;
    }


    Tree.Stm unNx()
    {
        Label begin = new Label();
        Label t = new Label();
        return new SEQ(new LABEL(begin),
                new SEQ(test.unCx(t, out),
                        new SEQ(new LABEL(t),
                                new SEQ(body.unNx(),
                                        new SEQ(new JUMP(begin),
                                                new LABEL(out))))));
    }

    //while 只有一个出口,故无法转换
    Tree.Stm unCx(Label t, Label f)
    {
        return null;
    }
}
