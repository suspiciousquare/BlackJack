import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Random;



public class BlackJack extends JFrame implements ActionListener{
	private static BlackJack window = null;
	private static JFrame frame;

	private static Card[] deck = null;
	private static Card[] gamedeck = null;

	private static int playerscore, dealerscore, playercardcount, dealercardcount, playersum, dealersum;
	
	private static int[] playercards = null;
	private static int[] dealercards = null;

	private static JLabel[][] cards = null;

	private static void ImportDeck(){
		File directory = new File("PNG-cards-1.3");
		String[] cardnames = directory.list();
		deck = new Card[cardnames.length];
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
						deck[iterator] = new Card(cardname, 11);
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
	}

	private static void ShuffleDeck(){
		Random rand = new Random();
		Card storage = null;
		int location = 0;
		gamedeck = new Card[deck.length];
		gamedeck = deck;

		for (int i = 0; i < gamedeck.length; i++){
			location = rand.nextInt(gamedeck.length);
			storage = gamedeck[i];
			gamedeck[i] = gamedeck[location];
			gamedeck[location] = storage;
		}
	}

	private static int ArraySum(int[] array){
		int sum = 0;
		for(int i = 0; i < array.length; i++){
			sum += array[i];
		}
		return sum;
	}

	public BlackJack(){
		playerscore=dealerscore=playercardcount=dealercardcount=playersum=dealersum=0;
		BufferedImage greenbackground = null;
		cards = new JLabel[11][5];
		playercards= new int[11];
		dealercards = new int[11];
		try{
			greenbackground = ImageIO.read(new File("greencard.png"));
		}
		catch (IOException e){
			System.out.println("The file in question was not found, verify integrity of system files");
		}
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		setLocationRelativeTo(null);

		getContentPane().setLayout(new GridBagLayout());

		GridBagConstraints a = new GridBagConstraints();
		JButton stand = new JButton("Stand");
		a.fill = GridBagConstraints.BOTH;
		a.gridx = 11;
		a.gridy = 0;
		getContentPane().add(stand, a);
		stand.addActionListener(this);

		GridBagConstraints b = new GridBagConstraints();
		JButton hit = new JButton("Hit");
		b.fill = GridBagConstraints.BOTH;
		b.gridx = 11;
		b.gridy = 1;
		getContentPane().add(hit, b);
		hit.addActionListener(this);

		GridBagConstraints c = new GridBagConstraints();
		JButton surrender = new JButton("Surrender");
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 11;
		c.gridy = 2;
		getContentPane().add(surrender, c);
		surrender.addActionListener(this);
		

		GridBagConstraints d = new GridBagConstraints();
		JButton split = new JButton("Split");
		d.fill = GridBagConstraints.BOTH;
		d.gridx = 11;
		d.gridy = 3;
		getContentPane().add(split, d);
		split.addActionListener(this);
		

		GridBagConstraints e = new GridBagConstraints();
		JButton doubled = new JButton("Double");
		e.fill = GridBagConstraints.BOTH;
		e.gridx = 11;
		e.gridy = 4;
		getContentPane().add(doubled, e);
		pack();
		doubled.addActionListener(this);

		GridBagConstraints f = new GridBagConstraints();
		f.fill = GridBagConstraints.BOTH;
		for (int i = 0; i < 11; i++){
			for(int j = 0; j < 5; j++){
				cards[i][j] = new JLabel(new ImageIcon(greenbackground));
				f.gridx = i;
				f.gridy = j;
				getContentPane().add(cards[i][j], f);
			}
		}
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point middle = new Point(screenSize.width / 2, screenSize.height / 2);
		Point newLocation = new Point(middle.x - (getWidth() / 2), middle.y - (getHeight() / 2));
		setLocation(newLocation);
		setVisible(true);

///////////////////////////////////////////////////////////////////////////////////
		playercards[0] = gamedeck[0].value;
		playercards[1] = gamedeck[1].value;

		dealercards[0] = gamedeck[2].value;
		dealercards[1] = gamedeck[3].value;

		playercardcount = 2;
		dealercardcount = 2;

		playersum = ArraySum(playercards);
		dealersum = ArraySum(dealercards);

		cards[0][4].setIcon(new ImageIcon(gamedeck[0].cardimage));
		cards[1][4].setIcon(new ImageIcon(gamedeck[1].cardimage));
		cards[0][0].setIcon(new ImageIcon(gamedeck[2].cardimage));
		cards[1][0].setIcon(new ImageIcon(gamedeck[3].cardimage));

		cards[10][3].setIcon(null);
		cards[10][3].setText("Player: " + Integer.toString(playersum));
		cards[10][1].setIcon(null);
		cards[10][1].setText("Dealer: " + Integer.toString(dealersum));

		gamedeck = Arrays.copyOfRange(gamedeck,4, gamedeck.length);
	}

//Add popup to make reset
	private Boolean CheckBust(String player){
		if (player == "player"){
			if (playersum > 21){
				cards[6][2].setIcon(null);
				cards[6][2].setText("You're bust!");
				return true;
			}
		} else if (player == "dealer"){
			if (dealersum > 21){
				cards[6][2].setIcon(null);
				cards[6][2].setText("Dealer's bust!");
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args){
		ImportDeck();
		ShuffleDeck();
		window = new BlackJack();
	}

	@Override
	public void actionPerformed(ActionEvent e){
		String button = e.getActionCommand();
		if (button.equals("Hit")){
			playercards[playercardcount] = gamedeck[0].value;
			playersum = ArraySum(playercards);
			cards[10][3].setText("Player: " + Integer.toString(playersum));
			cards[playercardcount][4].setIcon(new ImageIcon(gamedeck[0].cardimage));
			gamedeck = Arrays.copyOfRange(gamedeck,1, gamedeck.length);
			playercardcount += 1;
			if (CheckBust("player")){
				GridBagConstraints f = new GridBagConstraints();
				JButton restart = new JButton("Restart");
				f.fill = GridBagConstraints.BOTH;
				f.gridx = 7;
				f.gridy = 2;
				getContentPane().add(restart, f);
				pack();
				restart.addActionListener(this);
			}
		} else if (button.equals("Restart")){
			this.dispose();
			window = new BlackJack();
		}
	}
}