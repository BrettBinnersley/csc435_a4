// GooMain.java
//
// TEAM MEMBERS:
//     Brett Binnersley, V00776751
//     Sebastien Guillemot, V00741620
//
// Main program which invokes the various compiler phases:
// lexer/parser, symbol table construction, type checking ...

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.atn.*;
import java.io.*;

public class GooMain {

	public static void usage() {
		String[] lines = {
			"USAGE:",
			"    java GooMain [options] [filename]",
			"OPTIONS:",
			"    -triple TTTT   use TTTT as the LLVM target triple",
			"DEBUGGING OPTIONS:",
			"    -dtsy   enable tracing of symbol table actions",
			"    -dsym   dump symbol table at each scope close",
			"    -dpre   dump predefined symbols",
			"    -dppt   print parse tree",
			null
		};
		for(String s: lines)
			System.out.println(s);
		System.exit(1);
	}

    public static void main(String[] args) throws Exception {
		boolean traceSymTab = false;
		boolean printTree = false;
		boolean dumpSymTab = false;
		boolean dumpPredefineds = false;
    	String fileName = null;
    	String triple = null;
    	boolean nextArgIsTriple = false;

        for(String s : args) {
        	if (nextArgIsTriple) {
        		nextArgIsTriple = false;
        		triple = s;
        		continue;
        	}
        	if (s.startsWith("-")) {
        		if (s.equals("-dtsy"))
        			traceSymTab = true;
        		else if (s.equals("-dppt"))
        			printTree = true;
        		else if (s.equals("-dsym"))
        			dumpSymTab = true;
        		else if (s.equals("-dpre"))
        			dumpPredefineds = true;
        		else if (s.equals("-triple"))
        			nextArgIsTriple = true;
        		else  // unknown option, ignore it
        			System.out.println("-- unrecognized option: "+s);
        	} else {
        		if (fileName != null)
        			usage();
        		else
        			fileName = s;
        	}
        }

		// Open the input
        ANTLRInputStream input;
        if (fileName == null) {
        	input = new ANTLRInputStream(System.in);
        	fileName = "(standard input)";
        } else
        	input = new ANTLRInputStream(new FileInputStream(fileName));

		// Create the lexer
        GooLexer lexer = new GooLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

		// Parse the Goo source file, creating a parse tree
        GooParser parser = new GooParser(tokens);

        ParseTree tree = parser.sourceFile();	// sourceFile is start symbol

        if (printTree) {
        	System.out.println(tree.toStringTree(parser));
        	System.out.println();
        }

		if (parser.getNumberOfSyntaxErrors() > 0) {
			// diagnostic parsing mode seems to upset the error count
			System.out.println("* Compilation halted -- there were "
				+ parser.getNumberOfSyntaxErrors() + " syntax errors");
			System.exit(1);
		}

		// Traverse the parse tree, building the symbol table
        SymTabVisitor1 bg = new SymTabVisitor1(traceSymTab,dumpSymTab,dumpPredefineds);
        bg.visit(tree);

		if (ReportError.getErrorCount() > 0) {
			System.out.println("* Compilation halted -- there were "
				+ ReportError.getErrorCount() + " errors");
			System.exit(1);
		}

		// The next phase will be type checking and most semantic checking
		// of function bodies

		// Traverse the parse tree, building the symbol table
        SymTabVisitor2 ck = new SymTabVisitor2(traceSymTab,dumpSymTab,dumpPredefineds);
        ck.setScopes(bg.getScopes());
        ck.setTypes(bg.getTypes());
        ck.visit(tree);

		// The final phase will complete the semantic checking and
		// generate the input for LLVM
		String llFileName;
		if (fileName.charAt(0) == '(')
			llFileName = "code.ll";
		else {
			int dotPos = fileName.lastIndexOf('.');
			if (dotPos > 0)
				llFileName = fileName.substring(0,dotPos)+".ll";
			else
				llFileName = fileName+".ll";
		}
		LLVM ll = new LLVM(llFileName, triple);
		CGenVisitor cgv = new CGenVisitor(ll);
        cgv.setScopes(bg.getScopes());
        cgv.setTypes(bg.getTypes());
		ll.open();
        cgv.visit(tree);
        ll.close();
    }
}
