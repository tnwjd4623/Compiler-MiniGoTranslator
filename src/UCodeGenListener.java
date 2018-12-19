import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class UCodeGenListener extends MiniGoBaseListener {
	ParseTreeProperty<String> ucodes;
	ArrayList<HashMap<String, Integer>> variables;
	int localNum;
	int funcNum;
	int labelNum;
	
	public UCodeGenListener() {
		ucodes = new ParseTreeProperty<String>();
		variables = new ArrayList<HashMap<String, Integer>>();
		variables.add(new HashMap<String, Integer>());
		variables.add(new HashMap<String, Integer>());
		localNum = 2;
		funcNum = 1;
		labelNum = 0;
	}
	
	@Override
	public void exitProgram(MiniGoParser.ProgramContext ctx) {
		StringBuffer buf = new StringBuffer();
		
		// 함수 선언
		for (int i=0; i<ctx.getChild(0).getChildCount(); i++) {
			if (ctx.getChild(0).getChild(i).getClass().equals(MiniGoParser.Fun_declContext.class)) {
				buf.append(ucodes.get(ctx.getChild(0).getChild(i)));
			}
		}
		
		buf.append(makeUcode("", "end", null));
		buf.append(makeUcode("", "bgn 0", null));
		
		// 전역 변수 선언
		/*
		 * To-do : work for global variables
		 */
		
		// main 함수 시작
		buf.append(makeUcode("", "ldp", null));
		buf.append(makeUcode("", "call", "main"));
		buf.append(makeUcode("", "end", null));
		
		// 결과 출력 및 파일 쓰기 (result.uco)
		System.out.println(buf.toString());
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("result.uco"));
			writer.write(buf.toString());
			writer.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	// exitStmt 및 exitExpr_stmt는 건드리지 말 것
	@Override
	public void exitStmt (MiniGoParser.StmtContext ctx) {
		ucodes.put(ctx, ucodes.get(ctx.getChild(0)));
	}
	
	@Override
	public void exitExpr_stmt (MiniGoParser.Expr_stmtContext ctx) {
		ucodes.put(ctx, ucodes.get(ctx.getChild(0)));
	}
	
	// expression
	@Override
	public void exitExpr (MiniGoParser.ExprContext ctx) {
		String s1 = null, s2 = null, op = null;
		StringBuffer buf = new StringBuffer();

		if (isAssignmentExpression(ctx)) {
			// '=' 연산
			// example : 'A = 2'
			
			String localName = ctx.IDENT().getText();
			s1 = ucodes.get(ctx.expr(0));
			Integer location = variables.get(funcNum).get(localName);
			
			buf.append(s1);
			buf.append(makeUcode("", "str", funcNum + " " + location));
			
		} else if (isFunctionCallExpression(ctx)) {
			// 함수 호출 표현식
			// 인자 등록 (ldp)
			buf.append(makeUcode("", "ldp", null));
			
			// ctx.args()를 순회하면서 ucodes에 인자를 넣음
			/*
			 * To-do : 
			 */
			for (int i=0; i<ctx.args().getChildCount(); i++) {
				buf.append(ucodes.get(ctx.args().getChild(i)));
			}
			
			// call을 이용해 호출
			buf.append(makeUcode("", "call", ctx.IDENT().getText()));
		} else if (isBinaryOperation(ctx)) {
			// example : '1 + 3'
			s1 = ucodes.get(ctx.expr(0));
			op = ctx.op.getText();
			s2 = ucodes.get(ctx.expr(1));
			switch (op) {
			case "+":
				buf.append(s1);
				buf.append(s2);
				buf.append(makeUcode("", "add", null));
				break;
			case "-":
				buf.append(s1);
				buf.append(s2);
				buf.append(makeUcode("", "sub", null));
				break;
			case "*":
				buf.append(s1);
				buf.append(s2);
				buf.append(makeUcode("", "mult", null));
				break;
			case "/":
				buf.append(s1);
				buf.append(s2);
				buf.append(makeUcode("", "div", null));
				break;
			case ">":
				buf.append(s1);
				buf.append(s2);
				buf.append(makeUcode("", "gt", null));
				break;
			case "<":
				buf.append(s1);
				buf.append(s2);
				buf.append(makeUcode("", "lt", null));
				break;
			case "==":
				buf.append(s1);
				buf.append(s2);
				buf.append(makeUcode("", "eq", null));
				break;
			case ">=":
				buf.append(s1);
				buf.append(s2);
				buf.append(makeUcode("", "ge", null));
				break;
			case "<=":
				buf.append(s1);
				buf.append(s2);
				buf.append(makeUcode("", "le", null));
				break;
			}
			
		} else if (isPrefixOperation(ctx)) {
			// example : '++8', '-3', '!true'
			/*
			 * To-do : add some unable prefix operations
			 */
			op = ctx.op.getText();
			s1 = ucodes.get(ctx.expr(0));
			switch (op) {
			case "-":
				buf.append(makeUcode("", "neg", s1));
				break;
			case "+":
				buf.append(s1);
				break;
			case "--":
			case "++":
			case "!":
			}
		} else if (isLiteralExpression(ctx)) {
			buf.append(makeUcode("", "ldc", ctx.LITERAL().getText()));
		} else if (isIdentExpression(ctx)) {
			String localName = ctx.IDENT().getText();
			Integer location = variables.get(funcNum).get(localName);
			
			buf.append(makeUcode("", "lod", funcNum + " " + location));
		} else {
			//System.out.println("To-do");
			//ucodes.put(ctx, ctx.getText());
		}
		ucodes.put(ctx, buf.toString());
	}
	
	@Override
	public void exitVar_decl(MiniGoParser.Var_declContext ctx) {
		/*
		 * To-do : global variable
		 */
		if (isInitDeclaration(ctx)) {
			//newTexts.put(ctx, ctx.dec_spec().getText() + " " + ctx.IDENT().getText() + " " + ctx.type_spec().getText() + " = " + ctx.LITERAL().getText());
		} else if (isArrayDeclaration(ctx)) {
			//newTexts.put(ctx, ctx.dec_spec().getText() + " " + ctx.IDENT().getText() + "[" + ctx.LITERAL().getText() + "] " + ctx.type_spec().getText());
		} else {
			//newTexts.put(ctx, ctx.dec_spec().getText() + " " + ctx.IDENT().getText() + " " + ctx.type_spec().getText());
		}
	}
	
	@Override
	public void exitLocal_decl(MiniGoParser.Local_declContext ctx) {
		int tmp_size = 1;
		StringBuffer buf = new StringBuffer();
		
		/*
		 * To-do : only 1 block can be accepted
		 * only constant can be accpeted for init declaration
		 * array cannot be accepted
		 */
		if (isInitDeclaration(ctx)) {
			//newTexts.put(ctx, ctx.dec_spec().getText() + " " + ctx.IDENT().getText() + " " + ctx.type_spec().getText() + " = " + ctx.LITERAL().getText());
			buf.append(makeUcode("", "sym", funcNum + " " + localNum + " " + tmp_size));
			buf.append(makeUcode("", "ldc", ctx.LITERAL().getText()));
			buf.append(makeUcode("", "str", "2 " + localNum));
			variables.get(funcNum).put(ctx.IDENT().getText(), localNum);
		} else if (isArrayDeclaration(ctx)) {
			tmp_size = Integer.parseInt(ctx.getChild(3).toString());
			//newTexts.put(ctx, ctx.dec_spec().getText() + " " + ctx.IDENT().getText() + "[" + ctx.LITERAL().getText() + "] " + ctx.type_spec().getText());
			buf.append(makeUcode("", "sym", funcNum + " " + localNum + " " + tmp_size));
			variables.get(funcNum).put(ctx.IDENT().getText(), localNum);
		} else {
			//newTexts.put(ctx, ctx.dec_spec().getText() + " " + ctx.IDENT().getText() + " " + ctx.type_spec().getText());
			buf.append(makeUcode("", "sym", funcNum + " " + localNum + " " + tmp_size));
			variables.get(funcNum).put(ctx.IDENT().getText(), localNum);
		}
		localNum += tmp_size;
		
		ucodes.put(ctx, buf.toString());
	}
	
	@Override
	public void enterFun_decl(MiniGoParser.Fun_declContext ctx) {
		localNum = 1;
		variables.add(new HashMap<String, Integer>());
		funcNum++;
		//String funcName = ctx.getChild(1).getText();
	}
	
	@Override
	public void exitFun_decl(MiniGoParser.Fun_declContext ctx) {
		StringBuffer buf = new StringBuffer();
		String funcName = ctx.getChild(1).getText();
		String params = null;
		String funcContent = null;
		
		if (ctx.getChild(3) == null) {
			params = "";
		} else {
			params = ucodes.get(ctx.getChild(3));
		}
		if (ucodes.get(ctx.compound_stmt()) == null) {
			funcContent = "";
		} else {
			//funcContent = newTexts.get(ctx.compound_stmt());
			funcContent = ucodes.get(ctx.compound_stmt());
		}
		
		/*
		 * To-do : enable parameters
		 */
		//buf.append(params);
		buf.append(makeUcode(funcName, "proc", (localNum-1) + " " + funcNum + " 1"));
		buf.append(funcContent);
		ucodes.put(ctx, buf.toString());
		
		//newTexts.put(ctx, "func " + funcName + params + " " + funcContent + "\n");
	}
	
	@Override
	public void exitIf_stmt(MiniGoParser.If_stmtContext ctx) {
		String s1 = null, s2 = null, s3 = null;
		StringBuffer buf = new StringBuffer();

		s1 = ucodes.get(ctx.expr()); // condition
		s2 = ucodes.get(ctx.stmt(0).compound_stmt()); // stmt
		
		buf.append(s1);
		int end_point = labelNum;
		
		if (isElseThere(ctx)) {
			int else_point = labelNum;
			end_point = labelNum + 1;
			s3 = ucodes.get(ctx.stmt(1).compound_stmt());
			
			buf.append(makeUcode("", "fjp", "$" + else_point));
			buf.append(s2);
			buf.append(makeUcode("", "ujp", "$" + end_point));
			buf.append(makeUcode("$" + (labelNum++), "nop", null)); // else point
			buf.append(s3);
			buf.append(makeUcode("$" + (labelNum++), "nop", null)); // end point
		} else {
			buf.append(makeUcode("", "fjp", "$" + end_point));
			buf.append(s2);
			buf.append(makeUcode("$" + (labelNum++), "nop", null));
		}
		ucodes.put(ctx, buf.toString());
	}
	
	@Override
	public void exitFor_stmt(MiniGoParser.For_stmtContext ctx) {
		String s1 = null, s2 = null;
		StringBuffer buf = new StringBuffer();
		
		int start_loop = labelNum;
		int end_loop = labelNum + 1;
		
		buf.append(makeUcode("$"+(labelNum++), "nop", null));
		
		if (ctx.loop_expr() != null) {
			s1 = ucodes.get(ctx.loop_expr());
		} else {
			s1 = ucodes.get(ctx.expr());
		}
		buf.append(s1);
		buf.append(makeUcode("", "fjp", "$" + end_loop));
		if (ctx.stmt() != null) {
			s2 = ucodes.get(ctx.stmt());
			buf.append(s2);
		}
		buf.append(makeUcode("", "ujp", "$" + start_loop));
		buf.append(makeUcode("$"+(labelNum++), "nop", null)); // end of loop
		
		ucodes.put(ctx, buf.toString());
	}
	
	@Override
	public void exitLoop_expr(MiniGoParser.Loop_exprContext ctx) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<ctx.getChildCount(); i++) {
			if (ucodes.get(ctx.getChild(i)) != null) buf.append(ucodes.get(ctx.getChild(i)));
		}

		ucodes.put(ctx,  buf.toString());
	}
	
	@Override
	public void exitArgs(MiniGoParser.ArgsContext ctx) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<ctx.expr().size(); i++) {
			buf.append(ucodes.get(ctx.expr(i)));
		}
		ucodes.put(ctx, buf.toString());
	}
	
	@Override
	public void enterCompound_stmt(MiniGoParser.Compound_stmtContext ctx) {
		
	}
	
	@Override
	public void exitCompound_stmt(MiniGoParser.Compound_stmtContext ctx) {
		StringBuffer buf = new StringBuffer();

		for (int i=0; i<ctx.getChildCount(); i++) {
			if (ucodes.get(ctx.getChild(i)) != null) {
				buf.append(ucodes.get(ctx.getChild(i)));
			}
		}

		ucodes.put(ctx, buf.toString());
	}
	
	private boolean isBinaryOperation(MiniGoParser.ExprContext ctx) {
		return ctx.getChildCount() == 3 && ctx.getChild(1) != ctx.expr(0) && !ctx.getChild(1).getText().equals("=");
	}
	private boolean isPrefixOperation(MiniGoParser.ExprContext ctx) {
		return ctx.getChildCount() == 2 && ctx.getChild(0) == ctx.op;
	}
	private boolean isBracketExpression(MiniGoParser.ExprContext ctx) {
		return ctx.getChildCount() == 3 && ctx.getChild(1) == ctx.expr(0);
	}
	private boolean isAssignmentExpression(MiniGoParser.ExprContext ctx) {
		return (ctx.getChildCount() == 3 && ctx.getChild(1).getText().equals("="));
	}
	private boolean isLiteralExpression(MiniGoParser.ExprContext ctx) {
		return ((ctx.getChildCount() == 1) && (ctx.LITERAL() != null));
	}
	private boolean isIdentExpression(MiniGoParser.ExprContext ctx) {
		return ((ctx.getChildCount() == 1) && (ctx.IDENT() != null));
	}
	private boolean isFunctionCallExpression(MiniGoParser.ExprContext ctx) {
		return ((ctx.getChildCount() == 4) && 
				(ctx.getChild(1).getText().equals("(")) && 
				(ctx.getChild(3).getText().equals(")")));
	}
	private boolean isInitDeclaration(ParserRuleContext ctx) {
		return ctx.getChildCount() == 5;
	}
	private boolean isArrayDeclaration(ParserRuleContext ctx) {
		return ctx.getChildCount() == 6;
	}
	private boolean isElseThere(MiniGoParser.If_stmtContext ctx) {
		return ctx.getChildCount() != 3;
	}
	
	private String makeUcode (String label, String opcode, String operands) {
		StringBuffer buf = new StringBuffer();
		if (label.length() > 10) {
			System.out.println("writeUcode error : label length must be 10 or lower.");
			return "";
		}
		int wslen = 11 - (label.length());
		buf.append(label); // append label
		for (int i=0; i<wslen; i++) buf.append(" "); // append whitespace
		buf.append(opcode);
		
		if (operands != null) {
			buf.append(" ");
			buf.append(operands);
		}
		
		buf.append("\n");
		
		return buf.toString();
	}
}
