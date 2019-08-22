package Semant;

/**
 * Created by zhushunjia on 2016/12/3.
 */

import Absyn.FieldList;
import ErrorMsg.*;
import Translate.Level;
import Types.*;
import Util.BoolList;
import Symbol.Symbol;


public class Semant {
	private Env env;//���ű�
	private Translate.Translate trans;
	private Translate.Level level = null;
	private java.util.Stack<Temp.Label> loopStack = new java.util.Stack<Temp.Label>();//����ѭ��Ƕ�׵Ķ�ջ
	private Boolean TDecFlag=false,FDecFlag=false,TypeDecFlag=false,FuncDecFlag=false;

	public Semant(Translate.Translate t, ErrorMsg err)
	{
		trans = t;
		level = new Level(t.frame);
		level = new Level(level, Symbol.symbol("main"), null);

		env = new Env(err, level);
	}
	public Frag.Frag transProg(Absyn.Exp e)
	{
		//���������鲢����ΪIR��
		ExpTy et = transExp(e);

		trans.procEntryExit (level, et.exp, false);
		level = level.parent;

		return trans.getResult();
	}
	public ExpTy transVar(Absyn.Var e)
	{
		if (e instanceof Absyn.SimpleVar) return transVar((Absyn.SimpleVar)e);
		if (e instanceof Absyn.SubscriptVar) return transVar((Absyn.SubscriptVar)e);
		if (e instanceof Absyn.FieldVar) return transVar((Absyn.FieldVar)e);
		return null;
	}
	public ExpTy transExp(Absyn.Exp e)
	{
		if (e instanceof Absyn.IntExp) return transExp((Absyn.IntExp)e);
		if (e instanceof Absyn.StringExp) return transExp((Absyn.StringExp)e);
		if (e instanceof Absyn.NilExp) return transExp((Absyn.NilExp)e);
		if (e instanceof Absyn.VarExp) return transExp((Absyn.VarExp)e);
		if (e instanceof Absyn.OpExp) return transExp((Absyn.OpExp)e);
		if (e instanceof Absyn.AssignExp) return transExp((Absyn.AssignExp)e);
		if (e instanceof Absyn.CallExp) return transExp((Absyn.CallExp)e);
		if (e instanceof Absyn.RecordExp) return transExp((Absyn.RecordExp)e);
		if (e instanceof Absyn.ArrayExp) return transExp((Absyn.ArrayExp)e);
		if (e instanceof Absyn.IfExp) return transExp((Absyn.IfExp)e);
		if (e instanceof Absyn.WhileExp) return transExp((Absyn.WhileExp)e);
		if (e instanceof Absyn.ForExp) return transExp((Absyn.ForExp)e);
		if (e instanceof Absyn.BreakExp) return transExp((Absyn.BreakExp)e);
		if (e instanceof Absyn.LetExp) return transExp((Absyn.LetExp)e);
		if (e instanceof Absyn.SeqExp) return transExp((Absyn.SeqExp)e);
		return null;
	}
	public void transDec0(Absyn.Dec e)
	{
		if (e instanceof Absyn.VarDec) transDec0((Absyn.VarDec)e);
		if (e instanceof Absyn.TypeDec) transDec0((Absyn.TypeDec)e);
		if (e instanceof Absyn.FunctionDec) transDec0((Absyn.FunctionDec)e);
	}
	public Translate.Exp transDec(Absyn.Dec e)
	{
		if (e instanceof Absyn.VarDec)
		{
			if(TypeDecFlag==true) {
				TDecFlag=true;
			}
			if(FuncDecFlag==true) {
				FDecFlag=true;
			}
			return transDec((Absyn.VarDec)e);
		}
		if (e instanceof Absyn.TypeDec)
		{
			if(TypeDecFlag==false) {
				TypeDecFlag=true;
				return transDec((Absyn.TypeDec)e);
			}
			if(TDecFlag==true){
				env.errorMsg.error(e.pos, "���Ͷ��屻��;���");
				return null;
			}
		}
		if (e instanceof Absyn.FunctionDec)
		{
			if(FuncDecFlag==false) {
				FuncDecFlag=true;
				return transDec((Absyn.FunctionDec)e);
			}
			if(FDecFlag==true){
				env.errorMsg.error(e.pos, "�������屻��;���");
				return null;
			}
		}
		return null;
	}
	public Type transTy(Absyn.Ty e)
	{
		if (e instanceof Absyn.ArrayTy) return transTy((Absyn.ArrayTy)e);
		if (e instanceof Absyn.RecordTy) return transTy((Absyn.RecordTy)e);
		if (e instanceof Absyn.NameTy) return transTy((Absyn.NameTy)e);
		return null;
	}
	private ExpTy transExp(Absyn.IntExp e)
	{
		return new ExpTy(trans.transIntExp(e.value), new INT());
	}
	private ExpTy transExp(Absyn.StringExp e)
	{
		return new ExpTy(trans.transStringExp(e.value), new STRING());
	}
	private ExpTy transExp(Absyn.NilExp e)
	{
		return new ExpTy(trans.transNilExp(), new NIL());
	}
	private ExpTy transExp(Absyn.VarExp e)
	{
		return transVar(e.var);
	}
	private ExpTy transExp(Absyn.OpExp e)
	{
		ExpTy el = transExp(e.left);//����������
		ExpTy er = transExp(e.right);//����������
		if (el == null || er == null)
		{
			return null;
		}

		if (e.oper == Absyn.OpExp.EQ || e.oper == Absyn.OpExp.NE)
		{
			if (el.ty.actual() instanceof NIL && er.ty.actual() instanceof NIL)
			{
				env.errorMsg.error(e.pos, " Nil���Ͳ�����Nil���ͱȽ�");
				return null;
			}

			if (el.ty.actual() instanceof VOID || er.ty.actual() instanceof VOID)
			{
				env.errorMsg.error(e.pos, "������Void���Ͳ���Ƚ�");
				return null;
			}

			if (el.ty.actual() instanceof NIL && er.ty.actual() instanceof RECORD)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			if (el.ty.actual() instanceof RECORD && er.ty.actual() instanceof NIL)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());


			if (el.ty.coerceTo(er.ty))
			{
				if (el.ty.actual() instanceof STRING && e.oper == Absyn.OpExp.EQ)
				{
					return new ExpTy(trans.transStringRelExp(level, e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
				}
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			}
			env.errorMsg.error(e.pos, "������������Ͳ�һ��");
			return null;
		}

		if (e.oper > Absyn.OpExp.NE)
		{
			if (el.ty.actual() instanceof INT && er.ty.actual() instanceof INT)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			if (el.ty.actual() instanceof STRING && er.ty.actual() instanceof STRING)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new STRING());
			env.errorMsg.error(e.pos, "�������Ͳ�һ�»�����Ͳ�֧�ֱȽ�����");
			return null;
		}

		if (e.oper < Absyn.OpExp.EQ)
		{
			if (el.ty.actual() instanceof INT && er.ty.actual() instanceof INT)
				return new ExpTy(trans.transOpExp(e.oper, transExp(e.left).exp, transExp(e.right).exp), new INT());
			env.errorMsg.error(e.pos, "������������Ͳ�һ��");
			return null;
		}

		return new ExpTy(trans.transOpExp(e.oper, el.exp, er.exp), new INT());
	}
	private ExpTy transExp(Absyn.AssignExp e)
	{

		int pos=e.pos;
		Absyn.Var var=e.var;//��ֵ
		Absyn.Exp exp=e.exp;//��ֵ
		ExpTy er = transExp(exp);

		if (er.ty.actual() instanceof VOID)
		{
			env.errorMsg.error(pos, "�����ÿ�ֵΪ������ֵ");
			return null;
		}

		if (var instanceof Absyn.SimpleVar)
		{
			Absyn.SimpleVar ev = (Absyn.SimpleVar)var;
			Entry x= (Entry)(env.vEnv.get(ev.name));

			if (x instanceof VarEntry && ((VarEntry)x).isFor)
			{
				env.errorMsg.error(pos, "ѭ���������ܱ���ֵ");
				return null;
			}
		}
		ExpTy vr = transVar(var);

		if (!er.ty.coerceTo(vr.ty))
		{
			env.errorMsg.error(pos, er.ty.actual().getClass().getSimpleName()+"���͵�ֵ���ܸ�ֵ��"+vr.ty.actual().getClass().getSimpleName()+"���͵ı���");
			return null;
		}
		return new ExpTy(trans.transAssignExp(vr.exp, er.exp), new VOID());

	}
	private ExpTy transExp(Absyn.CallExp e)
	{
		FuncEntry fe;
		Object x = env.vEnv.get(e.func);

		if (x == null || !(x instanceof FuncEntry))
		{
			env.errorMsg.error(e.pos, "����"+e.func.toString()+"δ����");
			return null;
		}
		Absyn.ExpList ex =e.args;//������
		fe = (FuncEntry)x;
		RECORD rc = fe.paramlist;//������

		//����β���ʵ���Ƿ�һ��
		while (ex != null)
		{
			if (rc == null)
			{
				env.errorMsg.error(e.pos, "������һ��,���ú���ʱ�����˶������");
				return null;
			}

			if (!transExp(ex.head).ty.coerceTo(rc.fieldType))
			{
				env.errorMsg.error(e.pos, "���������Ͳ�һ��");
				return null;
			}
			ex = ex.tail;
			rc = rc.tail;
		}
		if (ex == null && !(RECORD.isNull(rc)))
		{
			env.errorMsg.error(e.pos, "������һ��,ȱ�ٲ�������");
			return null;
		}
		//�������ÿ��ʵ������Ӧ�ı��ʽ
		java.util.ArrayList<Translate.Exp> arrl = new java.util.ArrayList<Translate.Exp>();
		for (Absyn.ExpList i = e.args; i != null; i = i.tail)
			arrl.add(transExp(i.head).exp);

		//��Ϊ��׼�⺯��,�����transStdCallExp�ر���
		if (x instanceof StdFuncEntry)
		{
			StdFuncEntry sf = (StdFuncEntry)x;
			return new ExpTy(trans.transStdCallExp(level, sf.label, arrl), sf.returnTy);
		}
		//������Ϊ��ͨ��������,���ߵ��������ڱ�׼�⺯�����ش���̬��,�ʲ��ش��뺯���Ĳ�
		return new ExpTy(trans.transCallExp(level, fe.level, fe.label, arrl), fe.returnTy);
	}
	private ExpTy transExp(Absyn.RecordExp e)
	{
		//�����¼�Ķ�����ʽ
		Type t =(Type)env.tEnv.get(e.typ);
		//�������ͷ��ű�,�Ҳ����򱨴�
		if (t == null || !(t.actual() instanceof RECORD))
		{
			env.errorMsg.error(e.pos, "�˼�¼���Ͳ�����");
			return null;
		}

		Absyn.FieldExpList fe = e.fields;
		RECORD rc = (RECORD)(t.actual());
		if (fe == null && rc != null)
		{
			env.errorMsg.error(e.pos, "��¼�����еĳ�Ա������һ��");
			return null;
		}

		while (fe != null)
		{
			ExpTy ie = transExp(fe.init);
			if (rc == null || ie == null ||!ie.ty.coerceTo(rc.fieldType) || fe.name != rc.fieldName)
			{
				env.errorMsg.error(e.pos, "��¼�����еĳ�Ա������һ��");
				return null;
			}
			fe = fe.tail;
			rc = rc.tail;
		}

		java.util.ArrayList<Translate.Exp> arrl = new java.util.ArrayList<Translate.Exp>();
		for (Absyn.FieldExpList i = e.fields; i != null; i = i.tail)
			arrl.add(transExp(i.init).exp);
		return new ExpTy(trans.transRecordExp(level, arrl), t.actual());
	}
	private ExpTy transExp(Absyn.ArrayExp e)
	{

		Type ty = (Type)env.tEnv.get(e.typ);

		if (ty == null || !(ty.actual() instanceof ARRAY))
		{
			env.errorMsg.error(e.pos, "�����鲻����");
			return null;
		}

		ExpTy size = transExp(e.size);

		//�±����Ϊ����
		if (!(size.ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "����ĳ��Ȳ�����������");
			return null;
		}
		ARRAY ar = (ARRAY)ty.actual();
		ExpTy ini = transExp(e.init);

		if (!ini.ty.coerceTo(ar.element.actual()))
		{
			env.errorMsg.error(e.pos, "��ʼֵ������������Ԫ�ص����Ͳ�һ��");
			return null;
		}
		return new ExpTy(trans.transArrayExp(level, ini.exp, size.exp), new ARRAY(ar.element));
	}
	private ExpTy transExp(Absyn.IfExp e)
	{
		//����if���
		ExpTy testET = transExp(e.test);
		ExpTy thenET = transExp(e.thenclause);
		ExpTy elseET = transExp(e.elseclause);

		if (e.test == null || testET == null || !(testET.ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "if����е��������ʽ������������");
			return null;
		}

		if (e.elseclause == null && (!(thenET.ty.actual() instanceof VOID)))
		{
			env.errorMsg.error(e.pos, "��Ӧ�з���ֵ");
			return null;
		}

		if (e.elseclause != null && !thenET.ty.coerceTo(elseET.ty))
		{
			env.errorMsg.error(e.pos, "������֧�����Ͳ�һ��");
			return null;
		}
		if (elseET == null)
			return new ExpTy(trans.transIfExp(testET.exp, thenET.exp, trans.transNoExp()), thenET.ty);
		return new ExpTy(trans.transIfExp(testET.exp, thenET.exp, elseET.exp), thenET.ty);
	}
	private ExpTy transExp(Absyn.WhileExp e)
	{
		//����whileѭ�����
		ExpTy transt = transExp(e.test);//����ѭ������
		if (transt == null)	return null;

		//ѭ����������Ϊ��������
		if (!(transt.ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "ѭ������������������");
			return null;
		}

		//ѭ������
		Temp.Label out = new Temp.Label();

		loopStack.push(out);
		ExpTy bdy = transExp(e.body);
		loopStack.pop();//����ǰѭ������ջ

		if (bdy == null)	return null;

		if (!(bdy.ty.actual() instanceof VOID))
		{
			env.errorMsg.error(e.pos, "whileѭ�����ܷ���ֵ");
			return null;
		}

		return new ExpTy(trans.transWhileExp(transt.exp, bdy.exp, out), new VOID());
	}
	private ExpTy transExp(Absyn.ForExp e)
	{
		//����forѭ��
		boolean flag = false;

		//ѭ������������
		if (!(transExp(e.hi).ty.actual() instanceof INT) || !(transExp(e.var.init).ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "ѭ��������������������");
		}


		env.vEnv.beginScope();
		Temp.Label label = new Temp.Label();//����ѭ�������
		loopStack.push(label);


		Translate.Access acc = level.allocLocal(true);

		//Ϊѭ����������ռ�
		env.vEnv.put(e.var.name, new VarEntry(new INT(), acc, true));


		ExpTy body = transExp(e.body);
		ExpTy high = transExp(e.hi);
		ExpTy low = transExp(e.var.init);

		if (body == null)	flag = true;
		loopStack.pop();

		env.vEnv.endScope();


		if (flag)	return null;
		return new ExpTy(trans.transForExp(level, acc, low.exp, high.exp, body.exp, label), new VOID());
	}
	private ExpTy transExp(Absyn.BreakExp e)
	{
		//����break���

		if (loopStack.isEmpty())
		{
			env.errorMsg.error(e.pos, "break��䲻��ѭ����");
			return null;
		}
		return new ExpTy(trans.transBreakExp(loopStack.peek()), new VOID());//���뵱ǰ��ѭ��
	}
	private ExpTy transExp(Absyn.LetExp e)
	{
		//����let-in-end���
		Translate.Exp ex = null;

		env.vEnv.beginScope();
		env.tEnv.beginScope();
		ExpTy td = transDecList(e.decs);


		if (td != null)
			ex = td.exp;
		ExpTy tb = transExp(e.body);


		if (tb == null)
			ex = trans.stmcat(ex, null);
		else if (tb.ty.actual() instanceof VOID)
			ex = trans.stmcat(ex, tb.exp);
		else
			ex = trans.exprcat(ex, tb.exp);


		env.tEnv.endScope();
		env.vEnv.endScope();//����������

		return new ExpTy(ex, tb.ty);
	}
	private ExpTy transDecList(Absyn.DecList e)
	{
		//���������б�
		Translate.Exp ex = null;

		//�����ķ���Ҫ��������
		for (Absyn.DecList i = e; i!= null; i = i.tail)
			transDec0(i.head);


		for (Absyn.DecList i = e; i!= null; i = i.tail)
		{
			ex = trans.stmcat(ex, transDec(i.head));
		}

		return new ExpTy(ex, new VOID());
	}
	private ExpTy transExp(Absyn.SeqExp e)
	{
		//������ʽ����
		Translate.Exp ex = null;
		for (Absyn.ExpList t = e.list; t != null; t = t.tail)
		{
			ExpTy x = transExp(t.head);

			if (t.tail == null)
			{
				if(x!=null)
				{
					if (x.ty.actual() instanceof VOID)
					{
						ex = trans.stmcat(ex, x.exp);
					}
					else
					{
						ex = trans.exprcat(ex, x.exp);
					}
				}
				if(x!=null) return new ExpTy(ex, x.ty);
				else return new ExpTy(ex, new VOID());
			}
			ex = trans.stmcat(ex, x.exp);
		}
		return null;
	}
	private ExpTy transVar(Absyn.SimpleVar e)
	{
		//����򵥱���
		Entry ex = (Entry)env.vEnv.get(e.name);
		if (ex == null || !(ex instanceof VarEntry))
		{
			env.errorMsg.error(e.pos, "����δ����");
			return null;
		}
		VarEntry evx = (VarEntry)ex;
		return new ExpTy(trans.transSimpleVar(evx.acc, level), evx.Ty);
	}
	private ExpTy transVar(Absyn.SubscriptVar e)
	{
		//�����������(��ֵ)
		//�����±����Ϊ����,��Ȼ�򱨴�
		if (!(transExp(e.index).ty.actual() instanceof INT))
		{
			env.errorMsg.error(e.pos, "�±����Ϊ����");
			return null;
		}
		ExpTy ev = transVar(e.var);

		ExpTy ei = transExp(e.index);

		//�����Ϊ���򱨴�
		if (ev == null || !(ev.ty.actual() instanceof ARRAY))
		{
			env.errorMsg.error(e.pos, "���鲻����");
			return null;
		}
		ARRAY ae = (ARRAY)(ev.ty.actual());
		return new ExpTy(trans.transSubscriptVar(ev.exp, ei.exp), ae.element);
	}
	private ExpTy transVar(Absyn.FieldVar e)
	{
		//���������(��ֵ)
		ExpTy et = transVar(e.var);

		if (!(et.ty.actual() instanceof RECORD))
		{
			env.errorMsg.error(e.pos, "�˱�������һ����¼����");
			return null;
		}

		RECORD rc = (RECORD)(et.ty.actual());
		int count = 1;
		while (rc != null)
		{
			if (rc.fieldName == e.field)
			{
				return new ExpTy(trans.transFieldVar(et.exp, count), rc.fieldType);
			}
			count++;
			rc = rc.tail;
		}
		env.errorMsg.error(e.pos, "�����������");
		return null;
	}
	private Type transTy(Absyn.NameTy e)
	{
		//����δ֪����  NameTy
		if (e == null)
			return new VOID();

		Type t =(Type)env.tEnv.get(e.name);

		if (t == null)
		{
			env.errorMsg.error(e.pos, "����δ����");
			return null;
		}
		return t;
	}
	private ARRAY transTy(Absyn.ArrayTy e)
	{
		Type t = (Type)env.tEnv.get(e.typ);

		if (t == null)
		{
			env.errorMsg.error(e.pos, "���Ͳ�����");
			return null;
		}
		return new ARRAY(t);
	}
	private RECORD transTy(Absyn.RecordTy e)
	{
		RECORD rc = new RECORD(),  r = new RECORD();
		if (e == null || e.fields == null)
		{
			rc.gen(null, null, null);
			return rc;
		}

		Absyn.FieldList fl = e.fields;
		boolean first = true;
		while (fl != null)
		{
			if (env.tEnv.get(fl.typ) == null)
			{
				env.errorMsg.error(e.pos, "�����Ͳ�����");
				return null;
			}

			rc.gen(fl.name, (Type)env.tEnv.get(fl.typ), new RECORD());
			if (first)
			{
				r = rc;
				first = false;
			}
			if (fl.tail == null)
				rc.tail = null;
			rc = rc.tail;
			fl = fl.tail;
		}

		return r;
	}
	private void transDec0(Absyn.VarDec e)
	{

	}
	private Translate.Exp transDec(Absyn.VarDec e)
	{
		//�����������
		ExpTy et = transExp(e.init);

		if (e.typ == null && e.init instanceof Absyn.NilExp)
		{
			env.errorMsg.error(e.pos, "��ʼֵ���ܸ�ֵΪnil");
			return null;
		}


		if (et == null && e.init==null)
		{
			env.errorMsg.error(e.pos,"����������븳��ʼֵ");
			return null;
		}

		if(et == null)
		{

			et=new ExpTy(trans.transNilExp(), new NIL());
			e.init=new Absyn.NilExp(e.pos);
		}


		if (e.typ != null && !(transExp(e.init).ty.coerceTo((Type)env.tEnv.get(e.typ.name))))
		{
			env.errorMsg.error(e.pos,"��ʼֵ��������Ͳ�ƥ��");
			return null;
		}
		if (e.init == null )
		{
			env.errorMsg.error(e.pos, "����������븳��ʼֵ");
			return null;
		}
		Translate.Access acc = level.allocLocal(true);
		//Ϊ��������ռ�
		if (e.typ != null)
		{
			env.vEnv.put(e.name, new VarEntry((Type)env.tEnv.get(e.typ.name), acc));
		}
		else
		{
			env.vEnv.put(e.name, new VarEntry(transExp(e.init).ty, acc));
		}
		return trans.transAssignExp(trans.transSimpleVar(acc, level), et.exp);
	}
	private void transDec0(Absyn.TypeDec e)
	{
		java.util.HashSet<Symbol> hs = new java.util.HashSet<Symbol>();

		for (Absyn.TypeDec i = e; i != null; i = i.next)
		{
			if (hs.contains(i.name))
			{
				env.errorMsg.error(e.pos, "��ͬһ�������ظ���������");
				return ;
			}
			hs.add(i.name);
			env.tEnv.put(i.name, new NAME(i.name));
		}
	}
	private Translate.Exp transDec(Absyn.TypeDec e)
	{
		//���������������
		for (Absyn.TypeDec i = e; i != null; i = i.next)
		{
			env.tEnv.put(i.name, new NAME(i.name));
			((NAME)env.tEnv.get(i.name)).bind(transTy(i.ty).actual());
			NAME field = (NAME)env.tEnv.get(i.name);
			if(field.isLoop() == true)
			{
				env.errorMsg.error(i.pos, "����ѭ������");
				return null;
			}

		}
		//�����ͷ������ͷ��ű�
		for (Absyn.TypeDec i = e; i != null; i = i.next)
			env.tEnv.put(i.name, transTy(i.ty));

		return trans.transNoExp();
	}
	private void transDec0(Absyn.FunctionDec e)
	{
		for (Absyn.FunctionDec i = e; i != null; i = i.next)
		{
			Absyn.RecordTy rt = new Absyn.RecordTy(i.pos, i.params);
			RECORD  r = transTy(rt);
			if ( r == null)	return;

			BoolList bl = null;
			for (FieldList f = i.params; f != null; f = f.tail)
			{
				bl = new BoolList(true, bl);
			}
			level = new Level(level, i.name, bl);
			env.vEnv.put(i.name, new FuncEntry(level, new Temp.Label(i.name), r, transTy(i.result)));
			level = level.parent;
		}
	}
	private Translate.Exp transDec(Absyn.FunctionDec e)
	{
		//���뺯������
		java.util.HashSet<Symbol> hs = new java.util.HashSet<Symbol>();
		ExpTy et = null;
		//����ظ�����,��Ϊ��ͨ�������׼�⺯��
		for (Absyn.FunctionDec i = e; i != null; i = i.next)
		{
			if (hs.contains(i.name))
			{
				env.errorMsg.error(e.pos, "��ͬһ�������ظ����庯��");
				return null;
			}
			if (env.stdFuncSet.contains(i.name))
			{
				env.errorMsg.error(e.pos, "���׼�⺯������");
				return null;
			}

			Absyn.RecordTy rt = new Absyn.RecordTy(i.pos, i.params);
			RECORD  r = transTy(rt);
			if ( r == null)	return null;
			//��������б�,���¼����RecordTy�ļ����ȫ��ͬ,�õ� RECORD ���͵��β��б�
			BoolList bl = null;
			for (FieldList f = i.params; f != null; f = f.tail)
			{
				bl = new BoolList(true, bl);
			}
			level = new Level(level, i.name, bl);
			env.vEnv.beginScope();
			Translate.AccessList al = level.formals.next;
			for (RECORD j = r; j!= null; j = j.tail)
			{
				if (j.fieldName != null)
				{
					env.vEnv.put(j.fieldName, new VarEntry(j.fieldType, al.head));
					al = al.next;
				}
			}
			et = transExp(i.body);
			//���뺯����
			if (et == null)
			{	env.vEnv.endScope();	return null;	}
			if(!(et.ty.coerceTo((transTy(i.result).actual()))))
			{
				env.errorMsg.error(i.pos,"���������з���ֵ���Ͳ�ƥ��");
				return null;
			}

			if (!(et.ty.actual() instanceof VOID))
				trans.procEntryExit(level, et.exp, true);
			else
				trans.procEntryExit(level, et.exp, false);

			env.vEnv.endScope();
			level = level.parent;

			hs.add(i.name);
		}
		return trans.transNoExp();

	}

}

