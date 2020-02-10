import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.IOException;
import java.lang.Math;

//The purpose of this class is to store an image of a card alongside it's value in Blackjack
//The card images that I found were large for my purposes so I scaled them down to 1/5 their actual size
public class Card{
	int value;
	BufferedImage cardimage;

	public Card(String filename, int value) throws IOException{
		//Set Value
		this.value = value;

		//Pull image using the input filename given
		BufferedImage image = ImageIO.read(new File("PNG-cards-1.3/" + filename));

		//Get the dimensions of the image
		final double w = image.getWidth();
		final double h = image.getHeight();

		//Create a new BufferedImage with 1/5 the dimensions of the input image
		BufferedImage scaledImage = new BufferedImage((int)Math.round(w * .2),(int)Math.round(h * .2), BufferedImage.TYPE_INT_ARGB);
		
		//Create and apply a 1/5 scaling matrix and define a transformation operation with it
		final AffineTransform at = AffineTransform.getScaleInstance(.2, .2);
		final AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
		
		//Apply the transformation operation to the input image
		scaledImage = ato.filter(image, scaledImage);

		//Set the card image to the scaled input image
		this.cardimage = scaledImage;
	}
}