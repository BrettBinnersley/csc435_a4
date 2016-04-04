
public class Phi {

  private StringBuilder phi;
  public final String type;
  
  public Phi(String type){
    phi = new StringBuilder();
    this.type = type;
  }
  
  public Phi add(LLVMValue val, LLVMValue branch){
    return this.add(val.getValue(), branch.getValue());
  }
  public Phi add(String val, LLVMValue branch){
    return this.add(val, branch.getValue());
  }
  public Phi add(LLVMValue val, String branch){
    return this.add(val.getValue(), branch);
  }
  public Phi add(String val, String branch){
    if(phi.length() != 0){
      phi.append(", ");
    }
    phi.append(String.format("[%s, %%%s]", val, branch));
    return this;
  }
  
  public String toString(){
    return this.phi.toString();
  }
}
