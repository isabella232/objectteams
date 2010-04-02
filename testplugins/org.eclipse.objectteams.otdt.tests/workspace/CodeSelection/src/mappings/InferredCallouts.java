package mappings;
import base basepkg.B1;
public team class InferredCallouts {
	protected class R playedBy B1 {
		int foo() {
			return bm1(4);
		}
		void bar () {
			this.jon = 3L; // qualified version
			long jane = this.jon; // callout-to-field read
		}
		void barShort () {
			jon = 3L; // unqualified version
			long jane = jon; // unqualified: callout-to-field read
		}
	}
}