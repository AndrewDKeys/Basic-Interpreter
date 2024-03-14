import static org.junit.Assert.*;

import Lexer.*;
import Parser.*;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

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

    public File createFile(String fileContent) throws IOException {
        File f = new File("testData.txt");
        FileWriter w = new FileWriter(f.getName());
        w.write(fileContent);
        w.close();
        return f;
    }

}
