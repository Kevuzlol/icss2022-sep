package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import java.util.HashMap;

public class Evaluator implements Transform {

    // Stack of scopes, each scope maps variable names to their literal value
    private IHANLinkedList<HashMap<String, Literal>> variableScopes = new HANLinkedList<>();

    @Override
    public void apply(AST ast) {
        variableScopes.addFirst(new HashMap<>()); // put global scope on top
        evaluateStylesheet(ast.root);
    }

    // Go through the whole stylesheet and handle vars + rules
    private void evaluateStylesheet(Stylesheet stylesheet) {
        for (ASTNode node : stylesheet.getChildren()) {
            if (node instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) node);
            } else if (node instanceof Stylerule) {
                evaluateStylerule((Stylerule) node);
            }
        }
    }

    // Evaluate a variable, replace its expr with the literal, and store in scope
    private void evaluateVariableAssignment(VariableAssignment varAssign) {
        Literal value = evaluateExpression(varAssign.expression);
        if (value == null) throw new RuntimeException("Cannot evaluate variable: " + varAssign.name.name);
        varAssign.expression = value;

        // if var exists in any scope, update there, else put in current scope
        for (int i = 0; i < variableScopes.getSize(); i++) {
            if (variableScopes.get(i).containsKey(varAssign.name.name)) {
                variableScopes.get(i).put(varAssign.name.name, value);
                return;
            }
        }
        variableScopes.getFirst().put(varAssign.name.name, value);
    }

    // Handle rules, also check declarations, vars, and if/else inside
    private void evaluateStylerule(Stylerule rule) {
        variableScopes.addFirst(new HashMap<>()); // new scope for this rule
        HANLinkedList<ASTNode> newChildren = new HANLinkedList<>();
        int index = 0;

        for (ASTNode child : rule.getChildren()) {
            if (child instanceof Declaration) {
                Declaration decl = (Declaration) child;
                decl.expression = evaluateExpression(decl.expression); // evaluate expr
                newChildren.insert(index++, decl);
            } else if (child instanceof VariableAssignment) {
                evaluateVariableAssignment((VariableAssignment) child);
            } else if (child instanceof IfClause) {
                index = evaluateIfClause((IfClause) child, newChildren, index); // handle if/else
            }
        }

        // replace old children with new evaluated ones
        rule.getChildren().clear();
        for (int i = 0; i < newChildren.getSize(); i++) {
            rule.getChildren().add(newChildren.get(i));
        }
        variableScopes.removeFirst();
    }

    // Evaluate if/else blocks, replace with body of whichever branch is taken
    private int evaluateIfClause(IfClause ifClause, HANLinkedList<ASTNode> target, int index) {
        Literal cond = evaluateExpression(ifClause.conditionalExpression);
        if (!(cond instanceof BoolLiteral)) throw new RuntimeException("If condition must be boolean");

        variableScopes.addFirst(new HashMap<>()); // scope inside if/else

        if (((BoolLiteral) cond).value) {
            for (ASTNode node : ifClause.body) index = processIfNode(node, target, index);
        } else if (ifClause.elseClause != null) {
            for (ASTNode node : ifClause.elseClause.body) index = processIfNode(node, target, index);
        }

        variableScopes.removeFirst();
        return index;
    }

    // handle individual nodes inside if/else bodies
    private int processIfNode(ASTNode node, HANLinkedList<ASTNode> target, int index) {
        if (node instanceof Declaration) ((Declaration) node).expression = evaluateExpression(((Declaration) node).expression);
        if (node instanceof VariableAssignment) evaluateVariableAssignment((VariableAssignment) node);
        target.insert(index++, node);
        return index;
    }

    // recursively evaluate expressions, return Literal
    private Literal evaluateExpression(Expression expr) {
        if (expr instanceof Literal) return (Literal) expr; // already literal
        if (expr instanceof VariableReference) return resolveVariable(((VariableReference) expr).name);
        if (expr instanceof AddOperation || expr instanceof SubtractOperation || expr instanceof MultiplyOperation)
            return evaluateOperation((Operation) expr);
        return null;
    }

    // look up variable in all scopes
    private Literal resolveVariable(String name) {
        for (int i = 0; i < variableScopes.getSize(); i++) {
            HashMap<String, Literal> scope = variableScopes.get(i);
            if (scope.containsKey(name)) return scope.get(name);
        }
        throw new RuntimeException("Variable not defined: " + name);
    }

    // Evaluate +, -, *
    private Literal evaluateOperation(Operation op) {
        Literal left = evaluateExpression(op.lhs);
        Literal right = evaluateExpression(op.rhs);

        if (op instanceof AddOperation) return add(left, right);
        if (op instanceof SubtractOperation) return subtract(left, right);
        if (op instanceof MultiplyOperation) return multiply(left, right);

        return null;
    }

    private Literal add(Literal lhs, Literal rhs) {
        if (lhs.getClass() != rhs.getClass()) return null;
        if (lhs instanceof PixelLiteral) return new PixelLiteral(((PixelLiteral) lhs).value + ((PixelLiteral) rhs).value);
        if (lhs instanceof PercentageLiteral) return new PercentageLiteral(((PercentageLiteral) lhs).value + ((PercentageLiteral) rhs).value);
        if (lhs instanceof ScalarLiteral) return new ScalarLiteral(((ScalarLiteral) lhs).value + ((ScalarLiteral) rhs).value);
        return null;
    }

    private Literal subtract(Literal lhs, Literal rhs) {
        if (lhs.getClass() != rhs.getClass()) return null;
        if (lhs instanceof PixelLiteral) return new PixelLiteral(((PixelLiteral) lhs).value - ((PixelLiteral) rhs).value);
        if (lhs instanceof PercentageLiteral) return new PercentageLiteral(((PercentageLiteral) lhs).value - ((PercentageLiteral) rhs).value);
        if (lhs instanceof ScalarLiteral) return new ScalarLiteral(((ScalarLiteral) lhs).value - ((ScalarLiteral) rhs).value);
        return null;
    }

    private Literal multiply(Literal lhs, Literal rhs) {
        // Only allow scalar * other literal
        if (lhs instanceof ScalarLiteral && rhs instanceof PixelLiteral)
            return new PixelLiteral((int) (((ScalarLiteral) lhs).value * ((PixelLiteral) rhs).value));
        if (rhs instanceof ScalarLiteral && lhs instanceof PixelLiteral)
            return new PixelLiteral((int) (((ScalarLiteral) rhs).value * ((PixelLiteral) lhs).value));
        if (lhs instanceof ScalarLiteral && rhs instanceof PercentageLiteral)
            return new PercentageLiteral(((ScalarLiteral) lhs).value * ((PercentageLiteral) rhs).value);
        if (rhs instanceof ScalarLiteral && lhs instanceof PercentageLiteral)
            return new PercentageLiteral(((ScalarLiteral) rhs).value * ((PercentageLiteral) lhs).value);
        if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral)
            return new ScalarLiteral(((ScalarLiteral) lhs).value * ((ScalarLiteral) rhs).value);
        return null;
    }
}
