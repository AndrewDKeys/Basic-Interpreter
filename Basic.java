import Interpreter.Interpreter;
import Lexer.Lexer;
import Parser.Parser;

public class Basic {
    public static void main(String[] args) throws Exception {
        // We only are allowing 1 BASIC document to be lexed, therefore anymore or less should throw an error
        if(args.length != 1)
            throw new IllegalArgumentException("Incorrect amount of arguments");

        var lexer = new Lexer();
        var list = lexer.lex(args[0]);
        for(var i: list)
            System.out.print(i);
        System.out.println();

        var parse = new Parser(list).parse();
        System.out.println(parse);

        var interpreter = new Interpreter(parse);
        System.out.println(interpreter);
    }
}
