import java.awt.*;
import javax.swing.*;
import java.io.IOException;
import java.io.File;
import java.util.Arrays;
import java.util.Random;



public class BlackJack{

	private static void createWindow(){
		BFrame window = new BFrame();
	}

	private static Card[] ImportDeck(){
		File directory = new File("PNG-cards-1.3");
		String[] cardnames = directory.list();
		Card[] deck = new Card[cardnames.length];
		int iterator = 0;
		for (String cardname : cardnames){
			Character testing = cardname.charAt(0);
			try{
				if (Character.isDigit(testing)){
					if (testing == '1'){
						deck[iterator] = new Card(cardname, 10);
					} else {
						deck[iterator] = new Card(cardname, Character.getNumericValue(testing));
					}
				} else {
					if (testing == 'a'){
						deck[iterator] = new Card(cardname, 111);
					} else {
						deck[iterator] = new Card(cardname, 10);
					}
				}
			}
			catch (IOException e){
				System.out.println("The file in question was not found, verify integrity of system files");
			}
			iterator += 1;
		}
		return deck;
	}

	private static Card[] ShuffleDeck(Card[] deck){
		Random rand = new Random();
		Card storage = null;
		int location = 0;

		for (int i = 0; i < deck.length; i++){
			location = rand.nextInt(deck.length);
			storage = deck[i];
			deck[i] = deck[location];
			deck[location] = storage;
		}
		return deck;
	}

	private static void StartGame(Card[] deck){
		Card[] gamedeck = ShuffleDeck(deck);
	}

	private static void InitializeWindow(){
		BFrame window = new BFrame();
		window.getFrame().getContentPane().setLayout(new GridBagLayout());

		GridBagConstraints a = new GridBagConstraints();
		JButton stand = new JButton("Stand");
		a.fill = GridBagConstraints.HORIZONTAL;
		a.gridx = 11;
		a.gridy = 0;
		window.getFrame().getContentPane().add(stand, a);
		

		GridBagConstraints b = new GridBagConstraints();
		JButton hit = new JButton("Hit");
		b.fill = GridBagConstraints.HORIZONTAL;
		b.gridx = 11;
		b.gridy = 1;
		window.getFrame().getContentPane().add(hit, b);
		

		GridBagConstraints c = new GridBagConstraints();
		JButton surrender = new JButton("Surrender");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 11;
		c.gridy = 2;
		window.getFrame().getContentPane().add(surrender, c);
		

		GridBagConstraints d = new GridBagConstraints();
		JButton split = new JButton("Split");
		d.fill = GridBagConstraints.HORIZONTAL;
		d.gridx = 11;
		d.gridy = 3;
		window.getFrame().getContentPane().add(split, d);
		

		GridBagConstraints e = new GridBagConstraints();
		JButton doubled = new JButton("Double");
		e.fill = GridBagConstraints.HORIZONTAL;
		e.gridx = 11;
		e.gridy = 4;
		window.getFrame().getContentPane().add(doubled, e);
		window.getFrame().pack();

	}

	public static void main(String[] args){
		Card[] deck = ImportDeck();
		InitializeWindow();
	}
}