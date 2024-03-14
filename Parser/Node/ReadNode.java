package Parser.Node;

import java.util.List;

public class ReadNode extends StatementNode {

    //Holds list of variables read from the DATA statement
    //Does not verify that types are correct
    private final List<VariableNode> variableList;

    public ReadNode(List<VariableNode> variableList) {
        this.variableList = variableList;
    }

    public List<VariableNode> getValue() {
        return variableList;
    }

    @Override
    public String toString() {
        return "Parser.Node.ReadNode(" + variableList.toString() + ")";
    }
}
