package query;

/********************************************************/
/*** IDEAS:                                             */
/***        1.- En SQLselect crear una funcion para     */
/***            ejecutar en el constructor que se       */
/*              encargue de recorrer la consulta y      */
/*              detecte las posiciones de las palabras  */
/*              clave mas importantes (FROM, WHERE, etc)*/
/*          2.- Utilizar listas multidimensionales para */
/*              las diferentes caracteristicas de las   */
/*              partes como columnas, from, etc         */
/********************************************************/

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SQLselect_bck extends SQLQuery{
	private int idxSelect = 0, idxFrom = 0;
	private List<String> columnList = new ArrayList<String>();
	private List<String> fromList = new ArrayList<String>();
	private List<SQLselect_bck> unionQuery = new ArrayList<SQLselect_bck>();
	private List<SQLselect_bck> subQuery = new ArrayList<SQLselect_bck>();
	private int endofcols = 0, endoffrom = 0;
	private int unionCount = 0;
	
	/********************************************************/
	/*     PATRONES                                         */
	/********************************************************/

	private Pattern patMain = Pattern.compile("^SELECT\\s.{0,}$");
	private Pattern patBasicSelect = Pattern.compile("^SELECT\\s[\\w().,${}+-]+\\sFROM\\s.+$");
	private Pattern patJoin = Pattern.compile("^(LEFT|RIGHT|INNER|CROSS|OUTER|FULL)\\s(OUTER|JOIN|[\\w${}]{0,60}\\.{0,1}[\\w${}]{1,60})\\s[\\w${}]{0,60}\\.{0,1}[\\w${}]{1,60}\\s$");
	
	private String[] queryPP;
	
	//METODO CONSTRUCTOR
	//DESDE ESTE, SE LLAMAN A OTROS METODOS
	//DE LA CLASE POR SEPARAR Y MANTENER LA LIMPIEZA
	public SQLselect_bck(String query) {
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
		int z = this.endofcols + 1;
		boolean eof = false;
		String tmpFrom = "";
		while (!eof && z < this.queryPP.length) {
			if (this.queryPP[z].equals("(")) {        //BUSCA SUBQUERYS
				z++;
				int parentesis = 1;
				String subqtext = "";
				while (parentesis > 0) {
					if (this.queryPP[z].equals("(")) {
						parentesis++;
					}else if (this.queryPP[z].equals(")")) {
						parentesis--;
					}
					if (parentesis == 0 && this.queryPP[z].equals(")")) {
						z++;
					}else {
						subqtext = subqtext + this.queryPP[z] + " ";
						z++;
					}
				}
				this.subQuery.add(new SQLselect_bck(subqtext));
			}else {
				tmpFrom = tmpFrom + " " + this.queryPP[z];
				
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
				this.unionQuery.add(new SQLselect_bck(queryUnion));
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
	
	public String getFrom(int i) {
		if (this.fromList.size() > i) {
			return this.fromList.get(i);
		}else {
			return null;
		}
	}
	
	public String getSubQFrom(int qid, int i) {
		if (this.subQuery.size() > qid) {
			return this.subQuery.get(qid).getFrom(i);
		}else {
			return null;
		}
	}
	
	public String getUnionFrom(int qid, int i) {
		if (this.unionCount >= qid) {
			return this.unionQuery.get(qid).getFrom(i);
		}else {
			return null;
		}
	}
}


