package Parser.Node;

public class GosubNode extends StatementNode {

    private final String identifier; //The label that it is referencing

    public GosubNode(String identifier) {
        this.identifier =  identifier;
    }

    @Override
    public String toString() {
        return "GOSUB(" + identifier + ")";
    }
}
