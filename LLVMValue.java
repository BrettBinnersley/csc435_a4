// LLVMValue.java

// This datatype provides a <type, value> pair as used by many
// LLVM instructions.
// (For some instructions, only the value part is needed as the
// type is known by context.)
// The isReference flag distinguishes a value in memory from a value
// held in a temporary. If isReference is true, the temporary holds
// a reference to a memory location.
//
// The nested subclass LLVMValue.LLVMValueList is used for a list of
// LLVMValues (and is needed to hold the result from visiting an
// expressionList node in the parse tree during the code generation pass.

import java.util.*;

public class LLVMValue {
    private boolean isRef;
    private String LLType;
    private String LLValue;

    public LLVMValue( String t, String v, boolean isref ) {
        LLType = t; LLValue = v; isRef = isref;
    }

	public String getType() { return LLType; }
	//public void setType(String t) { LLType = t; }

	public String getValue() { return LLValue; }
	//public void setValue(String v) { LLValue = v; }

	public boolean isReference() { return isRef; }
	//public void setReference(boolean r) { isRef = r; }

    @Override
    public String toString() {
    	return LLType + (isRef? "* " : " ") + LLValue;
    }

    static private LLVMValue dummy = new LLVMValue(null, null, false);

	static public LLVMValueList newLLVMValueList() {
		return dummy.new LLVMValueList();
	}

	// Used for a list of LLVM values
	// (the member fields of the parent are unused)
    public class LLVMValueList extends LLVMValue {
    	public ArrayList<LLVMValue> expressionList;

    	public LLVMValueList( ) {
    		super(null, null, false);
    		expressionList = new ArrayList<LLVMValue>();
    	}

    	int size() { return expressionList.size(); }

    	@Override
    	public String toString() {
    		StringBuilder sb = new StringBuilder();
    		sb.append("[");
    		for( LLVMValue ll : expressionList ) {
    			sb.append(" ");  sb.append(ll);
    		}
    		sb.append(" ]");
    		return sb.toString();
    	}
    }
}
