package Parser.Node;

public class ForNode extends StatementNode {

    private final int increment;

    private final int end;

    private final AssignmentNode initialize;

    private StatementNode next;

    public ForNode(int increment, AssignmentNode initialize, int end) {
        this.increment = increment;
        this.initialize = initialize;
        this.end = end;
    }

    public int getIncrement() {
        return increment;
    }

    public int getEnd() {
        return end;
    }

    public AssignmentNode getInitialize() {
        return initialize;
    }

    public void setNext(StatementNode next) {
        this.next = next;
    }

    public StatementNode next() {
        return next;
    }

    @Override
    public String toString() {
        return "FOR(" + initialize.toString() + " TO " + end + ", " + increment + ")";
    }
}
