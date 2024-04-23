package Parser;

import Lexer.Token;
import Parser.Node.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


public class Parser {

    private final TokenHandler tokens;

    public Parser(LinkedList<Token> tokenList) {
        tokens = new TokenHandler(tokenList);
    }

    //Finds any separators and "eats" them, returns true if there was at least one. This is because in Basic, there can
    //be any number of separators in between statements.
    private boolean acceptSeparators() {
        Optional<Token> seperator = Optional.empty();
        if(tokens.moreTokens()) {
            seperator = tokens.matchAndRemove(Token.TokenType.ENDOFLINE);
        }
        if(seperator.isEmpty()) {
            return false;
        } else {
            while(seperator.isPresent() && tokens.moreTokens())
                seperator = tokens.matchAndRemove(Token.TokenType.ENDOFLINE);
            return true;
        }
    }

    //Loops through to find every statement
    private StatementListNode statementList() {
        var nodeList = new LinkedList<Node>();
        var statement = statement();
        while(statement != null) { //stops when there isn't a valid statement
            nodeList.add(statement);
            acceptSeparators(); //there can be any amount of separators in between statements
            statement = statement();
        }
        return new StatementListNode(nodeList);
    }

    //Finds statements to return to the list or returns null
    private StatementNode statement() {
        var token = tokens.matchAndRemove(Token.TokenType.LABEL);
        if(token.isPresent())
            return new LabeledStatementNode(token.get().getValue(), statement()); //label holds a singular statement
        else if(tokens.matchAndRemove(Token.TokenType.PRINT).isPresent())
            return printStatement();
        else if((token = tokens.matchAndRemove(Token.TokenType.WORD)).isPresent()) //If there is a word then it will always be a variable and therefore an assignment
            return assignment(token.get());
        else if((tokens.matchAndRemove(Token.TokenType.DATA)).isPresent())
            return data();
        else if((tokens.matchAndRemove(Token.TokenType.READ)).isPresent())
            return read();
        else if(tokens.matchAndRemove(Token.TokenType.INPUT).isPresent())
            return input();
        else if(tokens.matchAndRemove(Token.TokenType.GOSUB).isPresent())
            return gosub();
        else if(tokens.matchAndRemove(Token.TokenType.RETURN).isPresent())
            return returnNode();
        else if(tokens.matchAndRemove(Token.TokenType.END).isPresent())
            return new EndNode();
        else if(tokens.matchAndRemove(Token.TokenType.FOR).isPresent())
            return forStatement();
        else if(tokens.matchAndRemove(Token.TokenType.NEXT).isPresent())
            return new NextNode();
        else if(tokens.matchAndRemove(Token.TokenType.END).isPresent())
            return new EndNode();
        else if(tokens.matchAndRemove(Token.TokenType.IF).isPresent())
            return ifStatement();
        else if(tokens.matchAndRemove(Token.TokenType.WHILE).isPresent())
            return whileStatement();
        else
            return null; //not a valid statement
    }

    private BooleanNode booleanExpression() {
        var left = expression(); //left sign of expression
        var check = tokens.peek(0); //looks to see what the next operator is
        Token.TokenType operator;
        if(check.isPresent()) {
            switch(check.get().getType()) {
                case GREATERTHAN -> operator = Token.TokenType.GREATERTHAN;
                case LESSTHAN -> operator = Token.TokenType.LESSTHAN;
                case GREATERTHANEQUALS -> operator = Token.TokenType.GREATERTHANEQUALS;
                case LESSTHANEQUALS -> operator = Token.TokenType.LESSTHANEQUALS;
                case NOTEQUALS -> operator = Token.TokenType.NOTEQUALS;
                default -> {return null;} // Not a valid operator
            }
        } else {
            return null;
        }
        tokens.matchAndRemove(operator);
        var right = expression(); //right side of expression
        return new BooleanNode(left, right, operator);
    }

    private IfNode ifStatement() {
        var condition = booleanExpression();
        if(tokens.matchAndRemove(Token.TokenType.THEN).isPresent()) {
            var label = tokens.matchAndRemove(Token.TokenType.WORD);
            return label.map(token -> new IfNode(condition, token.getValue() + ":")).orElse(null);
        } else {
            return null;
        }
    }

    private WhileNode whileStatement() {
        var condition = booleanExpression();
        var label = tokens.matchAndRemove(Token.TokenType.WORD); // The end label of a while loop
        return label.map(token -> new WhileNode(condition, token.getValue())).orElse(null);
    }

    //Gosub must have only 1 other token on the line with it, and it must be a word token else it will return null
    private GosubNode gosub() {
        var token = tokens.matchAndRemove(Token.TokenType.WORD);
        return token.map(value -> new GosubNode(value.getValue() + ":")).orElse(null);
    }

    //ReturnNode must be the only thing on the line, or else it is an invalid statement
    private ReturnNode returnNode() {
        return acceptSeparators() ? new ReturnNode() : null;
    }

    //adds in the header for a for statement, returns null if it is not a valid for initialization
    private ForNode forStatement() {
        AssignmentNode initialize;
        int end;
        var variable = tokens.matchAndRemove(Token.TokenType.WORD);
        if(variable.isPresent()) { //looking for the assignment at the beginning of a for loop
            initialize = assignment(variable.get());
        } else {
            return null;
        }

        variable = tokens.matchAndRemove(Token.TokenType.TO);
        if(variable.isPresent()) { //Looking for the end number of the loop
            variable = tokens.matchAndRemove(Token.TokenType.NUMBER);
            if(variable.isPresent()) {
                end = Integer.parseInt(variable.get().getValue());
            } else {
                return null;
            }
        } else {
            return null;
        }

        variable = tokens.matchAndRemove(Token.TokenType.STEP);
        if(variable.isPresent()) { //Looking to see if they added increment, this is optional
            int increment;
            variable = tokens.matchAndRemove(Token.TokenType.NUMBER);
            if(variable.isPresent()) {
                increment = Integer.parseInt(variable.get().getValue());
                return new ForNode(increment, initialize, end);
            } else {
                return null;
            }
        } else {
            return new ForNode(1, initialize, end); //1 is the default increment if it is not
        }
    }

    private PrintNode printStatement() {
        var printList = printList();
        return printList != null ? new PrintNode(printList) : null; //If it was a valid PRINT statement
    }

    private LinkedList<Node> printList() {
        var printList = new LinkedList<Node>();
        var comma = tokens.matchAndRemove(Token.TokenType.COMMA); //Should start off empty but needs to be initialized before the loop
        do {
            var token = tokens.matchAndRemove(Token.TokenType.WORD);
            if(token.isPresent()) {
                printList.add(new VariableNode(token.get().getValue()));
            } else if ((token = tokens.matchAndRemove(Token.TokenType.STRINGLITERAL)).isPresent()) {
                printList.add(new StringNode(token.get().getValue()));
            } else {
                return null; //Not a valid print statement
            }

            if(tokens.moreTokens()) {
                comma = tokens.matchAndRemove(Token.TokenType.COMMA);
            } else {
                comma = Optional.empty(); //It is the end of the file
            }
        } while(comma.isPresent());
        return printList;
    }

    private DataNode data() {
        var dataList = dataList();
        return dataList != null ? new DataNode(dataList) : null; //If it was a valid DATA statement
    }

    private List<Node> dataList() {
        var dataList = new LinkedList<Node>();
        var comma = tokens.matchAndRemove(Token.TokenType.COMMA); //Should start off empty but needs to be initialized before the loop
        do {
            var token = tokens.matchAndRemove(Token.TokenType.STRINGLITERAL);
            if(token.isPresent()) {
                dataList.add(new StringNode(token.get().getValue()));
            } else if((token = tokens.matchAndRemove(Token.TokenType.NUMBER)).isPresent()) {
                try {
                    dataList.add(new IntegerNode(Integer.parseInt(token.get().getValue())));
                } catch(NumberFormatException e) { //If the int is not parsable then it has to be a float
                    dataList.add(new FloatNode(Float.parseFloat(token.get().getValue())));
                }
            } else {
                return null; //not a valid DATA entry
            }

            if(tokens.moreTokens()) {
                comma = tokens.matchAndRemove(Token.TokenType.COMMA);
            } else {
                comma = Optional.empty(); //It is the end of the file
            }
        } while(comma.isPresent());
        return dataList;
    }

    private ReadNode read() {
        var readList = readList();
        return readList != null ? new ReadNode(readList) : null; //If it is a valid read statement
    }

    private List<VariableNode> readList() {
        var readList = new LinkedList<VariableNode>();
        var comma = tokens.matchAndRemove(Token.TokenType.COMMA); //Should start off empty but needs to be initialized before the loop
        do {
            var token = tokens.matchAndRemove(Token.TokenType.WORD); //A word will end up being a variable
            if(token.isPresent()) {
                readList.add(new VariableNode(token.get().getValue()));
            } else {
                return null;
            }

            if(tokens.moreTokens()) {
                comma = tokens.matchAndRemove(Token.TokenType.COMMA);
            } else {
                comma = Optional.empty(); //It is the end of the file
            }
        } while(comma.isPresent());
        return readList;
    }

    private InputNode input() {
        var input = inputList();
        return input != null ? new InputNode(input) : null; //If it was a valid INPUT statement
    }

    private List<Node> inputList() {
        var inputList = new LinkedList<Node>();
        var comma = tokens.matchAndRemove(Token.TokenType.COMMA); //Should start off empty but needs to be initialized before the loop
        boolean stringPresent = false; // Inputs can only have variables, except for the first parameter which can be a string that prints out
        do {
            var token = tokens.matchAndRemove(Token.TokenType.WORD); //A word will end up being a variable
            if (token.isPresent()) {
                inputList.add(new VariableNode(token.get().getValue()));
            } else if(!stringPresent & (token = tokens.matchAndRemove(Token.TokenType.STRINGLITERAL)).isPresent()) {
                inputList.add(new StringNode(token.get().getValue()));
            } else {
                return null;
            }

            stringPresent = true; //makes sure that no other strings are accepted after first parameter

            if(tokens.moreTokens()) {
                comma = tokens.matchAndRemove(Token.TokenType.COMMA);
            } else {
                comma = Optional.empty(); //It is the end of the file
            }
        } while(comma.isPresent());
        return inputList;
    }

    //checks for commas in the function invocation
    private boolean checkComma() {
        return tokens.matchAndRemove(Token.TokenType.COMMA).isPresent();
    }

    // Checks numbers for the function invocation method
    private Node checkNumbers() {
        Optional<Token> token;
        if((token = tokens.matchAndRemove(Token.TokenType.NUMBER)).isPresent()) { // A number can either be a variable or a number
            try {
                return new IntegerNode(Integer.parseInt(token.get().getValue()));
            } catch (NumberFormatException e) { //Try to convert the string to either a float or an int
                return new FloatNode(Float.parseFloat(token.get().getValue()));
            }
        } else if((token = tokens.matchAndRemove(Token.TokenType.WORD)).isPresent()) {
            return new VariableNode(token.get().getValue());
        } else {
            return null;
        }
    }

    private FunctionNode functionInvocation(String function) {
        if(function.equals("random")) {
            if(tokens.matchAndRemove(Token.TokenType.LPAREN).isEmpty() | tokens.matchAndRemove(Token.TokenType.RPAREN).isEmpty())
                return null; //Not valid function call
            else
                return new FunctionNode("random", new LinkedList<>()); //remove has no parameters
        }

        Optional<Token> token;
        LinkedList<Node> parameters = new LinkedList<>();

        if(function.equals("num$")) {
            var num = checkNumbers();
            if(num == null)
                return null;
            parameters.add(num);
            if(tokens.matchAndRemove(Token.TokenType.RPAREN).isEmpty())
                return null; //not a valid function call
            return new FunctionNode(function, parameters);
        }

        if(tokens.matchAndRemove(Token.TokenType.LPAREN).isEmpty()) //A string can either be a literal or a variable
            return null; //not a valid function call
        if((token = tokens.matchAndRemove(Token.TokenType.WORD)).isPresent())
            parameters.add(new VariableNode(token.get().getValue()));
        else if((token = tokens.matchAndRemove(Token.TokenType.STRINGLITERAL)).isPresent())
            parameters.add(new StringNode(token.get().getValue()));
        else
            return null;
        if(function.equals("val") || function.equals("val%")) { //these functions have a singular string parameter
            if(tokens.matchAndRemove(Token.TokenType.RPAREN).isEmpty())
                return null;
            return new FunctionNode(function, parameters);
        }
        if(!checkComma()) {return null;} //not a comma in between parameters

        var num = checkNumbers();
        if(num == null)
            return null;
        parameters.add(num);
        if(function.equals("left$") || function.equals("right%")) { //these functions have 2 parameters
            if(tokens.matchAndRemove(Token.TokenType.RPAREN).isEmpty())
                return null;
            return new FunctionNode(function, parameters);
        }
        if(!checkComma()) {return null;} //not a comma in between parameters

        num = checkNumbers();
        if(num == null)
            return null;
        parameters.add(num);
        if(function.equals("mid$")) { //these functions have 2 parameters
            if(tokens.matchAndRemove(Token.TokenType.RPAREN).isEmpty())
                return null;
            return new FunctionNode(function, parameters);
        } else {
            return null;
        }
    }

    private AssignmentNode assignment(Token variable) {
        var equals = tokens.matchAndRemove(Token.TokenType.EQUALS);
        if(equals.isPresent()) {
            return new AssignmentNode(new VariableNode(variable.getValue()), expression());
        } else {
            throw new RuntimeException("Invalid assignment");
        }
    }

    //Checks for any addition or subtraction, we start here in order to preserve PEMDAS
    private Node expression(){
        var left = term();
        if(left == null)
            return null;
        Optional<Token> addition;
        Optional<Token> subtract;
        if(tokens.moreTokens()) {
            addition = tokens.matchAndRemove(Token.TokenType.ADD);
            subtract = tokens.matchAndRemove(Token.TokenType.SUBTRACT);
        } else {
            return left;
        }
        if (addition.isPresent()) {
            var right = term();
            left = new MathOpNode(addition.get().getType(), left, right);
        } else if (subtract.isPresent()) {
            var right = term();
            left = new MathOpNode(subtract.get().getType(), left, right);
        }
        return left;
    }

    //Check for any multiplication or division, we recursively call term() to preserve PEMDAS for the right node
    private Node term() {
        var left = factor();
        if(left == null)
            return null;
        Optional<Token> multiply;
        Optional<Token> divide;
        if(tokens.moreTokens()) {
            multiply = tokens.matchAndRemove(Token.TokenType.MULTIPLY);
            divide = tokens.matchAndRemove(Token.TokenType.DIVIDE);
        } else {
            return left;
        }
        if (multiply.isPresent()) {
            var right = term();
            left = new MathOpNode(multiply.get().getType(), left, right);
        } else if (divide.isPresent()) {
            var right = term();
            left = new MathOpNode(divide.get().getType(), left, right);
        }
        return left;
    }

    //Returns a Float or Integer node, check for parenthesis to make sure any expression inside parenthesis is done first
    //Throws error if there is not a number or there is no closed parenthesis
    private Node factor() {
        var L = tokens.matchAndRemove(Token.TokenType.FUNCTION);
        if(L.isPresent()) {
            return functionInvocation(L.get().getValue());
        }
        L = tokens.matchAndRemove(Token.TokenType.NUMBER);
        if(L.isPresent()) { //if it is indeed a number
            try {
                return new IntegerNode(Integer.parseInt(L.get().getValue()));
            } catch (NumberFormatException e) { //the number is not parsable to an int then it is a float
                return new FloatNode(Float.parseFloat(L.get().getValue()));
            }
        } else if((L = tokens.matchAndRemove(Token.TokenType.WORD)).isPresent()) { //the number is a variable
            return new VariableNode(L.get().getValue());
        } else if ((tokens.matchAndRemove(Token.TokenType.LPAREN)).isPresent()) {
            var expression = expression();
            if ((tokens.matchAndRemove(Token.TokenType.RPAREN)).isEmpty()) {
                throw new RuntimeException("Not a valid expression");
            }
            return expression;
        } else {
            throw new RuntimeException("Not a valid expression");
        }
    }

    //Parses the AST tree
    public StatementListNode parse() {
        return statementList();
    }
}