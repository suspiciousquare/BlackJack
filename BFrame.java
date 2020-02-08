import javax.swing.*;
import java.awt.*;

public class BFrame{
	private JFrame frame;

	public BFrame(){
		frame = new JFrame("BlackJack");
		frame.setPreferredSize(new Dimension(1100, 600));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		frame.setLocationRelativeTo(null);
		frame.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point middle = new Point(screenSize.width / 2, screenSize.height / 2);
		Point newLocation = new Point(middle.x - (frame.getWidth() / 2), middle.y - (frame.getHeight() / 2));
		frame.setLocation(newLocation);
		frame.setVisible(true);
	}

	public JFrame getFrame(){
		return frame;
	}
}