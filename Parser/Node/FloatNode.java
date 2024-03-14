package Parser.Node;

//AST node that holds a float value
public class FloatNode extends Node {

    private float value;

    public FloatNode(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "" + value;
    }
}
