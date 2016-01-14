
import java.io.*;


public class TestReaderWorldData implements ListenerWorldData {
	static InputStream is;

	/* this function gets called whenever a world data packet is recevied */
	public void worldDataPacketReceived(int packetType, int serialPrefix, int serialNumber, int[] data, long timeMilli) {
		/* just dump packet for debugging */
		System.out.println("# worldDataPacketReceived");
		System.out.println("#       timeMilli=" + timeMilli);
		System.out.println("#      packetType=" + packetType);
		System.out.println("#    serialPrefix=" + serialPrefix + " ('" + (char) (serialPrefix&0xff) + "')");
		System.out.println("#    serialNumber=" + serialNumber);
		
		for ( int i=0 ; i<data.length ; i++ ) {
			System.out.printf("#   data[%d]=0x%02x\n",i,data[i]);
		}
		
		/* you should route your packets to appropriate decoders based on serial number and packet type 
		 * for instance, throw out packets from stations you aren't interested in. Then direct remaining
		 * packets to approriate logging / GUI / gateway functions based on packetType
		 */
		
	}
	
	public void go() {
		is=System.in;
			
		/* setup ReaderWorldData with our existing input stream reader 
		 * You can re-create ReaderWorldData whenever you need to use a new input stream. You would loose
		 * any partially received packets. But that is probably fine. Don't forget to re-add your packet listener(s)
		 * Or you can use the .setInputStream() method to give it a new InputStream. 
		 */
		ReaderWorldData r = new ReaderWorldData(is);
		
		/* register ourselves as a listener. You can have aanything that implements the ReaderWorldData as
		 * a listener. */
		r.addPacketListener(this);

		/* little stub that spins forever processing from standard in until an IOException happens */
		while ( true ) {
			try { 
				r.readForPacket();
			} catch ( IOException e ) {
				System.err.println("# caught IOException in go(). Bailing out.");
				System.exit(1);
			}
		}
		
		
	}


}
