package query;

import java.util.ArrayList;
import java.util.List;

public class SQLselect extends SQLQuery{
	private List<String> columnList = new ArrayList<String>();
	private List<String> fromList = new ArrayList<String>();
	private List<SQLselect> subQuery = new ArrayList<SQLselect>(); 
	int unionquery = 0, endofcols = 0, endoffrom = 0;
	
	public SQLselect(String query) {
		super(query);
		this.columnList = setColumns();
		this.fromList = setFrom();
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
		this.endofcols = i - 1;
		return listCol;
	}
	
	private List<String> setFrom(){
		List<String> listFrom = new ArrayList<String>();
		String tmpPP;
		int z = this.endofcols + 1;
		boolean eof = false;
		while (!eof) {
			tmpPP = super.getPos(z);
			if(tmpPP.equals("WHERE") ||
					tmpPP.equals("GROUP") ||
					tmpPP.equals("ORDER") ||
					tmpPP.isEmpty()) {
				eof = true;
			}else if (tmpPP.equals("(")) {
				z++;
				int parentesis = 1;
				String subqtext = "";
				while (parentesis > 0) {
					tmpPP = super.getPos(z);
					if (tmpPP.contains("(")) {
						parentesis++;
					}else if (tmpPP.contains(")")) {
						parentesis--;
					}
					if (parentesis == 0 && tmpPP.equals(")")) {
						z++;
					}else {
						subqtext = subqtext + tmpPP + " ";
						z++;
					}
				}
				this.subQuery.add(new SQLselect(subqtext));
			}
			z++;
		}
		
		

		return listFrom;
	}
	
	public String getCol(int i) {
		if (this.columnList.size() > i) {
			return this.columnList.get(i);
		}else {
			return null;
		}
	}
	public String getSubQCol(int qid, int i) {
		if (this.subQuery.size() > qid) {
			return this.subQuery.get(qid).getCol(i);
		}else {
			return null;
		}
	}
}


