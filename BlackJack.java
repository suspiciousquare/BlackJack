////////////////////////////////////////
//Name: Clay Fonseca
//Date: 2/9/2019
//Program Title: BlackJack
//Description: This is an implemention of the card game Blackjack using a GUI built with JFrames.
//I decided at the start of this project that I wanted to explore non command line java programming so this is my first foray into this area
//There are likely many things that could be restructured or done more efficiently
//Source for game rules: https://www.888casino.com/blog/blackjack-strategy-guide/how-to-play-blackjack
////////////////////////////////////////

//Both awt and wing are used for creating and handling the GUI
//awt is used for window handling, button handling, and image storage
//Swing is used to handle formatting and other non-button input methods
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.border.Border;

//ImageIO is only used to read my greencard image file used to make the green background for the play area
import javax.imageio.ImageIO;

//Here for the same purpose as ImageIO
import java.io.File;

//IOException is needed for making new card variables and importing new files in case of failure
import java.io.IOException;

//Array utilities are used to copy shorter sections of existing arrays, essentially removing cards from the top of our deck
import java.util.Arrays;

//Random is used in shuffling our deck
import java.util.Random;

//ArrayList is used to find specific values in our current list of cards to change the values of aces, avoiding busts
import java.util.ArrayList;

public class BlackJack extends JFrame implements ActionListener{
	//Window is our primary variable used to create our initial board and is remade with each new hand
	private static BlackJack window = null;

	//Switches for various gamestates
	//Roundfinish -> Has the hand finished (Disables all but reset button)
	//Betmade -> Used with splitting, identifies if a bet has already been made for this hand (Disables buttons and opens betting window if false)
	//Splitstate -> Also for splitting, causes reset button to start a new round with some existing parameters based on the split
	private Boolean roundfinish = false;
	private Boolean betmade = false;
	private Boolean splitstate = false;

	//Deck holds the ordered deck once it's been imported
	//Gamedeck is filled by randomly shuffling Deck, is reduced in size as cards are drawn
	private static Card[] deck = null;
	private static Card[] gamedeck = null;

	//SplitStorage holds the card that will be used for the next hand after a split is done
	//CardBack stores the scaled image of a card back that is used to obscure the first card the dealer deals themselves
	//CardBack is a card because the image scaling function is built into the Card class
	private static Card splitstorage = null;
	private static Card cardback = null;

	//BackImage is used to store the scaled image of the back of the card so it can be restored to CardBack after the dealer's card is revealed
	//This is needed because later on the image attached to CardBack is replaced with that of the dealer's first card so that it can be revealed easily
	private static BufferedImage backimage;

	//Playerbet: Current player bet, Playerchips: player's current chip total, Playercardcount: total number of cards the player currently has
	//Dealercardcount: total number of cards the dealer currently has, Playersum: sum of the values of the player's cards, Dealersum: sum of the values of the dealer's cards
	private int playerbet, playerchips, playercardcount, dealercardcount, playersum, dealersum;
	
	//Playercards -> Array of all of the values of the player's current cards
	//Dealercards -> Array of all of the values of the dealer's current cards
	private int[] playercards = null;
	private int[] dealercards = null;

	//The grid used for the game board is a 2-D array of JLabels that can be modified by any function in this class
	private JLabel[][] cards = null;

	//This border is used to distinguish text cells from the rest of the board
	private Border border = BorderFactory.createLineBorder(Color.BLACK, 5);

	//ImportDeck identifies all of the relevant card images from the PNG-cards-1.3 folder and initializes Card objects with them
	//The card back is stored in cardback and the remaining 52 are stored in Deck
	private static void ImportDeck(){
		File directory = new File("PNG-cards-1.3");
		String[] cardnames = directory.list();
		deck = new Card[cardnames.length - 1];
		int iterator = 0;

		//For each name in the list of names in PNG-cards-1.3
		for (String cardname : cardnames){
			//Here we check to see if we have the card back image
			if (cardname.charAt(0) == '1' && cardname.charAt(1) == '.'){
				try{
					cardback = new Card(cardname, 1);

				}
				catch (IOException e){
					System.out.println("The file in question was not found, verify integrity of system files");
				}
			//If not the card back image, we just initialize an element of Deck with the card image
			} else{
				//Cards files are identified using the first character in their filename
				Character testing = cardname.charAt(0);
				try{
					if (Character.isDigit(testing)){
						//Cards are given values based on their name. If it starts with 1, it must be 10, every other number is single digit so can be used as it's own value
						if (testing == '1'){
							deck[iterator] = new Card(cardname, 10);
						} else {
							deck[iterator] = new Card(cardname, Character.getNumericValue(testing));
						}
					} else {
						//All filenames that start with letters must be 10 outside of the ace, which is the only card name to start with 'a'
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

	//ShuffleDeck sets gamedeck to be a copy of deck. Afterwards, it randomly mixes gamedeck by swapping the contents of each of it's addresses with another randomly generated address
	//The random generation is done using Random, and restricted to be within the length of GameDeck
	private static void ShuffleDeck(){
		Random rand = new Random();
		Card storage = null;
		int location = 0;
		gamedeck = new Card[deck.length];
		gamedeck = deck;

		//For each address in gamedeck, swap it's contents with that of a random address in gamedeck
		for (int i = 0; i < gamedeck.length; i++){
			location = rand.nextInt(gamedeck.length);
			storage = gamedeck[i];
			gamedeck[i] = gamedeck[location];
			gamedeck[location] = storage;
		}
	}

	//ArraySum is just a simple function to take in an array and a starting address, and sum everything in that array after the given starting address
	//The address argument is used to skip the first card a dealer has when calculating their displayed total, concealing it's value from the player until it is revealed
	private static int ArraySum(int address, int[] array){
		int sum = 0;
		for(int i = address; i < array.length; i++){
			sum += array[i];
		}
		return sum;
	}

	//BlackJack is the constructor for objects of the BlackJack class. it initializes the main JFrame and all of it's contents, as well as taking arguments to preserve the gamestate from hand to hand
	//Preserving the gamestate is especially important when splitting
	//Organization within the frame is done using GridBagLayout, and all button actions are handled in an override ActionPerformed function at the bottom of the program
	//InputChips: Players current chips
	//InputBet: This hand's bet, used when splitting, since bet is defined when the split is performed
	//InputCard: Also for splitting, sets the First card of the player's hand
	//InputDeck: Makes sure the deck is consistent hand to hand by passing gamedeck to new instances of BlackJack
	public BlackJack(int inputchips, int inputbet, Card inputCard, Card[] inputdeck){
		//Initializing our instance variables from the top
		playerbet=playercardcount=dealercardcount=playersum=dealersum=0;
		cards = new JLabel[11][5];
		playercards= new int[11];
		dealercards = new int[11];

		playerchips = inputchips;

		//If we have a bet argument, we won't need to get a bet from the player to start the round
		if (inputbet != 0){
			betmade = true;
			playerbet = inputbet;
		}

		//Normally, BlackJack decks are shuffled once 75% of the deck is drawn. To implement that here, gamedeck is refilled with shuffled cards if less than 25% of 52 (13) cards are left
		if (inputdeck.length <= 13){
			ShuffleDeck();
		} else {
			gamedeck = inputdeck;
		}

		//In order for the player to properly have the split card, we add it to the top of gamedeck
		if(inputCard != null){
			Card[] cardstorage = new Card[gamedeck.length+1];
			cardstorage[0] = inputCard;
			for (int i = 0; i < gamedeck.length; i++){
				cardstorage[i+1] = gamedeck[i];
			}
			gamedeck = cardstorage;
		}

		//Here we take in our greenbackground image that we'll use to make our grid board green
		BufferedImage greenbackground = null;

		try{
			greenbackground = ImageIO.read(new File("greencard.png"));
		}
		catch (IOException e){
			System.out.println("The file in question was not found, verify integrity of system files");
		}


		//Set behavior for closing window
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		
		//Set our layout to be GridBagLayout
		getContentPane().setLayout(new GridBagLayout());


		//In this section, we initialize all of our primary buttons, giving them each their own set of GridBagConstraints to ensure they end up in the right place and to make it easier to differentiate their locations
		//All buttons are added to the window using their location constraints, and all buttons have their ActionListener set to the current object. These actions are handled later in the program
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

		//Here we fill in our game board with green
		//This is done procedurally with a 'for' loop so I'm less concerned about having separate GridBagConstraints
		//All icons in cards are set to be the green background image we imported to make our green board
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

		//Our first display through the green background, we get the player's current chips and display them on the right side of the field by removing one of the green background images, giving that space a border, and labeling it with the player's chip count
		cards[9][2].setIcon(null);
		cards[9][2].setBorder(border);
		cards[9][2].setText("Chips: " + playerchips);

		pack();

		//After packing, we make the window show up right in the center of the screen by lining up the center of the window with the center of the rectangular display screen
		//This is done for every window that this program generates
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point middle = new Point(screenSize.width / 2, screenSize.height / 2);
		Point newLocation = new Point(middle.x - (getWidth() / 2), middle.y - (getHeight() / 2));
		setLocation(newLocation);
		setVisible(true);

		//If there will be a split, it will be with the very first card, so we just grab that here before we do anything with the deck
		splitstorage = gamedeck[0];

		//If there has been a split, playerbet will have a value and so will skip getting the player's bet
		if (playerbet == 0){
			GetBet();
		} else {
			SplitStart();
		}
	}

	//In main, we collect all the cards into deck, shuffle them into gamedeck, and then launch our window with the rules of the game
	public static void main(String[] args){
		ImportDeck();
		ShuffleDeck();
		RulesWindow();
	}

	//StartGame starts a hand normally
	//A normal hand starts after the player has placed a bet. The dealer deals two cards to the players and then two to themselves, one face down and one face up
	private void StartGame(){
		//Playercards values collected from first two cards in gamedeck
		playercards[0] = gamedeck[0].value;
		playercards[1] = gamedeck[1].value;

		//Dealercards values collected from next two cards in gamedeck
		dealercards[0] = gamedeck[2].value;
		dealercards[1] = gamedeck[3].value;

		//Playercardcount and dealercardcount set to their proper values after cards are dealt
		playercardcount = 2;
		dealercardcount = 2;

		//First sum values set based on the first cards dealt
		playersum = ArraySum(0, playercards);
		dealersum = ArraySum(0, dealercards);

		//Player card images set to the first two cards
		cards[0][4].setIcon(new ImageIcon(gamedeck[0].cardimage));
		cards[1][4].setIcon(new ImageIcon(gamedeck[1].cardimage));

		//Dealer card images set to the cardback and the fourth card
		cards[0][0].setIcon(new ImageIcon(cardback.cardimage));
		cards[1][0].setIcon(new ImageIcon(gamedeck[3].cardimage));

		//Image of the dealer's first card is stored in cardback, cardback image is stored in backimage
		backimage = cardback.cardimage;
		cardback.cardimage = gamedeck[2].cardimage;

		//Current player score is printed on the right side of the screen in the same way as the player's chips
		cards[10][3].setIcon(null);
		cards[10][3].setBorder(border);
		cards[10][3].setText("Player: " + Integer.toString(playersum));

		//Visible dealer score is printed on the right side of the screen in the same way as the player's chips
		//This is where the starting address argument in ArraySum is used functionally
		cards[10][1].setIcon(null);
		cards[10][1].setBorder(border);
		cards[10][1].setText("Dealer: " + Integer.toString(ArraySum(1, dealercards)));

		//The top 4 cards are removed from gamedeck by setting it to a shorter copy of itself
		gamedeck = Arrays.copyOfRange(gamedeck,4, gamedeck.length);
	}

	//SplitStart starts the game after the player has split, with the player only having their split card and the dealer dealing themselves normally
	private void SplitStart(){
		//Retrieve stored split card from the top of gamedeck
		playercards[0] = gamedeck[0].value;

		//Same as StartGame
		dealercards[0] = gamedeck[1].value;
		dealercards[1] = gamedeck[2].value;

		//Player only starts with one card
		playercardcount = 1;
		dealercardcount = 2;

		//Same as StartGame
		playersum = ArraySum(0, playercards);
		dealersum = ArraySum(0, dealercards);

		//Player only has the image of their single split card
		cards[0][4].setIcon(new ImageIcon(gamedeck[0].cardimage));

		//Same as StartGame
		cards[0][0].setIcon(new ImageIcon(cardback.cardimage));
		cards[1][0].setIcon(new ImageIcon(gamedeck[2].cardimage));

		//Same as StartGame
		backimage = cardback.cardimage;
		cardback.cardimage = gamedeck[1].cardimage;

		//Same as StartGame
		cards[10][3].setIcon(null);
		cards[10][3].setBorder(border);
		cards[10][3].setText("Player: " + Integer.toString(playersum));

		cards[10][1].setIcon(null);
		cards[10][1].setBorder(border);
		cards[10][1].setText("Dealer: " + Integer.toString(ArraySum(1, dealercards)));

		//Since the player will have made a bet beforehand, it can be displayed here
		cards[10][2].setIcon(null);
		cards[10][2].setBorder(border);
		cards[10][2].setText("Current Bet:" + playerbet);

		gamedeck = Arrays.copyOfRange(gamedeck,4, gamedeck.length);
	}

	//A player needs to know the rules of BlackJack before playing it, so before doing anything else, we launch a JFrame containing the rules of BlackJack
	private static void RulesWindow(){
		//This one we organize using GridLayout because it's simpler to manage
		JFrame rules = new JFrame("Blackjack Rules");
		rules.setLayout(new GridLayout(7, 1));
		rules.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel bets = new JLabel("1. Blackjack starts with players making bets.");
		JLabel dealing = new JLabel("2. Dealer deals 2 cards to the players and two to himself (1 card face up, the other face down).");
		JLabel values = new JLabel("3. All cards count their face value in blackjack. Picture cards cound as 10 and the ace can count as either 1 or 11. Card suits have no meaning in blackjack. The total of any hand is the sum of the card values in the hand.");
		JLabel decision = new JLabel("4. Players must decide whether to stand, hit, surrender, double down, or split.");
		JLabel dealermove = new JLabel("5. The dealer acts last and must hit on 16 or less and stand on 17 through 21.");
		JLabel winning = new JLabel("6. Players win when their hand totals higher than the dealer's hand, or they have 21 or less when the dealer busts (exceeds 21).");

		//The acknowledgement button launches the first instance of our BlackJack window and closes the Rules Window
		JButton acknowledge = new JButton("I understand");
		acknowledge.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				//Player starts with 50 chips
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

	//LoseWindow is a very simple JFrame that pops up if the player starts a round with no chips, this conditional launch is handled later in the program
	//There is no way to win this Blackjack, you just play until you run out of money
	private void LoseWindow(){
		JFrame lose = new JFrame("You've lost");
		lose.setLayout(new GridLayout(3, 1));
		lose.setSize(300, 100);
		lose.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel loss = new JLabel("You successfully lost all your money gambling!");
		JLabel congrats = new JLabel("Congratulations!");

		//In the context where it is initialized, LoseWindow is the only window, so disposing of it exits the program entirely
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

	//If a round is started normally, a player needs to make a bet before they are dealt their hand. This is handled using a slider with a live display of it's value and a confirmation button
	private void GetBet(){
		//Window is organized using GridLayout since it is the simplest to deal with
		JFrame betting = new JFrame("Betting Slider");
		betting.setLayout(new GridLayout(3, 1));
		betting.setSize(300, 100);
		betting.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//This panel is used to hold our slider
		JPanel sliderstorage = new JPanel();

		//Slider is initialized to be on the range from 0 to the player's current chip count. Initial value is set to half the chip count
		JSlider slider = new JSlider(0, playerchips, playerchips/2);

		//Count displays the current selected value on the slider
		JLabel count = new JLabel("Current Value:" + (slider.getValue()), JLabel.CENTER);

		//ChangeListener added to that updates the Count label with the current value of the slider whenever it changes
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e){
				count.setText("Current Value:" + ((JSlider)e.getSource()).getValue());
			}
		});

		//Whenever the player is satisfied, they can press "Bet" to start up the hand with their selected bet value
		//This displays the players current bet amount and updates their total displayed chip count, then runs StartGame and disposes of the betting window
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

	//DealerHit implements the simple logic used to determine the dealer's actions in BlackJack. It also checks for and adjusts the value of the dealer's aces if they would cause the dealer to go bust
	private void DealerHit(){
		//Dealer only hits if their current value is 16 or less
		if (dealersum <= 16){
			//Dealer deals themselves a card
			dealercards[dealercardcount] = gamedeck[0].value;
			dealersum = ArraySum(0, dealercards);

			//Check for bust, then check for aces and change their value if they're still at 11
			//Aces are found by making an ArrayList of every value in dealercards and getting the index of the 11 in it
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
			//Update displayed Dealer value with total sum of dealer cards
			cards[10][1].setText("Dealer: " + Integer.toString(dealersum));

			//Display the image of the dealer's new card
			cards[dealercardcount][0].setIcon(new ImageIcon(gamedeck[0].cardimage));

			//Remove dealt card from gamedeck
			gamedeck = Arrays.copyOfRange(gamedeck,1, gamedeck.length);
			dealercardcount += 1;

			//See if the dealer can hit again
			DealerHit();
		}
	}

	//Gives the player a new card in the same way as the dealer, giving them a new card, modifying existing ace values if necessary
	//Afterwards, checks to see if the player is bust, and ends the round if they are
	private void PlayerHit(){
		//Same as DealerHit
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

		//Checks to see if the player has bust, and takes appropriate action if they did
		if (playersum > 21){
			cards[6][2].setIcon(null);
			cards[6][2].setText("You're Bust!");
			RestartButton("Restart");
		}
	}

	//CheckCards resolves the many possible endstates of BlackJack after the player stands and the dealer is done hitting
	private void CheckCards(){
		//Reward for an untied Blackjack is 2.5 * The player's bet
		if (playersum == 21 && playersum != dealersum){
			cards[6][2].setIcon(null);
			cards[6][2].setText("You Win!");
			playerchips = playerchips + (int)(2.5 * playerbet);
			RestartButton("Restart");

		//Normal reward for doing better than the dealer/dealer busting is 2 * the player's bet
		}else if (dealersum > 21 || (playersum > dealersum)){
			cards[6][2].setIcon(null);
			cards[6][2].setText("You Win!");
			playerchips = playerchips + 2 * playerbet;
			RestartButton("Restart");

		//If the dealer did better, player loses their bet
		} else if (playersum < dealersum){
			cards[6][2].setIcon(null);
			cards[6][2].setText("You Lose!");
			RestartButton("Restart");

		//In the event of a tie, bet is returned to player
		} else if (playersum == dealersum){
			cards[6][2].setIcon(null);
			cards[6][2].setText("Tie Game!");
			playerchips = playerchips + playerbet;
			RestartButton("Restart");
		}
	}

	//When the call to restart is issued, roundfinish is flipped to true, disabling all other buttons, and a new button is made to restart in a new round
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

	//Here we handle all button presses in the main BlackJack window by getting the name of the button that was pressed and making an appropriate response
	@Override
	public void actionPerformed(ActionEvent e){
		String button = e.getActionCommand();

		//Most buttons can only be triggered if a bet has been made, and the Restart button has not yet been initialized
		if (roundfinish != true && betmade == true){

			//If 'Hit' is pressed we simply call PlayerHit
			if (button.equals("Hit")){
				PlayerHit();

			//'Stand' reveals the dealer's first card and actual total, then runs DealerHit and checks the endstate
			} else if (button.equals("Stand")){
				cards[10][1].setText("Dealer: " + Integer.toString(dealersum));
				cards[0][0].setIcon(new ImageIcon(cardback.cardimage));
				DealerHit();
				CheckCards();

			//Surrendering returns half of the player's bet and makes the Restart Button available
			} else if (button.equals("Surrender")){
				cards[6][2].setIcon(null);
				cards[6][2].setText("You Lose!");
				playerchips = playerchips + (int)(0.5 * playerbet);
				RestartButton("Restart");

			//Split Displays the player's left card on top of the right card, stores a bet equal to their current bet to use with that card, and toggles splitstate so that starting a new round will use the stored split card to start
			} else if (button.equals("Split")){
				//playercards[0] == playercards[1] && 
				if (playercardcount == 2 && playerchips >= playerbet){
					//Toggle splitstate
					splitstate = true;

					//Remove another bet from the player's chip total and display new chip total
					playerchips = playerchips - playerbet;
					cards[9][2].setText("Chips: " + playerchips);

					//Rearrange card images for current hand
					cards[0][3].setIcon(cards[0][4].getIcon());
					cards[0][4].setIcon(cards[1][4].getIcon());
					cards[1][4].setIcon(cards[2][4].getIcon());

					//Remove first card value from playercards and set playercardcount, playersum, and display playersum accordingly
					playercards = Arrays.copyOfRange(playercards, 1, playercards.length);
					playercardcount = 1;
					playersum = ArraySum(0, playercards);
					cards[10][3].setText("Player: " + Integer.toString(playersum));
				}

			//Double doubles the player's current bet, taking from their chip pool, hits for the player, hits for the dealer, then checks the endstate
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
		//If the restart button is initialized, it is the only button that has functionality
		//The Restart button disposes of the current window and generates a new one using the players chip pool, the current deck, and if a split has been done, the bet associated with that split and the card that was split that the player will start with
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