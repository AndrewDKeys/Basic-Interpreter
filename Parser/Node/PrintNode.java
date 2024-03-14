package Parser.Node;

import java.util.List;

//Holds a print statement
public class PrintNode extends StatementNode {

    //Should have anywhere from 1 - infinite variable and string literal nodes to print
    private final List<Node> nodes;

    public PrintNode(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Node> getList() {
        return nodes;
    }

    @Override
    public String toString() {
        return "PRINT(" + nodes.toString() + ")";
    }
}
