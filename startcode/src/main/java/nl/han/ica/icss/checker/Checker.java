package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
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

        // Variabele declaratie -> type opslaan in huidige scope
        if (node instanceof VariableAssignment) {
            VariableAssignment assignment = (VariableAssignment) node;
            ExpressionType type = inferType(assignment.expression);
            variableTypes.getFirst().put(assignment.name.name, type);
        }

        // Variabele referentie -> controleren of die bestaat
        if (node instanceof VariableReference) {
            VariableReference reference = (VariableReference) node;
            if (!isDefined(reference.name)) {
                reference.setError("Variable '" + reference.name + "' is not defined.");
            }
        }

        // Nieuwe scope openen bij Stylerule of IfClause
        if (node instanceof Stylerule || node instanceof IfClause) {
            variableTypes.addFirst(new HashMap<>());
        }

        // --- check operaties ---
        if (node instanceof Operation) {
            Operation op = (Operation) node;

            // we verwachten 2 children: left en right
            if (op.getChildren().size() >= 2) {
                Expression left = (Expression) op.getChildren().get(0);
                Expression right = (Expression) op.getChildren().get(1);

                ExpressionType leftType = inferType(left);
                ExpressionType rightType = inferType(right);

                if (op instanceof AddOperation || op instanceof SubtractOperation) {
                    if (leftType != rightType) {
                        // types niet gelijk -> zet type op UNDEFINED
                        op.setError("Operands of + or - must be the same type");
                    }
                }

                if (op instanceof MultiplyOperation) {
                    if (leftType != ExpressionType.SCALAR && rightType != ExpressionType.SCALAR) {
                        // minstens een scalar
                        op.setError("At least one operand of * must be a scalar");
                    }
                }

                // geen kleuren in operaties
                if (leftType == ExpressionType.COLOR || rightType == ExpressionType.COLOR) {
                    op.setError("Colors cannot be used in operations");
                }
            }
        }

        // Recursief door alle children
        for (ASTNode child : node.getChildren()) {
            checkNode(child);
        }

        // Scope sluiten
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

    // Type bepaling voor variabele toewijzingen
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
