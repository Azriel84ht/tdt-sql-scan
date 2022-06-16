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

public class SQLselect extends SQLQuery{
	/********************************************************/
	/* Indicadores de palabras clave                        */
	/********************************************************/

	private int idxSelect = 0, idxFrom = 0, idxEOQ = 0;
	private List<Integer> unionPosition = null;
	
	private boolean union = false, subquerys = false;
	
	private List<SQLselect> unionQuery = null;
	private List<SQLselect> subQuery = null;
	
	private List<String> fromList = new ArrayList<String>();
	
	
	
	
	public SQLselect(String query) {
		super(query);
		keyWordSearch();
		fromAnalize();
	}
	
	private void keyWordSearch() {
		// LOCALIZAMOS PALABRAS CLAVE
		int i = 0, par = 0;
		String palabra = "";
		while (i < super.getLength()) {
			palabra = super.getPos(i);
			if (palabra.equals("(")) {
				par ++;
			}else if (palabra.equals(")")) {
				par --;
			}
			
			if (par == 0) {
				if (palabra.equals("SEL") || palabra.equals("SELECT")) {
					this.idxSelect = i;
				}else if (palabra.equals("FROM")) {
					this.idxFrom = i;
				}else if (palabra.equals("UNION")) {
					this.union = true;
					this.unionQuery = new ArrayList<SQLselect>();
					this.unionPosition = new ArrayList<Integer>();
					this.unionPosition.add(i);
					this.idxEOQ = i-1;
					i++;
					String unionSQL = "";
					int par2 = 0;
					while (i < super.getLength()) {
						palabra = super.getPos(i);
						if (palabra.equals("(")) {
							par2 ++;
						}else if (palabra.equals(")")) {
							par2 --;
						}
						if ((par2 == 0 && (palabra.equals("UNION")) || super.getPos(i+1) == null)) {
							if (palabra.equals("UNION")) {
								this.unionPosition.add(i);
							}else {
								unionSQL = unionSQL + " " + palabra;
							}
							this.unionQuery.add(new SQLselect(unionSQL));
							unionSQL = "";
						}else {
							unionSQL = unionSQL + " " + palabra;
						}
						i++;
					}
				}
			}
			i++;
		}
	}
	
	private void fromAnalize() {
		int i = this.idxFrom, fin = 0;
		String palabra = "", fromString = "";
		if (this.union) {
			fin = this.unionPosition.get(0);
		}else {
			fin = this.idxEOQ;
		}
		while (i <= fin) {
			palabra = super.getPos(i);
			if (palabra.equals("(")) {
				this.subQuery = new ArrayList<SQLselect>();
				this.subquerys = true;
				String subQuerytxt = "";
				int parentesis = 1;
				i++;
				while (parentesis > 0) {
					palabra = super.getPos(i);
					if (palabra.equals("(")) {
						parentesis++;
					}else if (palabra.equals(")")) {
						parentesis--;
					}
					if (parentesis == 0) {
						this.subQuery.add(new SQLselect(subQuerytxt));
						break;
					}else {
						subQuerytxt = subQuerytxt + " " + palabra;
					}
					i++;
				}
				fromString = fromString + " [SUBQUERYID_" + (this.subQuery.size() - 1) + "]";
			}else if (palabra.equals(",")) {
				this.fromList.add(fromString);
				fromString = "";
			}else if (i + 1 == fin) {
				fromString = fromString + " " + palabra;
				this.fromList.add(fromString);
				fromString = "";
			}else if (palabra.endsWith("_JOIN") && fromString.trim().length() > 0) {
				this.fromList.add(fromString);
				fromString = palabra;
			}else {
				fromString = fromString + " " + palabra;
			}
			i++;
		}
	}

	//METODOS PARA OBTENER LOS INDICADORES ESPECIFICOS
	public boolean isUnion() {
		return this.union;
	}
	
	public boolean isSubQuery() {
		return this.subquerys;
	}
	
		//METODOS PARA OBTENER CONTADORES
	public int getUnionCount() {
		return this.unionPosition.size();
	}
	
	public int getSubqueryCount() {
		return this.subQuery.size();
	}
	
	public int getFromCount() {
		return this.fromList.size();
	}

	//METODOS PARA OBTENER ELEMENTOS CONCRETOS
	public String getFrom(int i) {
		if (this.fromList.size() > i) {
			return this.fromList.get(i);
		}else {
			return null;
		}
		
	}
}


