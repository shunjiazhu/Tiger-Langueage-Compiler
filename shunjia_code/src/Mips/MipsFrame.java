package Mips;

/**
 * Created by zhushunjia on 2016/12/3.
 */

import Frame.*;
import Temp.*;
import Util.*;

public class MipsFrame extends Frame{
    public int allocDown = 0;//ջƫ����
    public java.util.ArrayList saveArgs = new java.util.ArrayList();//���ڱ������
    private Temp fp = new Temp(0);
    private Temp sp = new Temp(1);
    private Temp ra = new Temp(2);
    private Temp rv = new Temp(3);
    private Temp zero = new Temp(4);
    private TempList calleeSaves = null;//�Ĵ���$s0~$s7
    public TempList callerSaves = null;//�Ĵ���$t0~$t9
    private int numOfcalleeSaves = 8;//$s�Ĵ���������,��8��

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
            allocDown -= wordSize();//��������
            return ret;
        }
        else return new InReg();
    }


    public Frame newFrame(Label name, BoolList formals) {

        MipsFrame ret = new MipsFrame();
        ret.name = name; //����
        TempList argReg = argRegs; //������

        for (BoolList f = formals; f != null; f = f.tail, argReg = argReg.tail)
        {

            Access a = ret.allocLocal(f.head); //Ϊÿ���������� Access
            ret.formals = new AccessList(a, ret.formals);
            if (argReg != null)
                ret.saveArgs.add(new Tree.MOVE(a.exp(new Tree.TEMP(fp)),
                        new Tree.TEMP(argReg.head)));

        }
        return ret;
    }

    public Tree.Stm procEntryExit1(Tree.Stm body)
    {
        //����ԭfp->������fp->���� ra->����Callee-save�Ĵ���->�������->(������ԭָ��)->�ָ�Callee-save�Ĵ���->�ָ����ص�ַ->�ָ�fp
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

    //���ú�������->����֡�ռ�->(������ԭָ��)-> ��$sp ������Ӧ��֡�ռ�->��ת�����ص�ַ
    public Assem.InstrList procEntryExit3(Assem.InstrList body)
    {

        body = new Assem.InstrList(new Assem.OPER("subu $sp, $sp, " + (-allocDown), new TempList(sp, null), new TempList(sp, null)), body);

        if (name.toString().equals("main"))
            body = new Assem.InstrList(new Assem.OPER("t_main: ", null, null), body);
        else body = new Assem.InstrList(new Assem.OPER(name.toString() + ":", null, null), body);

        //��ת�����ص�ַ
        Assem.InstrList epilogue = new Assem.InstrList(new Assem.OPER("jr $ra", null, new TempList(ra, null)), null);


        //��$sp ������Ӧ��֡�ռ�
        epilogue = new Assem.InstrList(new Assem.OPER("addu $sp, $sp, " + (-allocDown), new TempList(sp, null), new TempList(sp, null)), epilogue);


        body = Assem.InstrList.append(body, epilogue);
        return body;
    }

    public String string(Label label, String value)
    {
        //�����ַ��������ݶλ�����
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

    //����$fp\$sp\$ra\$rv�Ĵ���
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
        //���ñ�׼�⺯��
        return new Tree.CALL(new Tree.NAME(new Label(func)), args);
    }

    public String tempMap(Temp t)
    {
        //������ļĴ���ת��Ϊ�Ĵ�������
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
        //����CodeGen���ڵ�codegen����,���ڲ������ָ��
        return (new CodeGen(this)).codegen(s);
    }

    public java.util.HashSet registers()
    {
        java.util.HashSet ret = new java.util.HashSet();

        //��calleeSave�Ĵ��������ϣ��
        for (TempList tl = this.calleeSaves; tl != null; tl = tl.tail)
            ret.add(tl.head);

        //��callerSave�Ĵ��������ϣ��
        for (TempList tl = this.callerSaves; tl != null; tl = tl.tail)
            ret.add(tl.head);

        return ret;
    }
}
