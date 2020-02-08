import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.IOException;
import java.lang.Math;

public class Card{
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