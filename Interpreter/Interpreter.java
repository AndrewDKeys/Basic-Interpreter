package Interpreter;

import Parser.Node.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Interpreter {

    private final StatementListNode statementList;

    //Holds all the elements that are in the DataNode's linked list, will act as a queue
    private final LinkedList<Node> dataQueue;

    //Maps all the gosub names to their node's
    private final HashMap<String, LabeledStatementNode> gosubMap;

    //Maps variable ints to their value
    private HashMap<String, Integer> intMap;

    //Maps variable floats to their value
    private HashMap<String, Float> floatMap;

    //Maps variable strings to their value
    private HashMap<String, String> stringMap;

    public Interpreter(StatementListNode statementList) {
        this.statementList = statementList;
        gosubMap = gosubWalk();
        dataQueue = dataWalk();
    }

    //Walks the tree until a data statement is found, can return empty queue if none are used
    private LinkedList<Node> dataWalk() {
        var queue = new LinkedList<Node>();
        for(Node search: statementList.getList()) {
            if(search instanceof DataNode) {
                queue.addAll(((DataNode) search).getValue());
                break; //there can only be one DATA statement in a basic file
            }
        }
        return queue;
    }

    //Walks the tree finding all the labels, can return an empty hashmap if there are none used
    private HashMap<String, LabeledStatementNode> gosubWalk() {
        var gosubMap = new HashMap<String, LabeledStatementNode>();
        for(Node search : statementList.getList()) {
            if(search instanceof LabeledStatementNode) {
                gosubMap.put(((LabeledStatementNode) search).getName(), (LabeledStatementNode) search);
            }
        }
        return gosubMap;
    }

    private int evaluateInt() {

        return 0;
    }

    private float evaluateFloat() {

        return 0;
    }

    public void interpret() {
        for(Node node : statementList.getList()) {
            if(node instanceof ReadNode) {
                var readList = ((ReadNode) node).getValue();
                if(readList.size() != dataQueue.size()) {
                    throw new RuntimeException(); //READ needs to have same amount of variables as DATA
                }
                for (int i = 0; i < dataQueue.size(); i++) {

                }
            }
        }
    }

    /*
        The remaining methods are implementations of BASIC's built-in functions
     */

    public static int random() {
        return new Random().nextInt();
    }

    //returns n-most left characters
    public static String left(String data, int bound) {
        return data.substring(0, bound);
    }

    //returns n-most right characters
    public static String right(String data, int bound) {
        return data.substring(data.length() - bound);
    }

    //returns m-n-most middle characters
    public static String mid(String data, int leftBound, int rightBound) {
        return data.substring(leftBound, rightBound);
    }

    //Int to String
    public static String num(int number) {
        return Integer.toString(number);
    }

    //Float to String
    public static String num(float number) {
        return Float.toString(number);
    }

    //String to int
    public static int intVal(String data) {
        return Integer.parseInt(data);
    }

    //String to float
    public static float floatVal(String data) {
        return Float.parseFloat(data);
    }
}
