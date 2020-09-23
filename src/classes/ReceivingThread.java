package classes;

import java.io.*;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReceivingThread extends Thread {
	private BufferedReader br;

	public ReceivingThread(BufferedReader br) {
		this.br = br;
	}

	@Override
	public void run() {
		try {
			String jsonReceivedMessage = br.readLine();
			ObjectMapper mapper = new ObjectMapper();
			mapper.setDateFormat(new SimpleDateFormat("HH:mm"));
			
			try {
				Message receivedMessage = mapper.readValue(jsonReceivedMessage, Message.class);
				receivedMessage.print("CLIENT");
				
				// Keep receiving message
				ReceivingThread rt = new ReceivingThread(br);
				rt.start();
				
			} catch(JsonMappingException e) {
				System.err.println("ReceivingThread: " + e.getMessage());
			}

		} catch (Exception e) {
			System.err.println("ReceivingThread: " + e.getStackTrace());
		}
	}
}
