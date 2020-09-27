package classes;

import java.io.*;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.*;

public class ForwardingThread extends Thread {
	private Socket fromClient;
	static private Vector<Socket> clients;
	static private Vector<Member> onlineMembers = new Vector<Member>();

	public ForwardingThread(Socket fromClient, Vector<Socket> clients) {
		this.fromClient = fromClient;
		ForwardingThread.clients = new Vector<Socket>(clients);
	}

	@Override
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(this.fromClient.getInputStream(),Charset.forName( "UTF-8" )));
			String jsonReceivedMessage = br.readLine();
			ObjectMapper mapper = new ObjectMapper();
			mapper.setDateFormat(new SimpleDateFormat("HH:mm"));
			
			try {
				Message receivedMessage = mapper.readValue(jsonReceivedMessage, Message.class);
				receivedMessage.print("SERVER");
			
				// send the message to all clients
				for(Socket client : ForwardingThread.clients) {
					if(receivedMessage.getType() != Message.ONLINE && receivedMessage.getType() != Message.OFFLINE) {
						BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(),Charset.forName( "UTF-8" )));	
						SendingThread st = new SendingThread(bw, receivedMessage);
						st.start();
					} else {
						if(receivedMessage.getType() == Message.ONLINE) {
							try {
								Member sender = mapper.readValue(receivedMessage.getJsonSender(), Member.class);
								
								for(int i = 0; i < ForwardingThread.onlineMembers.size(); i++)
									if(ForwardingThread.onlineMembers.get(i).getId().equals(sender.getId()))
										ForwardingThread.onlineMembers.remove(i);
									
								ForwardingThread.onlineMembers.add(sender);
																
							} catch(JsonMappingException e) {
								System.err.println("ForwardingThread::run::online: cannot parse jsonSender");
							}
						}
						
						if(receivedMessage.getType() == Message.OFFLINE) {
							try {
								Member sender = mapper.readValue(receivedMessage.getJsonSender(), Member.class);
					
								for(int i = 0; i < ForwardingThread.onlineMembers.size(); i++)
									if(ForwardingThread.onlineMembers.get(i).getId().equals(sender.getId())) {
										ForwardingThread.onlineMembers.remove(i);
									}
																
							} catch(JsonMappingException e) {
								System.err.println("ForwardingThread::run:offline: cannot parse jsonSender");
							}
						}
						
						// parse each member of online member list to json
						Vector<String> strOnlineMembers = new Vector<String>();
						for(Member mem : ForwardingThread.onlineMembers) {
							try {	
								String jsonMember = mapper.writeValueAsString(mem);
								strOnlineMembers.add(jsonMember);
							} catch(JsonMappingException e) {
								System.err.println("ForwardingThread:run: cannot parse jsonMember");
							}
						}
						
						try {
							String jsonOnlineMembers = mapper.writeValueAsString(strOnlineMembers);
							Message message = new Message(receivedMessage.getJsonSender(), jsonOnlineMembers, new Date(), Message.MEMBER_LIST);
							BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
							SendingThread st = new SendingThread(bw, message);
							st.start();
						} catch(JsonMappingException e) {
							System.err.println("ForwardingThread:run: cannot parse strOnlineMembers");
						}

					}
				}
				
				// keep forwarding this client to all clients
				ForwardingThread ft = new ForwardingThread(this.fromClient, ForwardingThread.clients);
				ft.start();
				
			} catch(JsonMappingException e) {
				System.err.println("ForwardingThread::run: cannot parse jsonReceivedMesasge " + e.getMessage());
			}

		} catch (Exception e) {
			System.out.println("A client has been disconnected");
		}
	}
}
