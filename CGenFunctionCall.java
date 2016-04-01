// CGenFunctionCall.java

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.*;

public class CGenFunctionCall {

    static LLVMValue genCall(LLVM ll, ParserRuleContext ctx,
    			Symbol fn, String packageName, ArrayList<LLVMValue> args) {
    	String funcName = fn.getName();
    	Type.Function ftyp = (Type.Function)fn.getType();
		String rslt = null;
		if (packageName != null) {
			// it's a function in a package
			if (packageName.equals("fmt")) {
			switch(funcName) {
			case "Println":
			case "Print":
			case "Printf":
				boolean addNL = funcName.equals("Println");
				LLVMValue fmt;
				if (funcName.equals("Printf") && args.size() > 0) {
					fmt = args.get(0);
					args.remove(0);
				} else
					fmt = makeFormatString(ll, args, addNL);
				for( int k=0; k<args.size(); k++ ) {
					LLVMValue arg = args.get(k);
					if (arg.getValue().charAt(0) == 'c')
				        args.set(k, ll.forceStringReference(arg));
				    else if (arg.getType().equals("float"))  // C library requires doubles, not floats
				    	args.set(k, LLVMExtras.typeConversion(ll, Predefined.floatType, arg));
				}
				rslt = ll.nextTemporary();
				ll.printf("  %s = call i%d (i8*, ...) @printf(i8* getelementptr inbounds (%s, %s, i32 0, i32 0) ",
					rslt, ll.ptrSize, fmt.getType(), ll.forceStringReference(fmt) );
				for( LLVMValue arg : args ) {
					ll.printf(", %s", arg);
				}
				ll.println(")");
				return new LLVMValue("i32", rslt, false);
			}
			// maybe it's a function in the Goo program
			ReportError.error(ctx, "unimplemented package function call: " + packageName + "." + funcName);
			return new LLVMValue("i32", "0", false);
			}
		}
		// it has to be a built-in Go function or a function defined in the program
		if (fn.getScope().getEnclosingScope() == null) {
			// it's a predefined function
			ReportError.error(ctx, "unimplemented builtin go function: "+funcName);
		} else {
			assert fn instanceof FunctionSymbol;
			String rtyp;
			Type[] results = ftyp.getResults();
			if (results.length > 0) {
				rtyp = ll.createTypeDescriptor(results[0]);
				rslt = ll.nextTemporary();
				ll.printf("  %s =", rslt);
			} else
				rtyp = "void";
			ll.printf("  call %s @%s(", rtyp, funcName);
			boolean notFirst = false;
			for( LLVMValue arg : args ) {
				if (notFirst)
					ll.print(", ");
				else
					notFirst = true;
				ll.print(arg.toString());
			}
			ll.println(")");
			if (results.length > 0)
				return new LLVMValue(rtyp, rslt, false);
			return null;
		}
		return new LLVMValue("i32", "0", false);
    }

    static private LLVMValue makeFormatString( LLVM ll, ArrayList<LLVMValue> vals, boolean addNL ) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("c\"");
    	int len = 0;
    	for( LLVMValue v : vals ) {
    		String typ = v.getType();
    		if (typ=="i8*" || typ.endsWith("x i8]"))
    			sb.append("%s ");
    		else if (typ=="float" || typ=="double")
    			sb.append("%g ");
    		else if (typ.charAt(0)=='i')
    			sb.append("%d ");
    		else
    			sb.append("%X ");
    		len += 3;
    	}
    	if (addNL) {
    		sb.append("\\0a");  len++;
    	}
    	sb.append("\\00\""); len++;
    	return new LLVMValue( "["+len+" x i8]", sb.toString(), false);
    }
}