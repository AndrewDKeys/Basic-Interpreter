package Parser.Node;

//Blueprint for all statements: i.e. Assignments and Prints
public abstract class StatementNode extends Node {

    public abstract StatementNode next();

    public abstract void setNext(StatementNode next);

    @Override
    public abstract String toString();
}
