package Interpreter;

import Parser.Node.*;

import java.util.HashMap;
import java.util.LinkedList;
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
        intMap = new HashMap<String, Integer>();
        floatMap = new HashMap<String, Float>();
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

    private int evaluateInt(Node node) {
        if(node instanceof IntegerNode) {
           return ((IntegerNode) node).getValue();
        } else if(node instanceof MathOpNode) {

            return 0;
        } else if(node instanceof VariableNode) {

            return 0;
        } else if (node instanceof FunctionNode) {

            return 0;
        } else {
            throw new RuntimeException("Invalid integer variable assignment");
        }
    }

    private float evaluateFloat(Node node) {
        if(node instanceof FloatNode) {
            return ((FloatNode) node).getValue();
        } else if(node instanceof MathOpNode) {

            return 0;
        } else if(node instanceof VariableNode) {

            return 0;
        } else if (node instanceof FunctionNode) {

            return 0;
        } else {
            throw new RuntimeException("Invalid float variable assignment");
        }
    }

    //returns the type of a variable i.e. String, int, float
    private String evaluateVariableType(VariableNode node) {
        char type = node.toString().charAt(node.toString().length() - 1); //the last character of a variable signifies its type
        switch(type) {
            case '%' -> { return "float"; }
            case '$' -> { return "string"; }
            default  -> { return "int"; }
        }
    }

    private void evaluateRead(ReadNode node) {
        var readList = node.getValue();
        if(readList.size() != dataQueue.size())
            throw new RuntimeException("READ list size does not match DATA list size"); //READ needs to have same amount of variables as DATA

        for(int i = 0; i < dataQueue.size(); i++) {
            Node data = dataQueue.remove(); //removes the first inputted element in queue style
            if(data instanceof IntegerNode) {
                if(evaluateVariableType(readList.get(i)).equals("int")) { //checks to see if read variable is the correct type

                    intMap.put(readList.get(i).toString(), evaluateInt(data));
                } else {
                    throw new RuntimeException("Mismatched types in READ list");
                }
            } else if(data instanceof FloatNode) {
                if(evaluateVariableType(readList.get(i)).equals("float")) { //checks to see if read variable is the correct type
                    floatMap.put(readList.get(i).toString(), evaluateFloat(data));
                } else {
                    throw new RuntimeException("Mismatched types in READ list");
                }
            } else {
                if(evaluateVariableType(readList.get(i)).equals("string")) { //checks to see if read variable is the correct type
                    stringMap.put(readList.get(i).toString(), data.toString());
                } else {
                    throw new RuntimeException("Mismatched types in READ list");
                }
            }
        }
    }

    public void interpret() {
        for(Node node : statementList.getList()) {
            if(node instanceof ReadNode) {
                evaluateRead((ReadNode) node);
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
