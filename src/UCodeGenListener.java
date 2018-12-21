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
		localNum = 1;
		funcNum = 2;
		labelNum = 0;
	}
	
	@Override
	public void exitProgram(MiniGoParser.ProgramContext ctx) {
		StringBuffer buf = new StringBuffer();
		
		// 함수 및 전역 변수 선언
		// 전역 변수를 먼저 선언해야 하지 않을까?
		/*
		 * To-do : 전역변수 선언 가능
		 * 전역변수의 선언 위치를 함수 전으로 하면 해결될 것 같긴 함.
		 */
		for (int i=0; i<ctx.decl().size(); i++) {
			buf.append(ucodes.get(ctx.decl().get(i)));
		}
		
		// 전역변수의 갯수가 0이라서 bgn 인자가 0임.
		// 전역변수 선언 기능 추가하면 여기도 추가해야 함.
		buf.append(makeUcode("", "bgn 0", null));
		
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
	
	// 건드리지 말 것
	@Override
	public void exitStmt (MiniGoParser.StmtContext ctx) {
		ucodes.put(ctx, ucodes.get(ctx.getChild(0)));
	}
	
	@Override
	public void exitExpr_stmt (MiniGoParser.Expr_stmtContext ctx) {
		ucodes.put(ctx, ucodes.get(ctx.getChild(0)));
	}
	
	@Override
	public void exitDecl (MiniGoParser.DeclContext ctx) {
		ucodes.put(ctx,  ucodes.get(ctx.getChild(0)));
	}
	
	@Override
	public void exitParam (MiniGoParser.ParamContext ctx) {
		//ucodes.put(ctx, ucodes.get(ctx.getChild(0)));
		StringBuffer buf = new StringBuffer();
		
		if (ctx.getChildCount() == 1 || ctx.getChildCount() == 2) {
			variables.get(funcNum).put(ctx.IDENT().getText(), localNum);
			localNum ++;
		} else {
			// 에러 케이스
			System.out.println("no");
		}
		ucodes.put(ctx, buf.toString());
	}
	
	@Override
	public void exitParams (MiniGoParser.ParamsContext ctx) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<ctx.getChildCount(); i++) {
			buf.append(ucodes.get(ctx.getChild(i)));
		}
		ucodes.put(ctx, buf.toString());
	}
	// 여기까지
	
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
			
		} else if(isArrayAssignment(ctx)) {
			//배열 '=' 연산
			// b[5] = 100
			
			String localName = ctx.IDENT().getText();
			
			s1 = ucodes.get(ctx.expr(0));
			Integer location = variables.get(funcNum).get(localName);
			buf.append(s1);
			
			buf.append(makeUcode("", "lda", funcNum + " " + location));
			buf.append(makeUcode("", "add", null));
			
			s1 = ucodes.get(ctx.expr(1));
			buf.append(s1);
			buf.append(makeUcode("", "sti", null));
			
		
		} else if (isFunctionCallExpression(ctx)) {
		
			// 함수 호출 표현식
			// 인자 등록 (ldp)
			buf.append(makeUcode("", "ldp", null));
			
			// ucodes에서 인자를 가져와서 붙여넣음
			//인자가 여러개일 경우를 생각해서 i는 2씩 증가해야함
			for (int i=0; i<ctx.args().getChildCount(); i=i+2) {	
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
			// Literal일 경우
			buf.append(makeUcode("", "ldc", ctx.LITERAL().getText()));
			
		} else if (isIdentExpression(ctx)) {
			// IDENT일 경우
			String localName = ctx.IDENT().getText();
			Integer location = variables.get(funcNum).get(localName);
			buf.append(makeUcode("", "lod", funcNum + " " + location));
			
			// 테이블에서 로드해서 append
			// 만약 찾지 못하면?
			
			/*
			 * To-do : read 함수로 호출하면 ldp 해야 함
			 * 즉, 변수를 주소로 넘겨주는 부분 구현 필요.
			 * 배열을 인자로 넘기는 부분과 같음.
			 */
		} else if(isArrayExpression(ctx)) {
			//Array 변수일 경우
			String localName = ctx.IDENT().getText();
			
			s1 = ucodes.get(ctx.expr(0));
			Integer location = variables.get(funcNum).get(localName);
			buf.append(s1);
			
			buf.append(makeUcode("", "lda", funcNum + " " + location));
			buf.append(makeUcode("", "add", null));
			buf.append(makeUcode("", "ldi", null));
			
			
			
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
		 * 전역 변수를 선언하는 부분
		 * 지역 변수 선언부를 참고하면 쉬움
		 * variables (hash table list)에서 0번째가 전역 변수 테이블
		 */
		if (isInitDeclaration(ctx)) {
			
		} else if (isArrayDeclaration(ctx)) {
			
		} else {
			
		}
	}
	
	@Override
	public void exitLocal_decl(MiniGoParser.Local_declContext ctx) {
		int tmp_size = 1;
		StringBuffer buf = new StringBuffer();
		
		/*
		 * To-do : revise the following rules
		 * only 1 block can be accepted
		 * only constant can be accpeted for init declaration
		 * array cannot be accepted
		 */
		if (isInitDeclaration(ctx)) {
			buf.append(makeUcode("", "sym", funcNum + " " + localNum + " " + tmp_size));
			buf.append(makeUcode("", "ldc", ctx.LITERAL().getText()));
			buf.append(makeUcode("", "str", funcNum + " " + localNum));
			variables.get(funcNum).put(ctx.IDENT().getText(), localNum);
		} else if (isArrayDeclaration(ctx)) {
			tmp_size = Integer.parseInt(ctx.getChild(3).toString());
			buf.append(makeUcode("", "sym", funcNum + " " + localNum + " " + tmp_size));
			variables.get(funcNum).put(ctx.IDENT().getText(), localNum);
		} else {
			buf.append(makeUcode("", "sym", funcNum + " " + localNum + " " + tmp_size));
			variables.get(funcNum).put(ctx.IDENT().getText(), localNum);
		}
		localNum += tmp_size;
		
		ucodes.put(ctx, buf.toString());
	}
	
	@Override
	public void enterFun_decl(MiniGoParser.Fun_declContext ctx) {
		// 지역변수 갯수
		localNum = 1;
		
		// 함수 번호를 증가시키고, variable table을 증가시킴.
		variables.add(new HashMap<String, Integer>());
		
	}
	
	@Override
	public void exitFun_decl(MiniGoParser.Fun_declContext ctx) {
		StringBuffer buf = new StringBuffer();
		String funcName = ctx.getChild(1).getText();
		String funcContent = null;
	
		if (ucodes.get(ctx.compound_stmt()) == null) {
			funcContent = "";
		} else {
			funcContent = ucodes.get(ctx.compound_stmt());
		}
		
		// param은 따로 ucode에 붙여넣을 필요가 없다.
		//buf.append(params);
		buf.append(makeUcode(funcName, "proc", (localNum-1) + " " + funcNum + " 2"));
		buf.append(funcContent);
		
		//System.out.println(localNum);
		
		buf.append(makeUcode("", "end", null));
		ucodes.put(ctx, buf.toString());
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
	
	@Override
	public void exitReturn_stmt(MiniGoParser.Return_stmtContext ctx) {
		StringBuffer buf = new StringBuffer();
		if (ctx.getChildCount() == 1) {
			buf.append(makeUcode("", "ret", null));
		} else if (ctx.getChildCount() == 2) {
			String localName = ctx.expr(0).getText();
			Integer location = variables.get(funcNum).get(localName);
			buf.append(makeUcode("", "lod", funcNum + " " + location));
			buf.append(makeUcode("", "retv", null));
		} else if (ctx.getChildCount() == 3) {
			/*
			 * To-do : return 2 value
			 */
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
	private boolean isArrayAssignment(MiniGoParser.ExprContext ctx) {
		return (ctx.getChildCount() == 6 && ctx.getChild(4).getText().equals("="));
	}
	private boolean isArrayExpression(MiniGoParser.ExprContext ctx) {
		return ((ctx.getChildCount() == 4) && 
				(ctx.getChild(1).getText().equals("[")) &&
				(ctx.getChild(3).getText().equals("]")));
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
