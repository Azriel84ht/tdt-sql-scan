package query;

import java.util.Arrays;
import java.util.List;

public class SQLQuery {
	//////////////////////////////////////////////
	// LISTA DE TIPOS DE SQL's SOPORTADAS
	//////////////////////////////////////////////
	private final List<String> supportedTypes = (List<String>) Arrays.asList(
			"INSERT"
			,"SELECT","SEL"
			,"DROP");
	private String query = "" ,queryTratada = "";
	private String tipo;
	private String[] queryPP;
	private int queryLength = 0;
	
	// CONSTRUCTOR PRINCIPAL
	public SQLQuery(String query) {
		// ASIGNAMOS LA CADENA query A LA PROPIEDAD DEL OBJETO
		this.query = query;
		
		//TRATAMOS LA QUERY PARA PROCESARLA DE FORMA INTERNA
		this.queryTratada = query.toUpperCase();
		this.queryTratada = this.queryTratada.replace("(", " ( ");
		this.queryTratada = this.queryTratada.replace(")", " ) ");
		this.queryTratada = this.queryTratada.replace(",", " , ");
		this.queryTratada = this.queryTratada.replace(".", " . ");
		this.queryTratada = this.queryTratada.replace(";", " ; ");
		while (this.queryTratada.contains("  ")) {
			this.queryTratada = this.queryTratada.replace("  ", " ").trim();
		}
		//REEMPLAZAMOS PALABRAS CLAVE PARA FACILITAR EL TRATAMIENTO
		this.queryTratada = this.queryTratada.replace("LEFT OUTER JOIN", "LEFT_OUTER_JOIN");
		this.queryTratada = this.queryTratada.replace("LEFT JOIN", "LEFT_JOIN");
		this.queryTratada = this.queryTratada.replace("RIGHT OUTER JOIN", "RIGHT_OUTER_JOIN");
		this.queryTratada = this.queryTratada.replace("RIGHT JOIN", "RIGHT_JOIN");
		this.queryTratada = this.queryTratada.replace("FULL OUTER JOIN", "FULL_OUTER_JOIN");
		this.queryTratada = this.queryTratada.replace("INNER JOIN", "INNER_JOIN");
		this.queryTratada = this.queryTratada.replace("CROSS JOIN", "CROSS_JOIN");		
		
		//SEPARAMOS LA QUERY EN UN ARRAY PARA MANEJARLA CON FACILIDAD
		this.queryPP = this.queryTratada.trim().split(" ");
		if (supportedTypes.contains(this.queryPP[0])) {
			this.tipo = supportedTypes.get(supportedTypes.indexOf(this.queryPP[0]));
		}else {
			this.tipo = null;
		}
		
		this.queryLength = this.queryPP.length;
	}
	
	public int getLength() {
		return this.queryLength;
	}
	
	public String getTipo() {
		return this.tipo;
	}
	
	public String getPos(int position) {
		if (this.queryPP.length > position) {
			return this.queryPP[position].trim();
		} else
			return null;
	}
	public int buscaP (String palabra, int iter,boolean par) {
		int i = 0, c = 0;
		if (iter == 0) { //SACAMOS EL NUMERO TOTAL
			while (i < this.queryPP.length) {
				if (this.queryPP[i].equals(palabra)) {
					c++;
				}
				i++;
			}
		}else { //BUSCAMOS LA POSICION DE LA ITERACION DADA
			int y = 0, n = 0;
			while (c == 0) {
				if (this.queryPP[n].equals(palabra)) {
					y++;
					if (y == iter) {
						c = n;
					}
				}
				n++;
			}
		}
		return c;
	}
	public String[] getQueryPP() {
		return this.queryPP;
	}
}

