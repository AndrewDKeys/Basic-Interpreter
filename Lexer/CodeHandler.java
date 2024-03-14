package Lexer;

import java.io.*;
import java.nio.file.*;

public class CodeHandler {
    // Holds a string representation of the contents of a file
    private String file;

    // The number of characters into the file we are
    private int fingerPosition;

    // Error is thrown when an invalid path is given or any other IOException occurs
    public CodeHandler(String fileName) throws IOException {
        try{
            Path filePath = Path.of(fileName);
            file = new String(Files.readAllBytes(filePath));
            fingerPosition = 0;
        } catch(Exception e) {
            throw new IOException("Error Reading File");
        }
    }

    // Peeks at the i-th character ahead of the fingerPosition, if we reach the end of the file the null character is
    // returned.
    public char peek(int i){
        try {
            return file.charAt(fingerPosition + i);
        } catch(IndexOutOfBoundsException e) {
            return '\0';
        }
    }

    // The second argument is i + 1 since the substring method has exclusive bounds.
    public String peekString(int i){
        return file.substring(fingerPosition + 1, fingerPosition + i + 1);
    }

    // Pre incrementation ensures every value in the token is correct, except for the first token. This issue is handled
    // in the ProcessWord/Number methods in the lexer, be warned in changing it to a post increment...
    public char getChar(){
        return file.charAt(++fingerPosition);
    }

    public void swallow(int i){
        fingerPosition += i;
    }

    public boolean isDone(){
        return fingerPosition == file.length();
    }

    public String remainder() {
        return file.substring(fingerPosition);
    }
}
