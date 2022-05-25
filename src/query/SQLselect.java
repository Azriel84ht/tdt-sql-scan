package query;

import java.util.ArrayList;
import java.util.List;

public class SQLselect extends SQLQuery{
	private List<String> columnList = new ArrayList<String>();
	private List<String> fromList = new ArrayList<String>();
	private List<SQLselect> unionQuery = new ArrayList<SQLselect>();
	private List<SQLselect> subQuery = new ArrayList<SQLselect>();
	private int endofcols = 0, endoffrom = 0;
	private int unionCount = 0;
	
	private String[] queryPP;
	
	//METODO CONSTRUCTOR
	//DESDE ESTE, SE LLAMAN A OTROS METODOS
	//DE LA CLASE POR SEPARAR Y MANTENER LA LIMPIEZA
	public SQLselect(String query) {
		super(query);
		this.queryPP = super.getQueryPP();
		this.columnList = setColumns();
		setUnions();
		this.fromList = setFrom();
	}
	
	//METODOS DEL CONSTRUCTOR
	//ESTOS METODOS SOLO SE LLAMAN DESDE EL CONSTRUCTOR
	//PARA SEPARAR Y MANTENER LA LIMPIEZA
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
	
	//LISTAMOS LOS OBJETOS DE ORIGEN DE NUESTRA QUERY
	//SI EL ORIGEN ES UNA SUBQUERY, LA INSTANCIAMOS
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
					tmpPP.equals(";") ||
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
	
	//DEVOLVEMOS EL NUMERO DE UNIONES E INSTANCIAMOS
	//CADA UNA DE ELLAS COMO OTRO OBJETO
	private void setUnions() {
		//************************************************************//
		//**** EN ESTA PARTE OBTENEMOS EL NUMERO Y LAS POSICIONES ****//
		//**** DE LA PALABRA "UNION" PARA SEPARAR LAS QUERYS      ****//
		//************************************************************//
		int parentesis = 0, i = 0, unions = 0;
		List<Integer> unionPos = new ArrayList<Integer>();
		while (i < this.queryPP.length) {
			if (this.queryPP[i].equals("(")) {
				parentesis++;
			}else if (this.queryPP[i].equals(")")) {
				parentesis--;
			}else if (parentesis == 0 && this.queryPP[i].equals("UNION")) {
				unionPos.add(i);
				unions++;
			}
			i++;
		}
		this.unionCount = unions;
		if (this.unionCount > 0) {
			//************************************************************//
			//**** OBTENEMOS LAS QUERYS E INSTANCIAMOS                ****//
			//************************************************************//
			int init, fin;
			i = 0;
			while (i < this.unionCount) {
				String queryUnion = "";
				init = unionPos.get(i) + 1;
				if (this.unionCount > (i + 1)) {
					fin = unionPos.get(i+1) - 1;
				}else {
					fin = this.queryPP.length - 1;
				}
				while (init <= fin) {
					queryUnion = queryUnion + " " + this.queryPP[init];
					init++;
				}
				this.unionQuery.add(new SQLselect(queryUnion));
				i++;
			}
		}
	}


	//METODOS GETTER PARA OBTENER INFORMACION
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

	public int getUnionCount() {
		return this.unionCount;
	}
	
	public String getUnionCol(int qid, int i) {
		if (this.unionCount >= qid) {
			return this.unionQuery.get(qid).getCol(i);
		}else {
			return null;
		}
	}
}


