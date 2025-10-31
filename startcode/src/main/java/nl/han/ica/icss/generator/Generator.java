package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;

public class Generator {

    public String generate(AST ast) {
        StringBuilder builder = new StringBuilder();
        Stylesheet stylesheet = ast.root;

        // Loop over all stylerules and turn them into CSS
        for (ASTNode node : stylesheet.getChildren()) {
            if (node instanceof Stylerule) {
                generateStylerule((Stylerule) node, builder);
            }
        }
        return builder.toString();
    }

    // Make a CSS block for a rule
    private void generateStylerule(Stylerule rule, StringBuilder builder) {
        // Selector like: p, #id, .class
        builder.append(rule.selectors.get(0).toString());
        builder.append(" {\n");

        for (ASTNode child : rule.body) {
            if (child instanceof Declaration) {
                generateDeclaration((Declaration) child, builder);
            }
        }

        builder.append("}\n\n"); // close the block
    }

    // Turn a declaration into CSS text
    private void generateDeclaration(Declaration decl, StringBuilder builder) {
        builder.append("  "); // indent 2 spaces
        builder.append(decl.property.name);
        builder.append(": ");
        builder.append(literalToString((Literal) decl.expression));
        builder.append(";\n"); // end with semicolon
    }

    // Convert a literal to string, so we can print it in CSS
    private String literalToString(Literal literal) {
        if (literal instanceof PixelLiteral) return ((PixelLiteral) literal).value + "px";
        if (literal instanceof PercentageLiteral) return ((PercentageLiteral) literal).value + "%";
        if (literal instanceof ColorLiteral) return ((ColorLiteral) literal).value;
        if (literal instanceof BoolLiteral) return ((BoolLiteral) literal).value ? "true" : "false";
        return Integer.toString(((ScalarLiteral) literal).value);
    }
}
