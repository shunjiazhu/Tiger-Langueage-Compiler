package RegAlloc;

/**
 * Created by zhushunjia on 2016/12/4.
 */
public class RegAlloc implements Temp.TempMap{
    private Assem.InstrList instrs;
    private Color color;

    public String tempMap(Temp.Temp t)
    {
        return color.tempMap(t);
    }
    public RegAlloc(Frame.Frame f, Assem.InstrList instrs)
    {
        this.instrs = instrs;
        FlowGraph.FlowGraph flowGraph = new FlowGraph.AssemFlowGraph(instrs);//���ݻ��ָ��������ͼ
        InterferenceGraph interGraph=new Liveness(flowGraph); //���л��Է��������ɳ�ͻͼ
        color = new Color(interGraph, f, f.registers());//����ɫ������Ĵ���

    }
}
