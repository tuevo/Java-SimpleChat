package classes;

public class Member {
	public static final int OFFLINE = 0;
	public static final int ONLINE = 1;
	public static final int CHATTING = 2;
	
	private String id;
	private String name;
	private String avatar;
	private int status;
	private String username;
	private String password;
	
	public Member() {}
	
	public Member(String id, String name, String avatar, int status, String username, String password) {
		this.id = id;
		this.name = name;
		this.avatar = avatar;
		this.status = status;
		this.username = username;
		this.password = password;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getAvatar() {
		return this.avatar;
	}
	
	public int getStatus() {
		return this.status;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
}
