package Translate;

/**
 * Created by zhushunjia on 2016/12/3.
 */

import Symbol.Symbol;
import Util.*;

public class Level {
    public Level parent;
    public Frame.Frame frame;//层中对应的帧
    public AccessList formals = null;//参数表

    public Level(Level parent, Symbol name, BoolList formals)
    {
        this.parent = parent;
        BoolList bl = new BoolList(true, formals);

        this.frame = parent.frame.newFrame(new Temp.Label(name), bl);

        for (Frame.AccessList f = frame.formals; f != null; f = f.next)
            this.formals = new AccessList(new Access(this, f.head), this.formals);
    }

    public Level(Frame.Frame frm)
    {
        this.frame = frm;	this.parent = null;
    }

    public Access staticLink()
    {   //寄存器$a0
        return formals.head;
    }

    public Access allocLocal(boolean escape)
    {
        return new Access(this, frame.allocLocal(escape));
    }

}
