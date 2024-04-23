package Parser.Node;

import java.util.List;

public class InputNode extends StatementNode {

    private final List<Node> inputList;

    private StatementNode next;

    public InputNode(List<Node> inputList) {
        this.inputList = inputList;
    }

    public List<Node> getValue() {
        return inputList;
    }

    public void setNext(StatementNode next) {
        this.next = next;
    }

    public StatementNode next() {
        return next;
    }

    @Override
    public String toString() {
        return "Parser.Node.Parser.Node.InputNode(" + inputList.toString() + ")";
    }
}
