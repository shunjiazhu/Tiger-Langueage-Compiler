package Frame;

/**
 * Created by zhushunjia on 2016/12/3.
 */
import Temp.*;

public abstract class Frame implements TempMap{
	public Label name = null;//֡����
	public AccessList formals = null;//���ر���(�ֲ���������)�б� 
	public abstract Frame newFrame(Label name, Util.BoolList formals);//������֡(���ơ�����������Ϣ)
	public abstract Access allocLocal(boolean escape); //�����±��ر���(�Ƿ�����) 
	public abstract Tree.Exp externCall(String func, Tree.ExpList args);//�����ⲿ���� 
	public abstract Temp FP();  //ָ֡�� 
	public abstract Temp SP();  //ջָ�� 
	public abstract Temp RA();//���ص�ַ 
	public abstract Temp RV(); //����ֵ 
	public abstract java.util.HashSet registers(); //�Ĵ����б� 
	public abstract Tree.Stm procEntryExit1(Tree.Stm body); //��Ӷ��⺯������ָ��
	public abstract Assem.InstrList procEntryExit2(Assem.InstrList body); 
	public abstract Assem.InstrList procEntryExit3(Assem.InstrList body); 
	public abstract String string(Label label, String value);
	public abstract Assem.InstrList codegen(Tree.Stm s);
}
