import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.IOException;
import java.lang.Math;



public class BlackJack{

	public static class Card{
		int value;
		BufferedImage cardimage;

		public Card(String filename, int value) throws IOException{
			this.value = value;
			BufferedImage image = ImageIO.read(new File("PNG-cards-1.3/" + filename));
			final double w = image.getWidth();
			final double h = image.getHeight();
			BufferedImage scaledImage = new BufferedImage((int)Math.round(w * .2),(int)Math.round(h * .2), BufferedImage.TYPE_INT_ARGB);
			final AffineTransform at = AffineTransform.getScaleInstance(.2, .2);
			final AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
			scaledImage = ato.filter(image, scaledImage);
			//final Scale scaler = new Scale(2);
			//scaledImage= scaler.apply(image);
			this.cardimage = scaledImage;
		}
	}

	private static void createWindow(){
		JFrame frame = new JFrame("BlackJack");
		frame.setPreferredSize(new Dimension(1100, 600));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImageIcon card = new ImageIcon("PNG-cards-1.3/10_of_clubs.png");
		try{
			BlackJack.Card testcard = new BlackJack.Card("10_of_clubs.png", 10);
			card = new ImageIcon(testcard.cardimage);
		}
		catch (IOException e){
    		System.out.println("Bad things have happened. The apocolypse is coming");
    	}		
		//ImageIcon card = new ImageIcon("PNG-cards-1.3/10_of_clubs.png");
		
		JLabel textLabel = new JLabel("I'm a label in the window",SwingConstants.CENTER); textLabel.setPreferredSize(new Dimension(300, 100));
		JLabel imagetest = new JLabel(card);
		frame.getContentPane().add(imagetest, BorderLayout.CENTER);
		frame.setLocationRelativeTo(null);
		frame.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point middle = new Point(screenSize.width / 2, screenSize.height / 2);
		Point newLocation = new Point(middle.x - (frame.getWidth() / 2), middle.y - (frame.getHeight() / 2));
		frame.setLocation(newLocation);
		frame.setVisible(true); 
	}

	public static void main(String[] args){
		createWindow();
	}
}