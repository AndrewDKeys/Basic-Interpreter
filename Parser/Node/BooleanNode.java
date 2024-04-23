package Parser.Node;

import Lexer.Token;

//Holds a boolean expression i.e. variable1 >= variable2
public class BooleanNode extends Node {

    private final Node left; //left of comparison

    private final Token.TokenType operator; //comparison operator

    private final Node right; //right of comparison

    public BooleanNode(Node left, Node right, Token.TokenType operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public Token.TokenType getOperator() {
        return operator;
    }

    public Node getRight() {
        return right;
    }

    public Node getLeft() {
        return left;
    }

    @Override
    public String toString() {
        return left.toString() + " " + operator + " " + right.toString();
    }
}
