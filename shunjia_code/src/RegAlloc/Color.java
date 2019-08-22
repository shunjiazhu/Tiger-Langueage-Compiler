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
        //����ɫ������Ĵ���,���ս������tempmap��
        HashSet regs = new HashSet(registers);
        this.init = init;

        int number = 0;
        //Ԥ����
        for (NodeList nodes = interGraph.nodes(); nodes != null; nodes = nodes.tail)
        {
            //����ÿ����ʱ�������
            ++number;
            Temp temp = interGraph.gtemp(nodes.head);//�õ��������Ӧ����ʱ���� temp


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
                //�ٴα���ÿ����ʱ�������
                if (init.tempMap(interGraph.gtemp(n.head)) == null && !nodeStk.contains(n.head))
                {
                    //��û�б�����Ĵ����Ҳ��ڶ�ջ��
                    int num = n.head.outDegree(); //�õ��ڵ�ĳ���
                    if (max < num && num < regs.size())
                    {
                        max = num;
                        node = n.head;
                    }
                }
            if (node == null)
            {
                System.err.println("Color.color() : ���");
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

