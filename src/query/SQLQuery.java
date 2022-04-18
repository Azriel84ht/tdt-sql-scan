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
		
		//SEPARAMOS LA QUERY EN UN ARRAY PARA MANEJARLA CON FACILIDAD
		this.queryPP = this.queryTratada.split(" ");
		if (supportedTypes.contains(this.queryPP[0])) {
			this.tipo = supportedTypes.get(supportedTypes.indexOf(this.queryPP[0]));
		}else {
			this.tipo = null;
		}
	}
	
	public String getTipo() {
		return this.tipo;
	}
	
	public String getPos(int position) {
		return queryPP[position];
	}
	public int buscaP (String palabra, int iter) {
		int i = 0, c = 0;
		if (iter == 0) {
			while (this.queryPP.length < i) {
				if (this.queryPP[i].equals(palabra)) {
					c++;
				}
				i++;
			}
		}else {
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
}

