package classes;

import java.awt.*;

public class TopFlowLayout extends FlowLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public TopFlowLayout(int align) {
		super(align);
	}

	@Override
    public void layoutContainer(Container container)
    {
        super.layoutContainer(container);

        Component c = container.getComponent(0);

        int lineStart = getVgap();
        int lineHeight = lineStart + c.getSize().height + 100;

        for (int i = 0; i < container.getComponentCount(); i++)
        {
            c = container.getComponent(i);

            Point p = c.getLocation();
            Dimension d = c.getSize();

            if (p.y < lineHeight) // still on current line
            {
                p.y = lineStart;
                lineHeight = Math.max(lineHeight, lineStart + d.height);
            }
            else  // start a new line
            {
                lineStart = lineHeight + getVgap();
                p.y = lineStart;
                lineHeight = lineStart + d.height;
            }

            p.y = lineStart;
            c.setLocation(p);
        }
    }
}
