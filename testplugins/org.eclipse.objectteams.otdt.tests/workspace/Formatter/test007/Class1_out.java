import org.objectteams.Team;
public class Class1 {
	public static foo(Team t) {
		if (t == null)
			return;
		within (t) {
			System.out.println("active");
		}
	}
}