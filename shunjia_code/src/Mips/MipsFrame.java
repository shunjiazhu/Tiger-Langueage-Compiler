package Mips;

/**
 * Created by zhushunjia on 2016/12/3.
 */

import Frame.*;
import Temp.*;
import Util.*;

public class MipsFrame extends Frame{
    public int allocDown = 0;//栈偏移量
    public java.util.ArrayList saveArgs = new java.util.ArrayList();//用于保存参数
    private Temp fp = new Temp(0);
    private Temp sp = new Temp(1);
    private Temp ra = new Temp(2);
    private Temp rv = new Temp(3);
    private Temp zero = new Temp(4);
    private TempList calleeSaves = null;//寄存器$s0~$s7
    public TempList callerSaves = null;//寄存器$t0~$t9
    private int numOfcalleeSaves = 8;//$s寄存器的数量,共8个

    public int wordSize(){ return 4;}
    public TempList argRegs = new TempList(new Temp(5),
                                new TempList(new Temp(6),
                                        new TempList(new Temp(7),
                                                new TempList(new Temp(8),null))));

    public MipsFrame()
    {
        for (int i = 9; i<= 18; i++)
            callerSaves = new TempList(new Temp(i), callerSaves);
        for (int i = 19; i<= 26; i++)
            calleeSaves = new TempList(new Temp(i), calleeSaves);
    }

    public Access allocLocal(boolean escape)
    {
        if (escape)
        {
            Access ret = new InFrame(this, allocDown);
            allocDown -= wordSize();//向下增长
            return ret;
        }
        else return new InReg();
    }


    public Frame newFrame(Label name, BoolList formals) {

        MipsFrame ret = new MipsFrame();
        ret.name = name; //名称
        TempList argReg = argRegs; //参数表

        for (BoolList f = formals; f != null; f = f.tail, argReg = argReg.tail)
        {

            Access a = ret.allocLocal(f.head); //为每个参数分配 Access
            ret.formals = new AccessList(a, ret.formals);
            if (argReg != null)
                ret.saveArgs.add(new Tree.MOVE(a.exp(new Tree.TEMP(fp)),
                        new Tree.TEMP(argReg.head)));

        }
        return ret;
    }

    public Tree.Stm procEntryExit1(Tree.Stm body)
    {
        //保存原fp->计算新fp->保存 ra->保存Callee-save寄存器->保存参数->(函数体原指令)->恢复Callee-save寄存器->恢复返回地址->恢复fp
        for (int i = 0; i < saveArgs.size(); ++i)
            body = new Tree.SEQ((Tree.MOVE) saveArgs.get(i), body);

        Access fpAcc = allocLocal(true);
        Access raAcc = allocLocal(true);

        Access[] calleeAcc = new Access[numOfcalleeSaves];
        TempList calleeTemp = calleeSaves;

        for (int i = 0; i < numOfcalleeSaves; ++i, calleeTemp = calleeTemp.tail)
        {
            calleeAcc[i] = allocLocal(true);
            body = new Tree.SEQ(new Tree.MOVE(calleeAcc[i].exp(new Tree.TEMP(fp)), new Tree.TEMP(calleeTemp.head)), body);
        }

        body = new Tree.SEQ(new Tree.MOVE(raAcc.exp(new Tree.TEMP(fp)), new Tree.TEMP(ra)), body);
        body = new Tree.SEQ(new Tree.MOVE(new Tree.TEMP(fp), new Tree.BINOP(Tree.BINOP.PLUS, new Tree.TEMP(sp), new Tree.CONST(-allocDown - wordSize()))), body);
        body = new Tree.SEQ(new Tree.MOVE(fpAcc.expFromStack(new Tree.TEMP(sp)), new Tree.TEMP(fp)), body);
        calleeTemp = calleeSaves;

        for (int i = 0; i < numOfcalleeSaves; ++i, calleeTemp = calleeTemp.tail)
            body = new Tree.SEQ(body, new Tree.MOVE(new Tree.TEMP(calleeTemp.head), calleeAcc[i].exp(new Tree.TEMP(fp))));

        body = new Tree.SEQ(body, new Tree.MOVE(new Tree.TEMP(ra), raAcc.exp(new Tree.TEMP(fp))));
        body = new Tree.SEQ(body, new Tree.MOVE(new Tree.TEMP(fp), fpAcc.expFromStack(new Tree.TEMP(sp))));

        return body;
    }

    public Assem.InstrList procEntryExit2(Assem.InstrList body)
    {
        return Assem.InstrList.append(body, new Assem.InstrList(new Assem.OPER("", null, new TempList(zero, new TempList(sp, new TempList(ra, calleeSaves)))), null));
    }

    //设置函数体标号->分配帧空间->(函数体原指令)-> 将$sp 加上相应的帧空间->跳转到返回地址
    public Assem.InstrList procEntryExit3(Assem.InstrList body)
    {

        body = new Assem.InstrList(new Assem.OPER("subu $sp, $sp, " + (-allocDown), new TempList(sp, null), new TempList(sp, null)), body);

        if (name.toString().equals("main"))
            body = new Assem.InstrList(new Assem.OPER("t_main: ", null, null), body);
        else body = new Assem.InstrList(new Assem.OPER(name.toString() + ":", null, null), body);

        //跳转到返回地址
        Assem.InstrList epilogue = new Assem.InstrList(new Assem.OPER("jr $ra", null, new TempList(ra, null)), null);


        //将$sp 加上相应的帧空间
        epilogue = new Assem.InstrList(new Assem.OPER("addu $sp, $sp, " + (-allocDown), new TempList(sp, null), new TempList(sp, null)), epilogue);


        body = Assem.InstrList.append(body, epilogue);
        return body;
    }

    public String string(Label label, String value)
    {
        //产生字符串的数据段汇编代码
        String ret = label.toString() + ": " + System.getProperty("line.separator");
        if (value.equals("\n"))
        {
            ret = ret + ".word " + value.length() + System.getProperty("line.separator") ;
            ret = ret + ".asciiz \"" + System.getProperty("line.separator") + "\"";
            return ret;
        }
        ret = ret + ".word " + value.length() +  System.getProperty("line.separator");
        ret = ret + ".asciiz \"" + value + "\"";
        return ret;
    }

    //返回$fp\$sp\$ra\$rv寄存器
    public Temp FP()
    {
        return fp;
    }
    public Temp SP()
    {
        return sp;
    }
    public Temp RA()
    {
        return ra;
    }
    public Temp RV()
    {
        return rv;
    }


    public Tree.Exp externCall(String func, Tree.ExpList args)
    {
        //调用标准库函数
        return new Tree.CALL(new Tree.NAME(new Label(func)), args);
    }

    public String tempMap(Temp t)
    {
        //将传入的寄存器转换为寄存器名称
        if (t.toString().equals("t0"))
            return "$fp";
        if (t.toString().equals("t1"))
            return "$sp";
        if (t.toString().equals("t2"))
            return "$ra";
        if (t.toString().equals("t3"))
            return "$v0";
        if (t.toString().equals("t4"))
            return "$zero";

        for (int i = 5; i <= 8; i++)
            if (t.toString().equals("t" + i))
            {
                int r = i - 5;
                return "$a" + r;
            }
        for (int i = 9; i <= 18; i++)
            if (t.toString().equals("t" + i))
            {
                int r = i - 9;
                return "$t" + r;
            }
        for (int i = 19; i <= 26; i++)
            if (t.toString().equals("t" + i))
            {
                int r = i - 19;
                return "$s" + r;
            }

        return null;
    }

    public Assem.InstrList codegen(Tree.Stm s)
    {
        //调用CodeGen类内的codegen函数,用于产生汇编指令
        return (new CodeGen(this)).codegen(s);
    }

    public java.util.HashSet registers()
    {
        java.util.HashSet ret = new java.util.HashSet();

        //将calleeSave寄存器存入哈希表
        for (TempList tl = this.calleeSaves; tl != null; tl = tl.tail)
            ret.add(tl.head);

        //将callerSave寄存器存入哈希表
        for (TempList tl = this.callerSaves; tl != null; tl = tl.tail)
            ret.add(tl.head);

        return ret;
    }
}
