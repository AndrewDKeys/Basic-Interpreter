package Parser.Node;

public class ForNode extends StatementNode {

    private final int increment;

    private final int end;

    private final AssignmentNode initialize;

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

    @Override
    public String toString() {
        return "FOR(" + initialize.toString() + " TO " + end + ", " + increment + ")";
    }
}
