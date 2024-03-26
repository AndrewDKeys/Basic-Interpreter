package Parser.Node;

public class LabeledStatementNode extends StatementNode {

    private final String label;

    private final StatementNode statement; //Label should only have 1 statement associated with it

    public LabeledStatementNode(String label, StatementNode statement) {
        this.label = label;
        this.statement = statement;
    }

    @Override
    public String toString() {
        return "LABEL(" + label + ", " + statement.toString() + ")";
    }
}
