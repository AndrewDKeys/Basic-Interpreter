package Parser.Node;

import javax.swing.plaf.nimbus.State;

public class WhileNode extends StatementNode {

    private final BooleanNode condition;

    private final String label;

    private StatementNode next;

    public WhileNode(BooleanNode condition, String label) {
        this.condition = condition;
        this.label = label;
    }

    public void setNext(StatementNode next) {
        this.next = next;
    }

    public StatementNode next() {
        return next;
    }

    @Override
    public String toString() {
        return "WHILE(" + condition.toString() + ": " + label + ")";
    }
}
