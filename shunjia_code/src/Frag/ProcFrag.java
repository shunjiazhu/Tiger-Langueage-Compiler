package Frag;

/**
 * Created by zhushunjia on 2016/12/3.
 */
public class ProcFrag extends Frag {
    public Frame.Frame frame;//����ε�֡
    public Tree.Stm body;//������

    public ProcFrag(Tree.Stm body, Frame.Frame f)
    {
        this.body = body;
        frame = f;
    }
}
