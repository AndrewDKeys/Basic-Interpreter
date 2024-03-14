package Parser.Node;

import java.util.List;

public class InputNode extends StatementNode {

    private final List<Node> inputList;

    public InputNode(List<Node> inputList) {
        this.inputList = inputList;
    }

    public List<Node> getValue() {
        return inputList;
    }

    @Override
    public String toString() {
        return "Parser.Node.Parser.Node.InputNode(" + inputList.toString() + ")";
    }
}
