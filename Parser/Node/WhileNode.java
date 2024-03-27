package Parser.Node;

public class WhileNode extends StatementNode {

    private final BooleanNode condition;

    private final String label;

    public WhileNode(BooleanNode condition, String label) {
        this.condition = condition;
        this.label = label;
    }

    @Override
    public String toString() {
        return "WHILE(" + condition.toString() + ": " + label + ")";
    }
}
