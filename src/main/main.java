package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import query.*;

public class main {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		File fichero = new File("query_examples/select_ex.sql");
		try {
			FileReader fr = new FileReader(fichero);
			BufferedReader br = new BufferedReader(fr);
			String line = "", query = "";
			while ((line = br.readLine()) != null) {
				query = query + " " + line;
			}
			SQLselect my_query = new SQLselect(query);
			
			//Usamos este bloque para validar los resultados
			
			System.out.println("RESULTADOS:");
			System.out.println("Uniones: " + my_query.getUnionCount());
			int i = 0, w = 0;
			while (my_query.getUnionCount() > i) {
				System.out.println("UNION " + i);
				w=0;
				while (my_query.getUnionCol(i,w) != null) {
					System.out.println("COLUMNA " + w + ": " + my_query.getUnionCol(i,w));
					w++;
				}
				i++;
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}

}
