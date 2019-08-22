package Frag;

/**
 * Created by zhushunjia on 2016/12/3.
 */
public class ProcFrag extends Frag {
    public Frame.Frame frame;//程序段的帧
    public Tree.Stm body;//程序体

    public ProcFrag(Tree.Stm body, Frame.Frame f)
    {
        this.body = body;
        frame = f;
    }
}
