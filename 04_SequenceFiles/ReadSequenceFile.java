import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

/**
 * 
 * @author elena
 *
 * Clase que lee un SequenceFile de una ruta dada y lo muestra por consola.
 * La aplicaci�n busca puntos de sincronizaci�n (syncSeen) y cambia la posici�n
 * del reader a una posici�n conocida (seek) 
 */
public class ReadSequenceFile {

	private static final String rutaOrigen = new String ("pruebas/poemasequencefile");
	
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
		
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get( conf);
		Path path = new Path(rutaOrigen);
		
		//Creamos el Reader del SequenceFile
		SequenceFile.Reader reader = 
				new SequenceFile.Reader(fs, path, conf);
		// Leemos la key y value del SequenceFile, los tipos son conocidos,
		// por lo que se declaran variables de esos tipos.
		IntWritable key = 
				(IntWritable) reader.getKeyClass().newInstance();
		Text value = 
				(Text) reader.getValueClass().newInstance();
		
		StringBuilder strBuilder;
		boolean haySync = false;
		long posSync = 0;
		
		//Recorremos el reader recuperando los pares key/value
		while(reader.next(key,value)){
			
			// Comprobamos si la posici�n es un punto de sync
			// En principio en este fichero no encontrar� ninguno ya que es muy
			// peque�o, si fuera uno m�s grande y tuviera varios puntos de sync
			// se guardar� el �ltimo punto encontrado.
			if(reader.syncSeen()){
				haySync = true;
				posSync = reader.getPosition();
			}
			
			strBuilder = new StringBuilder("Posici�n: ").
					append(reader.getPosition()).append(" - Key: ").
					append(key.toString()).append(" Value: " ).
					append(value.toString());
			System.out.println(strBuilder);
		}
		
		if(haySync){
			// reader.sync posicionar� el reader en el sync siguiente m�s pr�ximo, en
			// si no hay ninguno se posicionar� al final del fichero.
			// En este caso se posicionar� en el punto dado, ya que es de sync.
			strBuilder = new StringBuilder("Sync en el punto: ").
					append(posSync);
			System.out.println(strBuilder);
			reader.sync(posSync);
		}else{
			// Es un valor conocido, si no existiera, habr�a un error
			// al realizar el reader.next.
			posSync = 459;
			reader.seek(posSync);
		}
		
		// En un caso o en otro a pesar de haber finalizado la iteraci�n hemos posicionado
		// el reader en un punto intermedio, as� que seguimos recorri�ndolo (repetimos las l�neas)
		// hasta finalizar de nuevo.
		strBuilder = new StringBuilder("Volvemos a la posici�n: ").append(posSync);
		System.out.println(strBuilder);
		
		System.out.println("Y seguimos recorriendo el reader desde la posici�n dada: ");
		while(reader.next(key,value)){
			strBuilder = new StringBuilder("Posici�n: ").
					append(reader.getPosition()).append(" - Key: ").
					append(key.toString()).append(" Value: " ).
					append(value.toString());
			System.out.println(strBuilder);
		}
		
		reader.close();
	}

}
