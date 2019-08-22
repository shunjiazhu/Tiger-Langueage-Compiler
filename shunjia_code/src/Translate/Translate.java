package Translate;

/**
 * Created by zhushunjia on 2016/12/3.
 */

import Tree.*;


//Translate输入一个Frame返回一个段链表
public class Translate {
    private Frag.Frag frags = null;
    public Frame.Frame frame = null;

    public Translate(Frame.Frame f)
    {
        frame = f;
    }
    public Frag.Frag getResult()
    {
        return frags;
    }

    public void addFrag(Frag.Frag frag)
    {
        frag.next = frags;
        frags = frag;
    }

    public void procEntryExit(Level level, Exp body, boolean returnValue)
    {
        Stm b = null;
        if (returnValue)
        {
            //若有返回值,则将返回值存入$v0
            b = new MOVE(new TEMP(level.frame.RV()), body.unEx());
        }
        else
            //若无返回值,则转换为Nx
            b = body.unNx();
        b = level.frame.procEntryExit1(b);//加入函数的入口和出口代码

        addFrag(new Frag.ProcFrag(b, level.frame));

    }

    public Exp transNoExp()
    {
        //产生一条空语句
        return new Ex(new CONST(0));
    }

    public Exp transIntExp(int value)
    {
        //翻译整形常数
        return new Ex(new CONST(value));
    }

    public Exp transStringExp(String string)
    {
        //翻译字符串常量,产生一个新的数据段
        Temp.Label lab = new Temp.Label();
        addFrag(new Frag.DataFrag(lab, frame.string(lab, string)));
        return new Ex(new NAME(lab));
    }

    public Exp transNilExp()
    {
        //翻译nil语句
        return new Ex(new CONST(0));
    }

    public Exp transOpExp(int oper, Exp left, Exp right)
    {
        //翻译二元运算
        if (oper >= BINOP.PLUS && oper <= BINOP.DIV)
            return new Ex(new BINOP(oper, left.unEx(), right.unEx()));

        return new RelCx(oper, left, right);
    }

    public Exp transStringRelExp(Level currentL, int oper, Exp left, Exp right)
    {

        Tree.Exp comp = currentL.frame.externCall("stringEqual", new ExpList(left.unEx(), new ExpList(right.unEx(), null)));
        return new RelCx(oper, new Ex(comp), new Ex( new CONST(1)));
    }

    public Exp transCallExp(Level currentL, Level dest, Temp.Label name, java.util.ArrayList<Exp> args_value)
    {

        ExpList args = null;
        for (int i = args_value.size() - 1; i >= 0; --i)
        {
            args = new ExpList(((Exp) args_value.get(i)).unEx(), args);
        }
        //产生实参参数表
        Level l = currentL;
        Tree.Exp currentFP = new TEMP(l.frame.FP());
        while (dest.parent != l)
        {
            currentFP = l.staticLink().acc.exp(currentFP);
            l = l.parent;
        }

        args = new ExpList(currentFP, args);

        return new Ex(new CALL(new NAME(name), args));
    }

    public Exp transStdCallExp(Level currentL, Temp.Label name, java.util.ArrayList<Exp> args_value)
    {
        //翻译调用标准库函数
        ExpList args = null;
        for (int i = args_value.size() - 1; i >= 0; --i)
            args = new ExpList(((Exp) args_value.get(i)).unEx(), args);

        return new Ex(currentL.frame.externCall(name.toString(), args));
    }

    public Exp stmcat(Exp e1, Exp e2)
    {
        //连接两个表达式,连接后生成无返回值的表达式
        if (e1 == null)
        {
            if(e2!=null) return new Nx(e2.unNx());
            else return transNoExp();
        }
        else if (e2 == null)
            return new Nx(e1.unNx());
        else return new Nx(new SEQ(e1.unNx(), e2.unNx()));
    }

    public Exp transAssignExp(Exp lvalue, Exp exp)
    {
        //翻译赋值表达式,注意赋值表达式无返回值
        return new Nx(new MOVE(lvalue.unEx(), exp.unEx()));
    }

    public Exp exprcat(Exp e1, Exp e2)
    {
        //连接两个表达式,连接后生成有返回值的表达式
        if (e1 == null)
        {
            return new Ex(e2.unEx());
        }
        else
        {
            return new Ex(new ESEQ(e1.unNx(), e2.unEx()));
        }
    }

    public Exp transRecordExp(Level currentL, java.util.ArrayList<Exp> field)
    {

        Temp.Temp addr = new Temp.Temp();
        Tree.Exp rec_id = currentL.frame.externCall("allocRecord", new ExpList(new CONST((field.size() == 0 ? 1 : field.size()) * 4), null));//4 is wordsize

        Stm stm = transNoExp().unNx();
        //初始化指令
        for (int i = field.size() - 1; i >= 0; --i)
        {
            Tree.Exp offset = new BINOP(BINOP.PLUS, new TEMP(addr),new CONST(i * 4));//4 is wordsize
            Tree.Exp value = (field.get(i)).unEx();
            stm = new SEQ(new MOVE(new MEM(offset), value), stm);
        }
        //返回记录的首地址
        return new Ex(new ESEQ(new SEQ(new MOVE(new TEMP(addr), rec_id), stm), new TEMP(addr)));
    }

    public Exp transArrayExp(Level currentL, Exp init, Exp size)
    {

        Tree.Exp alloc = currentL.frame.externCall("initArray", new ExpList(size.unEx(), new ExpList(init.unEx(), null)));
        return new Ex(alloc);
    }

    public Exp transIfExp(Exp test, Exp e1, Exp e2)
    {
        //将if语句翻译为IR树的节点
        return new IfExp(test, e1, e2);
    }
    public Exp transWhileExp(Exp test, Exp body, Temp.Label out)
    {
        //将while语句翻译为IR树的节点
        return new WhileExp(test, body, out);
    }

    public Exp transForExp(Level currentL, Access var, Exp low, Exp high, Exp body, Temp.Label out)
    {
        //将for语句翻译为IR树的节点
        return new ForExp(currentL, var, low, high, body, out);
    }

    public Exp transBreakExp(Temp.Label l)
    {
        //翻译break语句为IR树的节点
        return new Nx(new JUMP(l));
    }

    public Exp transSimpleVar(Access acc, Level currentL)
    {
        //翻译简单变量
        Tree.Exp e = new TEMP(currentL.frame.FP());
        Level l = currentL;
        //由于可能为外层的变量,故沿着静态链接不断上溯, 直到变量的层与当前层相同
        while (l != acc.home)
        {
            e = l.staticLink().acc.exp(e);
            l = l.parent;
        }
        return new Ex(acc.acc.exp(e));
    }

    public Exp transSubscriptVar(Exp var, Exp index)
    {
        //翻译数组元素
        Tree.Exp arr_addr = var.unEx();

        Tree.Exp arr_offset = new BINOP(BINOP.MUL, index.unEx(), new CONST(4));//4 is wordsize

        return new Ex(new MEM(new BINOP(BINOP.PLUS, arr_addr, arr_offset)));

    }

    public Exp transFieldVar(Exp var, int fig)
    {
        //翻译域的成员变量
        Tree.Exp rec_addr = var.unEx();

        Tree.Exp rec_offset = new CONST(fig * 4);//4 is wordsize
        return new Ex(new MEM(new BINOP(BINOP.PLUS, rec_addr, rec_offset)));
    }



}

