package ee.ioc.cs.vsle.vclass;

/**
 * Created by IntelliJ IDEA.
 * User: Aulo
 * Date: 14.10.2003
 * Time: 9:14:33
 * To change this template use Options | File Templates.
 */
public class GroupUnfolder {
	public static ObjectList unfold(ObjectList objects) {
		ObjectList objects2 = new ObjectList();
		GObj obj;

		for (int i = 0; i < objects.size(); i++) {
			obj = (GObj) objects.get(i);
			objects2.addAll(obj.getComponents());
		}
		return objects2;
	}
}