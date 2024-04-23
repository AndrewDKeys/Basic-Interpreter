package Parser.Node;

public class GosubNode extends StatementNode {

    private final String identifier; //The label that it is referencing

    private StatementNode next;

    public GosubNode(String identifier) {
        this.identifier =  identifier;
    }

    public void setNext(StatementNode next) {
        this.next = next;
    }

    public StatementNode next() {
        return next;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "GOSUB(" + identifier + ")";
    }
}
