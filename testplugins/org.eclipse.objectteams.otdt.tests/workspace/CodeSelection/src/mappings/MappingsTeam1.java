package mappings;

import base basepkg.B1;

public team class MappingsTeam1 {
	protected class R playedBy B1 {
        callin int rm1(int i) { base.rm1(i); }
        int rm1(int i) <- replace int bm1(int x) with {
            i <- x,
            result -> result
        }
        int rm2(int j) -> int bm1(int x)
        	with { 
        		j -> x,//callout mapping
        		result <- doubleIt(result)
        	}
        int doubleIt(int in) {
        	return 2 * in;
        }
        short shortJon() -> get long jon
        	with { result <- (short)jon } // c-t-f
        
        int doubleIt(int in) <- after int bm1(int x) 
        	with { result <- (int)jon } // callin
	}
}
