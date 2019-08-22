package Semant;

/**
 * Created by zhushunjia on 2016/12/3.
 */

import Types.*;

public class FuncEntry extends Entry {
    RECORD paramlist;//参数表
    Type returnTy;//返回类型
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
