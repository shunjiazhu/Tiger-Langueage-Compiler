/**
 * Created by zhushunjia on 2016/12/4.
 */
import java_cup.runtime.*;
import Parse.*;
import Absyn.*;
import java.util.Scanner;
import java.io.*;
import Semant.*;
import RegAlloc.*;

public class Final_Main {
    public static boolean ifLex = true;
    public static boolean ifCup = false;
    public static boolean ifIRTree = false;
    public static boolean ifMips = false;
    //public static boolean ifSemant = false;
    static java.io.PrintStream irOut;

    public static void main(String[] args)throws java.io.IOException
    {
        System.out.println("请输入测试文件(testcases/*.tig):");

        Scanner s = new Scanner(System.in);
        String name = s.nextLine();

        String filename = name;
        ErrorMsg.ErrorMsg errorMsg = new ErrorMsg.ErrorMsg(filename);

        java.io.FileInputStream inp = new java.io.FileInputStream(filename);

        //这里的这个词法分析器纯粹用来在控制台打印结果
        Yylex lexer = new Yylex(inp, errorMsg);

        java_cup.runtime.Symbol tok;

        System.out.println("词法分析输出。");
        try
        {
            do
            {
                tok=lexer.next_token();

               if(ifLex == true) System.out.println(symnames[tok.sym] + " " + tok.left);
            }
            while (tok.sym != sym.EOF);
            inp.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        //System.out.println("\n# 语法分析输出并进行语义检查:");
        inp = new java.io.FileInputStream(filename);

        lexer = new Yylex(inp, errorMsg);

        parser p = new parser(lexer, errorMsg);
        //语法分析
        Print pr = new Print(System.out);
        //Object result = null;

        try
        { p.parse();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("语法分析输出。");
        if(ifCup == true) pr.prExp(p.parseResult, 0);//打印抽象语法树
        System.out.println();

        Frame.Frame frame = new Mips.MipsFrame();
        Translate.Translate translator = new Translate.Translate(frame);
        Semant smt = new Semant(translator, errorMsg);

        Frag.Frag frags = smt.transProg(p.parseResult);

        if(ErrorMsg.ErrorMsg.anyErrors==false) System.out.println("无语义和语法错误，开始翻译。");
        else return;


        irOut = new java.io.PrintStream(new java.io.FileOutputStream(filename.substring(0, filename.length() - 3) + "ir"));

        System.out.println("IR 树输出到文件。");
        java.io.PrintStream out = new java.io.PrintStream(new java.io.FileOutputStream(filename.substring(0, filename.length() - 3) + "s"));

        out.println(".globl main");

        for(Frag.Frag f = frags; f!=null; f=f.next)//遍历所有的段
            if (f instanceof Frag.ProcFrag)
                emitProc(out, (Frag.ProcFrag)f);//若为程序段
            else if (f instanceof Frag.DataFrag)
                out.print("\n.data\n" +((Frag.DataFrag)f).data);//若为数据段

        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream("runtime.s")));
        String data = null;
        while((data = br.readLine())!=null)
        {
            out.println(data);
        }

        irOut.close();
        out.close();

        if(ifMips == true){
            FileReader MipsIn = new FileReader(filename.substring(0,filename.length() - 3)+ "s");

            int len = 0;

            try {
                while((len = MipsIn.read())!=-1)
                {
                    System.out.print((char)len);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("...Compiling...");
        System.out.println("MIPS汇编代码生成成功。");
        System.out.println("编译成功。");

    }


    static void emitProc(java.io.PrintStream out, Frag.ProcFrag f)
    {
        Tree.Print print = new Tree.Print(irOut);
        Tree.Print print2 = new Tree.Print(System.out);

        //输出IR树
        irOut.println("function " + f.frame.name);
        if(ifIRTree == true) System.out.println("function " + f.frame.name);

        Tree.StmList stms = Canon.Canon.linearize(f.body);
        Canon.BasicBlocks b = new Canon.BasicBlocks(stms);
        Tree.StmList traced = (new Canon.TraceSchedule(b)).stms;




        prStmList(print,traced);
        if(ifIRTree == true) prStmList(print2,traced);

        Assem.InstrList instrs = codegen(f.frame, traced);
        instrs = f.frame.procEntryExit2(instrs);

        RegAlloc regAlloc = new RegAlloc(f.frame, instrs);

        instrs = f.frame.procEntryExit3(instrs);
        Temp.TempMap tempmap = new Temp.CombineMap(f.frame, regAlloc);


        out.println("\n.text");
        for (Assem.InstrList p = instrs; p != null; p = p.tail)
            out.println(p.head.format(tempmap));
    }


    //打印表达式列表的函数
    static void prStmList(Tree.Print print, Tree.StmList stms)
    {
        for(Tree.StmList l = stms; l!=null; l=l.tail)
            print.prStm(l.head);
    }


    static Assem.InstrList codegen(Frame.Frame f, Tree.StmList stms)
    {
        Assem.InstrList first = null, last = null;
        for(Tree.StmList s = stms; s != null; s = s.tail)
        {
            Assem.InstrList i = f.codegen(s.head);
            if (last == null)
            {	first = last = i;	}
            else
            {
                while (last.tail != null)
                    last = last.tail;
                last = last.tail = i;
            }
        }
        return first;
    }

    static String symnames[] = new String[100];
    static {

        symnames[sym.FUNCTION] = "FUNCTION";
        symnames[sym.EOF] = "EOF";
        symnames[sym.INT] = "INT";
        symnames[sym.GT] = "GT";
        symnames[sym.DIVIDE] = "DIVIDE";
        symnames[sym.COLON] = "COLON";
        symnames[sym.ELSE] = "ELSE";
        symnames[sym.OR] = "OR";
        symnames[sym.NIL] = "NIL";
        symnames[sym.DO] = "DO";
        symnames[sym.GE] = "GE";
        symnames[sym.error] = "error";
        symnames[sym.LT] = "LT";
        symnames[sym.OF] = "OF";
        symnames[sym.MINUS] = "MINUS";
        symnames[sym.ARRAY] = "ARRAY";
        symnames[sym.TYPE] = "TYPE";
        symnames[sym.FOR] = "FOR";
        symnames[sym.TO] = "TO";
        symnames[sym.TIMES] = "TIMES";
        symnames[sym.COMMA] = "COMMA";
        symnames[sym.LE] = "LE";
        symnames[sym.IN] = "IN";
        symnames[sym.END] = "END";
        symnames[sym.ASSIGN] = "ASSIGN";
        symnames[sym.STRING] = "STRING";
        symnames[sym.DOT] = "DOT";
        symnames[sym.LPAREN] = "LPAREN";
        symnames[sym.RPAREN] = "RPAREN";
        symnames[sym.IF] = "IF";
        symnames[sym.SEMICOLON] = "SEMICOLON";
        symnames[sym.ID] = "ID";
        symnames[sym.WHILE] = "WHILE";
        symnames[sym.LBRACK] = "LBRACK";
        symnames[sym.RBRACK] = "RBRACK";
        symnames[sym.NEQ] = "NEQ";
        symnames[sym.VAR] = "VAR";
        symnames[sym.BREAK] = "BREAK";
        symnames[sym.AND] = "AND";
        symnames[sym.PLUS] = "PLUS";
        symnames[sym.LBRACE] = "LBRACE";
        symnames[sym.RBRACE] = "RBRACE";
        symnames[sym.LET] = "LET";
        symnames[sym.THEN] = "THEN";
        symnames[sym.EQ] = "EQ";
        symnames[sym.NUM] = "NUM";
        symnames[sym.STR] = "STR";
    }

}
