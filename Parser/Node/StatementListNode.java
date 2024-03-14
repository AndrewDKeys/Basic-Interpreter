package Parser.Node;

import java.util.LinkedList;

//Holds a list of all the statements in the program
public class StatementListNode extends Node {

    //Holds PrintNodes and/or AssignmentNodes
    private final LinkedList<Node> nodeList;

    public StatementListNode(LinkedList<Node> nodeList) {
        this.nodeList = nodeList;
    }

    public LinkedList<Node> getList() {
        return nodeList;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for(var i: nodeList) {
            s.append(i.toString()).append(" ");
        }
        return s.toString();
    }
}
