package geniusect.ai;


import geniusect.Action;
import geniusect.Attack;
import geniusect.Battle;
import geniusect.Change;
import geniusect.Move;
import geniusect.Pokemon;
import geniusect.Pokequations;
import geniusect.Team;
import geniusect.Type;
import geniusect.abilities.Ability;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import seleniumhelper.ShowdownHelper;

/**
 * The AI logic for deciding what to do in a battle.
 * Can currently adjust behavior based upon minimax (minimize losses) or generic (use best attack) modes.
 * TODO: Genetic AI based upon common spreads.
 * @author TeamForretress
 * @see geniusect.battle
 */
public class GeniusectAI {
	
	/**
	 * Holds the user's team.
	 * @see #enemy
	 */
	protected static Team us;
	/**
	 * Holds the opponent's team.
	 * @see #user
	 */
	protected static Team enemy;
	
	/**
	 * Best possible (most damaging) of all attacks we have.
	 * @see #theirBestMove
	 */
	protected static Move ourBestMove;
	/**
	 * Best possible (most damaging) of all attacks we know the opponent has.
	 * @see #ourBestMove
	 * @see geniusect.Action#updateLastTurn()
	 */
	protected static Move theirBestMove;
	/**
	 * The amount of turns we project it will take the user to kill the opponent.
	 * @see #ourBestMove
	 */
	protected static int turnsToKillThem;
	/**
	 * The amount of turns we project it will take the opponent to kill the user.
	 * @see #theirBestMove
	 */
	protected static int turnsToKillUs;
	/**
	 * The difference between turnsToKillThem and turnsToKillUs.
	 * Negative means they will kill us faster than we can kill them.
	 * @see #turnsToKillThem
	 * @see #turnsToKillUs
	 */
	protected static int deficit;
	
	/**
	 * Are we using Genetic Algorithms (GAs)?
	 * @see geniusect.ai.GeniusectAI#setGenetic()
	 */
	private static boolean genetic = false;
	/**
	 * Are we using MinimaxAI trees?
	 * @see geniusect.ai.GeniusectAI#setMiniMax()
	 */
	private static boolean miniMax = false;
	/**
	 * Are we using scripted logic behavior?
	 * @see geniusect.ai.GeniusectAI#setGeneric()
	 */
	private static boolean generic = true;
	
	/**
	 * Internal player ID.
	 * @see #enemyID
	 * @see #us
	 * @see #enemy
	 */
	protected static int teamID = 0;
	/**
	 * Internal opponent ID.
	 * @see #playerID
	 * @see #us
	 * @see #enemy
	 */
	protected static int enemyID = 1;
	
	/**
	 * The battle that we are in.
	 * @see #showdown
	 */
	private static Battle battle = null;
	
	/**
	 * TRUE if we are currently simulating future turns.
	 * @see #generic
	 * @see #genetic
	 * @see #miniMax
	 */
	protected static boolean simulating = false;
	
	/**
	 * The currently active Showdown Hookup.
	 * NULL means we are playing locally.
	 * @see geniusect.seleniumhelper.ShowdownHelper
	 */
	protected static ShowdownHelper showdown = null;

	/**
	 * Battles we've taken part in this session.
	 */
	protected static int battleCount = 0;
	/**
	 * Our wins this session.
	 */
	protected static int winCount = 0;
	/**
	 * Our losses this session.
	 */
	protected static int lossCount = 0;
	
	public static void main(String[] args) {
		//WebDriver driver = new FirefoxDriver();
    	// wait up to 10 seconds for elements to load
    	//driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        
        //showdown = new ShowdownHelper(driver);
		if(showdown == null)
		{
			battle = new Battle();
			battle.findBattle();
		}
		else
		{
			showdown.open();
			try
			{
				showdown.login("Geniusect2645","");
			}
			catch(Exception e)
			{
				System.err.println("Could not log in! Exception data: \n"+e);
				return;
			}
	        battle = new Battle(showdown);
	        if(!battle.findBattle())
	        	return;
		}
		battleCount++;
    	battle.battleStart();
    }
	
	/**
	 * Prints the Geniusect intro to the chat.
	 *
	 */
	public static void displayIntro()
	{
		print("Geniusect by rissole and Team Forretress found a battle. Session battle count: "+battleCount);
		print("Hello! You are playing against Geniusect, an AI early in development, coded in Java.");
		print("If you suspect I'm not responding, feel free to hit 'Kick Inactive Player.'");
	}
	
	/**
	 * Simulates the next turn to determine the best action.
	 * @return Action - the decided upon action.
	 */
	public static Action simulate()
	{
		if(battle == null)
			return null;
    	us = battle.getTeam(teamID);
    	enemy = battle.getTeam(enemyID);
		Action nextTurn = null;
		simulating = true;
		if(generic)
			nextTurn = GenericAI.bestMove(battle);
		else if(miniMax || genetic)
			nextTurn = MinimaxAI.minimax(4, battle);
		//else if(genetic)
			//nextTurn = TODO;
		simulating = false;
		return nextTurn;
	}
	
	/**
	 * Simulates a turn in the battle.
	 * @param b (Battle) - The battle to simulate.
	 */
	public static void simulateTurn(Battle b)
	{
		int turnCount = b.getTurnCount();
		Action nextTurn = b.getNextTurn();
		Action lastTurnEnemy = b.getLastTurnEnemy();
		if(turnCount % 2 == 1)
		{
			if(nextTurn instanceof Change) //Always change first.
			{
				Change c = (Change)nextTurn;
				c.sendToShowdown(b);
				if(lastTurnEnemy instanceof Attack)
				{
					Attack a = (Attack)lastTurnEnemy;
					a.defenderSwap(c.switchTo);
				}
			}
			if(lastTurnEnemy instanceof Change)
			{
				Change c = (Change)lastTurnEnemy;
				c.sendToShowdown(b);
				if(nextTurn instanceof Attack)
				{
					Attack a = (Attack)nextTurn;
					a.defenderSwap(c.switchTo);
				}
			}
			if(Team.getEnemyTeam(enemyID).getActive().isFasterThan(Team.getEnemyTeam(teamID).getActive())) //Check who is faster.
			{	//Things won't send if they've already been sent, so we don't need to check if we've already sent it.
				nextTurn.sendToShowdown(b);
				lastTurnEnemy.sendToShowdown(b);
			}
			else
			{
				lastTurnEnemy.sendToShowdown(b);
				nextTurn.sendToShowdown(b);
			}
		}
		GeniusectAI.swapSides();
	}
	/**
	 * Simulates a turn in the battle.
	 * @param b (Battle) - The battle to simulate.
	 */
	public static void simulateTurn(MinimaxNode node)
	{
		Battle b = node.getBattle();
		int turnCount = b.getTurnCount();
		Action nextTurn = b.getNextTurn();
		Action lastTurnEnemy = b.getLastTurnEnemy();
		if(turnCount % 2 == 0)
		{
			if(nextTurn instanceof Change) //Always change first.
			{
				Change c = (Change)nextTurn;
				c.sendToShowdown(node);
				if(lastTurnEnemy instanceof Attack)
				{
					Attack a = (Attack)lastTurnEnemy;
					a.defenderSwap(c.switchTo);
				}
			}
			if(lastTurnEnemy instanceof Change)
			{
				Change c = (Change)lastTurnEnemy;
				c.sendToShowdown(node);
				if(nextTurn instanceof Attack)
				{
					Attack a = (Attack)nextTurn;
					a.defenderSwap(c.switchTo);
				}
			}
			if(Team.getEnemyTeam(enemyID).getActive().isFasterThan(Team.getEnemyTeam(teamID).getActive())) //Check who is faster.
			{	//Things won't send if they've already been sent, so we don't need to check if we've already sent it.
				nextTurn.sendToShowdown(node);
				lastTurnEnemy.sendToShowdown(node);
			}
			else
			{
				lastTurnEnemy.sendToShowdown(node);
				nextTurn.sendToShowdown(node);
			}
		}
	}
	
	/**
	 * If we're running locally, switches sides to play against itself.
	 */
	protected static void swapSides()
	{
		//If there's no Showdown running, play against ourselves.
		if(GeniusectAI.showdown != null || battle == null || !battle.isPlaying())
			return;
		if(battle.getTurnCount() % 5 != 0)
			System.out.println("***THIS IS BEING PRINTED FOR DEBUGGING PURPOSES.***\n(It would not be printed in an actual battle.)");
		lastTurnLogic();
		int e = teamID;
		teamID = enemyID;
		enemyID = e;
		Team t = us;
		us = enemy;
		enemy = t;
	}
	
	/**
	 * Called when a Pokemon dies. The AI finds the best Pokemon to change to.
	 * @param dead - The Pokemon that died.
	 * @return Change - The Change action specifying which Pokemon to switch to.
	 */
	public static Change onPokemonDeath(Pokemon dead)
	{
		if(battle == null || battle.isPlaying())
		{
			//Called when a Pokemon gets killed.
			Pokemon change = Change.bestCounter(dead.getPokemonTeam(), dead.getEnemy());
			if(change == null)
			{
				System.err.println(dead.getEnemy().getName()+" wins the game!");
				boolean won = false;
				if(dead.getTeam() == enemy)
					won = true;
				gameOver(won);
				return null;
			}
			else
			{
				Change c = new Change(change,battle);
				c.say("Oh my god! You killed "+dead.getName()+"! D:");
				c.deploy();
				return c;
			}
		}
		return null;
	}
	/**
	 * Resets the AI on game over.
	 * @param won - TRUE if the player won the game, FALSE if the player lost.
	 */
	public static void gameOver(boolean won)
	{
		if(simulating)
			return;
		battle = null;
		if(won)
		{
			winCount++;
			print("I won! I, for one, welcome our new robot overlords.");
			print("Good game.");
		}
		else
		{
			lossCount++;
			print("GG. :(");
		}
		print("Geniusect win : loss ratio is "+winCount +" : "+ lossCount);
		if(showdown != null)
			showdown.leaveBattle();
	}
	
	/**
	 * Fills in the theirBestMove and ourBestMove values if we've decided without using shouldSwitch().
	 * @param decision - The Action we decided to take.
	 * @see geniusect.ai.GeniusectAI#shouldSwitch(Pokemon, Pokemon)
	 */
	protected static void markDecision(Action decision)
	{
		if(decision instanceof Attack)
		{
			Move bestMoveUs = ((Attack) decision).move;
			ourBestMove = bestMoveUs;
			System.out.println("We are going to try to use "+bestMoveUs.name);
			Move bestMoveThem = Pokequations.bestMove(us.getActive(), enemy.getActive(), ourBestMove);
			theirBestMove = bestMoveThem;
			if(bestMoveThem == null)
				turnsToKillUs++;
			else
				turnsToKillUs = Pokequations.turnsToKill(us.getActive().getHealth(), theirBestMove.getProjectedPercent(us.getActive()).y + ourBestMove.recoilPercent);
			turnsToKillThem = Pokequations.turnsToKill(enemy.getActive().getHealth(), ourBestMove.getProjectedPercent(enemy.getActive()).x + theirBestMove.recoilPercent);
		}
	}
	
	/**
	 * Prints the AI debug log to the chat text.
	 * @see geniusect.ai.GeniusectAI#printEnemy
	 * @see geniusect.ai.GeniusectAI#print(String)
	 */
	public static void lastTurnLogic()
	{
		int count = 0;
		if(showdown == null)
			count = battle.getTurnCount() / 2;
		else
			count = showdown.getCurrentTurn();
		print("*********GENIUSECT DEBUG LOG*********");
		print("This is the logic I used last turn (turn "+count+").");
		if(genetic)
		{
			print("I used Genetic Algorithms (http://en.wikipedia.org/wiki/Genetic_algorithm) to determine what to do.");
			print("Unfortunately, those haven't been written yet, so I used MinimaxAI logic (http://en.wikipedia.org/wiki/Minimax) instead.");
		}
		else if(miniMax)
		{
			print("I used MinimaxAI logic (http://en.wikipedia.org/wiki/Minimax) to determine what to do.");
		}
		else if(generic)
		{
			print("I used scripted AI reasoning strategies to determine the best approach.");
		}
		printEnemy();
	}
	
	/**
	 * Prints what the AI knows about the enemy Pokemon.
	 * @see geniusect.ai.GeniusectAI#print(String)
	 */
	protected static void printEnemy()
	{
		Pokemon usActive = us.getActive();
		Pokemon enemyActive = enemy.getActive();
		System.out.println("I am using a "+usActive.getName()+" with "+usActive.getHealth()+"% health.");
		print("Here's what I know about the enemy's "+enemyActive.getName());
		print("It has "+enemyActive.getHealth()+"% HP.");
		System.out.println("Its types are "+enemyActive.getType(0)+" and "+enemyActive.getType(1));
		print("Its nature is "+enemyActive.getNature().toString());
		Ability ability = enemyActive.getAbility();
		if(ability != null)
			print("Its ability is "+ability.getName());
		Type[] immunities = enemyActive.getImmunities();
		if(immunities.length > 0)
		{
			print("It is immune to:");
			for(int i = 0; i < immunities.length; i++)
			{
				print(immunities[i].toString());
			}
		}
		print("It knows:");
		boolean knowsMoves = false;
		Move[] moveset = enemyActive.getMoveset();
		for(int i = 0; i < moveset.length; i++)
		{
			if(moveset[i] == null || moveset[i].name.toLowerCase().startsWith("struggle"))
				continue;
			knowsMoves = true;
			print(moveset[i].name);
		}
		if(!knowsMoves)
			print("I am unsure what moves it knows.");
		print("I think it has an EV spread (format HP/Atk/Def/SpA/SpD/Spe) of:");
		int[] evs = enemyActive.getEVs();
		for(int i = 0; i < evs.length; i++)
		{
			print(evs[i]+"");
		}
		if(ourBestMove != null)
		{
			print("If I use my best move against them, "+ourBestMove.name+", it will do about "+ ourBestMove.getProjectedDamage(enemyActive, true) +" HP worth of damage ("+ourBestMove.getProjectedPercent(enemyActive, true)+" percent).");
			print("I project it will take about "+turnsToKillThem+" turns to kill their "+enemyActive.getName()+".");
			print("If they use their best move against me, "+theirBestMove.name+", it will do about "+ theirBestMove.getProjectedDamage(usActive, true) +" HP worth of damage ("+theirBestMove.getProjectedPercent(usActive, true)+" percent).");
			print("I project it will take them about "+turnsToKillUs+" turns to kill my " +usActive.getName()+".");
			Action nextTurn = battle.getNextTurn();
			if(nextTurn instanceof Change)
			{
				Change switching = (Change) nextTurn;
				print("I plan to change to "+switching.switchTo.getName()+", who will take about "+theirBestMove.getProjectedDamage(switching.switchTo, true)+" HP worth of damage ("+theirBestMove.getProjectedPercent(switching.switchTo, true)+" percent).");
			}
			else if(nextTurn instanceof Attack)
			{
				Attack attacking = (Attack) nextTurn;
				print(usActive.getName()+" plans to use "+attacking.move.name+".");
			}
		}
		Pokemon[] ourTeam = usActive.getPokemonTeam();
		System.err.println("Still living Pokemon (us):");
		for(int i = 0; i < ourTeam.length; i++)
		{
			if(ourTeam[i] == null || !ourTeam[i].isAlive())
				continue;
			System.err.println(ourTeam[i].getName());
		}
		
		Pokemon[] enemyTeam = enemyActive.getPokemonTeam();
		System.err.println("Still living Pokemon (enemy):");
		for(int i = 0; i < enemyTeam.length; i++)
		{
			if(enemyTeam[i] == null || !enemyTeam[i].isAlive())
				continue;
			System.err.println(enemyTeam[i].getName());
		}
	}
	
	/**
	 * Prints a line of text to the battle log and the console.
	 * @see seleniumhelper.ShowdownHelper#sendMessage(String)
	 * @param text - The text to print.
	 */
	public static void print(String text)
	{
		//Text to send to Showdown.
		//System.err.println("Sending text to chat.");
		//if(showdown == null)
			System.out.println("BATTLE CHAT: "+text);
		/*else
		{
			try
			{
				showdown.sendMessage(text);
			}
			catch (Exception e)
			{
				System.err.println("Could not print to chat!");
				System.err.println("Chat log: "+text);
				System.err.println("Exception data: "+e);
			}
		}*/
	}
	
	/**
	 * Gets the active battle.
	 * @return Battle - the currently-active battle.
	 */
	public static Battle getBattle()
	{
		return battle;
	}
	
	/**
	 * Sets the active battle.
	 * @param newBattle (Battle): The new battle to set the active battle to.
	 */
	public static void setBattle(Battle newBattle)
	{
		battle = newBattle;
	}
	
	/**
	 * Switches the AI to using Genetic Algorithms (GAs).
	 * NOTE: Not implemented! Will act like MinimaxAI!
	 * @see Geniusect.ai.GeniusectAI#setMinimax()
	 */
	protected static void setGenetic()
	{
		generic = false;
		genetic = true;
		miniMax = false;
	}
	
	/**
	 * Switches the AI to using generic behavior.
	 * @see Geniusect.ai.GeniusectAI#setMinimax()
	 */
	public static void setGeneric()
	{
		generic = true;
		genetic = false;
		miniMax = false;
	}
	
	/**
	 * Switches the AI to using MinimaxAI logic.
	 * @see geniusect.ai.GeniusectAI#minimax(int)
	 * @see geniusect.ai.GeniusectAI#scoreTree(MinimaxNode, int)
	 * @see geniusect.ai.GeniusectAI#score(MinimaxNode)
	 * @see	{@link http://en.wikipedia.org/wiki/Minimax}
	 */
	public static void setMiniMax()
	{
		generic = false;
		genetic = false;
		miniMax = true;
	}

	/**
	 * Checks to see if we are simulating future turns.
	 * @return boolean - TRUE if we are simulating future turns, else FALSE.
	 */
	public static boolean isSimulating() 
	{
		return simulating;
	}

	/**
	 * Returns our current teamID.
	 * @return (int): Our current teamID.
	 */
	protected static int getTeamID() 
	{
		return teamID;
	}
	
	/**
	 * Returns our current enemyID.
	 * @return (int): Our current enemyID.
	 */
	protected static int getEnemyID() 
	{
		return enemyID;
	}
}

