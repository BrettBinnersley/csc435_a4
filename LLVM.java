/* LLVM.java
 *
 * Utility code to help with outputting intermediate code in the
 * LLVM text format (as a '.ll' file).
 *
 * Author: Nigel Horspool
 * Date: March 2016
 */

import java.util.*;
import java.io.*;

public class LLVM {
    static final String defaultTriple = "x86_64-unknown-linux-gnu";

    int ptrSize = 64;           // characteristics of the target platform
    int ptrAlign = 8;
    boolean macOS = false;
    String targetTriple;
    String llFileName;

    PrintStream ll1 = null;  // where all LLVM code is eventually written
    PrintStream ll2 = null;  // where code is temporarily written
    ByteArrayOutputStream ll2Base = null;

    public int nextGlobalNum = 1;
    public Map<Symbol,LLVMValue> globalName = new HashMap<Symbol,LLVMValue>();
    public Map<String,LLVMValue> globalStringConsts = new HashMap<String,LLVMValue>();
    int nextStructNumber = 1;
    HashMap<Type,String> typeDescriptorCache = new HashMap<Type,String>();
    int nextTempNum = 1;

    // constructor -- the default target triple corresponds to the
    // CSc teaching server: linux.csc.uvic.ca
    public LLVM( String llFileName, String targetTriple ) {
        this.targetTriple = targetTriple==null? defaultTriple : targetTriple;
        this.llFileName = llFileName;
    }

	// must be called before any llvm code is written
	public void open() {
        try {
            ll1 = new PrintStream(llFileName);
	        ll2Base = new ByteArrayOutputStream();
	        ll2 = new PrintStream(ll2Base);
            if (targetTriple.startsWith("i686-")) {
                // eg "i686-pc-mingw32"  // triple for 32-bit Windows system
                ll1.println(LLVMPredefined.preamble32);
                ptrSize = 32;  ptrAlign = 4;
            } else
            if (targetTriple.startsWith("x86_64-")) {
                // eg "x86_64-w64-windows-gnu"    // 64-bit Windows system
                // or "x86_64-unknown-linux-gnu"  // 64-bit Linux system
                ll1.println(LLVMPredefined.preamble64);
                ptrSize = 64;  ptrAlign = 8;
            } else
            if (targetTriple.startsWith("x86_64-apple-")) {
                // eg "x86_64-apple-macosx10.11.3"  // 64-bit Mac OS X system
                ll1.println(LLVMPredefined.preambleMac64);
                ptrSize = 64;  ptrAlign = 8;  macOS = true;
            } else {
                System.err.println("LLVM: Unsupported triple: " + targetTriple);
            }
            ll1.printf("target triple = \"%s\"\n\n", targetTriple);
            LLVMPredefined.writePredefinedCode(this);
        } catch(Exception e) {
            System.err.printf("Unable to write to file %s:\n%s\n\n",
            	llFileName, e.toString());
            System.exit(1);
        }
	}

	public void printf(String format, Object... args) {
		ll2.printf(format, args);
	}

	public void print(String s) {
		ll2.print(s);
	}

	public void println(String s) {
		ll2.println(s);
	}

    // used to output definitions that have to be inserted near the
    // start of the llvm file
    public void prePrintf(String format, Object... args) {
		ll1.printf(format, args);
	}

    // used to output definitions that have to be inserted near the
    // start of the llvm file
    public void prePrintln(String s) {
		ll1.println(s);
	}

    // Must be called when the LLVM code generation is finished
    public void close() {
        // copy all ll2 code over to ll1
        ll2.close();
        ll1.println(ll2Base.toString());
        String s;
        switch(targetTriple) {
	        case "i686-pc-mingw32": s = LLVMPredefined.epilog32; break;
	        case "x86_64-apple-macosx10.9.3": s = "\n"; break;
	        default: s = LLVMPredefined.epilog64; break;
        }
        ll1.println(s);
        ll1.close();
        ll1 = null;
        ll2 = null;
    }

    public String getTypeDescriptor(Type typ) {
        String result = typeDescriptorCache.get(typ);
        if (result == null) {
            result = createTypeDescriptor(typ);
            typeDescriptorCache.put(typ,result);
        }
        return result;
    }

	// Returns the LLVM string representing a datatype
	// Does not handle these Goo types:
	//    TypeList  Slice   Function
    public String createTypeDescriptor(Type typ) {


    	// Cases which don't require an expensive instanceof test
        if (typ == Predefined.intType) return "i32";
        if (typ == Predefined.runeType) return "i8";
        if (typ == Predefined.stringType) return "i8*";
        if (typ == Predefined.boolType) return "i8";
        if (typ == Predefined.floatType) return "double";
        if (typ == Type.voidType) return "void";

        if (typ instanceof Type.Int) return "i"+ ((Type.Int)typ).getSize();
        if (typ instanceof Type.Uint) return "i"+ ((Type.Int)typ).getSize();
        if (typ instanceof Type.UntypedNumber)
        	return ((Type.UntypedNumber)typ).isPossibleDouble()? "double" : "i32";
        if (typ instanceof Type.Flt)
            return ((Type.Flt)typ).getSize()==32? "float" : "double";
        if (typ instanceof Type.Pointer)
            return getTypeDescriptor(((Type.Pointer)typ).getBaseType()) + "*";
        if (typ instanceof Type.Struct) {
            StringBuilder sb = new StringBuilder();
            sb.append("%struct.");
            sb.append(nextStructNumber++);
            String name = sb.toString();
            sb.append(" = type { ");
            for( Symbol sy : ((Type.Struct)typ).getFields().values() ) {
                String st = getTypeDescriptor(sy.getType());
                sb.append(st);
                sb.append(' ');
            }
            sb.append("}");
            prePrintln(sb.toString());
            return name;
        }
        if (typ instanceof Type.Array) {
            Type.Array atyp = (Type.Array)typ;
            StringBuilder sb = new StringBuilder();
            sb.append("[ ");
            sb.append(atyp.getSize());
            sb.append(" x ");
            sb.append(getTypeDescriptor(atyp.getElementType()));
            sb.append(" ]");
            return sb.toString();
        }

        if (typ != Type.unknownType)
            System.err.println("LLVM: call to createTypeDescriptor failed on type "+typ.toString());
        return "%";

    }

    public int getAlignment(Type typ) {
    	// Cases which don't require an expensive instanceof test
        if (typ == Predefined.intType) return 4;
        if (typ == Predefined.runeType) return 1;
        if (typ == Predefined.stringType) return ptrAlign;
        if (typ == Predefined.boolType) return 1;
        if (typ == Predefined.floatType) return 4;
        if (typ == Type.voidType) return 1;

        if (typ instanceof Type.Int) return ((Type.Int)typ).getSize();
        if (typ instanceof Type.Uint) return ((Type.Int)typ).getSize();
        if (typ instanceof Type.UntypedNumber)
        	return ((Type.UntypedNumber)typ).isPossibleDouble()? 8 : 4;
        if (typ instanceof Type.Flt)
            return ((Type.Flt)typ).getSize()==32? 4 : 8;
        if (typ instanceof Type.Pointer)
            return ptrAlign;
        if (typ instanceof Type.Struct)
        	return ptrAlign;
        if (typ instanceof Type.Array)
        	return ptrAlign;
        return ptrAlign;
    }

	// Use this when we have a LLVM representation of the type
    public int getAlignment(String llvmtyp) {
    	switch(llvmtyp) {
    	case "i8": return 1;
    	case "i16": return 2;
    	case "float":
    	case "i32": return 4;
    	case "double":
    	case "i64": return 8;
    	}
        return ptrAlign;
    }

    int nextBBNumber = 0;       // used to number basic blocks
    int nextUnnamedIndex = 0;  // used to generate %0, %1, %2 ... sequences

	// this method is called at the start of a function definition to
	// have basic blocks and local temporaries have their numbering reset
	public void resetNumbering() {
		nextBBNumber = 0;
		nextUnnamedIndex = 0;
	}

    // generates a unique name for a basic block label
    public String createBBLabel()
    {
        return createBBLabel("label");
    }

    // generates a unique name for a basic block label
    public String createBBLabel(String prefix)
    {
        return prefix + "." + nextBBNumber++;
    }

    public String nextTemporary() {
        return "%" + nextUnnamedIndex++;
    }

    public LLVMValue getReference(LLVMValue src) {
      String rv = nextTemporary();
      // ReportError.error(null, "E: " + src.toString());
      printf("  %s = alloca %s*, align %s\n", rv, src.getType(), getAlignment(src.getType()));
      printf("  store %s* %s, %s** %s, align %s\n", src.getType(), src.getValue(), src.getType(), rv, getAlignment(src.getType()));
      return new LLVMValue(src.getType(), rv, true);
    }

    // Given a reference to memory, this generates a load to get the value
    // into a LLVM temporary
    public LLVMValue dereference(LLVMValue src)
    {
        if (!src.isReference()) return src;
        String rv = nextTemporary();
        printf("  %s = load %s, %s* %s\n", rv, src.getType(), src.getType(), src.getValue());
        return new LLVMValue(src.getType(), rv, false);
    }

    // Convert the operand into an i32 LLVM value in a temporary
    public LLVMValue forceIntValue(LLVMValue sv)
    {
        LLVMValue src = dereference(sv);
        if (src.getType() == "i32")
            return src;
        String rv = nextTemporary();
        if (src.getType() == "i64")
        	printf("  %s = trunc %s to i32\n", rv, src);
        else
        if (src.getType() == "i8")
            printf("  %s = zext i8 %s to i32\n", rv, src.getValue());
        else
        	System.err.println("unhandled case for LLVM.forceIntValue: "+sv);
        return new LLVMValue("i32", rv, false);
    }

    public LLVMValue bitToBool(LLVMValue sv) {
      String rv = nextTemporary();
      LLVMValue src = dereference(sv);
      printf("  %s = zext i1 %s to i8\n", rv, src.getValue());

      return new LLVMValue("i8", rv, false);
    }

	// if src is a string constant in an LLVM temporary, then the string constant
	// is created as a global constant in memory and a reference to the string
	// is returned as the result.
    public LLVMValue forceStringReference(LLVMValue src) {
        if (src.getValue().charAt(0) != 'c')
            return src;
        LLVMValue strcnst = globalStringConsts.get(src.getValue());
        if (strcnst != null)
            return strcnst;
        String name = "@.str." + nextGlobalNum++;
        prePrintf("%s = private unnamed_addr constant %s, align 1\n", name, src);
        strcnst = new LLVMValue(src.getType(), name, true);
        globalStringConsts.put(src.getValue(), strcnst);
        return strcnst;
    }

    // stores a LLVM temporary into memory
    // dest must either be a memory reference or have a pointer type
   public void store( LLVMValue source, LLVMValue dest ) {
        if (!dest.isReference() && !dest.getType().endsWith("*")) {
        	System.err.println("LLVM.store needs a memory reference for the dest");
        	return;
        }
        source = dereference(source);
        String srcType = source.getType();
        String destType = dest.getType();
        int align = getAlignment(destType);
        if (dest.isReference())
	        printf("  store %s, %s* %s, align %d\n",
	            source, dest.getType(), dest.getValue(), align);
        else
	        printf("  store %s, %s %s, align %d\n",
	            source, dest.getType(), dest.getValue(), align);
    }

    public void writeReturnInst(LLVMValue result) {
        if (result == null)
            printf("  ret void\n");
        else
            printf("  ret %s\n", dereference(result));
        // If any unreachable code follows this ret instruction, it needs a
        // label otherwise the numbering of LLVM temporaries gets messed up
        writeLabel(createBBLabel("dead"));
    }

    // outputs a label
    public void writeLabel(String name)
    {
        printf(name + ":\n");
    }

    // outputs an unconditional branch
   public void writeBranch(String lab)
    {
        printf("  br label %%%s\n", lab);
    }

    // outputs a conditional branch
    public void writeCondBranch(LLVMValue cond, String trueDest, String falseDest)
    {
        assert(cond.getType() == "i1");
        printf("  br i1 %s, label %%%s, label %%%s\n",
            cond.getValue(), trueDest, falseDest);
    }

    public LLVMValue writePhi(Phi phi){
      String rv = nextTemporary();
      printf("  %s = phi %s %s\n", rv, phi.type, phi);
      return new LLVMValue(phi.type, rv, false);
    }

    // Outputs an LLVM instruction which has two int operands of same size
    // and produces an int result with that size
    public LLVMValue writeIntInst(String opcode, LLVMValue lhs, LLVMValue rhs)
    {
        lhs = forceIntValue(lhs);
        rhs = forceIntValue(rhs);
        assert(lhs.getType() == rhs.getType());
        String rv = nextTemporary();
        printf("  %s = %s %s, %s\n", rv, opcode, lhs, rhs.getValue());
        return new LLVMValue(lhs.getType(), rv, false);
    }

    // Outputs an LLVM instruction which has two float/double operands of same size
    // and produces a result with that type
	public LLVMValue writeFltInst(String opcode, LLVMValue lhs, LLVMValue rhs) {
        lhs = dereference(lhs);
        rhs = dereference(rhs);
        assert(lhs.getType() == rhs.getType());
        String rv = nextTemporary();
        printf("  %s = %s %s, %s\n", rv, opcode, lhs, rhs.getValue());
        return new LLVMValue(lhs.getType(), rv, false);
	}

    // compare two int or rune values
    public LLVMValue writeCompInst(String cmp, LLVMValue lhs, LLVMValue rhs)
    {
        lhs = dereference(lhs);
        rhs = dereference(rhs);
        if (lhs.getType() != rhs.getType()) {
            lhs = forceIntValue(lhs);
            rhs = forceIntValue(rhs);
        }
        // we are now comparing two int values of same size
        String rv = nextTemporary();
        printf("  %s = icmp %s %s, %s\n", rv, cmp, lhs, rhs.getValue());
        return new LLVMValue("i1", rv, false);
    }


    public LLVMValue makeValue(String type, String val) {
      String rv = nextTemporary();
      printf("  %s = alloca %s, align %s\n", rv, type, getAlignment(type));
      printf("  store %s %s, %s* /%%s", type, val, type, rv);
      return new LLVMValue(type, rv, false);
    }

    // // Added this method. Was not provided.
    // public LLVMValue makeNewBoolean(boolean isTrue) {
    //   String rv = nextTemporary();
    //   if (isTrue) {
    //     printf("  %s = icmp %s %s, %s\n", rv, cmp, lhs, rhs.getValue());
    //   } else {
    //     printf("  %s = icmp %s %s, %s\n", rv, cmp, lhs, rhs.getValue());
    //   }
    //   return new LLVMValue("i1", rv, false);
    // }

    // compare two float or double values
    public LLVMValue writeFCompInst(String cmp, LLVMValue lhs, LLVMValue rhs)
    {
        lhs = dereference(lhs);
        rhs = dereference(rhs);
        if (lhs.getType() != rhs.getType()) {
        	String rv2 = nextTemporary();
            if (lhs.getType() == "float") {
            	printf("  %s = fpext %s to double\n", rv2, lhs);
            	lhs = new LLVMValue("double", rv2, false);
            } else if (rhs.getType() == "float") {
            	printf("  %s = fpext %s to double\n", rv2, rhs);
            	rhs = new LLVMValue("double", rv2, false);
            } else
            	System.err.println("Bad arguments for writeFCompInst: "+lhs+", "+rhs);
        }
        // we are now comparing two float/double values of same size
        String rv = nextTemporary();
        printf("  %s = fcmp %s %s, %s\n", rv, cmp, lhs, rhs.getValue());
        return new LLVMValue("i1", rv, false);
    }

	static String hexchar = "0123456789ABCDEF";

	static private int escapedChar( char c ) {
		int val;
		switch(c) {
		case 'b':	val =  8;  break;
		case 'r':   val = 13;  break;
		case 'n':   val = 10;  break;
		default:    val = (int)c;  break;
		}
		return val;
	}

	// text is a string constant written with Go lexical syntax
	public LLVMValue newStringLit( String text ) {
		int len = text.length();
		assert len >= 2 && text.charAt(0) == '\"' && text.charAt(len-1) == '\"';
		StringBuilder sb = new StringBuilder();
		int i = 1;
		int nbytes = 0;
		sb.append("c\"");
		while(i < len-1) {
			char c = text.charAt(i++);
			if (c == '\\') {
				c = text.charAt(i++);
				int val = escapedChar(c);
				sb.append('\\');
				sb.append(hexchar.charAt(val/16));
				sb.append(hexchar.charAt(val%16));
			} else {
				sb.append(c);
			}
			nbytes++;
		}
		sb.append("\\00");
		nbytes++;
		sb.append('\"');
		return new LLVMValue("["+nbytes+" x i8]", sb.toString(), false);
	}

	// text is a rune constant written with Go lexical syntax
	public LLVMValue newCharLit( String text ) {
		int len = text.length();
		assert len > 2 && text.charAt(0) == '\'' && text.charAt(len-1) == '\'';
		int val = 0;
		if (text.charAt(1) != '\\')
			val = (int)text.charAt(1);
		else
			val = escapedChar(text.charAt(2));
		return new LLVMValue("i8", ""+val, false);
	}
}
