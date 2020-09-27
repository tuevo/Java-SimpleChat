package classes;

public class ClientFile {
	private String name;
	private long size;
	private byte[] data;
	
	public ClientFile() {}
	
	public ClientFile(String name, long size, byte[] data) {
		this.name = name;
		this.size = size;
		this.data = data;
	}
	
	public String getName() {
		return this.name;
	}
	
	public long getSize() {
		return this.size;
	}
	
	public byte[] getData() {
		return this.data;
	}
}
