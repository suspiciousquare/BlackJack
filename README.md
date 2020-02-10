# BlackJack
 Blackjack program for Praeses
 This Project was done using Java
 Since I haven't worked with graphical applications in Java before, I decided to explore it for this project
 Project implements all rules found at the website linked below, player actions are performed by clicking the appropriate button on the right side of the primary game window

 Supported features include: Betting, Stand, Hit, Surrender, Double Down, Split
 Deck persists between hands and is reshuffled when a round starts and less than 25% cards are left
 Player starts with 50 chips
 There is no 'Victory' state, player loses when they run out of chips

 Rules Source: https://www.888casino.com/blog/blackjack-strategy-guide/how-to-play-blackjack

 Compiled with: javac BlackJack.java
 Run with: java BlackJack

 .jar compiled with: jar -cfe BlackJack.jar BlackJack *.class PNG-cards-1.3 greencard.png
 .jar run with: java -jar BlackJack.jar

 Output of the command java -version:
 java version "13" 2019-09-17
 Java(TM) SE Runtime Environment (build 13+33)
 Java HotSpot(TM) 64-Bit Server VM (build 13+33, mixed mode, sharing)