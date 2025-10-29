package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;

public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        variableTypes.addFirst(new HashMap<>());

        checkNode(ast.root);
    }

    private void checkNode(ASTNode node) {

        if (node instanceof VariableAssignment) {
            VariableAssignment assignment = (VariableAssignment) node;
            ExpressionType type = inferType(assignment.expression);
            variableTypes.getFirst().put(assignment.name.name, type);
        }

        if (node instanceof VariableReference) {
            VariableReference reference = (VariableReference) node;
            if (!isDefined(reference.name)) {
                reference.setError("Variable '" + reference.name + "' is not defined.");
            }
        }

        if (node instanceof Stylerule || node instanceof IfClause) {
            variableTypes.addFirst(new HashMap<>());
        }

        for (ASTNode child : node.getChildren()) {
            checkNode(child);
        }

        if (node instanceof Stylerule || node instanceof IfClause) {
            variableTypes.removeFirst();
        }
    }

    private boolean isDefined(String name) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> scope = variableTypes.get(i);
            if (scope.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    private ExpressionType inferType(Expression expr) {

        if (expr instanceof ColorLiteral) return ExpressionType.COLOR;
        if (expr instanceof PixelLiteral) return ExpressionType.PIXEL;
        if (expr instanceof PercentageLiteral) return ExpressionType.PERCENTAGE;
        if (expr instanceof ScalarLiteral) return ExpressionType.SCALAR;

        if (expr instanceof VariableReference) {
            VariableReference reference = (VariableReference) expr;
            for (int i = 0; i < variableTypes.getSize(); i++) {
                HashMap<String, ExpressionType> scope = variableTypes.get(i);
                if (scope.containsKey(reference.name)) {
                    return scope.get(reference.name);
                }
            }
        }
        return ExpressionType.UNDEFINED;
    }
}
