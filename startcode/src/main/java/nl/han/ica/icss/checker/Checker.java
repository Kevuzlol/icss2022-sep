package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;

public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        variableTypes.addFirst(new HashMap<>()); // start with global scope
        checkNode(ast.root);
    }

    private void checkNode(ASTNode node) {

        // --- open a new scope for Stylerule, IfClause or ElseClause ---
        boolean opensScope = node instanceof Stylerule || node instanceof IfClause || node instanceof ElseClause;
        if (opensScope) {
            variableTypes.addFirst(new HashMap<>());
        }

        // Variable assignment -> store its type in current scope
        if (node instanceof VariableAssignment) {
            VariableAssignment assignment = (VariableAssignment) node;
            ExpressionType type = inferType(assignment.expression);
            variableTypes.getFirst().put(assignment.name.name, type);
        }

        // --- Variable reference check ---
        if (node instanceof VariableReference) {
            VariableReference reference = (VariableReference) node;
            if (!isDefinedInCurrentScopes(reference.name)) {
                reference.setError("Variable '" + reference.name + "' is not defined in current scope.");
            }
        }

        // If-clause: condition must be boolean
        if (node instanceof IfClause) {
            IfClause ifNode = (IfClause) node;
            ExpressionType condType = inferType(ifNode.conditionalExpression);
            if (condType != ExpressionType.BOOL) ifNode.setError("If-clause condition must be boolean.");
        }

        // --- check operations ---
        if (node instanceof Operation) {
            Operation op = (Operation) node;
            ExpressionType leftType = inferType(op.lhs);
            ExpressionType rightType = inferType(op.rhs);

            // + and - must have same type on both sides
            if ((op instanceof AddOperation || op instanceof SubtractOperation) && leftType != rightType) {
                op.setError("Operands of + or - must be the same type");
            }

            // * requires at least one SCALAR
            if (op instanceof MultiplyOperation && leftType != ExpressionType.SCALAR && rightType != ExpressionType.SCALAR) {
                op.setError("At least one operand of * must be a scalar");
            }

            // Colors cannot be in operations
            if (leftType == ExpressionType.COLOR || rightType == ExpressionType.COLOR) {
                op.setError("Colors cannot be used in operations");
            }
        }

        // --- type checks for declarations ---
        if (node instanceof Declaration) {
            Declaration decl = (Declaration) node;
            String property = decl.property.name.toLowerCase();
            ExpressionType valueType = inferType(decl.expression);

            switch (property) {
                case "color":
                case "background-color":
                    if (valueType != ExpressionType.COLOR) {
                        decl.setError("Property '" + property + "' requires a COLOR.");
                    }
                    break;
                case "width":
                case "height":
                case "margin":
                case "padding":
                case "top":
                case "left":
                    if (valueType != ExpressionType.PIXEL && valueType != ExpressionType.PERCENTAGE) {
                        decl.setError("Property '" + property + "' must be PIXEL or PERCENTAGE.");
                    }
                    break;
            }
        }

        // Go through all children recursively
        for (ASTNode child : node.getChildren()) {
            checkNode(child);
        }

        // Close the scope if we opened one
        if (opensScope) {
            variableTypes.removeFirst();
        }
    }

    // check if a variable is defined in any visible scope
    private boolean isDefinedInCurrentScopes(String name) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            if (variableTypes.get(i).containsKey(name)) return true;
        }
        return false;
    }

    // infer type of an expression
    private ExpressionType inferType(Expression expr) {
        if (expr instanceof ColorLiteral) return ExpressionType.COLOR;
        if (expr instanceof PixelLiteral) return ExpressionType.PIXEL;
        if (expr instanceof PercentageLiteral) return ExpressionType.PERCENTAGE;
        if (expr instanceof ScalarLiteral) return ExpressionType.SCALAR;
        if (expr instanceof BoolLiteral) return ExpressionType.BOOL;

        if (expr instanceof VariableReference) {
            String name = ((VariableReference) expr).name;
            for (int i = 0; i < variableTypes.getSize(); i++) {
                if (variableTypes.get(i).containsKey(name)) return variableTypes.get(i).get(name);
            }
        }

        // --- handle operations ---
        if (expr instanceof Operation) {
            Operation op = (Operation) expr;
            ExpressionType leftType = inferType(op.lhs);
            ExpressionType rightType = inferType(op.rhs);

            if (op instanceof MultiplyOperation) {
                // multiplication: if one side is SCALAR, return the other type
                if (leftType == ExpressionType.SCALAR) return rightType;
                if (rightType == ExpressionType.SCALAR) return leftType;
            }

            // + or -: both sides must be same type, return that type
            if (op instanceof AddOperation || op instanceof SubtractOperation) {
                if (leftType == rightType) return leftType;
            }
        }

        return ExpressionType.UNDEFINED; // couldn't figure it out
    }
}
