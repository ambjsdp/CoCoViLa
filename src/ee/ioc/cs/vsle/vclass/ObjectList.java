package ee.ioc.cs.vsle.vclass;

import java.io.Serializable;
import java.util.ArrayList;

public class ObjectList
	extends ArrayList
	implements Serializable {
	public ObjectList() {
		super();
	}

	public GObj checkInside(int x, int y) {
		GObj obj;

		for (int i = this.size() - 1; i >= 0; i--) {
			obj = (GObj)this.get(i);
			if (obj.contains(x, y)) {
				return obj;
			}
		}
		return null;
	}

	public GObj checkInside(int x, int y, GObj asker) {
		GObj obj;

		for (int i = 0; i < this.size(); i++) {
			obj = (GObj)this.get(i);
			if (obj.contains(x, y) && obj != asker) {
				return obj;
			}
		}
		return null;
	}

	public void selectObjectsInsideBox(int x1, int y1, int x2, int y2) {
		GObj obj;

		for (int i = 0; i < this.size(); i++) {
			obj = (GObj)this.get(i);
			if (obj.isInside(x1, y1, x2, y2)) {
				obj.setSelected(true);
			}
		}
	}

	public void updateSize(float newXSize, float newYSize) {
		GObj obj;

		for (int i = 0; i < this.size(); i++) {
			obj = (GObj)this.get(i);
			obj.setXSize(obj.getXSize() * newXSize);
			obj.setYSize(obj.getYSize() * newYSize);
			obj.setX( (int) (obj.getX() * newXSize));
			obj.setY( (int) (obj.getY() * newYSize));
		}
	}

	public void clearSelected() {
		GObj obj;

		for (int i = 0; i < this.size(); i++) {
			obj = (GObj)this.get(i);
			obj.setSelected(false);
		}
	}

	public ArrayList getSelected() {
		ArrayList a = new ArrayList();
		GObj obj;

		for (int i = 0; i < this.size(); i++) {
			obj = (GObj)this.get(i);
			if (obj.isSelected()) {
				a.add(obj);
			}
		}
		return a;
	}

}
