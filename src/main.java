import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Vector;


public class main {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		// TODO Auto-generated method stub
		
	/*ByteBuffer  x = ByteBuffer.allocate(20);
	
	x.putInt(5);
	x.putInt(5);
	x.putInt(5);
	x.putInt(x.position()-4, 6);
	System.out.println(x.position());

	x.position(0);
	x.getInt();
	x.getInt();
*/
		System.out.println("Loading fetcher...");

		long startTime = System.nanoTime();	

		Fetcher f = new Fetcher (10000000);

		
		long stopTime = System.nanoTime();
		long duration = stopTime - startTime;
		
		System.out.println("Loading duration: "+duration);
		
		
		
		System.out.println("Running layout...");

		startTime = System.nanoTime();	
		
//		f.loadFetcher(0, 64, 1024, 4096, 2097152);
//		f.dumbload();
//		f.BFSBlock();
		
		
		stopTime = System.nanoTime();
		duration = stopTime - startTime;
		
		System.out.println("Layout duration: "+duration);
		
		
		System.out.println("Running BFS...");
		
		startTime = System.nanoTime();		
		
		int end = f.unformatedBFS(0, 6544314);

		stopTime = System.nanoTime();
		 duration = stopTime - startTime;
		
		System.out.println("BFS duration: "+duration);
		System.out.println(end);
	}

}



