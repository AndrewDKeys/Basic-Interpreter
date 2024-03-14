package Parser.Node;

//Holds any string literal
public class StringNode extends Node {

    //Excludes quotations from the string because they are implied and handled in the lexer
    private final String value;

    public StringNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Parser.Node.StringNode(" + value + ")";
    }
}
