package Frame;

/**
 * Created by zhushunjia on 2016/12/3.
 */
import Temp.*;

public abstract class Frame implements TempMap{
	public Label name = null;//帧名称
	public AccessList formals = null;//本地变量(局部量、参数)列表 
	public abstract Frame newFrame(Label name, Util.BoolList formals);//建立新帧(名称、参数逃逸信息)
	public abstract Access allocLocal(boolean escape); //分配新本地变量(是否逃逸) 
	public abstract Tree.Exp externCall(String func, Tree.ExpList args);//调用外部函数 
	public abstract Temp FP();  //帧指针 
	public abstract Temp SP();  //栈指针 
	public abstract Temp RA();//返回地址 
	public abstract Temp RV(); //返回值 
	public abstract java.util.HashSet registers(); //寄存器列表 
	public abstract Tree.Stm procEntryExit1(Tree.Stm body); //添加额外函数调用指令
	public abstract Assem.InstrList procEntryExit2(Assem.InstrList body); 
	public abstract Assem.InstrList procEntryExit3(Assem.InstrList body); 
	public abstract String string(Label label, String value);
	public abstract Assem.InstrList codegen(Tree.Stm s);
}
