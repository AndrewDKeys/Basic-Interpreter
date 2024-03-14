package Parser.Node;

import Lexer.*;

//AST node that hold mathematical operations
public class MathOpNode extends Node {

    //Should only hold ADD, MULTIPLY, DIVIDE, or SUBTRACT
    private Token.TokenType operation;

    //node left of the operation
    private Node left;

    //node right of the operation
    private Node right;

    public MathOpNode(Token.TokenType type, Node left, Node right) {
        operation = type;
        this.left = left;
        this.right = right;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "Parser.Node.Parser.Node.MathOpNode(" + operation + ", " + left + ", " + right + ")";
    }
}
