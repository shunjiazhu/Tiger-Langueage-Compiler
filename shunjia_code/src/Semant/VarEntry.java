package Semant;

/**
 * Created by zhushunjia on 2016/12/3.
 */

import Types.*;

public class VarEntry extends Entry {
    Type Ty;//类型
    Translate.Access acc;//存储空间
    boolean isFor;//循环变量

    public VarEntry(Type ty, Translate.Access acc){ Ty = ty; this.acc = acc; this.isFor=false; }
    public VarEntry(Type ty, Translate.Access acc, boolean isf){ Ty = ty; this.acc = acc; this.isFor=isf; }
}
