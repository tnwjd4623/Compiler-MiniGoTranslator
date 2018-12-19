import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class UcodeGenerator {
	static void minigo2ucode(String mgFile) throws IOException {
		MiniGoLexer lexer = new MiniGoLexer(CharStreams.fromFileName(mgFile));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		MiniGoParser parser = new MiniGoParser(tokens);
		ParseTree tree = parser.program();
		
		ParseTreeWalker walker = new ParseTreeWalker();
		walker.walk(new UCodeGenListener(), tree);
	}
	// 기타 추가 코드
}
