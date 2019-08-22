package FlowGraph;

/**
 * Created by zhushunjia on 2016/12/3.
 */

import Graph.*;

public class AssemFlowGraph extends FlowGraph{
    private java.util.Hashtable<Node, Assem.Instr> node2instr = new java.util.Hashtable<Node, Assem.Instr>();

    private java.util.Hashtable<Temp.Label, Node> label2node = new java.util.Hashtable<Temp.Label, Node>();//��label����ͼ�ϵĽڵ�


    public AssemFlowGraph(Assem.InstrList instrs)
    {

        for (Assem.InstrList i = instrs; i != null; i = i.tail)
        {

            Node node = newNode();
            node2instr.put(node, i.head);

            if (i.head instanceof Assem.LABEL)
                label2node.put(((Assem.LABEL) i.head).label, node);

        }

        for (NodeList node = nodes(); node != null; node = node.tail)
        {

            Assem.Targets next = instr(node.head).jumps();
            //�õ���ת��Ŀ��label
            if (next == null)
            {

                if (node.tail != null)
                    addEdge(node.head, node.tail.head);
            }
            else
            {
                for (Temp.LabelList l = next.labels; l != null; l = l.tail)
                    addEdge(node.head, (Node) label2node.get(l.head));
            }
        }
    }

    public Assem.Instr instr(Node n) //�ڹ�ϣ���в�ѯͼ�Ľڵ㲢��������Ӧ��ָ��
    {	return (Assem.Instr)node2instr.get(n);	}

    public Temp.TempList def(Node node)
    {	return instr(node).def();	}

    public Temp.TempList use(Node node)
    {	return instr(node).use();	}

    public boolean isMove(Node node)
    {
        //�ж�һ��ָ��(�ڵ�)�Ƿ���move���,��Ϊ�ڻ��Է�����move�����Ҫ�ر���
        Assem.Instr instr = instr(node);
        return instr.assem.startsWith("move");
    }
}
