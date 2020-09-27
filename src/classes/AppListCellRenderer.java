package classes;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.*;
import javax.swing.*;

public class AppListCellRenderer implements ListCellRenderer<Message> {
	private Member currentMember;

	public AppListCellRenderer(Member currentMember) {
		this.currentMember = currentMember;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
			boolean isSelected, boolean cellHasFocus) {

		try {
			Helper helper = new Helper();
			ObjectMapper mapper = new ObjectMapper();
			Member sender = mapper.readValue(value.getJsonSender(), Member.class);

			// message
			JPanel message = new JPanel();
			message.setBackground(list.getBackground());
			message.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, list.getBackground()));

			// avatar
			BufferedImage bi = ImageIO
					.read(new File(sender.getAvatar().isEmpty() ? GlobalConstant.RESOURCE_AVATAR_DIR + "user.png"
							: GlobalConstant.RESOURCE_AVATAR_DIR + sender.getAvatar()));

			Area clip = new Area(new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));
			Area oval = new Area(new Ellipse2D.Double(0, 0, bi.getWidth() - 1, bi.getHeight() - 1));
			clip.subtract(oval);
			Graphics g2d = bi.createGraphics();
			g2d.setClip(clip);
			g2d.setColor(list.getBackground());
			g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
			Image avatar = bi.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
			JLabel avatarLabel = new JLabel(new ImageIcon(avatar));

			// avatar container
			JPanel avatarContainer = new JPanel();
			avatarContainer.setBackground(list.getBackground());
			avatarContainer.add(avatarLabel);

			// message content
			JPanel messageContent = new JPanel();
			messageContent.setLayout(new BorderLayout());
			messageContent.setBorder(BorderFactory.createMatteBorder(5, 5, 0, 0, list.getBackground()));

			// sender name
			JLabel senderName = new JLabel(
					"<html><p style='padding: 10px 10px 0 10px'>" + sender.getName() + "</span></html>");

			// message text
			String s = value.getContent();
			String html1 = "<html><body style='width: ";
			String html2 = "px; padding: 10px 10px 10px 10px'>";
			JLabel messageText = new JLabel(html1 + "200" + html2 + s);

			if (value.getType() != Message.IMAGE) {

				if (value.getType() == Message.FILE) {
					html2 = "px; padding: 10px 10px 10px 10px; text-decoration: underline; cursor: pointer'>";

					try {
						ClientFile receivedFile = mapper.readValue(s, ClientFile.class);
						messageText = new JLabel(html1 + "200" + html2 + receivedFile.getName());
					} catch (JsonMappingException e) {
						System.err.println("AppListCellRenderer: parse received file failed " + e.getMessage());
					}

					messageText.setForeground(Color.BLUE);
				}

				messageText.setVerticalAlignment(SwingConstants.TOP);
				messageText.setFont(new Font(messageText.getFont().getName(), Font.PLAIN, 14));
				messageContent.add(messageText, BorderLayout.CENTER);
			} else {
				try {
					ClientFile receivedFile = mapper.readValue(s, ClientFile.class);
					BufferedImage receivedImageBuf = ImageIO.read(new ByteArrayInputStream(receivedFile.getData()));
					
					int width = receivedImageBuf.getWidth(), height = receivedImageBuf.getHeight();
					Image receivedImage = receivedImageBuf.getScaledInstance(width, height, Image.SCALE_SMOOTH);
					JLabel uploadedImageLabel = new JLabel(new ImageIcon(receivedImage));
					messageContent.add(uploadedImageLabel, BorderLayout.CENTER);

					// re-customize padding of sender name
					senderName = new JLabel(
							"<html><p style='padding: 10px 10px 20px 10px'>" + sender.getName() + "</span></html>");
					
				} catch (JsonMappingException e) {
					System.err.println("AppListCellRenderer: parse received file failed " + e.getMessage());
				}
			}

			if (sender.getId().equals(this.currentMember.getId())) {
				message.setLayout(new TopFlowLayout(FlowLayout.RIGHT));
				messageContent.setBackground(Color.decode("#d8e7ff"));
				message.add(messageContent);
				message.add(avatarContainer);
			} else {
				message.setLayout(new TopFlowLayout(FlowLayout.LEFT));
				messageContent.setBackground(Color.WHITE);
				message.add(avatarContainer);
				message.add(messageContent);
				messageContent.add(senderName, BorderLayout.PAGE_START);
			}

			// sending time
			JLabel sendingTime = new JLabel("<html><p style='padding: 10px 10px 10px 10px'>"
					+ helper.parseDateToString(value.getDate(), "HH:mm") + "</span></html>");
			sendingTime.setFont(new Font(sendingTime.getFont().getName(), Font.PLAIN, 12));
			sendingTime.setForeground(Color.GRAY);
			messageContent.add(sendingTime, BorderLayout.PAGE_END);

			return message;
		} catch (IOException e) {
			System.err.println("AppListCellRenderer::getListCellRendererComponent: " + e.getMessage());
			return null;
		}
	}
}
