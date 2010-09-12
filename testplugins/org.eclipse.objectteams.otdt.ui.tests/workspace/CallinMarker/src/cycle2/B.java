package cycle2;
public team class B {
        public class R playedBy A {
                void run() <- replace void run();
                callin void run() {
                        base.run();
                        System.out.println("R.run()");                  
                }
        }       
}