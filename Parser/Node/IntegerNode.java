package Parser.Node;

//AST node that holds an integer value
public class IntegerNode extends Node {

    private int value;

    public IntegerNode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "" + value;
    }
}
