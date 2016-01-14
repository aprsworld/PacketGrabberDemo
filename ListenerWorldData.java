

public interface ListenerWorldData {
	/**
	 * Interface to be implemented to get WorldData packets
	 * @param packetType WorldData packet type (8-bit)
	 * @param serialPrefix Serial number prefix (8-bit, normally 'A' to 'Z')
	 * @param serialNumber Serial number, 0 to 65535
	 * @param data Data portion of packet (does not include headers)
	 * @param timeMilli System time last byte was received
	 */
	public void worldDataPacketReceived(int packetType, int serialPrefix, int serialNumber, int[] data, long timeMilli);
}
