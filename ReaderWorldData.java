
import java.io.IOException;
import java.util.Vector;

import java.io.InputStream;

public class ReaderWorldData {
	final static boolean debug=false;

	final static int maxByteAge=50; /* maximum milliseconds between bytes */
	final static int minPacketLength=10; /* minimum packet length */
	final static int maxPacketLength=255; /* maximum packet length */

	protected Vector<ListenerWorldData> packetListeners;
	protected Vector<Integer> buff;
	protected long lastCharacterAge;
	protected InputStream is;

	/**
	 * Constructor
	 * @param input InputStream to read WorldData from
	 */
	public ReaderWorldData(InputStream input) {
		/* set initial buffer size to expected maximum packet length */
		buff = new Vector<Integer>(maxPacketLength);
		/* vector of listeners that we should notify when we get data */
		packetListeners = new Vector<ListenerWorldData>();
		lastCharacterAge=0;

		is=input;
	}
	
	/**
	 * Set input stream to read from
	 * @param input New input stream to get data from
	 */
	public void setInputStream(InputStream input) {
		is=input;
	}
	
	/** 
	 * Register listener to get received packets
	 * @param ListenerWorldData class that implements ListenerWorldData
	 */
	public void addPacketListener(ListenerWorldData b) {
		packetListeners.add(b);
	}

	/**
	 * Blocking read on input stream (set in constructor) that reads and finds packets and then
	 * sends them to registered listeners.
	 * 
	 * @throws IOException
	 */
	public void readForPacket() throws IOException {
		int c = is.read();

		if ( -1 == c ) {
			throw new IOException();
		}

		long now=System.currentTimeMillis();
		long age=now - lastCharacterAge;
		int validStartPos=-1;


		if ( buff.size() > 0 && age > maxByteAge ) {
			if ( debug )
				System.err.println("# ReaderWorldData clearing buffer (length=" + buff.size() + " age=" + age + ")");
			buff.clear();
		}

		if ( buff.size() > maxPacketLength ) {
			if ( debug )
				System.err.println("# ReaderWorldData clearing buffer (length=" + buff.size() + ") due to it exceeding maximum packet length");
			buff.clear();

		}
		lastCharacterAge=now;


		buff.add(c);

		//	System.err.printf("# ReaderWorldData.readForPacket() got character=0x%02x buff.size()=%d\n",c,buff.size());
		//	System.out.printf("# buff[%d]=0x%02x\n",buff.size(),c);


		/* scan buffer looking for start of packet and subsequent valid CRC */
		for ( int i=0 ; i<buff.size()-minPacketLength ; i++ ) {
			/* packets only start with '#' */
			if ( '#' != buff.elementAt(i) ) {
				continue;
			}



			/* sent/received checksum */
			int rCRC=(int) (((buff.elementAt(buff.size()-2)&0xff)<<8) + (buff.elementAt(buff.size()-1)&0xff));
			/* locally computed checksum */
			if ( debug )
				System.out.printf("# calling crc_chk(buff,%d,%d)\n",i+1,buff.size()-3);
			
			int lCRC=crc_chk(buff, i+1, buff.size()-i-3);

			if ( debug )
				System.out.printf("#  buff.size()=%d rCRC=0x%04x lCRC=0x%04x\n",buff.size(),rCRC,lCRC);

			if ( lCRC == rCRC ) {
				validStartPos=i;
				break;
			}
		}

		/* didn't find a valid start pos */
		if ( -1 == validStartPos ) 
			return;

		if ( debug )
		System.out.println("validStartPos=" + validStartPos);


		/* valid data length is buffer size less our offset, 6 bytes header, and two bytes for CRC */
		int b[]=new int[buff.size()-validStartPos-2-6];

		/* copy vector to integer array starting at validStartPos */
		for ( int i=0 ; (i+validStartPos+6)<buff.size()-2 ; i++ ) {
			b[i]=(int) buff.elementAt(i+validStartPos+6);
		}

		int serialPrefix=buff.elementAt(validStartPos+1);
		int serialNumber=((buff.elementAt(validStartPos+2)<<8) + buff.elementAt(validStartPos+3));
		int packetType=buff.elementAt(validStartPos+5);


		/* send packet to listeners */
		for ( int i=0 ; i<packetListeners.size(); i++ ) {
			packetListeners.elementAt(i).worldDataPacketReceived(packetType, serialPrefix, serialNumber, b, now);
		}

		/* clear for our next pass through */
		buff.clear();
	}

/**
 * Calculate CRC on a vector of 8 bit integers. Using APRS World WorldData CRC format. 
 * @param data Vector of integers (only uses lowest 8 bits)
 * @param start element to start calculation at. For WorldData, the '#' start of packet is ignored
 * @param length number of elements to process. For WorldData, the start '#' and last two bytecs (CRC) are ignored
 * @return 16-bit CRC value
 */
	public static int crc_chk(Vector<Integer> data, int start, int length) {
		int j;
		int reg_crc=0xFFFF;

		for ( int i=start ; i<(length+start) ; i++ ) {
			System.out.printf("CRC[%d]=0x%02x\n",i,data.elementAt(i));

			reg_crc ^= data.elementAt(i);

			for ( j=0 ; j<8 ; j++ ) {
				if ( (reg_crc&0x01) == 1 ) { 
					reg_crc=(reg_crc>>1) ^ 0xA001;
				} else {
					reg_crc=reg_crc>>1;
				}
			}	
		}

		return reg_crc;
	}

}
