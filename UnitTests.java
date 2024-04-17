import static org.junit.Assert.*;

import Interpreter.Interpreter;
import Lexer.*;
import Parser.*;
import Parser.Node.*;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class UnitTests {

    @Test
    public void testCodeHandler() throws IOException {
        File f = createFile("This is the Lexer\n1 < 1.5");
        CodeHandler c = new CodeHandler(f.getName());

        //Testing for peek
        assertEquals('T', c.peek(0));
        assertEquals('h', c.peek(1));
        assertEquals('i', c.peek(2));
        assertEquals('s', c.peek(3));
        assertEquals(' ', c.peek(4));

        //Testing for getChar and swallow
        assertEquals('h', c.getChar());
        c.swallow(1);
        assertEquals('s', c.getChar());
        assertEquals(' ', c.getChar());

        //Testing isDone
        assertFalse(c.isDone());

        //Testing for peekString
        assertEquals("is the Lexer", c.peekString(12));

        //Testing swallow
        c.swallow(12);
        assertEquals('\n', c.getChar());
        c.swallow(1);

        //Testing remainder
        assertEquals("1 < 1.5", c.remainder());

        //Testing isDone
        c.swallow(7);
        assertTrue(c.isDone());
    }

    @Test
    public void testLexer1() throws Exception {
        File f = createFile("This is the Lexer\n1.3.3 is not a number\nfirst$second%last");
        LinkedList<Token> l = new Lexer().lex(f.getName());

        //Testing for correct amount of tokens
        assertEquals(15, l.size());

        //Testing basic output and ENDOFLINE
        assertEquals("WORD(This) ", l.get(0).toString());
        assertEquals("WORD(is) ", l.get(1).toString());
        assertEquals("WORD(the) ", l.get(2).toString());
        assertEquals("WORD(Lexer) ", l.get(3).toString());
        assertEquals("ENDOFLINE ", l.get(4).toString());

        //Testing double decimal inputs
        assertEquals("NUMBER(1.3) ", l.get(5).toString());
        assertEquals("NUMBER(.3) ", l.get(6).toString());

        //Testing weird words
        assertEquals("ENDOFLINE ", l.get(11).toString());
        assertEquals("WORD(first$) ", l.get(12).toString());
        assertEquals("WORD(second%) ", l.get(13).toString());
        assertEquals("WORD(last) ", l.get(14).toString());
    }

    @Test
    public void testLexer2() throws Exception {
        File f = createFile("phrase = \"This is a string literal and \\\"this is a quote\\\"\"\n" +
                                         "2 > 1 IF 10 <> 9 THEN function\n" +
                                         "label: 2/2 = 1");
        LinkedList<Token> l = new Lexer().lex(f.getName());

        //reaffirming length of list
        assertEquals(20, l.size());

        //Testing StringLiteral and escaped characters
        assertEquals("STRINGLITERAL(This is a string literal and \"this is a quote\") ", l.get(2).toString());

        //Testing one and two char symbols
        assertEquals("GREATERTHAN ", l.get(5).toString());
        assertEquals("NOTEQUALS ", l.get(9).toString());

        //Testing known words
        assertEquals("IF ", l.get(7).toString());
        assertEquals("THEN ", l.get(11).toString());
        assertEquals("FUNCTION ", l.get(12).toString());

        //Testing label
        assertEquals("LABEL(label:) ", l.get(14).toString());
    }

    @Test
    public void testLexerFunctions() throws Exception {
        File f = createFile("RANDOM()\nmid$(x$, 3, 10)");
        LinkedList<Token> l = new Lexer().lex(f.getName());

        //Testing function lex-ability
        assertEquals("FUNCTION(RANDOM) ", l.get(0).toString());
        assertEquals("RANDOM", l.get(0).getValue());
        assertEquals(12, l.size());
    }

    @Test
    public void testTokenHandler() throws Exception {
        File f = createFile("1 + 2 * 3");
        var t = new TokenHandler(new Lexer().lex(f.getName()));

        //Testing peek() from Parser.TokenHandler
        assertEquals("NUMBER(1) ", t.peek(0).get().toString());
        assertEquals("ADD ", t.peek(1).get().toString());
        assertEquals("NUMBER(2) ", t.peek(2).get().toString());
        assertEquals("MULTIPLY ", t.peek(3).get().toString());
        assertEquals("NUMBER(3) ", t.peek(4).get().toString());

        //Testing moreTokens
        assertTrue(t.moreTokens());

        //Testing matchAndRemove
        assertEquals("NUMBER(1) ", t.matchAndRemove(Token.TokenType.NUMBER).get().toString());
        assertEquals("ADD ", t.matchAndRemove(Token.TokenType.ADD).get().toString());
        assertFalse(t.matchAndRemove(Token.TokenType.EQUALS).isPresent());
        assertEquals("NUMBER(2) ", t.matchAndRemove(Token.TokenType.NUMBER).get().toString());
        assertEquals("MULTIPLY ", t.matchAndRemove(Token.TokenType.MULTIPLY).get().toString());
        assertEquals("NUMBER(3) ", t.matchAndRemove(Token.TokenType.NUMBER).get().toString());

        //Testing moreTokens
        assertFalse(t.moreTokens());
    }

    @Test
    public void testParserExpressions() throws Exception {
        File f = createFile("x=3/7\ny=x\nz=2.34\n\n\n\n\n\nPRINT x,y\nPRINT z");
        var t = new Parser(new Lexer().lex(f.getName())).parse();

        //testing parse and acceptSeparators and statements
        assertEquals("x EQUALS Parser.Node.Parser.Node.MathOpNode(DIVIDE, 3, 7) y EQUALS x z EQUALS 2.34 PRINT([x, y])" +
                    " PRINT([z]) ", t.toString());
    }

    @Test
    public void testParserData() throws Exception {
        File f = createFile("DATA \"banana fish\", 17, 23.0091");
        var t = new Parser(new Lexer().lex(f.getName())).parse();

        assertEquals("Parser.Node.Parser.Node.DataNode([Parser.Node.StringNode(banana fish), 17, 23.0091]) ", t.toString());
    }

    @Test
    public void testParserRead() throws Exception {
        File f = createFile("READ string$, int, num%");
        var t = new Parser(new Lexer().lex(f.getName())).parse();

        assertEquals("Parser.Node.ReadNode([string$, int, num%]) ", t.toString());
    }

    @Test
    public void testParserInput() throws Exception {
        File f = createFile("INPUT \"Please Input\", y, z\nINPUT x, \"y\", z");
        var t = new Parser(new Lexer().lex(f.getName())).parse();

        assertEquals(1, t.getList().size()); //Checks to see if second INPUT statement doesn't parse
        assertEquals("Parser.Node.Parser.Node.InputNode([Parser.Node.StringNode(Please Input), y, z]) ", t.toString());
    }

    @Test
    public void testParserLabels() throws Exception {
        File f = createFile("test: y = x + 3\nRETURN\nGOSUB test\nRETURN 7");
        var t = new Parser(new Lexer().lex(f.getName())).parse();

        //Testing to see if RETURN 7 doesn't parse
        assertEquals(3, t.getList().size());

        //Testing to see if labels work
        assertEquals("LABEL(test:, y EQUALS Parser.Node.Parser.Node.MathOpNode(ADD, x, 3))" , t.getList().get(0).toString());
        assertEquals("RETURN", t.getList().get(1).toString());
        assertEquals("GOSUB(test)", t.getList().get(2).toString());
    }

    @Test
    public void testParserFor() throws Exception{
        File f = createFile("FOR i = 0 TO 100 STEP 10\nNEXT\nFOR j = 0 TO 100\nNEXT\nEND");
        var t = new Parser(new Lexer().lex(f.getName())).parse();

        // Testing to see if everything parses
        assertEquals(5, t.getList().size());

        //Testing For
        assertEquals("FOR(i EQUALS 0 TO 100, 10)", t.getList().get(0).toString());

        //Testing Next
        assertTrue(t.getList().get(1) instanceof NextNode);

        //Testing to see if default increment is 1
        assertEquals("FOR(j EQUALS 0 TO 100, 1)", t.getList().get(2).toString());

        //Testing End
        assertTrue(t.getList().get(4) instanceof EndNode);
    }

    @Test
    public void testParserIf() throws Exception {
        File f = createFile("IF x < 7 THEN label\nIF x <> 10 then label\nIF x >= 20 THEN label\nIF x <= 2");
        var t = new Parser(new Lexer().lex(f.getName())).parse();

        // Testing to see if last line doesn't parse
        assertEquals(3, t.getList().size());

        assertEquals("IF(x LESSTHAN 7 THEN label)", t.getList().get(0).toString());
        assertTrue(t.getList().get(1) instanceof IfNode);
        assertEquals("IF(x GREATERTHANEQUALS 20 THEN label)", t.getList().get(2).toString());
    }

    @Test
    public void testParserWhile() throws Exception {
        File f = createFile("WHILE x < 5 endWhileLabel\nWHILE x <> 3");
        var t = new Parser(new Lexer().lex(f.getName())).parse();

        //Checking to see if final line doesn't parse
        assertEquals(1, t.getList().size());

        //Checking for correct parse
        assertEquals("WHILE(x LESSTHAN 5: endWhileLabel)", t.getList().get(0).toString());
    }

    @Test
    public void testParserFunction() throws Exception {
        File f = createFile("x = mid$(word$, num1, 2)\ny = random()\nx = 2 + random()");
        var t = new Parser(new Lexer().lex(f.getName())).parse();

        //testing that everything parsed correctly
        assertEquals(3, t.getList().size());

        //if the smallest and the biggest functions parse then the rest will also
        assertEquals("x EQUALS mid$([word$, num1, 2])", t.getList().get(0).toString());
        assertEquals("y EQUALS random([])", t.getList().get(1).toString());
        assertEquals("x EQUALS Parser.Node.Parser.Node.MathOpNode(ADD, 2, random([]))", t.getList().get(2).toString());
    }


    @Test
    public void testInterpreterFunctions() throws Exception {
        //testing left-function
        assertEquals("test" ,Interpreter.left("testing", 4));

        //testing right-function
        assertEquals("ting" ,Interpreter.right("testing", 4));

        //testing mid-function
        assertEquals("esti", Interpreter.mid("testing", 1, 5));

        //testing num
        assertEquals("3", Interpreter.num(3));
        assertEquals("3.002", Interpreter.num(3.002f));

        //testing numVal
        assertEquals(3, Interpreter.intVal("3"));
        assertEquals(3.002, Interpreter.floatVal("3.002"), 0.05);
    }

    @Test
    public void testInterpreterInput() throws Exception {
        File f = createFile(" ");
        var t = new Interpreter(new Parser(new Lexer().lex(f.getName())).parse());
        var inputList = createInputList();

        t.evaluateInput(new InputNode(createInputNode()), inputList);

        assertEquals(0, inputList.size()); //If evaluateInput works correctly, all elements should have been removed and added to a hash map
    }

    @Test
    public void testInterpreterPrint() throws Exception {
        File f = createFile("sum = 0\nfloat% = 1.1\nPrint sum, float%");
        var t = new Interpreter(new Parser(new Lexer().lex(f.getName())).parse());

        var printed = t.evaluatePrint(new PrintNode(createInputList()), true); //boolean indicates that we are in test mode

        assertEquals(2, printed.size());
    }

    public List<Node> createInputNode() {
        var inputList = new LinkedList<Node>();

        inputList.add(new StringNode("Please Input: "));
        inputList.add(new VariableNode("integer"));
        inputList.add(new VariableNode("float%"));
        inputList.add(new VariableNode("string$"));

        return inputList;
    }

    public LinkedList<Node> createInputList() {
        var inputList = new LinkedList<Node>();

        inputList.add(new StringNode("Please Input: "));
        inputList.add(new IntegerNode(1));
        inputList.add(new FloatNode(1.1f));
        inputList.add(new StringNode("Banana Fish"));

        return inputList;
    }

    public File createFile(String fileContent) throws IOException {
        File f = new File("testData.txt");
        FileWriter w = new FileWriter(f.getName());
        w.write(fileContent);
        w.close();
        return f;
    }

}
