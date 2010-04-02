package test001;

public team  class T5 extends T2
{
    public class R1 
    {
        public void m8_T5R1() {}
    }
    public class R3 extends R1 
    {
        public void m9_T5R3() {}
        public void m_override() {}
    }
    public class R2 extends R3 
    {
        public void m10_T5R2() {}
    }
}
