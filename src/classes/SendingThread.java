package classes;
import java.io.*;
import java.text.SimpleDateFormat;
import com.fasterxml.jackson.databind.*;

public class SendingThread extends Thread {
	private BufferedWriter bw;
	private Message sentMessage;

	public SendingThread(BufferedWriter bw, Message sentMessage) {
		this.bw = bw;
		this.sentMessage = sentMessage;
	}

	@Override
	public void run() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setDateFormat(new SimpleDateFormat("HH:mm"));
			String jsonSentMessage = mapper.writeValueAsString(this.sentMessage);
			bw.write(jsonSentMessage);
			bw.newLine();
			bw.flush();
		} catch (Exception e) {
			//System.err.println("SendingThread: " + e.getStackTrace());
		}
	}	
}
