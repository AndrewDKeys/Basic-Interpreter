package Parser.Node;

import java.util.List;

public class DataNode extends StatementNode {

    private final List<Node> dataList;

    private StatementNode next;

    public DataNode(List<Node> dataList) {
        this.dataList = dataList;
    }

    public List<Node> getValue(){
        return dataList;
    }

    public void setNext(StatementNode next) {
        this.next = next;
    }

    public StatementNode next() {
        return next;
    }

    @Override
    public String toString() {
        return "Parser.Node.Parser.Node.DataNode(" + dataList.toString() + ")";
    }
}
