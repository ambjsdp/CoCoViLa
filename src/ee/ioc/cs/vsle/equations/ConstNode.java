package ee.ioc.cs.vsle.equations;

import ee.ioc.cs.vsle.util.db;

import java.util.HashMap;

/**
 */
class ConstNode
	extends ExpNode {

	/**
	 * Node number.
	 */
	String value;

	/**
	 * Class constructor.
	 * @param val String
	 */ConstNode(String val) {
		value = val;
	} // ee.ioc.cs.editor.equations.ConstNode

	/**
	 * <UNCOMMENTED>
	 */ void getExpressions() {} // getExpressions

	/**
	 * <UNCOMMENTED>
	 */
	void getVars() {
		if (!Character.isDigit(value.charAt(0))) {
			EquationSolver.vars.add(value);
		}
	} // getVars

	/**
	 * <UNCOMMENTED>
	 * @param upper String
	 */
	void getExpressions(String upper) {
		String a, rel = "";

		if (!Character.isDigit(value.charAt(0))) {
			rel = value + "=" + upper + ":";
			for (int i = 0; i < EquationSolver.vars.size(); i++) {
				a = (String) EquationSolver.vars.get(i);
				if (!a.equals(value)) {
					rel += a + " ";
				}
			}
			rel += ":" + value;
			EquationSolver.relations.add(rel);
		}
	} // getExpressions

	/**
	 * <UNCOMMENTED>
	 * @param table -
	 * @return double -
	 */
	double calcValue(HashMap table) {
		if (!Character.isDigit(value.charAt(0))) {
			Double d = (Double) table.get(value);

			if (d != null) {
				return d.doubleValue();
			}
			else {
				System.out.println(value + " isnt assigned a value but used in equation");
			}
		}
		else {
			return Double.parseDouble(value);
		}
		return 0;
	}

	void leftReverse() {} // leftReverse

	/**
	 * <UNCOMMENTED>
	 */
	void rightReverse() {} // rightReverse

	/**
	 * <UNCOMMENTED>
	 */
	void decorate() {} // decorate

	/**
	 * <UNCOMMENTED>
	 * @return String
	 */
	String inFix() {
		return value;
	} // inFix

	/**
	 * <UNCOMMENTED>
	 */ void postFix() {
		db.p(value);
	} // postFix

}