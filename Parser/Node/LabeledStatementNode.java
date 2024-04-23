package Parser.Node;

public class LabeledStatementNode extends StatementNode {

    private final String label;

    private final StatementNode statement; //Label should only have 1 statement associated with it

    private StatementNode next;

    public LabeledStatementNode(String label, StatementNode statement) {
        this.label = label;
        this.statement = statement;
    }

    public String getName() {
        return label;
    }

    public StatementNode getStatement() {
        return statement;
    }

    public void setNext(StatementNode next) {
        this.next = next;
    }

    public StatementNode next() {
        return next;
    }

    @Override
    public String toString() {
        String s = "LABEL(" + label + ", ";
        return s + (statement != null ? statement + ")" : "endWhileLabel)");
    }
}
