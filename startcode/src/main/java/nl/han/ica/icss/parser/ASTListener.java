package nl.han.ica.icss.parser;

import java.util.Stack;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {
	
	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> stack;

	public ASTListener() {
		ast = new AST();
		stack = new HANStack<>();

	}
    public AST getAST() {
        return ast;
    }

    @Override
    public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
        stack.push(new Stylesheet());
    }

    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        ast.setRoot((Stylesheet) stack.pop());
    }

    // --- Style rules ---
    @Override
    public void enterStylerule(ICSSParser.StyleruleContext ctx) {
        stack.push(new Stylerule());
    }

    @Override
    public void exitStylerule(ICSSParser.StyleruleContext ctx) {
        Stylerule stylerule = (Stylerule) stack.pop();
        ASTNode parent = stack.peek();
        if (parent != null) {
            parent.addChild(stylerule);
        }
    }

    // --- Selector ---
    @Override
    public void enterSelector(ICSSParser.SelectorContext ctx) {
        Selector selector = null;

        if (ctx.ID_IDENT() != null) {
            selector = new IdSelector(ctx.ID_IDENT().getText());
        } else if (ctx.CLASS_IDENT() != null) {
            selector = new ClassSelector(ctx.CLASS_IDENT().getText());
        } else if (ctx.LOWER_IDENT() != null) {
            selector = new TagSelector(ctx.LOWER_IDENT().getText());
        }

        if (selector != null) {
            ASTNode parent = stack.peek();
            if (parent instanceof Stylerule) {
                parent.addChild(selector);
            }
        }
    }

    // --- Variable Assignment ---
    @Override
    public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableAssignment assignment = new VariableAssignment();
        // Voeg de variabele naam toe
        assignment.name = new VariableReference(ctx.CAPITAL_IDENT().getText());
        stack.push(assignment);
    }

    @Override
    public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableAssignment assignment = (VariableAssignment) stack.pop();
        ASTNode parent = stack.peek();
        if (parent != null) {
            parent.addChild(assignment);
        }
    }

    // --- Declaration ---
    @Override
    public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
        Declaration declaration = new Declaration();
        // Voeg de property naam toe
        declaration.property = new PropertyName(ctx.LOWER_IDENT().getText());
        stack.push(declaration);
    }

    @Override
    public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
        Declaration declaration = (Declaration) stack.pop();
        ASTNode parent = stack.peek();
        if (parent != null) {
            parent.addChild(declaration);
        }
    }

    // --- Expression ---
    @Override
    public void enterExpression(ICSSParser.ExpressionContext ctx) {
        if (ctx.getChildCount() != 3) {
            return;
        }

        String operation = ctx.getChild(1).getText();
        if (operation.equals("+")) {
            stack.push(new AddOperation());
        } else if (operation.equals("-")) {
            stack.push(new SubtractOperation());
        } else if (operation.equals("*")) {
            stack.push(new MultiplyOperation());
        }
    }

    @Override
    public void exitExpression(ICSSParser.ExpressionContext ctx) {
        if (ctx.getChildCount() == 3) {
            // Dit is een operatie - pop de operatie en voeg toe aan parent
            Operation operation = (Operation) stack.pop();
            ASTNode parent = stack.peek();
            if (parent != null) {
                parent.addChild(operation);
            }
        }
        // Voor single values hoeven we niets te doen - die zijn al toegevoegd via enterValue
    }

    // --- Value ---
    @Override
    public void enterValue(ICSSParser.ValueContext ctx) {
        ASTNode node = null;

        if (ctx.COLOR() != null) {
            node = new ColorLiteral(ctx.COLOR().getText());
        } else if (ctx.PIXELSIZE() != null) {
            node = new PixelLiteral(ctx.PIXELSIZE().getText());
        } else if (ctx.PERCENTAGE() != null) {
            node = new PercentageLiteral(ctx.PERCENTAGE().getText());
        } else if (ctx.TRUE() != null) {
            node = new BoolLiteral(true);
        } else if (ctx.FALSE() != null) {
            node = new BoolLiteral(false);
        } else if (ctx.SCALAR() != null) {
            node = new ScalarLiteral(ctx.SCALAR().getText());
        } else if (ctx.CAPITAL_IDENT() != null) {
            // Variabele referentie
            node = new VariableReference(ctx.CAPITAL_IDENT().getText());
        }

        if (node != null) {
            ASTNode parent = stack.peek();
            if (parent != null) {
                parent.addChild(node);
            }
        }
    }
}