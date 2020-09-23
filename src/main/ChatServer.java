package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import classes.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ChatServer {

	public static void main(String[] args) {
		JFrame window = new JFrame("Chat Server | Not Running");
		window.setLayout(new GridBagLayout());
		window.setSize(400, 400);
		window.setBackground(Color.WHITE);
		window.setLocationRelativeTo(null);

		JPanel buttonPane = new JPanel();
		buttonPane.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		buttonPane.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
		window.add(buttonPane);

		// logo icon
		JLabel logoLabel = new JLabel();
		logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
		buttonPane.add(logoLabel);
		
		// message
		JLabel message = new JLabel("Server is running");
		message.setVisible(false);
		buttonPane.add(message);

		// load logo icon
		String noWifiIconUrl = GlobalConstant.RESOURCE_ICON_DIR + "no-wifi.png";
		String wifiIconUrl = GlobalConstant.RESOURCE_ICON_DIR + "wifi.png";
		int iconWidth = 110, iconHeight = 110;
		
		try {
			BufferedImage bi = ImageIO.read(new File(noWifiIconUrl));
			Image logoImage = bi.getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
			logoLabel.setIcon(new ImageIcon(logoImage));
		} catch (Exception e1) {
			System.err.println("Cannot load image");
		}

		// start button
		JButton button = new JButton("START");
		button.setMaximumSize(new Dimension(200, 200));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setBackground(Color.decode("#2681f2"));
		button.setForeground(Color.WHITE);
		buttonPane.add(button);

		// handle click start button
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					BufferedImage bi = ImageIO.read(new File(wifiIconUrl));
					Image logoImage = bi.getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
					logoLabel.setIcon(new ImageIcon(logoImage));
				} catch (Exception e1) {
					System.err.println("Cannot load image");
				}

				window.setTitle("Chat Server | Running");
				button.setVisible(false);
				logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 15, 0));
				message.setVisible(true);

				WaitingClientThread t = new WaitingClientThread();
				t.start();
			}
		});
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}

}
