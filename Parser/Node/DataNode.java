package Parser.Node;

import java.util.List;

public class DataNode extends StatementNode {

    private final List<Node> dataList;

    public DataNode(List<Node> dataList) {
        this.dataList = dataList;
    }

    public List<Node> getValue(){
        return dataList;
    }

    @Override
    public String toString() {
        return "Parser.Node.Parser.Node.DataNode(" + dataList.toString() + ")";
    }
}
