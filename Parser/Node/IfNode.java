package Parser.Node;

public class IfNode extends StatementNode {

    private final BooleanNode condition;

    // All if statements in our version of BASIC call a label if the condition is true
    private final String label;

    public IfNode(BooleanNode condition, String label) {
        this.condition = condition;
        this.label = label;
    }

    @Override
    public String toString() {
        return "IF(" + condition.toString() + " THEN " + label + ")";
    }
}