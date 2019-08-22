package Frame;

/**
 * Created by zhushunjia on 2016/12/3.
 */
public abstract class Access {
	public abstract Tree.Exp exp(Tree.Exp framePtr);
	public abstract Tree.Exp expFromStack(Tree.Exp stackPtr);
}
