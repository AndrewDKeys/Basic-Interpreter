package Parser.Node;

//Parser.Node.Parser.Node that holds assignments: i.e. x = 1
public class AssignmentNode extends StatementNode {

    //the assignee
    private final VariableNode variable;

    //the assignment
    //Should either be a Parser.Node.VariableNode or a Parser.Node.Parser.Node.MathOpNode for now
    private final Node expression;

    public AssignmentNode(VariableNode variable, Node expression) {
        this.variable = variable;
        this.expression = expression;
    }

    public VariableNode getVariable() {
        return variable;
    }

    public Node getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return variable.toString() + " EQUALS " + expression.toString();
    }
}
