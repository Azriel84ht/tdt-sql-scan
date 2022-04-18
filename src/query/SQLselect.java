package query;

import java.util.ArrayList;
import java.util.List;

public class SQLselect extends SQLQuery{
	private List<String> columnList = new ArrayList<String>();
	private boolean inSubquery = false;
	int unionquery = 0;
	
	public SQLselect(String query) {
		super(query);
		this.columnList = setColumns();
		this.unionquery = super.buscaP("UNION", 0);
	}
	private List<String> setColumns(){
		List<String> listCol = new ArrayList<String>();
		int i = 1, parentesis = 0;
		boolean eos = false;
		String tmpColDef = "", tmpPP = "";
		while (!eos) {
			//PARA NO ABUSAR DE LA FUNCION DE LA SUPERCLASE, RECOGEMOS EN UNA VARIABLE
			// LA PALABRA QUE VAMOS A ANALIZAR
			tmpPP = super.getPos(i);
			if (tmpPP.equals("(")) {
				parentesis++;
				tmpColDef = tmpColDef + " " + tmpPP;
			} else if (tmpPP.equals(")")) {
				parentesis--;
				tmpColDef = tmpColDef + " " + tmpPP;
			} else if (parentesis == 0 && tmpPP.equals(",")) {
				listCol.add(tmpColDef);
				tmpColDef = "";
			} else if (parentesis == 0 && tmpPP.equals("FROM")) {
				listCol.add(tmpColDef);
				eos = true;
			} else {
				//SOLO PARA EVITAR BUCLES INFINITOS
				tmpColDef = tmpColDef + " " + tmpPP;
			}
			i++;
		}
		return listCol;
	}
	
	public String getCol(int i) {
		if (this.columnList.size() > i) {
			return this.columnList.get(i);
		}else {
			return null;
		}
		
	}
}


