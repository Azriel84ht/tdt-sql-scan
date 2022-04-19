package query;

public class SQLdrop extends SQLQuery{

	private String objTP = "";
	private String objDB = null;
	private String objNM = "";
	
	public SQLdrop(String query) {
		super(query);
		this.objTP = super.getPos(1);
		if (super.getPos(3).equals(".")) {
			this.objDB = super.getPos(2);
			this.objNM = super.getPos(4);
		} else {
			this.objNM = super.getPos(2);
		}
	}
	public String getObjName () {
		return (this.objDB + "." + this.objNM);
	}
	public String getObjType() {
		return(this.objTP);
	}
}


