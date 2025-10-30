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
        variableTypes.addFirst(new HashMap<>());
        checkNode(ast.root);
    }

    private void checkNode(ASTNode node) {

        // --- nieuwe scope openen bij Stylerule, IfClause of ElseClause ---
        boolean opensScope = node instanceof Stylerule || node instanceof IfClause || node instanceof ElseClause;
        if (opensScope) {
            variableTypes.addFirst(new HashMap<>());
        }

        // Variabele declaratie -> type opslaan in huidige scope
        if (node instanceof VariableAssignment) {
            VariableAssignment assignment = (VariableAssignment) node;
            ExpressionType type = inferType(assignment.expression);
            variableTypes.getFirst().put(assignment.name.name, type);
        }

        // --- Variabele referentie ---
        if (node instanceof VariableReference) {
            VariableReference reference = (VariableReference) node;
            if (!isDefinedInCurrentScopes(reference.name)) {
                reference.setError("Variable '" + reference.name + "' is not defined in current scope.");
            }
        }

        // If-clause conditie check
        if (node instanceof IfClause) {
            IfClause ifNode = (IfClause) node;
            if (!ifNode.getChildren().isEmpty()) {
                ASTNode condNode = ifNode.getChildren().get(0);
                if (condNode instanceof Expression) {
                    ExpressionType condType = inferType((Expression) condNode);
                    if (condType != ExpressionType.BOOL) {
                        ifNode.setError("If-clause condition must be a boolean.");
                    }
                }
            }
        }

        // --- check operaties ---
        if (node instanceof Operation) {
            Operation op = (Operation) node;
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

        // --- type controle op declaraties ---
        if (node instanceof Declaration) {
            Declaration decl = (Declaration) node;
            String property = decl.property.name;
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

        // Recursief door alle children
        for (ASTNode child : node.getChildren()) {
            checkNode(child);
        }

        // Scope sluiten
        if (opensScope) {
            variableTypes.removeFirst();
        }
    }

    // controleer variabele binnen zichtbare scopes
    private boolean isDefinedInCurrentScopes(String name) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            HashMap<String, ExpressionType> scope = variableTypes.get(i);
            if (scope.containsKey(name)) return true;
        }
        return false;
    }

    // Type bepaling voor variabele toewijzingen
    private ExpressionType inferType(Expression expr) {
        if (expr instanceof ColorLiteral) return ExpressionType.COLOR;
        if (expr instanceof PixelLiteral) return ExpressionType.PIXEL;
        if (expr instanceof PercentageLiteral) return ExpressionType.PERCENTAGE;
        if (expr instanceof ScalarLiteral) return ExpressionType.SCALAR;
        if (expr instanceof BoolLiteral) return ExpressionType.BOOL;

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
