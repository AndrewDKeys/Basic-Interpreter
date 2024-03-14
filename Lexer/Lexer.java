package Lexer;

import java.util.HashMap;
import java.util.LinkedList;
import java.lang.*;

public class Lexer {

    // Current line in the file we are in, starts at 0.
    private int lineNum;
    // Current char position in a given line, NOT the entire file
    private int charPosition;
    private char next;
    private final LinkedList<Token> tokenList;
    // Every known key word or symbol in this version of BASIC
    private final HashMap<String, Token.TokenType> knownWords;
    // Symbols like '>' '<' '+'
    private final HashMap<Character, Token.TokenType> oneCharSymbols;
    // Symbols like '>=' '<=' '<>'
    private final HashMap<String, Token.TokenType> twoCharSymbols;

    public Lexer() {
        lineNum = 0;
        charPosition = 0;
        tokenList = new LinkedList<>();
        knownWords = fillKnownWords();
        oneCharSymbols = fillOneCharSymbols();
        twoCharSymbols = fillTwoCharSymbols();
    }

    public LinkedList<Token> lex(String fileName) throws Exception {
        var file = new CodeHandler(fileName);
        next = file.peek(0); // 0 will always be the first character within a file
        while(!file.isDone()) {
            switch(next) {
                case ' ', '\t' -> {
                    charPosition++;
                    file.swallow(1);
                }
                case '\r' -> file.swallow(1);
                case '\n' -> { // lineNum needs to be incremented, and charPosition needs to be reset to 0
                    tokenList.add(new Token(Token.TokenType.ENDOFLINE, lineNum, charPosition));
                    lineNum++;
                    charPosition = 0;
                    file.swallow(1);
                }

                case '_', '$', '%' -> tokenList.add(processWord(file)); // Words in BASIC can have these given characters in them
                case '\0' -> {return tokenList;} // Case for when end of file is reached
                case '"' -> tokenList.add(handleStringLiteral(file)); // Case for StringLiteral
                default -> {
                    if (Character.isLetter(next)) {
                        tokenList.add(processWord(file));
                    } else if (Character.isDigit(next) || next == '.') { // Numbers can have a decimal in them
                        tokenList.add(processNumber(file));
                    } else if (oneCharSymbols.containsKey(next)) { // Next is the beginning of a symbol
                        tokenList.add(handleSymbol(file));
                    } else { // Error isn't thrown to ensure that lexer finishes
                        System.out.println("Unrecognized char at line " + lineNum
                                + ", position " + (charPosition) + ": " + next);
                        file.swallow(1);
                        charPosition++;
                    }
                }
            }
            // Next is being incremented last or else we will miss the first character in the file. Since getChar
            // moves forward the fingerPosition we don't need to peek anything but 1 ahead.
            next = file.peek(1);
        }
        return tokenList;
    }

    private Token processWord(CodeHandler file) {
        StringBuilder value = new StringBuilder();

        // This is a base case for the first token, the pre increment in the Lexer.Lexer.CodeHandler causes the first character of the
        // file to get glossed over, but changing it to post increment breaks more of the code.
        if(tokenList.isEmpty())
            value.append(next);

        boolean completeWord = false;
        while(!completeWord) {
            next = file.peek(1);
            if(next == '%' || next == '$') { // Words have to end when one of these is encountered.
                value.append(file.getChar());
                charPosition++;
                completeWord = true;
            } else if(next == ':') { // Labels will always end with a colon
                value.append(file.getChar());
                charPosition++;
                return new Token(Token.TokenType.LABEL, lineNum, charPosition - value.length(), value.toString());
            } else if (next == '_' || Character.isLetter(next) || Character.isDigit(next)) {
                value.append(file.getChar());
                charPosition++;
            } else { // Anything that doesn't belong in a word in BASIC
                completeWord = true;
            }
        }

        if(knownWords.containsKey(value.toString().toLowerCase())) {
            return new Token(knownWords.get(value.toString().toLowerCase()), lineNum, charPosition - value.length());
        } else {
            return new Token(Token.TokenType.WORD, lineNum, charPosition - value.length(), value.toString());
        }
    }

    private Token processNumber(CodeHandler file) {
        StringBuilder number = new StringBuilder();

        // This is a base case for the first token, the pre increment in the Lexer.Lexer.CodeHandler causes the first character of the
        // file to get glossed over, but changing it to post increment breaks more of the code.
        if(tokenList.isEmpty())
            number.append(next);

        boolean decimalOccurred = false; //Only one decimal can occur in a given number
        boolean completeNumber = false;
        while(!completeNumber) {
            char next = file.peek(1);
            if (Character.isDigit(next)) {
                number.append(file.getChar());
                charPosition++;
            } else if (next == '.' && !decimalOccurred) {
                number.append(file.getChar());
                decimalOccurred = true;
                charPosition++;
            } else { //Anything that doesn't belong in a BASIC number or a second decimal occurred
                completeNumber = true;
            }
        }
        return new Token(Token.TokenType.NUMBER, lineNum, charPosition - number.length(), number.toString());
    }

    private Token handleSymbol(CodeHandler file) {
        char symbol = file.getChar(); // We always append the first char because it is guaranteed to be a part of the symbol
        charPosition++;

        char checkNext = file.peek(1);
        if(twoCharSymbols.containsKey(symbol + "" + checkNext)) { // The string concatenation of the two chars
            charPosition++;
            file.swallow(1); //swallow the second part of the symbol because we are already going to add it
            return new Token(twoCharSymbols.get(symbol + "" + checkNext), lineNum, charPosition - 2);
        } else {
            return new Token(oneCharSymbols.get(symbol), lineNum, charPosition - 1);
        }
    }

    private Token handleStringLiteral(CodeHandler file) {
        StringBuilder literal = new StringBuilder();
        file.swallow(1); //We won't include the declarative " for the String literal -> "quote"
        charPosition++;

        next = file.peek(1);
        while(next != '"') {
            if(next == '\\') { //When there is an escaped character
                file.swallow(1); //Ignore the \
                literal.append(file.getChar()); //Append the escaped character
                charPosition += 2;
            } else {
                literal.append(file.getChar());
                charPosition++;
            }
            next = file.peek(1);
        }
        file.swallow(1); //We won't include the final " for the String literal "quote" <-

        return new Token(Token.TokenType.STRINGLITERAL, lineNum, charPosition - literal.length(), literal.toString());
    }

    private HashMap<String, Token.TokenType> fillKnownWords() {
        var m = new HashMap<String, Token.TokenType>();
        m.put("print", Token.TokenType.PRINT);
        m.put("read", Token.TokenType.READ);
        m.put("input", Token.TokenType.INPUT);
        m.put("data", Token.TokenType.DATA);
        m.put("gosub", Token.TokenType.GOSUB);
        m.put("for", Token.TokenType.FOR);
        m.put("to", Token.TokenType.TO);
        m.put("then", Token.TokenType.THEN);
        m.put("step", Token.TokenType.STEP);
        m.put("next", Token.TokenType.NEXT);
        m.put("return", Token.TokenType.RETURN);
        m.put("if", Token.TokenType.IF);
        m.put("function", Token.TokenType.FUNCTION);
        m.put("while", Token.TokenType.WHILE);
        m.put("end", Token.TokenType.END);
        return m;
    }

    private HashMap<String, Token.TokenType> fillTwoCharSymbols() {
        var m = new HashMap<String, Token.TokenType>();
        m.put(">=", Token.TokenType.GREATERTHANEQUALS);
        m.put("<=", Token.TokenType.LESSTHANEQUALS);
        m.put("<>", Token.TokenType.NOTEQUALS);
        return m;
    }

    private HashMap<Character, Token.TokenType> fillOneCharSymbols() {
        var m = new HashMap<Character, Token.TokenType>();
        m.put('>', Token.TokenType.GREATERTHAN);
        m.put('<', Token.TokenType.LESSTHAN);
        m.put('(', Token.TokenType.LPAREN);
        m.put(')', Token.TokenType.RPAREN);
        m.put('+', Token.TokenType.ADD);
        m.put('-', Token.TokenType.SUBTRACT);
        m.put('*', Token.TokenType.MULTIPLY);
        m.put('/', Token.TokenType.DIVIDE);
        m.put('=', Token.TokenType.EQUALS);
        m.put(',', Token.TokenType.COMMA);
        return m;
    }
}