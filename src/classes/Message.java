package classes;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Message {
	public static final int SYSTEM = 0;
	public static final int TEXT = 1;
	public static final int FILE = 2;
	public static final int IMAGE = 3;
	public static final int ONLINE = 4;
	public static final int OFFLINE = 5;
	public static final int MEMBER_LIST = 6;
	
	private String jsonSender;
	private String content;
	private Date date;
	private int type;
	
	public Message() {}
	
	public Message(String jsonSender, String content, Date date, int type) {
		this.jsonSender = jsonSender;
		this.content = content;
		this.date = date;
		this.type = type;
	}
	
	public String getJsonSender() {
		return this.jsonSender;
	}
	
	public String getContent() {
		return this.content;
	}
	
	public Date getDate() {
		return this.date;
	}
	
	public int getType() {
		return this.type;
	}
	
	public void print(String option) {
		try {
			Helper helper = new Helper();
			
			try {
				ObjectMapper mapper = new ObjectMapper();
				Member sender = mapper.readValue(this.jsonSender, Member.class);
				
				if(this.type != Message.ONLINE && this.type != Message.OFFLINE) {
					System.out.println("\nfrom " + sender.getName() + " at " + helper.parseDateToString(this.date, "HH:mm"));
					System.out.println(this.content);
				} else {
					System.out.print(sender.getName());
					
					if(this.type == Message.ONLINE)
						System.out.print(" is online ");
				
					if(this.type == Message.OFFLINE)
						System.out.print(" is offline ");
					
					System.out.println("at " + helper.parseDateToString(this.date, "HH:mm"));
				}
				
			} catch(JsonMappingException e) {
				System.err.println("MESSAGE::print: " + e.getMessage());
			}
		} catch(IOException e) {
			System.err.println("MESSAGE::print: " + e.getStackTrace());
		}
	}
}
