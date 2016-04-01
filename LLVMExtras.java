/* LLVMExtras.java
 * 
 * Methods which generate LLVM code for accessing
 * arrays, structs, ...
 * 
 * Author: Nigel Horspool
 * Date: March 2016
 */
 
        
public class LLVMExtras {

	// Write LLVM code to declare an initialized global variable or global constant
	// (i.e. a declaration at package level in Go)
	// The isConst parameter is true for declaring a constant, false for a variable
	// The argument initValues should be null for zero initialization, otherwise
	// it provides one or more constant values:
	// for an array or struct: a list of constants enclosed in square brackets, e.g. "[i32 11, 132 5, i32 7]"
	// for a string: an example is c"abc\0A\00"
	// for a number: an example is float 2.0e+00
	static public LLVMValue writeGlobalDecl( LLVM ll, Symbol sy, boolean isConst, String initValues ) {
	    if (initValues == null)
	        initValues = "zeroinitializer";
		LLVMValue result = ll.globalName.get(sy);
		if (result != null)		// already created?
			return result;
		Type typ = sy.getType();
		String name = "@" + sy.getName() + "." + ll.nextGlobalNum++;
		String gdesc = ll.createTypeDescriptor(typ);
		String which = isConst? "constant" : "common global";
		ll.prePrintf("%s = %s %s %s, align %d\n",
			name, which, gdesc, initValues, ll.getAlignment(typ));
		result = new LLVMValue(gdesc, name, true);
		ll.globalName.put(sy, result);
		return result;
	}

	// Write LLVM code to declare a zero initialized global variable
	static public LLVMValue writeGlobalDecl( LLVM ll, Symbol sy ) {
		LLVMValue result = ll.globalName.get(sy);
		if (result != null)		// already created?
			return result;
		Type typ = sy.getType();
		String name = "@" + sy.getName() + "." + ll.nextGlobalNum++;
		String gdesc = ll.createTypeDescriptor(typ);
		ll.prePrintf("%s = common global %s zeroinitializer, align %d\n",
			name, gdesc, ll.getAlignment(typ));
		result = new LLVMValue(gdesc, name, true);
		ll.globalName.put(sy, result);
		return result;
	}

	static public LLVMValue lookupGlobal( LLVM ll, Symbol sy ) {
		return ll.globalName.get(sy);
	}

    // Allocates local storage for a local variable or constant sy
    // The storage is uninitialized; the caller must generate assignments
    // or a memset call if initialization is required.
    static public LLVMValue writeLocalDecl( LLVM ll, Symbol sy ) {
		LLVMValue result = ll.globalName.get(sy);
		if (result != null)		// already created?
			return result;
		Type typ = sy.getType();
		String name = ll.nextTemporary();
		String gdesc = ll.createTypeDescriptor(typ);
		ll.printf("  %s = alloca %s, align %d ; %s\n",
					name, gdesc, ll.ptrAlign, sy.getName());
		return new LLVMValue(gdesc, name, true);
    }

    // Writes code for an assignment. The dest parameter must be a reference
    // to a memory location (either local or global).
    static public void writeAssignment( LLVM ll, LLVMValue src, LLVMValue dest ) {
        src = ll.dereference(src);
        if (!dest.isReference()) {
        	// test if we have a pointer value
        	if (!dest.getType().endsWith("*")) {
	            ReportError.error("Bad call to LLVM.writeAssignment");
	            return;
            }
            ll.printf("  store %s, %s %s\n", src, dest.getType(), dest.getValue());
        } else
        	ll.printf("  store %s, %s* %s\n", src, dest.getType(), dest.getValue());
    }

    // Write code to index an array; the returned result is a reference
    // to the array element
	static public LLVMValue elementReference( LLVM ll,
			Type.Array arrType, LLVMValue arrPtr, LLVMValue index ) {
		Type elemType = arrType.getElementType();
		String etyp = ll.createTypeDescriptor(elemType);
		String atyp = ll.createTypeDescriptor(arrType);
		index = ll.forceIntValue(index);
		String rv1 = ll.nextTemporary();
		ll.printf("  %s = getelementptr inbounds %s, %s* %s, i32 0, %s\n",
			rv1, atyp, atyp, arrPtr.getValue(), index);
		//   %arrayidx = getelementptr inbounds [10 x i32], [10 x i32]* @ia, i32 0, i64 %idxprom
		return new LLVMValue(etyp, rv1, true);
	}

    // Write code to access a field of a struct; the returned result is a reference
    // to the field
	static public LLVMValue elementReference( LLVM ll,
			Type.Struct strType, LLVMValue strPtr, String fieldName ) {
		Type fldType = null;
		int fnum = 0;
		for( Symbol f : strType.getFields().values() ) {
			if (f.getName().equals(fieldName)) {
				fldType = f.getType();
				break;
			}
			fnum++;
		}
		assert(fldType != null);
		String ftyp = ll.createTypeDescriptor(fldType);
		String styp = ll.createTypeDescriptor(strType);
		String rv1 = ll.nextTemporary();
		ll.printf("  %s = getelementptr inbounds (%s, %s* %s, i32 0, i32 %d\n",
			rv1, styp, styp, strPtr.getValue(), fnum);
		return new LLVMValue(ftyp + "*", rv1, true);
	}

	// returns 1 for i1, 8 for i8, 32 for i32, 64 for i64 and 0 for everything else
	static private int numBitsFromInt(String desc) {
		switch(desc) {
		case "i1": return 1;
		case "i8": return 8;
		case "i16": return 16;
		case "i32": return 32;
		case "i64": return 64;
		}
		return 0;
	}

    static public LLVMValue typeConversion( LLVM ll, Type toType, LLVMValue val ) {
        String ltype = ll.createTypeDescriptor(toType);
        val = ll.dereference(val);
        String stype = val.getType();
        if (ltype.equals(stype)) return val;
        String dest = ll.nextTemporary();
        if (ltype.endsWith("*") && stype.endsWith("*")) {
            // pointer conversion
            ll.printf("  %s = bitcast %s to %s\n", dest, val, ltype);
        } else
        if ((ltype.equals("float") || ltype.equals("double")) && stype.charAt(0)=='i') {
            // int to floating point
            ll.printf("  %s = sitofp %s to %s\n", dest, val, ltype);
        } else
        if ((stype.equals("float") || stype.equals("double")) && (ltype.charAt(0)=='i')) {
            // floating point to int
            ll.printf("  %s = fptosi %s to %s\n", dest, val, ltype);
        } else
        if (stype.equals("float") && ltype.equals("double")) {
        	// widening a float
        	ll.printf("  %s = fpext %s to double\n", dest, val);
        } else
        if (ltype.equals("float") && stype.equals("double")) {
        	// narrowing a float
        	ll.printf("  %s = fptrunc %s to float\n", dest, val);
        } else
        if (ltype.charAt(0)=='i' && stype.charAt(0)=='i') {
        	// int to int conversion
        	int lbits = numBitsFromInt(ltype);
        	int sbits = numBitsFromInt(stype);
        	if (lbits > sbits && sbits > 0) { // widening
        		boolean unsignedConv = toType instanceof Type.Uint;
        		ll.printf("  %s = %s %s to %s\n", dest, unsignedConv? "zext" : "sext", val, ltype);
        	} else
        	if (sbits > lbits && lbits > 0) { // narrowing
        		ll.printf("  %s = trunc %s to %s\n", dest, val, ltype);
        	} else
        		System.err.println("unimplemented conversion: "+stype+" to "+ltype);
        } else
        	System.err.println("unimplemented conversion: "+stype+" to "+ltype);
        return new LLVMValue(ltype, dest, false);
    }
}
