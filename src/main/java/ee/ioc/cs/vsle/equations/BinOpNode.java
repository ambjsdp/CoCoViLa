package ee.ioc.cs.vsle.equations;

/**
 * represents all binary operations in an equations
 * @author Ando Saabas
 */
class BinOpNode extends ExpNode {

    char op; // The operator.
    ExpNode left; // The expression for its left operand.
    ExpNode right; // The expression for its right operand.
    String leftSub; // The infix representation of its left subtree, used to generate the solution of the equation
    String rightSub; // The infix representation of its right subtree, used to generate  the solution of the equation

    /**
     * Class constructor. Constructs a BinOpNode containing the specified data.
     * @param op char
     * @param left ExpNode
     * @param right ExpNode
     */
    BinOpNode(char op, ExpNode left, ExpNode right) {
        this.op = op;
        this.left = left;
        this.right = right;
    } // ee.ioc.cs.editor.equations.BinOpNode

    /**
     * Returns an opposite value of the operand.
     * @param _op char - operand.
     * @return char - opposite value of the operand.
     */
    char getOpposite(char _op) {
        switch (_op) {
        case '-':
            return '+';

        case '+':
            return '-';

        case '/':
            return '*';

        case '*':
            return '/';

        case '=':
            return ' ';

        default:
            return _op;
        }
    } // getOpposite

    /**
     * orders the list of variables for the left and right expression to be created.
     */
    @Override
    void getVars() {
        left.getVars();
        right.getVars();
    } // getVars

    /**
     * Creates the suitable infix expression of the left subtree
     */
    void leftReverse() {
        if (op=='-' || op=='/')
            leftSub = left.inFix() + op;
        else
            leftSub = getOpposite(op) + left.inFix();
    } // leftReverse

    /**
     * Creates the suitable infix expression of the right subtree
     */
    void rightReverse() {
        rightSub = getOpposite(op) + right.inFix();
    } // rightReverse

    /**
     * annotates itself with correct infix representation of its subtrees
     */
    @Override
    void decorate() {
        leftReverse();
        rightReverse();
        left.decorate();
        right.decorate();
    } // decorate

    /**
     * propagate fully solved epxressions to the leafs
     */
    @Override
    void getExpressions() {
        left.getExpressions(rightSub);
        right.getExpressions(leftSub);
    } // getExpression

    /**
     * propagate fully solved epxressions to the leafs
     * @param upper - the infix representation of the rest of the equation
     */
    @Override
    void getExpressions(String upper) {
        if (op == '^') {
            left.getExpressions(
                    "Math.pow(" + upper + ", 1.0/("
                    + rightSub.substring(1, rightSub.length()) + "))");
        } else {
            left.getExpressions(upper + rightSub);
            if (op == '-') {
                right.getExpressions("("+leftSub + upper+")");
            } else if (op == '/') {
                right.getExpressions("("+leftSub + upper+")");
            } else {
                right.getExpressions("("+upper + leftSub+")");
            }
        }
    } // getExpressions

    /**
     * returns the infix representation of the equation
     * @return Infix representation of the equation
     */
    @Override
    String inFix() {
        String l = "(" + left.inFix();
        String r = right.inFix() + ")";

        if (op == '^') {
            return ("Math.pow" + l + ", " + r + "");
        } 
        return (l + " " + op + " " + r);

    } // inFix

}

