package main;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.io.*;
import classes.*;
import java.nio.charset.Charset;

public class ChatWidget {
	private static JFrame welcomeFrame;
	private static JFrame registerFrame;
	private static JFrame loginFrame;
	private static JFrame widgetFrame;
	private static Member currentMember;
	private static BufferedReader br;
	private static BufferedWriter bw;
	private static ObjectMapper mapper = new ObjectMapper();

	static void scrollToBottom(JScrollPane scrollPane) {
		JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
		AdjustmentListener downScroller = new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				Adjustable adjustable = e.getAdjustable();
				adjustable.setValue(adjustable.getMaximum());
				verticalBar.removeAdjustmentListener(this);
			}
		};
		verticalBar.addAdjustmentListener(downScroller);
	}

	static void receiveMessage(Vector<Message> messageListDataSource, JList<Message> messageList,
			JScrollPane messageListScrollPane, Vector<Member> memberListDataSource, JList<Member> memberList,
			JLabel sidebarHeaderTitle) {

		Thread receivingMessageThread = new Thread() {
			public void run() {
				try {
					String jsonReceivedMessage = br.readLine();
					mapper.setDateFormat(new SimpleDateFormat("HH:mm"));

					try {
						Message receivedMessage = mapper.readValue(jsonReceivedMessage, Message.class);
						if (receivedMessage.getType() != Message.MEMBER_LIST) {
							messageListDataSource.add(receivedMessage);
							messageList.setListData(messageListDataSource);
							scrollToBottom(messageListScrollPane);
						} else {
							try {
								String[] jsonMembers = mapper.readValue(receivedMessage.getContent(), String[].class);
								memberListDataSource.clear();
								memberListDataSource.add(currentMember);

								for (String jsonMember : jsonMembers) {
									try {
										Member member = mapper.readValue(jsonMember, Member.class);
										if (!member.getId().equals(currentMember.getId())) {
											memberListDataSource.add(member);
										}
									} catch (JsonMappingException e) {
										System.err.println(
												"ReceivingMemberListThread: cannot parse json to member object");
									}
								}

								memberList.setListData(memberListDataSource);

								sidebarHeaderTitle.setText("Online members (" + memberListDataSource.size() + ")");
								if (!sidebarHeaderTitle.isVisible())
									sidebarHeaderTitle.setVisible(true);

							} catch (JsonMappingException e) {
								System.err.println("ReceivingMemberListThread: cannot parse jsonMembers");
							}
						}

						// Keep receiving message
						receiveMessage(messageListDataSource, messageList, messageListScrollPane, memberListDataSource,
								memberList, sidebarHeaderTitle);

					} catch (JsonMappingException e) {
						System.err.println("ReceivingThread: " + e.getMessage());
					}

				} catch (Exception e) {
					System.err.println("ReceivingThread: " + e.getStackTrace());
				}
			}
		};

		receivingMessageThread.start();
	}

	static void sendMessage(Message message) {
		Thread sendingMessageThread = new Thread() {
			public void run() {
				try {
					ObjectMapper mapper = new ObjectMapper();
					mapper.setDateFormat(new SimpleDateFormat("HH:mm"));
					String jsonSentMessage = mapper.writeValueAsString(message);
					bw.write(jsonSentMessage);
					bw.newLine();
					bw.flush();
				} catch (Exception e) {
					System.err.println("SendingThread: " + e.getStackTrace());
				}
			}
		};

		sendingMessageThread.start();
	}

	static void handleCloseWidget(JFrame widgetFrame) {
		try {
			String jsonSender = mapper.writeValueAsString(currentMember);
			Message message = new Message(jsonSender, "I am offline", new Date(), Message.OFFLINE);
			sendMessage(message);
			widgetFrame.setVisible(false);
			widgetFrame.dispose();
			System.exit(0);
		} catch (JsonProcessingException e1) {
			System.err.println("ChatWidget::initWidgetGUI::close window: cannot parse jsonSender");
		}
	}

	static void initWidgetGUI() {
		// Main window
		widgetFrame = new JFrame(currentMember.getName() + " | Chat Widget");
		widgetFrame.setLayout(new BorderLayout());
		widgetFrame.setSize(1300, 800);
		widgetFrame.setLocationRelativeTo(null);

		// Handle close window
		widgetFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				handleCloseWidget(widgetFrame);
			}
		});

		// Side-bar
		JPanel sidebar = new JPanel();
		sidebar.setLayout(new BorderLayout());
		sidebar.setPreferredSize(new Dimension(300, 800));
		sidebar.setBackground(Color.WHITE);
		sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.decode("#eeeeee")));
		widgetFrame.add(sidebar, BorderLayout.LINE_START);

		// Side-bar header
		JPanel sidebarHeader = new JPanel();
		sidebarHeader.setLayout(new FlowLayout(FlowLayout.LEADING));
		sidebarHeader.setPreferredSize(new Dimension(300, 60));
		sidebarHeader.setBackground(Color.WHITE);
		sidebarHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#eeeeee")));
		sidebar.add(sidebarHeader, BorderLayout.PAGE_START);

		// Member list
		String memberListColor = "#ffffff";
		Vector<Member> memberListDataSource = new Vector<Member>();
		JList<Member> memberList = new JList<Member>(memberListDataSource);
		memberList.setBackground(Color.decode(memberListColor));
		memberList.setSelectionBackground(Color.decode(memberListColor));
		memberList.setCellRenderer(new MemberListCellRenderer(currentMember));

		// Member list scroll pane
		JScrollPane memberListScrollPane = new JScrollPane(memberList);
		memberListScrollPane.setPreferredSize(new Dimension(300, 100));
		memberListScrollPane.setBorder(BorderFactory.createEmptyBorder());
		memberListScrollPane.getVerticalScrollBar().setBackground(Color.decode(memberListColor));
		memberListScrollPane.getVerticalScrollBar().setUI(new AppScrollBarRenderer());
		sidebar.add(memberListScrollPane, BorderLayout.CENTER);

		// Side-bar header title
		JLabel sidebarHeaderTitle = new JLabel();
		sidebarHeaderTitle.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 0));
		sidebarHeaderTitle.setVisible(false);
		sidebarHeader.add(sidebarHeaderTitle);

		// Widget
		JPanel widget = new JPanel();
		widget.setLayout(new BorderLayout());
		widget.setPreferredSize(new Dimension(1000, 800));
		widgetFrame.add(widget, BorderLayout.CENTER);

		// Widget header
		JPanel widgetHeader = new JPanel();
		widgetHeader.setLayout(new GridBagLayout());
		widgetHeader.setPreferredSize(new Dimension(1000, 60));
		widgetHeader.setBackground(Color.WHITE);
		widget.add(widgetHeader, BorderLayout.PAGE_START);

		// Widget header title
		JLabel headerTitle = new JLabel("You are chatting with ");
		headerTitle.setFont(new Font(headerTitle.getFont().getName(), Font.PLAIN, 12));
		widgetHeader.add(headerTitle);

		// Widget chatting member list text
		JLabel chattingMemberListText = new JLabel("Everyone");
		widgetHeader.add(chattingMemberListText);

		// Widget message list
		String messageListColor = "#f1f3f4";
		Vector<Message> messageListDataSource = new Vector<Message>();
		JList<Message> messageList = new JList<Message>(messageListDataSource);
		messageList.setBackground(Color.decode(messageListColor));
		messageList.setSelectionBackground(Color.decode(messageListColor));
		messageList.setCellRenderer(new AppListCellRenderer(currentMember));

		// Widget inner scroll pane
		JScrollPane messageListScrollPane = new JScrollPane(messageList);
		messageListScrollPane.setPreferredSize(new Dimension(1000, 820));
		messageListScrollPane.setBorder(BorderFactory.createEmptyBorder());
		messageListScrollPane.getVerticalScrollBar().setBackground(Color.decode(messageListColor));
		messageListScrollPane.getVerticalScrollBar().setUI(new AppScrollBarRenderer());
		widget.add(messageListScrollPane, BorderLayout.CENTER);
		scrollToBottom(messageListScrollPane);

		messageList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (messageList.getSelectedValue() != null) {
					Message selectedMessage = messageList.getSelectedValue();

					if (selectedMessage.getType() == Message.FILE || selectedMessage.getType() == Message.IMAGE) {
						try {
							ClientFile receivedFile = mapper.readValue(selectedMessage.getContent(), ClientFile.class);
							try {
								Member sender = mapper.readValue(selectedMessage.getJsonSender(), Member.class);

								if (sender.getId().equals(currentMember.getId())) {
									Thread openFileThread = new Thread() {
										@Override
										public void run() {
											if (Desktop.isDesktopSupported()) {
												try {
													Desktop.getDesktop().open(new File(
															GlobalConstant.UPLOAD_DIR + receivedFile.getName()));
												} catch (IOException e1) {
													System.out.println(
															"ChatWidget::select message list item: file could not be found");
												}
											} else {
												System.out.println(
														"ChatWidget::select message list item: desktop does not support");
											}
										}
									};
									openFileThread.start();

								} else {
									Thread downloadFileThread = new Thread() {
										@Override
										public void run() {
											try {
												FileOutputStream fout = new FileOutputStream(
														new File(GlobalConstant.DOWNLOAD_DIR + receivedFile.getName()));
												fout.write(receivedFile.getData());
												fout.close();

												JOptionPane.showMessageDialog(widgetFrame,
														"Saved " + receivedFile.getName() + " to download folder!");

											} catch (FileNotFoundException e) {
												System.err.println(
														"ChatWidget::initWidgetFrame: cannot create file to save "
																+ e.getMessage());
											} catch (IOException e) {
												System.err.println(
														"ChatWidget::initWidgetFrame: cannot close file to save "
																+ e.getMessage());
											}
										}
									};
									downloadFileThread.start();
								}

							} catch (JsonProcessingException e1) {
								System.err.println(
										"ChatWidget::initWidgetFrame: cannot parse jsonSender " + e1.getMessage());
							}
						} catch (JsonProcessingException e1) {
							System.err.println(
									"ChatWidget::initWidgetFrame: parse received file failed " + e1.getMessage());
						}

						// keep receiving message from server
						receiveMessage(messageListDataSource, messageList, messageListScrollPane, memberListDataSource,
								memberList, sidebarHeaderTitle);
					}

					messageList.clearSelection();
				}
			}
		});

		// Widget footer
		JPanel widgetFooter = new JPanel();
		widgetFooter.setLayout(new BorderLayout());
		widgetFooter.setPreferredSize(new Dimension(1000, 120));
		widgetFooter.setBackground(Color.decode("#f9f8fd"));
		widget.add(widgetFooter, BorderLayout.PAGE_END);

		// Widget tool-bar
		JPanel widgetToolbar = new JPanel();
		widgetToolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
		widgetToolbar.setPreferredSize(new Dimension(1000, 45));
		widgetToolbar.setBackground(Color.decode("#f9f8fd"));
		widgetToolbar.setBorder(BorderFactory.createEmptyBorder(8, 8, 5, 5));
		widgetFooter.add(widgetToolbar, BorderLayout.PAGE_START);

		// file chooser
		JFileChooser fileChooser = new JFileChooser();

		// Send file button
		try {
			BufferedImage sendFileIconBuf = ImageIO.read(new File(GlobalConstant.RESOURCE_ICON_DIR + "attachment.png"));
			Image attachmentIcon = sendFileIconBuf.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
			JButton chooseFileButton = new JButton();
			chooseFileButton.setIcon(new ImageIcon(attachmentIcon));
			chooseFileButton.setToolTipText("Send a file");
			chooseFileButton.setBackground(null);
			chooseFileButton.setBorder(BorderFactory.createEmptyBorder());
			widgetToolbar.add(chooseFileButton);

			chooseFileButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int returnVal = fileChooser.showOpenDialog(widgetFrame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fileChooser.getSelectedFile();

						try {
							String fileName = file.getName();
							String fileType = fileName.substring(fileName.lastIndexOf("."), fileName.length());
							long fileSize = file.length();

							if (fileSize > 1024 * 1024) {
								JOptionPane.showMessageDialog(widgetFrame,
										"Allowed file size must be less than or equal 1MB");
								return;
							}

							int messageType = Message.FILE;
							if (fileType.equals(".png") || fileType.equals(".jpg") || fileType.equals(".jpeg")
									|| fileType.equals(".gif") || fileType.equals(".ico") || fileType.equals(".svg")) {
								messageType = Message.IMAGE;
							}

							// save file to upload folder
							FileInputStream fin = new FileInputStream(file);
							FileOutputStream fout = new FileOutputStream(
									new File(GlobalConstant.UPLOAD_DIR + fileName));

							int i = 0;
							byte[] data = new byte[(int) fileSize];

							while ((i = fin.read(data)) != -1) {
								fout.write(data, 0, i);
							}

							fin.close();
							fout.close();

							// send file to server
							ClientFile sentFile = new ClientFile(fileName, fileSize, data);
							String jsonSentFile = mapper.writeValueAsString(sentFile);

							String jsonSender = mapper.writeValueAsString(currentMember);
							Message message = new Message(jsonSender, jsonSentFile, new Date(), messageType);
							sendMessage(message);

						} catch (Exception ex) {
							System.out.println("problem accessing file: " + file.getAbsolutePath());
						}
					} else {
						System.out.println("File access cancelled by user.");
					}
				}
			});

		} catch (Exception e) {
			System.err.println("ChatWidget::initWidgetGUI: Cannot load image");
		}

		// Widget message text input
		JTextArea messageInput = new JTextArea();
		AppTextPrompt messageInputTextPrompt = new AppTextPrompt("Type message...", messageInput);
		messageInputTextPrompt.setBorder(BorderFactory.createEmptyBorder(-40, 0, 0, 0));
		messageInput.setLineWrap(true);
		messageInput.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
		messageInput.setBackground(Color.WHITE);
		messageInput.setFont(new Font(messageInput.getFont().getName(), Font.PLAIN, 14));
		widgetFooter.add(messageInput, BorderLayout.CENTER);

		messageInput.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String messageContent = messageInput.getText().replace("\n", "").replace("\r", "");
					messageInput.setText("");

					try {
						if (!messageContent.isEmpty()) {
							String jsonSender = mapper.writeValueAsString(currentMember);
							Message message = new Message(jsonSender, messageContent, new Date(), Message.TEXT);
							sendMessage(message);
						}
					} catch (JsonProcessingException e1) {
						System.err.println("Chat Widget::initWidgetGUI::enter message input: cannot parse jsonSender");
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		// choose emoji box
		String[] emojis = new String[] { "😀", "😍", "😔", "😘", "😠", "😜", "😪", "😑", "😎", "😌", "😵", "🙃", "😣",
				"😔", "😢" };
		JFrame emojiBox = new JFrame("Emoji");
		emojiBox.setLayout(new FlowLayout(FlowLayout.LEFT));
		emojiBox.setSize(305, 160);
		emojiBox.setLocationRelativeTo(null);

		for (String e : emojis) {
			JButton emojiButton = new JButton(e);
			emojiButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
			emojiButton.setBackground(Color.WHITE);
			emojiButton.setBorderPainted(false);
			emojiButton.setFocusPainted(false);
			emojiBox.add(emojiButton);

			emojiButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String emoji = emojiButton.getText();
					int pos = messageInput.getCaretPosition();
					String oldText = messageInput.getText();
					String newText = pos < oldText.length()
							? oldText.substring(0, pos) + emoji + oldText.substring(pos, oldText.length())
							: oldText + emoji;
					messageInput.setText(newText);
					messageInput.setCaretPosition(pos + 2);
				}
			});
		}

		// open choose emoji box button
		try {
			BufferedImage chooseEmojiIconBuf = ImageIO.read(new File(GlobalConstant.RESOURCE_ICON_DIR + "smile.png"));
			Image chooseEmojiIcon = chooseEmojiIconBuf.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
			JButton chooseEmojiButton = new JButton();
			chooseEmojiButton.setPreferredSize(new Dimension(50, 25));
			chooseEmojiButton.setIcon(new ImageIcon(chooseEmojiIcon));
			chooseEmojiButton.setToolTipText("Select emoji");
			chooseEmojiButton.setBackground(null);
			chooseEmojiButton.setBorder(BorderFactory.createEmptyBorder());
			widgetToolbar.add(chooseEmojiButton);

			chooseEmojiButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					emojiBox.setVisible(true);
				}
			});

		} catch (Exception e) {
			System.err.println("ChatWidget::initWidgetGUI: Cannot load image");
		}

		// Send button
		JButton sendButton = new JButton("SEND");
		sendButton.setPreferredSize(new Dimension(120, 0));
		sendButton.setBackground(Color.WHITE);
		sendButton.setForeground(Color.decode("#3829f9"));
		sendButton.setBorderPainted(false);
		sendButton.setFocusPainted(false);
		widgetFooter.getRootPane().setDefaultButton(sendButton);
		widgetFooter.add(sendButton, BorderLayout.LINE_END);

		// handle click send button
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String messageContent = messageInput.getText().replace("\n", "").replace("\r", "");
				messageInput.setText("");

				try {
					if (!messageContent.isEmpty()) {
						String jsonSender = mapper.writeValueAsString(currentMember);
						Message message = new Message(jsonSender, messageContent, new Date(), Message.TEXT);
						sendMessage(message);
					}
				} catch (JsonProcessingException e1) {
					System.err.println("Chat Widget::initWidgetGUI::click send button: cannot parse jsonSender");
				}
			}
		});

		try {
			// Connect to chat server
			Socket socket = new Socket("localhost", 3200);
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName( "UTF-8" )));
			bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName( "UTF-8" )));

			// Start receiving message from server
			receiveMessage(messageListDataSource, messageList, messageListScrollPane, memberListDataSource, memberList,
					sidebarHeaderTitle);

			// send online message to server
			try {
				String jsonSender = mapper.writeValueAsString(currentMember);
				Message onlineMessage = new Message(jsonSender, "I am online", new Date(), Message.ONLINE);
				sendMessage(onlineMessage);
			} catch (JsonProcessingException e1) {
				System.err.println("ChatWidget::initWidgetGUI::send online message: cannot parse jsonSender");
			}

			widgetFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			widgetFrame.setVisible(true);

		} catch (Exception e) {
			welcomeFrame.setVisible(true);
			loginFrame.setVisible(true);
			JOptionPane.showMessageDialog(loginFrame, "Please start chat server before chatting", "WARNING", 1);
		}
	}

	static void initRegisterGUI() {
		String memberId = UUID.randomUUID().toString();

		registerFrame = new JFrame("Create an Account");
		registerFrame.setLayout(new GridBagLayout());
		registerFrame.setSize(400, 400);
		registerFrame.setLocationRelativeTo(null);

		JPanel form = new JPanel();
		form.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		form.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
		registerFrame.add(form);

		JPanel avatarContainer = new JPanel();
		avatarContainer.setLayout(new GridBagLayout());

		JLabel avatarLabel = new JLabel();
		avatarContainer.add(avatarLabel);

		BufferedImage bi;
		try {
			bi = ImageIO.read(new File(GlobalConstant.RESOURCE_AVATAR_DIR + "user.png"));
			Area clip = new Area(new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));
			Area oval = new Area(new Ellipse2D.Double(0, 0, bi.getWidth() - 1, bi.getHeight() - 1));
			clip.subtract(oval);
			Graphics g2d = bi.createGraphics();
			g2d.setClip(clip);
			g2d.setColor(registerFrame.getBackground());
			g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
			Image avatar = bi.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
			avatarLabel.setIcon(new ImageIcon(avatar));
		} catch (IOException e2) {
			System.err.println("ChatWidget::initRegisterGUI: cannot load member default image");
		}

		form.add(avatarContainer);
		form.add(Box.createRigidArea(new Dimension(0, 30)));

		// input fullname
		JPanel fullnamePane = new JPanel();
		fullnamePane.setLayout(new FlowLayout(FlowLayout.LEFT));
		form.add(fullnamePane);
		JLabel fullnameLabel = new JLabel("Full name");
		fullnameLabel.setPreferredSize(new Dimension(85, 25));
		fullnamePane.add(fullnameLabel);
		JTextField fullnameInput = new JTextField();
		fullnameInput.setPreferredSize(new Dimension(200, 25));
		fullnamePane.add(fullnameInput);

		// input avatar
		JPanel inputAvatarPane = new JPanel();
		inputAvatarPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		form.add(inputAvatarPane);
		JLabel inputAvatarLabel = new JLabel("Avatar");
		inputAvatarLabel.setPreferredSize(new Dimension(85, 25));
		inputAvatarPane.add(inputAvatarLabel);
		JTextField avatarInput = new JTextField();
		avatarInput.setPreferredSize(new Dimension(135, 25));
		avatarInput.setEditable(false);
		inputAvatarPane.add(avatarInput);

		// hide avatar url label
		JLabel avatarNameLabel = new JLabel();

		// choose file button
		JFileChooser avatarChooser = new JFileChooser();
		JButton chooseFileButton = new JButton("Browse");
		chooseFileButton.setPreferredSize(new Dimension(60, 25));
		chooseFileButton.setBackground(Color.decode("#2681f2"));
		chooseFileButton.setForeground(Color.WHITE);
		chooseFileButton.setFocusPainted(false);
		chooseFileButton.setBorder(BorderFactory.createEmptyBorder());
		inputAvatarPane.add(chooseFileButton);

		// handle click choose file button
		chooseFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// upload avatar image
				int returnVal = avatarChooser.showOpenDialog(registerFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = avatarChooser.getSelectedFile();

					try {
						// save file to upload folder
						String fileName = file.getName();
						String fileType = fileName.substring(fileName.lastIndexOf("."), fileName.length());

						// check file type
						if (!fileType.equals(".png") && !fileType.equals(".jpg") && !fileType.equals(".jpeg")
								&& !fileType.equals(".gif") && !fileType.equals(".ico") && !fileType.equals(".svg")) {
							JOptionPane.showMessageDialog(registerFrame, "Please choose an image");
							return;
						}

						// load filename to avatar input
						avatarInput.setText(fileName);

						// upload avatar image
						String avatarName = memberId + fileType;
						String avatarPath = GlobalConstant.RESOURCE_AVATAR_DIR + avatarName;
						avatarNameLabel.setText(avatarName);

						FileInputStream fin = new FileInputStream(file);
						FileOutputStream fout = new FileOutputStream(new File(avatarPath));
						int i = 0;
						while ((i = fin.read()) != -1) {
							fout.write((char) i);
						}
						fin.close();
						fout.close();

						// display avatar image
						BufferedImage bi = ImageIO.read(new File(avatarPath));
						Area clip = new Area(new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));
						Area oval = new Area(new Ellipse2D.Double(0, 0, bi.getWidth() - 1, bi.getHeight() - 1));
						clip.subtract(oval);
						Graphics g2d = bi.createGraphics();
						g2d.setClip(clip);
						g2d.setColor(registerFrame.getBackground());
						g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
						Image avatar = bi.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
						avatarLabel.setIcon(new ImageIcon(avatar));

					} catch (Exception ex) {
						System.out.println("problem accessing file: " + file.getAbsolutePath());
					}
				} else {
					System.out.println("File access cancelled by user.");
					return;
				}
			}
		});

		// input username
		JPanel usernamePane = new JPanel();
		usernamePane.setLayout(new FlowLayout(FlowLayout.LEFT));
		form.add(usernamePane);
		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setPreferredSize(new Dimension(85, 25));
		usernamePane.add(usernameLabel);
		JTextField usernameInput = new JTextField();
		usernameInput.setPreferredSize(new Dimension(200, 25));
		usernamePane.add(usernameInput);

		// input password
		JPanel passwordPane = new JPanel();
		passwordPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		form.add(passwordPane);
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setPreferredSize(new Dimension(85, 25));
		passwordPane.add(passwordLabel);
		JPasswordField passwordInput = new JPasswordField();
		passwordInput.setPreferredSize(new Dimension(200, 25));
		passwordPane.add(passwordInput);

		form.add(Box.createRigidArea(new Dimension(0, 30)));

		// submit button
		JButton submitButton = new JButton("REGISTER");
		submitButton.setBorderPainted(false);
		submitButton.setFocusPainted(false);
		submitButton.setBackground(Color.decode("#2681f2"));
		submitButton.setForeground(Color.WHITE);
		form.add(submitButton);
		form.getRootPane().setDefaultButton(submitButton);

		// handle click submit button
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// check empty field
				if (fullnameInput.getText().isEmpty() || usernameInput.getText().isEmpty()
						|| passwordInput.getPassword().toString().isEmpty()) {
					JOptionPane.showMessageDialog(form, "Please complete all fields");
					return;
				}

				MemberService ms = new MemberService();
				Member member = ms.findByUsername(usernameInput.getText());

				// check duplicate user
				if (member != null) {
					JOptionPane.showMessageDialog(form, "Username is in use");
					return;
				}

				try {
					Member newMember = ms.add(memberId, fullnameInput.getText(),
							avatarInput.getText().isEmpty() ? "user.png" : avatarNameLabel.getText(),
							usernameInput.getText(), new String(passwordInput.getPassword()));
					currentMember = newMember;
				} catch (JsonProcessingException e1) {
					System.err.println("ChatWidget::initRegisterGUI: " + e1.getMessage());
				}

				welcomeFrame.setVisible(false);
				registerFrame.setVisible(false);
				initWidgetGUI();
			}
		});

		registerFrame.setVisible(true);
	}

	static void initLoginGUI() {
		loginFrame = new JFrame("Join Chatting Room");
		loginFrame.setLayout(new GridBagLayout());
		loginFrame.setSize(400, 400);
		loginFrame.setLocationRelativeTo(null);

		JPanel form = new JPanel();
		form.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		form.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
		loginFrame.add(form);

		JPanel avatarContainer = new JPanel();
		avatarContainer.setLayout(new GridBagLayout());

		JLabel avatarLabel = new JLabel();
		avatarContainer.add(avatarLabel);

		BufferedImage bi;
		try {
			bi = ImageIO.read(new File(GlobalConstant.RESOURCE_ICON_DIR + "lock.png"));
			Image avatar = bi.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
			avatarLabel.setIcon(new ImageIcon(avatar));
		} catch (IOException e2) {
			System.err.println("ChatWidget::initLoginGUI: cannot load login icon");
		}

		form.add(avatarContainer);
		form.add(Box.createRigidArea(new Dimension(0, 30)));

		// input username
		JPanel usernamePane = new JPanel();
		usernamePane.setLayout(new FlowLayout(FlowLayout.LEFT));
		form.add(usernamePane);
		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setPreferredSize(new Dimension(85, 25));
		usernamePane.add(usernameLabel);
		JTextField usernameInput = new JTextField();
		usernameInput.setPreferredSize(new Dimension(200, 25));
		usernamePane.add(usernameInput);

		// input password
		JPanel passwordPane = new JPanel();
		passwordPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		form.add(passwordPane);
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setPreferredSize(new Dimension(85, 25));
		passwordPane.add(passwordLabel);
		JPasswordField passwordInput = new JPasswordField();
		passwordInput.setPreferredSize(new Dimension(200, 25));
		passwordPane.add(passwordInput);

		form.add(Box.createRigidArea(new Dimension(0, 30)));

		// submit button
		JButton submitButton = new JButton("CONTINUE");
		submitButton.setBorderPainted(false);
		submitButton.setFocusPainted(false);
		submitButton.setBackground(Color.decode("#2681f2"));
		submitButton.setForeground(Color.WHITE);
		form.add(submitButton);
		form.getRootPane().setDefaultButton(submitButton);

		// handle click submit button
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// check empty field
				if (usernameInput.getText().isEmpty() || passwordInput.getPassword().toString().isEmpty()) {
					JOptionPane.showMessageDialog(form, "Please complete all fields");
					return;
				}

				// authentication
				MemberService ms = new MemberService();
				Member user = ms.findByUsername(usernameInput.getText());

				if (user == null) {
					JOptionPane.showMessageDialog(form, "User does not exist");
					return;
				}

				String password = new String(passwordInput.getPassword());
				String currentPassword = user.getPassword();
				if (!password.equals(currentPassword)) {
					JOptionPane.showMessageDialog(form, "Wrong username or password");
					return;
				}

				currentMember = user;

				welcomeFrame.setVisible(false);
				loginFrame.setVisible(false);
				initWidgetGUI();
			}
		});

		loginFrame.setVisible(true);
	}

	static void initWelcomeGUI() {
		welcomeFrame = new JFrame("Chat for Fun");
		welcomeFrame.setLayout(new GridBagLayout());
		welcomeFrame.setSize(450, 500);
		welcomeFrame.setLocationRelativeTo(null);

		JPanel buttonGroup = new JPanel();
		buttonGroup.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		buttonGroup.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		buttonGroup.setLayout(new BoxLayout(buttonGroup, BoxLayout.Y_AXIS));
		welcomeFrame.add(buttonGroup);

		try {
			BufferedImage bi = ImageIO.read(new File(GlobalConstant.RESOURCE_ICON_DIR + "chatting.png"));
			Image logoImage = bi.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
			JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
			logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 40, 30, 0));
			buttonGroup.add(logoLabel);

		} catch (Exception e) {
			System.err.println("ChatWidget::showWelcomeGUI: Cannot load image");
		}

		// register button
		JButton registerButton = new JButton("Create an Account");
		registerButton.setMaximumSize(new Dimension(200, 200));
		registerButton.setBorderPainted(false);
		registerButton.setFocusPainted(false);
		registerButton.setBackground(Color.decode("#2681f2"));
		registerButton.setForeground(Color.WHITE);
		buttonGroup.add(registerButton);

		// handle click register button
		registerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initRegisterGUI();
			}
		});

		buttonGroup.add(Box.createRigidArea(new Dimension(0, 15)));

		// join button
		JButton joinButton = new JButton("Join Chatting Room");
		joinButton.setBorderPainted(false);
		joinButton.setFocusPainted(false);
		joinButton.setBackground(Color.white);
		buttonGroup.add(joinButton);

		// handle click join button
		joinButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initLoginGUI();
			}
		});

		welcomeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		welcomeFrame.setVisible(true);
	}

	public static void main(String[] args) {
		initWelcomeGUI();
	}

}
