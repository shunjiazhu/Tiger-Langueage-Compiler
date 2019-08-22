package Graph;

/**
 * Created by zhushunjia on 2016/12/4.
 */
import java.util.*;

public class GraphNodeInfo {
    public Set<Temp.Temp> in = new HashSet<Temp.Temp>(); //��ָ��ǰ�Ļ��Ա���
    public Set<Temp.Temp> out = new HashSet<Temp.Temp>(); //��ָ���Ļ��Ա���
    public Set<Temp.Temp> use = new HashSet<Temp.Temp>(); //��ֵ��
    public Set<Temp.Temp> def = new HashSet<Temp.Temp>(); //��ֵ��

    public GraphNodeInfo(Node node)
    {
        for (Temp.TempList i = ((FlowGraph.FlowGraph)node.mygraph).def(node); i != null; i = i.tail)
        {
            def.add(i.head);
        }
        for (Temp.TempList i = ((FlowGraph.FlowGraph)node.mygraph).use(node); i != null; i = i.tail)
        {
            use.add(i.head);
        }
    }
}
