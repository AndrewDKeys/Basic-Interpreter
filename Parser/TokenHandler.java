package Parser;

import Lexer.Token;

import java.util.LinkedList;
import java.util.Optional;

public class TokenHandler {

    private LinkedList<Token> tokenList;

    //Takes the output from the Lexer.Lexer lex() method
    public TokenHandler(LinkedList<Token> tokenList) {
        this.tokenList = tokenList;
    }

    public Optional<Token> peek(int i) {
        try {
            return Optional.ofNullable(tokenList.get(i));
        } catch(IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    public boolean moreTokens() {
        return !tokenList.isEmpty();
    }

    //Looks at the head of the list and removes the requested token if it is present, if not we return an empty
    //optional. This is to help prevent null pointer exceptions.
    public Optional<Token> matchAndRemove(Token.TokenType type){
        try {
            if (moreTokens() && type == tokenList.get(0).getType()) {
                return Optional.ofNullable(tokenList.remove(0));
            } else {
                return Optional.empty();
            }
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }
}
