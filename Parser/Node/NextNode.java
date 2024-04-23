package Parser.Node;

public class NextNode  extends StatementNode {

    private StatementNode next;

    public void setNext(StatementNode next) {
        this.next = next;
    }

    public StatementNode next() {
        return next;
    }

    @Override
    public String toString() {
        return "NEXT";
    }
}
