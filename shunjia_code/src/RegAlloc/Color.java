package RegAlloc;

import Graph.*;
import Temp.*;
import java.util.*;

/**
 * Created by zhushunjia on 2016/12/4.
 */

public class Color implements TempMap{
    private Stack nodeStk = new Stack();
    private Hashtable<Temp, Temp> map = new Hashtable<Temp, Temp>();
    private TempMap init;

    public String tempMap(Temp t)
    {
        return init.tempMap(map.get(t));
    }
    public Color(InterferenceGraph interGraph, TempMap init, HashSet registers)
    {
        //用着色法分配寄存器,最终结果放入tempmap中
        HashSet regs = new HashSet(registers);
        this.init = init;

        int number = 0;
        //预处理
        for (NodeList nodes = interGraph.nodes(); nodes != null; nodes = nodes.tail)
        {
            //遍历每个临时变量结点
            ++number;
            Temp temp = interGraph.gtemp(nodes.head);//得到结点所对应的临时变量 temp


            if (init.tempMap(temp) != null)
            {
                --number;
                nodeStk.add(nodes.head);
                map.put(temp, temp);
                for (NodeList adj = nodes.head.succ(); adj != null; adj = adj.tail)
                    interGraph.rmEdge(nodes.head, adj.head);
            }
        }

        for (int i = 0; i < number; ++i)
        {
            Node node = null;
            int max = -1;

            for (NodeList n = interGraph.nodes(); n != null; n = n.tail)
                //再次遍历每个临时变量结点
                if (init.tempMap(interGraph.gtemp(n.head)) == null && !nodeStk.contains(n.head))
                {
                    //若没有被分配寄存器且不在堆栈中
                    int num = n.head.outDegree(); //得到节点的出度
                    if (max < num && num < regs.size())
                    {
                        max = num;
                        node = n.head;
                    }
                }
            if (node == null)
            {
                System.err.println("Color.color() : 溢出");
                break;
            }

            nodeStk.add(node);

            for (NodeList adj = node.pred(); adj != null; adj = adj.tail)
                if (!nodeStk.contains(adj.head))
                    interGraph.rmEdge(adj.head, node);
        }

        for (int i = 0; i < number; ++i)
        {
            Node node = (Node)nodeStk.pop();

            Set available = new HashSet(regs);

            for (NodeList adj = node.succ(); adj != null; adj = adj.tail)
            {
                available.remove(map.get(interGraph.gtemp(adj.head)));
            }
            Temp reg = (Temp) available.iterator().next();

            map.put(interGraph.gtemp(node), reg);

        }
    }
}

