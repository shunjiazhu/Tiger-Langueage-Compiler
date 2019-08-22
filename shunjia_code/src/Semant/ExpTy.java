package Semant;

/**
 * Created by zhushunjia on 2016/12/3.
 */

import Translate.*;
import Types.*;

public class ExpTy {
    Exp exp;
    Type ty;
    ExpTy(Exp e, Type t)
    {
        exp = e;
        ty = t;
    }
}
