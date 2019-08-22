package Semant;

/**
 * Created by zhushunjia on 2016/12/3.
 */

import Types.*;

public class FuncEntry extends Entry {
    RECORD paramlist;//������
    Type returnTy;//��������
    public Translate.Level level;
    public Temp.Label label;

    public FuncEntry( Translate.Level level, Temp.Label label, RECORD p, Type rt)
    {
        paramlist = p;
        returnTy = rt;
        this.level = level;
        this.label = label;
    }
}
