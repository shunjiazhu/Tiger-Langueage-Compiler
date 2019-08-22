package FlowGraph;

/**
 * Created by zhushunjia on 2016/12/3.
 */

import Graph.*;

public class AssemFlowGraph extends FlowGraph{
    private java.util.Hashtable<Node, Assem.Instr> node2instr = new java.util.Hashtable<Node, Assem.Instr>();

    private java.util.Hashtable<Temp.Label, Node> label2node = new java.util.Hashtable<Temp.Label, Node>();//用label查找图上的节点


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
            //得到跳转的目标label
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

    public Assem.Instr instr(Node n) //在哈希表中查询图的节点并返回所对应的指令
    {	return (Assem.Instr)node2instr.get(n);	}

    public Temp.TempList def(Node node)
    {	return instr(node).def();	}

    public Temp.TempList use(Node node)
    {	return instr(node).use();	}

    public boolean isMove(Node node)
    {
        //判断一条指令(节点)是否是move语句,因为在活性分析中move语句需要特别处理
        Assem.Instr instr = instr(node);
        return instr.assem.startsWith("move");
    }
}
