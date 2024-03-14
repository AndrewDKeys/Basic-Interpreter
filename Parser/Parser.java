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

    private StatementNode statement() {
        var token = tokens.matchAndRemove(Token.TokenType.PRINT);
        if(token.isPresent())
            return printStatement();
        else if((token = tokens.matchAndRemove(Token.TokenType.WORD)).isPresent()) //If there is a word then it will always be a variable and therefore an assignment
            return assignment(token.get());
        else if((tokens.matchAndRemove(Token.TokenType.DATA)).isPresent())
            return data();
        else if((tokens.matchAndRemove(Token.TokenType.READ)).isPresent())
            return read();
        else if(tokens.matchAndRemove(Token.TokenType.INPUT).isPresent())
            return input();
        else
            return null; //not a valid statement
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

    private AssignmentNode assignment(Token variable) {
        var equals = tokens.matchAndRemove(Token.TokenType.EQUALS);
        if(equals.isPresent()) {
            return new AssignmentNode(new VariableNode(variable.getValue()), expression());
        } else {
            return null; //not a valid assignment
        }
    }

    //Checks for any addition or subtraction, we start here in order to preserve PEMDAS
    private Node expression(){
        var left = term();
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
        var L = tokens.matchAndRemove(Token.TokenType.NUMBER);
        if(L.isPresent()) { //if it is indeed a number
            try {
                return new IntegerNode(Integer.parseInt(L.get().getValue()));
            } catch (NumberFormatException e) { //the number is not parsable to an int then it is a float
                return new FloatNode(Float.parseFloat(L.get().getValue()));
            }
        } else if((L = tokens.matchAndRemove(Token.TokenType.WORD)).isPresent()) { //the number is a variable
            return new VariableNode(L.get().getValue());
        } else if ((L = tokens.matchAndRemove(Token.TokenType.LPAREN)).isPresent()) {
            var expression = expression();
            if ((L = tokens.matchAndRemove(Token.TokenType.RPAREN)).isEmpty()) {
                throw new ArithmeticException("No ending parenthesis");
            }
            return expression;
        } else {
            throw new ArithmeticException("Not a number");
        }
    }

    //Parses the AST tree
    public StatementListNode parse() {
        return statementList();
    }
}