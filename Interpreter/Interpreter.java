package Interpreter;

import Parser.Node.*;

import java.util.*;

public class Interpreter {

    private final StatementListNode statementList;

    //Holds all the elements that are in the DataNode's linked list, will act as a queue
    private final LinkedList<Node> dataQueue;

    //Maps all the gosub names to their node's
    private final HashMap<String, LabeledStatementNode> gosubMap;

    //Maps variable ints to their value
    private final HashMap<String, Integer> intMap;

    //Maps variable floats to their value
    private final HashMap<String, Float> floatMap;

    //Maps variable strings to their value
    private final HashMap<String, String> stringMap;

    //tells us whether we have encountered an end node
    private boolean end = false;

    //The statement we are currently interpreting
    private StatementNode currentStatement;

    //Stack will be used to go back and forth in code
    private Stack<StatementNode> statementStack;

    public Interpreter(StatementListNode statementList) {
        this.statementList = statementList;
        gosubMap = gosubWalk();
        dataQueue = dataWalk();
        intMap = new HashMap<String, Integer>();
        floatMap = new HashMap<String, Float>();
        stringMap = new HashMap<String, String>();
        statementStack = new Stack<StatementNode>();
        walkNext();
    }

    //Makes sure every statement node has a next
    private void walkNext() {
        StatementNode current = (StatementNode) statementList.getList().get(0);
        for(int i = 1; i < statementList.getList().size(); i++) {
            current.setNext((StatementNode) statementList.getList().get(i));
            current = current.next();
        }
        current.setNext(null); //makes sure the linked list is terminated with null
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
            int left = evaluateInt(((MathOpNode) node).getLeft()); //recursively get int value
            int right = evaluateInt(((MathOpNode) node).getRight()); //recursively get int value
            var operation = ((MathOpNode) node).getOperation();
            switch(operation) {
                case ADD -> { return left + right; }
                case SUBTRACT -> { return left - right; }
                case DIVIDE -> { return left / right; }
                case MULTIPLY -> { return left * right; }
                default -> throw new RuntimeException("Invalid math operator");
            }
        } else if(node instanceof VariableNode) {
            if(intMap.containsKey(node.toString())) {
                return intMap.get(node.toString());
            } else {
                throw new RuntimeException("Use of unassigned variable"); //while all variables are global, they have to be assigned
            }
        } else if (node instanceof FunctionNode) {
            if(((FunctionNode) node).getFunctionName().equals("random")) { //this is the only function that can return an integer
                return random();
            } else if(((FunctionNode) node).getFunctionName().equals("val")) {
                Node params = ((FunctionNode) node).getParameters().get(0);
                return intVal(evaluateString(params));
            } else {
                throw new RuntimeException("Invalid integer variable assignment");
            }
        } else {
            throw new RuntimeException("Invalid integer variable assignment");
        }
    }

    private float evaluateFloat(Node node) {
        if(node instanceof FloatNode) {
            return ((FloatNode) node).getValue();
        } else if(node instanceof MathOpNode) {
            float left = evaluateFloat(((MathOpNode) node).getLeft()); //recursively get int value
            float right = evaluateFloat(((MathOpNode) node).getRight()); //recursively get int value
            var operation = ((MathOpNode) node).getOperation();
            switch(operation) {
                case ADD -> { return left + right; }
                case SUBTRACT -> { return left - right; }
                case DIVIDE -> { return left / right; }
                case MULTIPLY -> { return left * right; }
                default -> throw new RuntimeException("Invalid math operator");
            }
        } else if(node instanceof VariableNode) {
            if(floatMap.containsKey(node.toString())) {
                return floatMap.get(node.toString());
            } else {
                throw new RuntimeException("Use of unassigned variable"); //while all variables are global, they have to be assigned
            }
        } else if (node instanceof FunctionNode) {
            if(((FunctionNode) node).getFunctionName().equals("val%")) { //val% is the only built-in that returns a float
                Node params = ((FunctionNode) node).getParameters().get(0);
                return floatVal(evaluateString(params));
            } else {
                throw new RuntimeException("Invalid float variable assignment");
            }
        } else {
            throw new RuntimeException("Invalid float variable assignment");
        }
    }

    private String evaluateString(Node node) {
        if(node instanceof StringNode) {
            return ((StringNode) node).getValue();
        } else if(node instanceof VariableNode) {
            if(stringMap.containsKey(node.toString())) {
                return stringMap.get(node.toString());
            } else {
                throw new RuntimeException("Use of unassigned variable"); //while all variables are global, they have to be assigned
            }
        } else if(node instanceof FunctionNode) {
            var params = ((FunctionNode) node).getParameters(); //The types for the parameters were already checked in the parser
            if(((FunctionNode) node).getFunctionName().equals("left$")) {
                return left(evaluateString(params.get(0)), evaluateInt(params.get(1)));
            } else if(((FunctionNode) node).getFunctionName().equals("right$")) {
                return right(evaluateString(params.get(0)), evaluateInt(params.get(1)));
            } else if(((FunctionNode) node).getFunctionName().equals("mid$")) {
                return mid(evaluateString(params.get(0)), evaluateInt(params.get(1)), evaluateInt(params.get(2)));
            } else if(((FunctionNode) node).getFunctionName().equals("num$")) {
                try {
                    return num(evaluateInt(params.get(0))); //trys to see if it wants to convert an int
                } catch(Exception e) {
                    return num(evaluateFloat(params.get(0))); //else they would want to convert a float
                }
            } else {
                throw new RuntimeException("Invalid string variable assignment");
            }
        } else {
            throw new RuntimeException("Invalid string variable assignment");
        }
    }

    //returns the type of variable i.e. String, int, float
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
        int dataSize = dataQueue.size();
        for(int i = 0; i < dataSize; i++) {
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
                    stringMap.put(readList.get(i).toString(), evaluateString(data));
                } else {
                    throw new RuntimeException("Mismatched types in READ list");
                }
            }
        }
        statementStack.push(node);
    }

    private void evaluateAssignment(AssignmentNode node) {
        String variableType = evaluateVariableType(node.getVariable());
        if(variableType.equals("int")) {
            intMap.put(node.getVariable().toString(), evaluateInt(node.getExpression())); //updating or adding the variable
        } else if(variableType.equals("float")) {
            floatMap.put(node.getVariable().toString(), evaluateFloat(node.getExpression())); //updating or adding the variable
        } else {
            stringMap.put(node.getVariable().toString(), evaluateString(node.getExpression())); //updating or adding the variable
        }
        statementStack.push(node);
    }

    private void evaluateInput(InputNode node) {
        Scanner getInput = new Scanner(System.in);
        var inputList = node.getValue();
        for(Node input : inputList) {
           if(input instanceof StringNode) {
               System.out.print(((StringNode) input).getValue()); //String literals are straight up printed
           } else if(input instanceof VariableNode) {
               if(evaluateVariableType((VariableNode) input).equals("int")) {
                   if(getInput.hasNextInt()) { //The next token has to be an int here
                       intMap.put(input.toString(), getInput.nextInt());
                   } else {
                       throw new RuntimeException("Expected input int"); //There is a mismatch in variable types
                   }
               } else if(evaluateVariableType((VariableNode) input).equals("float")) {
                   if(getInput.hasNextFloat()) { //The next token has to be a float here
                       floatMap.put(input.toString(), getInput.nextFloat());
                   } else {
                       throw new RuntimeException("Expected input float"); //There is a mismatch in variable types
                   }
               } else {
                   if(getInput.hasNext()) { //Assume the next token has to be a string
                       stringMap.put(input.toString(), getInput.next());
                   } else {
                       throw new RuntimeException("Expected input string"); //There is a mismatch in variable types
                   }
               }
           } else {
               throw new RuntimeException("Invalid input variables");
           }
        }
        statementStack.push(node);
    }

    private void evaluatePrint(PrintNode node) {
        var printList = node.getList();
        for(Node print : printList) {
            if(print instanceof StringNode) {
                System.out.print(((StringNode) print).getValue());
            } else if(print instanceof VariableNode) {
                var type = evaluateVariableType((VariableNode) print);
                if(type.equals("int") & intMap.containsKey(print.toString())) {
                    System.out.println(intMap.get(print.toString()));
                } else if(type.equals("float") & floatMap.containsKey(print.toString())) {
                    System.out.println(floatMap.get(print.toString()));
                } else if(type.equals("string") & stringMap.containsKey(print.toString())) {
                    System.out.println(stringMap.get(print.toString()));
                } else {
                    String exception = print + " not declared";
                    throw new RuntimeException(exception); //Variable has not been assigned anything yet or doesn't exist
                }
            }
        }
        statementStack.push(node);
    }

    private void evaluateIf(IfNode node) {

    }

    private void evaluateFor(ForNode node) {

    }

    private void evaluateGosub(GosubNode node) {

    }

    private void evaluateReturn(ReturnNode node) {

    }

    private void evaluateNext(NextNode node) {

    }

    private void evaluateBoolean(BooleanNode node) {

    }

    //interprets the current statement
    private void interpret(StatementNode node) {
        if(node instanceof ReadNode) {
            evaluateRead((ReadNode) node);
        } else if(node instanceof AssignmentNode) {
            evaluateAssignment((AssignmentNode) node);
        } else if(node instanceof InputNode) {
            evaluateInput((InputNode) node);
        } else if(node instanceof PrintNode) {
            evaluatePrint((PrintNode) node);
        } else if(node instanceof IfNode) {
            evaluateIf((IfNode) node);
        } else if(node instanceof ForNode) {
            evaluateFor((ForNode) node);
        } else if(node instanceof GosubNode) {
            evaluateGosub((GosubNode) node);
        } else if(node instanceof ReturnNode) {
            evaluateReturn((ReturnNode) node);
        } else if(node instanceof NextNode) {
            evaluateNext((NextNode) node);
        } else if(node instanceof EndNode) {
            end = true;
        } else {
            throw new RuntimeException("Unexpected statement");
        }
    }

    //runs the code
    public void run() {
        currentStatement = (StatementNode) statementList.getList().getFirst();
        while(!end) {
            interpret(currentStatement);
            currentStatement = currentStatement.next();
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
