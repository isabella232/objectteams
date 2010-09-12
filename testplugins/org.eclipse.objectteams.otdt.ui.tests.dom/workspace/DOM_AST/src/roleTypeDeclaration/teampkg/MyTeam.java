package roleTypeDeclaration.teampkg;

import roleTypeDeclaration.basepkg.MyClass;

public team class MyTeam
{
    public class Role1 playedBy MyClass
    {
        public abstract void roleMethod0(Integer integer);
        public abstract void roleMethod1(int val);
        
        void roleMethod0(Integer integer) -> void baseMethod0(int val) with
        {
            integer.intValue() -> val
        }
        
        void roleMethod1(int val) -> void baseMethod1(Integer integer) with
        {
            new Integer(val) -> integer
        }
    }
    
    public team class Role2 playedBy MyClass 
    {
        public void teamMethod0()
        {
        }
        
        callin int roleMethod2 (Integer roleInteger)
        {
            return 0;
        }
        
        int roleMethod3 (Integer roleInteger)
        {
            return 0;
        }
        
        int roleMethod2(Integer roleInteger) <-  replace int baseMethod2(Integer integer) with
        {
            roleInteger <-  integer,
            result -> result
        }
        
        int roleMethod3(Integer roleInteger) <-  after int baseMethod3(Integer integer) with
        {
            roleInteger <-  integer,
            result -> result
        }
    }   

    public class Role3 
    {
        Role3()
        {
            doSomething();
        }
        
        public void doSomething() {}
    }   
}