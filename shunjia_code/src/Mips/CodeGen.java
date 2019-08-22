package Mips;

/**
 * Created by zhushunjia on 2016/12/3.
 */


public class CodeGen {
    Frame.Frame frame;
    Assem.InstrList last = null, ilist = null;

    public CodeGen(Frame.Frame f)
    {	frame = f;	}

    private void emit(Assem.Instr inst)
    {
        if (last != null)
        {
            last.tail = new Assem.InstrList(inst, null);
            last = last.tail;
        }
        else
        {
            ilist = new Assem.InstrList(inst, null);
            last = ilist;
        }
    }

    private Temp.TempList L(Temp.Temp h, Temp.TempList l)
    {
        return new Temp.TempList(h, l);
    }

    public Assem.InstrList codegen(Tree.Stm t)
    {
        Assem.InstrList l;
        munchStm(t);

        l = ilist;
        last = ilist = null;
        return l;
    }

    private void munchStm(Tree.Stm s)
    {
        if (s instanceof Tree.Exp) munchStm((Tree.Exp)s);
        if (s instanceof Tree.SEQ) munchStm((Tree.SEQ)s);
        if (s instanceof Tree.MOVE) munchStm((Tree.MOVE)s);
        if (s instanceof Tree.LABEL) munchStm((Tree.LABEL)s);
        if (s instanceof Tree.JUMP) munchStm((Tree.JUMP)s);
        if (s instanceof Tree.CJUMP) munchStm((Tree.CJUMP)s);
    }

    private void munchStm(Tree.Exp e)
    {
        munchExp(e);
    }

    private void munchStm(Tree.SEQ s)
    {
        //翻译SEQ,分别翻译左右子树
        munchStm(s.left);
        munchStm(s.right);
    }

    private void munchStm(Tree.MOVE m)
    {
        //翻译move语句
        if (m.dst instanceof Tree.TEMP)
        {
            if (m.src instanceof Tree.CONST)
            {
                emit(new Assem.OPER("li `d0, " + ((Tree.CONST) m.src).value, L(((Tree.TEMP) m.dst).temp, null), null));
            }
            else
            {
                Temp.Temp t1 = munchExp(m.src);
                emit(new Assem.OPER("move `d0, `s0", L(((Tree.TEMP) m.dst).temp, null), L(t1, null)));
            }
            return;
        }
        if (m.dst instanceof Tree.MEM)
        {
            Tree.MEM mem = (Tree.MEM)m.dst;
            if (mem.exp instanceof Tree.BINOP)
            {
                Tree.BINOP mexp = (Tree.BINOP)mem.exp;
                if (mexp.binop == Tree.BINOP.PLUS && mexp.right instanceof Tree.CONST)
                {
                    Temp.Temp t1 = munchExp(m.src);
                    Temp.Temp t2 = munchExp(mexp.left);
                    emit(new Assem.OPER("sw `s0, " + ((Tree.CONST)mexp.right).value + "(`s1)",
                            null,
                            L(t1, L(t2, null))));
                    return;
                }
                if (mexp.binop == Tree.BINOP.PLUS && mexp.left instanceof Tree.CONST)
                {
                    Temp.Temp t1 = munchExp(m.src);
                    Temp.Temp t2 = munchExp(mexp.right);
                    emit(new Assem.OPER("sw `s0, "	+ ((Tree.CONST)mexp.left).value + "(`s1)",
                            null,
                            L(t1, L(t2, null))));
                    return;
                }
            }
            if (mem.exp instanceof Tree.CONST)
            {

                Temp.Temp t1 = munchExp(m.src);
                emit(new Assem.OPER("sw `s0, " + ((Tree.CONST) mem.exp).value,
                        null,
                        L(t1, null)));
                return;
            }
            Temp.Temp t1 = munchExp(m.src);
            Temp.Temp t2 = munchExp(mem.exp);
            emit(new Assem.OPER("sw `s0, (`s1)",
                    null,
                    L(t1, L(t2, null))));
        }
    }
    private void munchStm(Tree.LABEL l)
    {
        //label用以标记下一条指令的地址(PC)
        emit(new Assem.LABEL(l.label.toString() + ": ", l.label));
    }
    private void munchStm(Tree.JUMP j)
    {
        emit(new Assem.OPER("j " + j.targets.head, null, null, j.targets));
    }
    private void munchStm(Tree.CJUMP j)
    {
        //翻译条件转移指令
        String oper = null;
        switch (j.relop)
        {
            case Tree.CJUMP.EQ:
                oper = "beq";	break;
            case Tree.CJUMP.NE:
                oper = "bne";	break;
            case Tree.CJUMP.GT:
                oper = "bgt";	break;
            case Tree.CJUMP.GE:
                oper = "bge";	break;
            case Tree.CJUMP.LT:
                oper = "blt";	break;
            case Tree.CJUMP.LE:
                oper = "ble";	break;
        }
        Temp.Temp t1 = munchExp(j.left);
        Temp.Temp t2 = munchExp(j.right);

        emit(new Assem.OPER(oper + " `s0, `s1, `j0",
                null,
                L(t1, L(t2, null)),
                new Temp.LabelList(j.iftrue, new Temp.LabelList(j.iffalse, null))));
    }


    private Temp.Temp munchExp(Tree.Exp m)
    {
        //调用重载函数翻译有返回值的表达式
        if (m instanceof Tree.MEM) return munchExp((Tree.MEM)m);
        if (m instanceof Tree.CONST) return munchExp((Tree.CONST)m);
        if (m instanceof Tree.TEMP) return munchExp((Tree.TEMP)m);
        if (m instanceof Tree.NAME) return munchExp((Tree.NAME)m);
        if (m instanceof Tree.BINOP) return munchExp((Tree.BINOP)m);
        if (m instanceof Tree.CALL) return munchExp((Tree.CALL)m);
        return munchExp(m.exp);
    }
    private Temp.Temp munchExp(Tree.MEM m)
    {
        //翻译读取内存的指令
        Temp.Temp ret = new Temp.Temp();
        if (m.exp instanceof Tree.CONST)
        {
            emit(new Assem.OPER("lw `d0, " + ((Tree.CONST) m.exp).value,
                    L(ret,null),
                    null));
            return ret;
        }
        if (m.exp instanceof Tree.BINOP && ((Tree.BINOP)m.exp).binop == Tree.BINOP.PLUS)
        {
            if (((Tree.BINOP)m.exp).right instanceof Tree.CONST)
            {
                Temp.Temp t1 = munchExp(((Tree.BINOP)m.exp).left);
                emit(new Assem.OPER("lw `d0, " + ((Tree.CONST) ((Tree.BINOP) m.exp).right).value + "(`s0)",
                        L(ret, null),
                        L(t1, null)));
                return ret;
            }
            if (((Tree.BINOP)m.exp).left instanceof Tree.CONST)
            {
                Temp.Temp t1 = munchExp(((Tree.BINOP)m.exp).right);
                emit(new Assem.OPER("lw `d0, " + ((Tree.CONST) ((Tree.BINOP) m.exp).left).value + "(`s0)",
                        L(ret, null),
                        L(t1, null)));
                return ret;
            }
        }

        Temp.Temp t1 = munchExp(m.exp);
        emit(new Assem.OPER("lw `d0, (`s0)",
                L(ret, null),
                L(t1, null)));
        return ret;
    }
    private Temp.Temp munchExp(Tree.CONST e)
    {
        //常数值的翻译,将常数放入寄存器中
        Temp.Temp ret = new Temp.Temp();
        emit(new Assem.OPER("li `d0, " + e.value, L(ret, null), null));
        return ret;
    }

    private Temp.Temp munchExp(Tree.TEMP t)
    {
        //寄存器翻译,直接翻译为寄存器
        return t.temp;
    }

    private Temp.Temp munchExp(Tree.NAME n)
    {
        //翻译name语句,将label的地址值存入寄存器中
        Temp.Temp ret = new Temp.Temp();
        emit(new Assem.OPER("la `d0, " + n.label, L(ret, null), null));
        return ret;
    }

    private Temp.Temp munchExp(Tree.BINOP b)
    {
        //翻译二元运算语句
        Temp.Temp ret = new Temp.Temp();
        String oper = null;
        switch (b.binop)
        {
            case Tree.BINOP.PLUS:
                oper = "add";	break;
            case Tree.BINOP.MINUS:
                oper = "sub";	break;
            case Tree.BINOP.MUL:
                oper = "mul";	break;
            case Tree.BINOP.DIV:
                oper = "div";	break;
        }
        if (b.right instanceof Tree.CONST)
        {
            Temp.Temp t1 = munchExp(b.left);
            emit(new Assem.OPER(oper + " `d0, `s0, " + ((Tree.CONST) b.right).value,
                    L(ret, null),
                    L(t1, null)));
            return ret;
        }

        if (b.left instanceof Tree.CONST)
        {
            Temp.Temp t1 = munchExp(b.right);
            emit(new Assem.OPER("li `d0, " + ((Tree.CONST) b.left).value,
                    L(ret, null),
                    null));
            emit(new Assem.OPER(oper + " `d0, `s0, `s1",
                    L(ret, null),
                    L(ret, L(t1, null))));
            return ret;
        }

        //两个运算数都是寄存器的情况
        Temp.Temp t1 = munchExp(b.left);
        Temp.Temp t2 = munchExp(b.right);
        emit(new Assem.OPER(oper + " `d0, `s0, `s1",
                L(ret, null),
                L(t1, L(t2, null))));
        return ret;
    }
    private Temp.Temp munchExp(Tree.CALL c)
    {

        Temp.TempList list = null;
        int i = 0;
        for (Tree.ExpList a = c.args; a != null; a = a.tail, i++)
        {
            //翻译参数表
            Temp.Temp t = null;
            if (a.head instanceof Tree.CONST)
                emit(new Assem.OPER("li $a" + i + ", " + ((Tree.CONST) a.head).value,
                        null,
                        null));
            else
            {

                t = munchExp(a.head);
                emit(new Assem.OPER("move $a" + i + ", `s0",
                        null,
                        L(t, null)));
            }
            if (t != null)
                list = L(t, list);
        }

        emit(new Assem.OPER("jal " + ((Tree.NAME) c.func).label, ((MipsFrame)frame).callerSaves, list));
        return frame.RV();
    }


}
