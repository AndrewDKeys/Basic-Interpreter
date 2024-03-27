package Parser.Node;

import java.util.List;

// Node for BASIC's built-in functions
public class FunctionNode extends Node {

    private final String functionName;

    private final List<Node> parameters; // built-in functions can have anywhere between 0 and 3 parameters

    public FunctionNode(String functionName, List<Node> parameters) {
        this.functionName = functionName;
        this.parameters = parameters;
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public String toString() {
        return functionName + "(" + parameters.toString() + ")";
    }
}
