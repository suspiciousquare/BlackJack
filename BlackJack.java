import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class BlackJack extends JFrame implements ActionListener{
	private static BlackJack window = null;

	private Boolean roundfinish = false;
	private Boolean betmade = false;
	private Boolean splitstate = false;

	private static Card[] deck = null;
	private static Card[] gamedeck = null;

	private static Card splitstorage = null;
	private static Card cardback = null;

	private static BufferedImage backimage;

	private int betstorage, playerbet, playerchips, playerscore, dealerstorage, dealerscore, playercardcount, dealercardcount, playersum, dealersum;
	
	private int[] playercards = null;
	private int[] dealercards = null;

	private JLabel[][] cards = null;
	private Border border = BorderFactory.createLineBorder(Color.BLACK, 5);

	private static void ImportDeck(){
		File directory = new File("PNG-cards-1.3");
		String[] cardnames = directory.list();
		deck = new Card[cardnames.length - 1];
		int iterator = 0;
		for (String cardname : cardnames){
			if (cardname.charAt(0) == '1' && cardname.charAt(1) == '.'){
				try{
					cardback = new Card(cardname, 1);

				}
				catch (IOException e){
					System.out.println("The file in question was not found, verify integrity of system files");
				}
			} else{
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

	private static int ArraySum(int address, int[] array){
		int sum = 0;
		for(int i = address; i < array.length; i++){
			sum += array[i];
		}
		return sum;
	}

	public BlackJack(int inputchips, int inputbet, Card inputCard, Card[] inputdeck){
		playerbet=playerscore=dealerscore=playercardcount=dealercardcount=playersum=dealersum=0;

		playerchips = inputchips;
		if (playerchips == 0){
			playerchips = 50;
		}

		if (inputbet != 0){
			betmade = true;
			playerbet = inputbet;
		}

		if (inputdeck.length <= 13){
			ShuffleDeck();
		} else {
			gamedeck = inputdeck;
		}

		if(inputCard != null){
			Card[] cardstorage = new Card[gamedeck.length+1];
			cardstorage[0] = inputCard;
			for (int i = 0; i < gamedeck.length; i++){
				cardstorage[i+1] = gamedeck[i];
			}
			gamedeck = cardstorage;
		}

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

		cards[9][2].setIcon(null);
		cards[9][2].setBorder(border);
		cards[9][2].setText("Chips: " + playerchips);

		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point middle = new Point(screenSize.width / 2, screenSize.height / 2);
		Point newLocation = new Point(middle.x - (getWidth() / 2), middle.y - (getHeight() / 2));
		setLocation(newLocation);
		setVisible(true);

		splitstorage = gamedeck[0];

		if (playerbet == 0){
			GetBet();
		} else {
			SplitStart();
		}
	}

	public static void main(String[] args){
		ImportDeck();
		ShuffleDeck();
		RulesWindow();
	}

	private void StartGame(){
		playercards[0] = gamedeck[0].value;
		playercards[1] = gamedeck[1].value;

		dealercards[0] = gamedeck[2].value;
		dealercards[1] = gamedeck[3].value;

		playercardcount = 2;
		dealercardcount = 2;

		playersum = ArraySum(0, playercards);
		dealersum = ArraySum(0, dealercards);

		cards[0][4].setIcon(new ImageIcon(gamedeck[0].cardimage));
		cards[1][4].setIcon(new ImageIcon(gamedeck[1].cardimage));

		cards[0][0].setIcon(new ImageIcon(cardback.cardimage));
		cards[1][0].setIcon(new ImageIcon(gamedeck[3].cardimage));

		backimage = cardback.cardimage;
		cardback.cardimage = gamedeck[2].cardimage;

		cards[10][3].setIcon(null);
		cards[10][3].setBorder(border);
		cards[10][3].setText("Player: " + Integer.toString(playersum));

		cards[10][1].setIcon(null);
		cards[10][1].setBorder(border);
		cards[10][1].setText("Dealer: " + Integer.toString(ArraySum(1, dealercards)));

		

		gamedeck = Arrays.copyOfRange(gamedeck,4, gamedeck.length);
	}

	private void SplitStart(){
		playercards[0] = gamedeck[0].value;

		dealercards[0] = gamedeck[1].value;
		dealercards[1] = gamedeck[2].value;

		playercardcount = 1;
		dealercardcount = 2;

		playersum = ArraySum(0, playercards);
		dealersum = ArraySum(0, dealercards);

		cards[0][4].setIcon(new ImageIcon(gamedeck[0].cardimage));

		cards[0][0].setIcon(new ImageIcon(cardback.cardimage));
		cards[1][0].setIcon(new ImageIcon(gamedeck[2].cardimage));

		backimage = cardback.cardimage;
		cardback.cardimage = gamedeck[1].cardimage;

		cards[10][3].setIcon(null);
		cards[10][3].setBorder(border);
		cards[10][3].setText("Player: " + Integer.toString(playersum));

		cards[10][1].setIcon(null);
		cards[10][1].setBorder(border);
		cards[10][1].setText("Dealer: " + Integer.toString(ArraySum(1, dealercards)));

		cards[10][2].setIcon(null);
		cards[10][2].setBorder(border);
		cards[10][2].setText("Current Bet:" + playerbet);

		gamedeck = Arrays.copyOfRange(gamedeck,4, gamedeck.length);
	}

	private static void RulesWindow(){
		JFrame rules = new JFrame("Betting Slider");
		rules.setLayout(new GridLayout(7, 1));
		rules.setSize(300, 100);
		rules.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel bets = new JLabel("1. Blackjack starts with players making bets.");
		JLabel dealing = new JLabel("2. Dealer deals 2 cards to the players and two to himself (1 card face up, the other face down).");
		JLabel values = new JLabel("3. All cards count their face value in blackjack. Picture cards cound as 10 and the ace can count as either 1 or 11. Card suits have no meaning in blackjack. The total of any hand is the sum of the card values in the hand.");
		JLabel decision = new JLabel("4. Players must decide whether to stand, hit, surrender, double down, or split.");
		JLabel dealermove = new JLabel("5. The dealer acts last and must hit on 16 or less and stand on 17 through 21.");
		JLabel winning = new JLabel("6. Players win when their hand totals higher than the dealer's hand, or they have 21 or less when the dealer busts (exceeds 21).");

		JButton acknowledge = new JButton("I understand");
		acknowledge.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				window = new BlackJack(50, 0, null, gamedeck);
				rules.dispose();
			}
		});

		rules.add(bets);
		rules.add(dealing);
		rules.add(values);
		rules.add(decision);
		rules.add(dealermove);
		rules.add(winning);
		rules.add(acknowledge);

		rules.pack();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point middle = new Point(screenSize.width / 2, screenSize.height / 2);
		Point newLocation = new Point(middle.x - (rules.getWidth() / 2), middle.y - (rules.getHeight() / 2));
		rules.setLocation(newLocation);
		rules.setVisible(true);
	}

	private void LoseWindow(){
		JFrame lose = new JFrame("You've lost");
		lose.setLayout(new GridLayout(3, 1));
		lose.setSize(300, 100);
		lose.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel loss = new JLabel("You successfully lost all your money gambling!");
		JLabel congrats = new JLabel("Congratulations!");

		JButton hooray = new JButton("Hooray");
		hooray.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				lose.dispose();
			}
		});

		lose.add(loss);
		lose.add(congrats);
		lose.add(hooray);

		lose.pack();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point middle = new Point(screenSize.width / 2, screenSize.height / 2);
		Point newLocation = new Point(middle.x - (lose.getWidth() / 2), middle.y - (lose.getHeight() / 2));
		lose.setLocation(newLocation);
		lose.setVisible(true);
	}

	private void GetBet(){
		JFrame betting = new JFrame("Betting Slider");
		betting.setLayout(new GridLayout(3, 1));
		betting.setSize(300, 100);
		betting.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel sliderstorage = new JPanel();

		JSlider slider = new JSlider(0, playerchips, playerchips/2);
		slider.setMajorTickSpacing(25);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);

		JLabel count = new JLabel("Current Value:" + (slider.getValue()), JLabel.CENTER);

		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e){
				count.setText("Current Value:" + ((JSlider)e.getSource()).getValue());
			}
		});

		JButton makebet = new JButton("Bet");
		makebet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				playerbet = slider.getValue();
				playerchips = playerchips - playerbet;
				betmade = true;
				cards[10][2].setIcon(null);
				cards[10][2].setBorder(border);
				cards[10][2].setText("Current Bet:" + playerbet);
				cards[9][2].setText("Chips: " + playerchips);
				StartGame();
				betting.dispose();
			}
		});

		sliderstorage.add(slider);

		betting.add(sliderstorage);
		betting.add(count);
		betting.add(makebet);

		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point middle = new Point(screenSize.width / 2, screenSize.height / 2);
		Point newLocation = new Point(middle.x - (betting.getWidth() / 2), middle.y - (betting.getHeight() / 2));
		betting.setLocation(newLocation);
		betting.setVisible(true);
	}

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

	private void DealerHit(){
		if (dealersum <= 16){
			dealercards[dealercardcount] = gamedeck[0].value;
			dealersum = ArraySum(0, dealercards);
			if (dealersum > 21){
				ArrayList<Integer> checklist = new ArrayList<Integer>();
				for (int x = 0; x < dealercards.length; x++){
					checklist.add(dealercards[x]);
				}
				if (checklist.contains(11)){
					dealercards[checklist.indexOf(11)] = 1;
					dealersum = ArraySum(0, dealercards);
				}
			}	
			cards[10][1].setText("Dealer: " + Integer.toString(dealersum));
			cards[dealercardcount][0].setIcon(new ImageIcon(gamedeck[0].cardimage));
			gamedeck = Arrays.copyOfRange(gamedeck,1, gamedeck.length);
			dealercardcount += 1;
			DealerHit();
		}
	}

	private void PlayerHit(){
		playercards[playercardcount] = gamedeck[0].value;
		playersum = ArraySum(0, playercards);
		
		if (playersum > 21){
			ArrayList<Integer> checklist = new ArrayList<Integer>();
			for (int x = 0; x < playercards.length; x++){
				checklist.add(playercards[x]);
			}
			if (checklist.contains(11)){
				playercards[checklist.indexOf(11)] = 1;
				playersum = ArraySum(0, playercards);
			}
		}
		cards[10][3].setText("Player: " + Integer.toString(playersum));
		cards[playercardcount][4].setIcon(new ImageIcon(gamedeck[0].cardimage));
		gamedeck = Arrays.copyOfRange(gamedeck,1, gamedeck.length);
		playercardcount += 1;
		if (CheckBust("player")){
			cards[6][2].setIcon(null);
			cards[6][2].setText("You're Bust!");
			RestartButton("Restart");
		}
	}

	private void CheckCards(){
		if (CheckBust("player")){
			cards[6][2].setIcon(null);
			cards[6][2].setText("You're Bust!");
			RestartButton("Restart");
		}else if (playersum == 21 && playersum != dealersum){
			cards[6][2].setIcon(null);
			cards[6][2].setText("You Win!");
			playerchips = playerchips + (int)(2.5 * playerbet);
			RestartButton("Restart");
		}else if (CheckBust("dealer") || (playersum > dealersum)){
			cards[6][2].setIcon(null);
			cards[6][2].setText("You Win!");
			playerchips = playerchips + 2 * playerbet;
			RestartButton("Restart");
		} else if (playersum < dealersum){
			cards[6][2].setIcon(null);
			cards[6][2].setText("You Lose!");
			RestartButton("Restart");
		} else if (playersum == dealersum){
			cards[6][2].setIcon(null);
			cards[6][2].setText("Tie Game!");
			playerchips = playerchips + playerbet;
			RestartButton("Restart");
		}
	}

	private void RestartButton(String title){
		roundfinish = true;

		GridBagConstraints f = new GridBagConstraints();
		JButton restart = new JButton(title);
		f.fill = GridBagConstraints.BOTH;
		f.gridx = 7;
		f.gridy = 2;
		getContentPane().add(restart, f);
		pack();
		restart.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e){
		String button = e.getActionCommand();
		if (roundfinish != true && betmade == true){
			if (button.equals("Hit")){
				PlayerHit();
			} else if (button.equals("Stand")){
				cards[10][1].setText("Dealer: " + Integer.toString(dealersum));
				cards[0][0].setIcon(new ImageIcon(cardback.cardimage));

				DealerHit();
				CheckCards();
			} else if (button.equals("Surrender")){
				cards[6][2].setIcon(null);
				cards[6][2].setText("You Lose!");
				playerchips = playerchips + (int)(0.5 * playerbet);
				RestartButton("Restart");
			} else if (button.equals("Split")){
				//playercards[0] == playercards[1] && 
				if (playercardcount == 2 && playerchips >= playerbet){
					splitstate = true;
					playerchips = playerchips - playerbet;
					cards[9][2].setText("Chips: " + playerchips);
					cards[0][3].setIcon(cards[0][4].getIcon());
					cards[0][4].setIcon(cards[1][4].getIcon());
					cards[1][4].setIcon(cards[2][4].getIcon());
					playercards = Arrays.copyOfRange(playercards, 1, playercards.length);
					playercardcount = 1;
					playersum = ArraySum(0, playercards);
					cards[10][3].setText("Player: " + Integer.toString(playersum));
					gamedeck = Arrays.copyOfRange(gamedeck, 1, gamedeck.length);
					betstorage = playerbet;
				}
			} else if (button.equals("Double")){
				if (playerchips >= playerbet && playercardcount == 2){
					playerchips = playerchips - playerbet;
					playerbet = playerbet * 2;
					cards[10][2].setText("Current Bet:" + playerbet);
					cards[9][2].setText("Chips: " + playerchips);
					PlayerHit();
					DealerHit();
					CheckCards();
				}

			}
		} else if (button.equals("Restart")){
			cardback.cardimage = backimage;
			this.dispose();
			if (playerchips != 0){
				if (splitstate){
					window = new BlackJack(playerchips, playerbet, splitstorage, gamedeck);
				} else {
					window = new BlackJack(playerchips, 0, null, gamedeck);
				}
			} else {
				LoseWindow();
			}
		}
	}
}