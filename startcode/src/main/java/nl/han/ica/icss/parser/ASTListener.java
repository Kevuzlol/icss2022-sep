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

    // --- Style rules ---
    @Override
    public void enterStylerule(ICSSParser.StyleruleContext ctx) {
        Stylerule rule = new Stylerule();

        if (ctx.selector().ID_IDENT() != null) {
            rule.addChild(new IdSelector(ctx.selector().ID_IDENT().getText()));
        } else if (ctx.selector().CLASS_IDENT() != null) {
            rule.addChild(new ClassSelector(ctx.selector().CLASS_IDENT().getText()));
        } else {
            rule.addChild(new TagSelector(ctx.selector().LOWER_IDENT().getText()));
        }

        stack.push(rule);
    }

    @Override
    public void exitStylerule(ICSSParser.StyleruleContext ctx) {
        stack.pop();
    }

    // --- Declarations ---
    @Override
    public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
        Declaration decl = new Declaration(ctx.LOWER_IDENT().getText());

        // Waarden voor level0: color, px, percentage
        if (ctx.value().COLOR() != null) {
            decl.addChild(new ColorLiteral(ctx.value().COLOR().getText()));
        } else if (ctx.value().PIXELSIZE() != null) {
            decl.addChild(new PixelLiteral(ctx.value().PIXELSIZE().getText()));
        } else if (ctx.value().PERCENTAGE() != null) {
            decl.addChild(new PercentageLiteral(ctx.value().PERCENTAGE().getText()));
        }

        stack.peek().addChild(decl);
    }
}