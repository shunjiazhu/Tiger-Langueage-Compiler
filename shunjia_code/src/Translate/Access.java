package Translate;

/**
 * Created by zhushunjia on 2016/12/3.
 */

//多了个Level的Access
public class Access {
    public Level home;
    public Frame.Access acc;//Frame中Access


    Access(Level l, Frame.Access a)
    {
        home = l;
        acc = a;
    }
}
