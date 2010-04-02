package foo;

public team class MyTeam {

    public class R1 playedBy MyBase
    {
        public void foo1()
        {
            System.out.println("R1.foo()");
        }
        
        public void foo2() 
        {
            System.out.println("R1.foo2()");
        }
     
        public void foo3()
        {
            System.out.println("R1.foo3()");
        }
        
        foo1 <- after baseMethod;
    }
    
    public class R2 extends R1
    {
        public void foobar1()
        {
            System.out.println("R2.foobar1()");
        }
        
        foo2 <- after bm2;
    }
    
}
