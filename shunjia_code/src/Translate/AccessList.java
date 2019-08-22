package Translate;

/**
 * Created by zhushunjia on 2016/12/3.
 */

public class AccessList {
    public Access head;
    public AccessList next;

    AccessList(Access head, AccessList accl)
    {
        this.head = head;	next = accl;
    }
}
