package Parser.Node;

public class VariableNode extends Node {

    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
