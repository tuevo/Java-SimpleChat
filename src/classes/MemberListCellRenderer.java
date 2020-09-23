package classes;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

public class MemberListCellRenderer implements ListCellRenderer<Member> {
	private Member currentMember;

	public MemberListCellRenderer(Member currentMember) {
		this.currentMember = currentMember;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Member> list, Member value, int index,
			boolean isSelected, boolean cellHasFocus) {

		try {
			// member
			JPanel member = new JPanel();
			member.setLayout(new FlowLayout(FlowLayout.LEFT));
			member.setBackground(list.getBackground());
			member.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, list.getBackground()));

			// avatar
			BufferedImage bi = ImageIO
					.read(new File(value.getAvatar().isEmpty() ? GlobalConstant.RESOURCE_AVATAR_DIR + "user.png"
							: GlobalConstant.RESOURCE_AVATAR_DIR + value.getAvatar()));
			Area clip = new Area(new Rectangle(0, 0, bi.getWidth(), bi.getHeight()));
			Area oval = new Area(new Ellipse2D.Double(0, 0, bi.getWidth() - 1, bi.getHeight() - 1));
			clip.subtract(oval);
			Graphics g2d = bi.createGraphics();
			g2d.setClip(clip);
			g2d.setColor(list.getBackground());
			g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
			Image avatar = bi.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
			JLabel avatarLabel = new JLabel(new ImageIcon(avatar));
			member.add(avatarLabel);

			// name
			JLabel name = new JLabel();
			name.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
			member.add(name);
			if (value.getId().equals(currentMember.getId())) {
				name.setText("<html>" + value.getName() + "<span style='color: gray'> (you)</span>");
			} else {
				name.setText(value.getName());
			}

			return member;
		} catch (IOException e) {
			System.err.println("MemberListCellRenderer::getListCellRendererComponent: " + e.getMessage());
			return null;
		}
	}
}
