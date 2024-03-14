package Lexer;

public class Token {

    private String value;
    private TokenType type;
    private final int lineNum; // Line in which the token occurred
    private final int charPosition; // Position in the line where the token began

    public enum TokenType {
        WORD, NUMBER, ENDOFLINE, PRINT, READ, INPUT, DATA, GOSUB, FOR, LABEL,
        TO, STEP, NEXT, RETURN, IF, THEN, FUNCTION, WHILE, END, STRINGLITERAL,
        LESSTHANEQUALS, GREATERTHANEQUALS, NOTEQUALS, LESSTHAN, GREATERTHAN,
        EQUALS, LPAREN, RPAREN, ADD, SUBTRACT, MULTIPLY, DIVIDE, COMMA
    }

    public Token(TokenType type, int lineNum, int charPosition, String value) {
        this.type = type;
        this.value = value;
        this.lineNum = lineNum;
        this.charPosition = charPosition;
    }

    public Token(TokenType type, int lineNum, int charPosition) {
        this.type = type;
        value = null; // Tokens like ENDOFLINE don't need a value
        this.lineNum = lineNum;
        this.charPosition = charPosition;
    }

    public String getValue() {return value;}

    public TokenType getType(){
        return type;
    }

    @Override
    public String toString() {
        return type + (value != null ? "(" + value + ") " : " ");
    }
}