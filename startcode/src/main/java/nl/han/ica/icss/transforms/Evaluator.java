package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableScopes = new HANLinkedList<>();

    @Override
    public void apply(AST ast) {
        Stylesheet stylesheet = ast.root;
        variableScopes.addFirst(new HashMap<>());
        ArrayList<ASTNode> newChildren = new ArrayList<>();

        for (ASTNode node : stylesheet.getChildren()) {
            if (node instanceof VariableAssignment) {
                handleVariableAssignment((VariableAssignment) node);
            } else if (node instanceof Stylerule) {
                handleStylerule((Stylerule) node);
                newChildren.add(node);
            }
        }

        variableScopes.removeFirst();
        stylesheet.body = newChildren;
    }

    private void handleVariableAssignment(VariableAssignment var) {
        Literal value = evaluateExpression(var.expression);
        var.expression = value;
        variableScopes.getFirst().put(var.name.name, value);
    }

    private void handleStylerule(Stylerule rule) {
        variableScopes.addFirst(new HashMap<>());

        ArrayList<ASTNode> newBody = new ArrayList<>();
        for (ASTNode node : rule.body) {
            if (node instanceof VariableAssignment) {
                handleVariableAssignment((VariableAssignment) node);
            } else if (node instanceof Declaration) {
                Declaration decl = (Declaration) node;
                decl.expression = evaluateExpression(decl.expression);
                newBody.add(decl);
            } else if (node instanceof IfClause) {
                handleIfClause((IfClause) node, newBody);
            }
        }
        rule.body = newBody;
        variableScopes.removeFirst();
    }

    private void handleIfClause(IfClause clause, ArrayList<ASTNode> target) {
        BoolLiteral condition = (BoolLiteral) evaluateExpression(clause.conditionalExpression);

        ArrayList<ASTNode> active = condition.value ? clause.body :
                (clause.elseClause != null ? clause.elseClause.body : new ArrayList<>());

        for (ASTNode node : active) {
            if (node instanceof Declaration) {
                Declaration decl = (Declaration) node;
                decl.expression = evaluateExpression(decl.expression);
                target.add(decl);
            } else if (node instanceof VariableAssignment) {
                handleVariableAssignment((VariableAssignment) node);
            }
        }
    }

    private Literal evaluateExpression(Expression expr) {
        if (expr instanceof Literal) return (Literal) expr;
        if (expr instanceof VariableReference) return resolveVariable(((VariableReference) expr).name);
        if (expr instanceof Operation) return evaluateOperation((Operation) expr);
        return null;
    }

    private Literal resolveVariable(String name) {
        for (int i = 0; i < variableScopes.getSize(); i++) {
            HashMap<String, Literal> scope = variableScopes.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

    private Literal evaluateOperation(Operation op) {
        Literal left = evaluateExpression(op.lhs);
        Literal right = evaluateExpression(op.rhs);

        int l = getValue(left);
        int r = getValue(right);

        if (op instanceof AddOperation) {
            return copyLiteral(left, l + r);
        }
        if (op instanceof SubtractOperation) {
            return copyLiteral(left, l - r);
        }
        if (op instanceof MultiplyOperation) {
            // Als één operand scalar is → neem het type van de andere
            if (left instanceof ScalarLiteral) {
                return copyLiteral(right, l * r);
            } else {
                return copyLiteral(left, l * r);
            }
        }

        return null;
    }
    private int getValue(Literal lit) {
        if (lit instanceof PixelLiteral) {
            return ((PixelLiteral) lit).value;
        } else if (lit instanceof PercentageLiteral) {
            return ((PercentageLiteral) lit).value;
        } else {
            return ((ScalarLiteral) lit).value;
        }
    }

    private Literal copyLiteral(Literal type, int value) {
        if (type instanceof PixelLiteral) {
            return new PixelLiteral(value);
        } else if (type instanceof PercentageLiteral) {
            return new PercentageLiteral(value);
        } else {
            return new ScalarLiteral(value);
        }
    }
}
