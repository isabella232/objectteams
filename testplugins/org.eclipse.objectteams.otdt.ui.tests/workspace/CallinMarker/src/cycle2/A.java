package cycle2;
public team class A<B b> {
        public void run() {
                System.out.println("A.run()");
        }
        public class Q playedBy R<@b> {
                void run() <- replace void run();
                // 1. originally the compiler forced to declare 'Object run()'
                // 2. compile process is fragile with circular references, like in this exercise
                callin void run() {
                        base.run();
                        System.out.println("callin: Q.run()");
                }
        }       
}