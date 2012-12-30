/*
 * The AI logic.
 * Can currently adjust behavior based upon minimax (minimize losses) or generic (use best attack) modes.
 * TODO: Genetic AI based upon common spreads.
 * @author TeamForretress
 */

package geniusect;


import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import seleniumhelper.ShowdownHelper;

public class GeniusectAI {
	
	public static Team us;
	public static Team enemy;
	
	public static Move ourBestMove;	
	public static Move theirBestMove;
	
	public static int turnsToKillThem;
	public static int turnsToKillUs;
	
	public static int deficit;
	
	private static boolean genetic = false;
	private static boolean miniMax = false;
	private static boolean generic = true;
	
	public static int teamID = 0;
	public static int enemyID = 1;
	
	public static Battle battle = null;
	public static boolean simulating = false; // TRUE if we are simulating future turns.
	
	public static ShowdownHelper showdown = null;
	public static String version = "0.0.1";

	public static int battleCount = 0;
	public static int winCount = 0;
	public static int lossCount = 0;
	
	
	public static void main(String[] args) {
		WebDriver driver = new FirefoxDriver();
    	// wait up to 10 seconds for elements to load
    	driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        
        showdown = new ShowdownHelper(driver);
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
				showdown.login("Porygon-G","test123");
			}
			catch(Exception e)
			{
				System.err.println("Could not log in! Exception data: "+e);
				return;
			}
	        battle = new Battle();
	        if(!battle.findBattle())
	        	return;
		}
    	battle.battleStart();
    }
	
	public static void displayIntro()
	{
		print("Geniusect version "+version+" has entered a battle. Session battle count: "+battleCount);
		print("Hello! You are playing against an AI. Occasionally I might get hung up while making a move.");
		print("If you suspect I'm not responding, feel free to hit 'Kick Inactive Player.'");
	}
	
	public static void populateTeams(Team one, Team two)
	{
		us = one;
		enemy = two;
		if(GeniusectAI.showdown!= null)
		{
			//Populate each team.
			us.userName = showdown.getUserName();
			List<String> ourPokes= showdown.getTeam(us.userName);
			for(int i = 0; i < ourPokes.size(); i++)
				us.addPokemon(ourPokes.get(i));
			enemy.userName = showdown.getOpponentName();
			List<String> enemyPokes = showdown.getTeam(enemy.userName);
			for(int i = 0; i < enemyPokes.size(); i++)
			{
				if(i > -1)
					enemy.addPokemon(enemyPokes.get(i));
			}
		}
		//TODO: If we can choose lead, do so.
		Pokemon.active[teamID].changeEnemy(Pokemon.active[enemyID]);
		Pokemon.active[enemyID].changeEnemy(Pokemon.active[teamID]);
	}
	
	public static Action simulate()
	{
		Action nextTurn = null;
		simulating = true;
		if(generic)
			nextTurn = bestMove();
		else if(miniMax || genetic)
			nextTurn = minimax(1);
		//else if(genetic)
			//nextTurn = TODO;
		simulating = false;
		return nextTurn;
	}
	
	public static void swapSides()
	{
		//If there's no Showdown running, play against ourselves.
		if(GeniusectAI.showdown != null || battle == null || !battle.playing)
			return;
		if(battle.turnCount % 5 != 0)
			System.out.println("***THIS IS BEING PRINTED FOR DEBUGGING PURPOSES.***\n(It would not be printed in an actual battle.)");
		lastTurnLogic();
		int e = teamID;
		teamID = enemyID;
		enemyID = e;
		Team t = us;
		us = enemy;
		enemy = t;
	}
	
	public static Change onPokemonDeath(Pokemon dead)
	{
		if(battle.playing)
		{
			//Called when a Pokemon gets killed.
			Pokemon change = Change.bestCounter(dead.team.team, dead.enemy);
			if(change == null)
			{
				System.err.println(dead.enemy.name+" wins the game!");
				boolean won = false;
				if(dead.team == enemy)
					won = true;
				gameOver(won);
				return null;
			}
			else
			{
				Change c = new Change(change);
				c.say("Oh my god! You killed "+dead+"! D:");
				c.deploy();
				return c;
			}
		}
		return null;
	}
	
	public static void gameOver(boolean won)
	{
		System.err.println("Game over.");
		if(simulating)
			return;
		battle.playing = false;
		battle.turnsToSimulate = 0;
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
			showdown.surrender();
	}
	
	public static Action bestMove()
	{
		return bestMove(Pokemon.active[teamID],Pokemon.active[enemyID]);
	}
	
	public static Action bestMove(Pokemon ourGuy, Pokemon theirGuy)
	{
		Action doNext;
		if(shouldSwitch(ourGuy,theirGuy))
		{
			Change sanityCheck = new Change(Change.bestChange(ourGuy, ourGuy.team.team, theirGuy, Pokequations.bestMove(theirGuy, ourGuy)));
			if(sanityCheck.switchTo.name.toLowerCase().startsWith(ourGuy.name.toLowerCase()))
				doNext = new Attack(ourBestMove,ourGuy,theirGuy);
			else doNext = sanityCheck;
		}
		else
		{
			doNext = new Attack(ourBestMove,ourGuy,theirGuy);
		}
		return doNext;
	}
	
	public static boolean shouldSwitch(Pokemon ourGuy, Pokemon theirGuy)
	{
		theirBestMove = Pokequations.bestMove(theirGuy,ourGuy);
		
		if(theirBestMove == null)
			return false;
		ourBestMove = Pokequations.bestMove(ourGuy, theirGuy,theirBestMove);
		if(ourBestMove == null)
			return true;
		turnsToKillUs = Pokequations.turnsToKill(ourGuy.hpPercent, theirBestMove.getProjectedPercent(ourGuy).y + ourBestMove.recoilPercent);
		turnsToKillThem = Pokequations.turnsToKill(theirGuy.hpPercent, ourBestMove.getProjectedPercent(theirGuy).x + theirBestMove.recoilPercent);
		
		deficit = turnsToKillUs - turnsToKillThem;
		if(deficit < 0 || deficit == 0 && theirGuy.isFasterThan(ourGuy))
			return true;
		else return false;
	}
	
	public static void markDecision(Action decision)
	{
		if(decision instanceof Attack)
		{
			ourBestMove = ((Attack) decision).move;
			print("We are going to try to use "+ourBestMove.name);
			theirBestMove = Pokequations.bestMove(Pokemon.active[enemyID], Pokemon.active[teamID], ourBestMove);
			if(theirBestMove == null)
				turnsToKillUs++;
			else
				turnsToKillUs = Pokequations.turnsToKill(Pokemon.active[teamID].hpPercent, theirBestMove.getProjectedPercent(Pokemon.active[teamID]).y + ourBestMove.recoilPercent);
			turnsToKillThem = Pokequations.turnsToKill(Pokemon.active[enemyID].hpPercent, ourBestMove.getProjectedPercent(Pokemon.active[enemyID]).x + theirBestMove.recoilPercent);
		}
	}
	
	public static void lastTurnLogic()
	{
		int count = 0;
		if(showdown == null)
			count = battle.turnCount / 2;
		else
			count = battle.turnCount = showdown.getCurrentTurn();
		print("This is the logic I used last turn (turn "+count+").");
		if(genetic)
		{
			print("I used Genetic Algorithms (http://en.wikipedia.org/wiki/Genetic_algorithm) to determine what to do.");
			print("Unfortunately, those haven't been written yet, so I used MiniMax logic (http://en.wikipedia.org/wiki/Minimax) instead.");
		}
		else if(miniMax)
		{
			print("I used MiniMax logic (http://en.wikipedia.org/wiki/Minimax) to determine what to do.");
		}
		else if(generic)
		{
			print("I used scripted AI reasoning strategies to determine the best approach.");
		}
		printEnemy();
	}
	
	public static void printEnemy()
	{
		System.out.println("I am using a "+Pokemon.active[teamID].name+" with "+Pokemon.active[teamID].hpPercent+"% health.");
		print("Here's what I know about the enemy's "+Pokemon.active[enemyID].name);
		print("It has "+Pokemon.active[enemyID].hpPercent+"% HP.");
		System.out.println("Its types are "+Pokemon.active[enemyID].types[0]+" and "+Pokemon.active[enemyID].types[1]);
		print("Its nature is "+Pokemon.active[enemyID].nature.toString());
		if(Pokemon.active[enemyID].ability != null)
			print("Its ability is "+Pokemon.active[enemyID].ability.name);
		if(!Pokemon.active[enemyID].immunities.isEmpty())
		{
			print("It is immune to:");
			for(int i = 0; i < Pokemon.active[enemyID].immunities.size(); i++)
			{
				print(Pokemon.active[enemyID].immunities.get(i).toString());
			}
		}
		print("It knows:");
		boolean knowsMoves = false;
		for(int i = 0; i <Pokemon.active[enemyID].moveset.length; i++)
		{
			if(Pokemon.active[enemyID].moveset[i] == null || Pokemon.active[enemyID].moveset[i].name.toLowerCase().startsWith("struggle"))
				continue;
			knowsMoves = true;
			print(Pokemon.active[enemyID].moveset[i].name);
		}
		if(!knowsMoves)
			print("I am unsure what moves it knows.");
		print("I think it has an EV spread of:");
		for(int i = 0; i < Pokemon.active[enemyID].evs.length; i++)
		{
			print(Stat.fromInt(i)+": "+Pokemon.active[enemyID].evs[i]);
		}
		if(ourBestMove != null)
		{
			print("If I use my best move against them, "+ourBestMove.name+", it will do about "+ ourBestMove.getProjectedDamage(Pokemon.active[enemyID], true) +" HP worth of damage ("+ourBestMove.getProjectedPercent(Pokemon.active[enemyID], true)+" percent).");
			print("I project it will take about "+turnsToKillThem+" turns to kill their "+Pokemon.active[enemyID].name+".");
			print("If they use their best move against me, "+theirBestMove.name+", it will do about "+ theirBestMove.getProjectedDamage(Pokemon.active[teamID], true) +" HP worth of damage ("+theirBestMove.getProjectedPercent(Pokemon.active[teamID], true)+" percent).");
			print("I project it will take them about "+turnsToKillUs+" turns to kill my " +Pokemon.active[teamID].name+".");
			if(battle.nextTurn instanceof Change)
			{
				Change switching = (Change) battle.nextTurn;
				print("I plan to change to "+switching.switchTo.name+", who will take about "+theirBestMove.getProjectedDamage(switching.switchTo, true)+" HP worth of damage ("+theirBestMove.getProjectedPercent(switching.switchTo, true)+" percent).");
			}
			else if(battle.nextTurn instanceof Attack)
			{
				Attack attacking = (Attack) battle.nextTurn;
				print(Pokemon.active[teamID].name+" plans to use "+attacking.move.name);
			}
		}
	}
	
	public static Action minimax(int depth)
	{
		depth *=2;
		AINode decide = scoreTree(depth);
		Pokequations.miniMax(decide, depth);
		Action decision;
		if(decide.result == null)
		{
			System.err.println("The minimax result was null!");
			decision = bestMove();
		}
		else
			decision = decide.result.decision;
		markDecision(decision);
		return decision;
	}
	
	public static AINode scoreTree(int depth)
	{
		AINode node = new AINode(us.team,enemy.team,Pokemon.active[teamID],Pokemon.active[enemyID]);
		node.addChild(scoreTree(new AINode(node),depth));
		return node;
	}
	
	public static AINode scoreTree(AINode node, int depth)
	{
		System.out.println("Player "+node.player + " is using "+node.ourActive.name +" ("+node.ourActive.getHealth()+" hp). Iterations to go: "+depth);
		if(depth > 0)
		{
			boolean madeMove = false;
			for(int i = 0; i < node.ourTeam.length; i++)
			{
				//Calculate every possible switch.
				if(node.ourTeam[i] == null || !node.ourTeam[i].isAlive() || node.ourTeam[i].name == node.ourActive.name)
					continue;
				else
				{
					madeMove = true;
					node.setDecision(new Change(node.ourTeam[i]));
					print(node.ourActive.name+" added decision: "+node.decision.name);
					AINode child = new AINode(node);
					return scoreTree(child,depth);
				}
			}
			for(int i = 0; i < node.ourActive.moveset.length; i++)
			{
				if(node.ourActive.moveset[i] == null || node.ourActive.moveset[i].disabled)
				{
					print(node.ourActive.name+"'s move in slot "+i+" is null or disabled.");
					continue;
				}
				print(node.ourActive.name+", "+node.ourActive.moveset[i].name);
				madeMove = true;
				node.ourActive.onNewTurn(node.ourActive.moveset[i].name,(node.ourActive.moveset[i].useMove(false,node.ourActive,node.enemyActive)), false);
				node.setDecision(new Attack(node.ourActive.moveset[i],node.ourActive,node.enemyActive));
				print(node.ourActive.name+" added decision: "+node.decision.name);
				AINode child = new AINode(node);
				scoreTree(child,depth - 1);
			}
			if(!madeMove)
			{
				node.setDecision(new Attack(new Move("Struggle",node.ourActive),node.ourActive,node.enemyActive));
				print(node.ourActive.name+" added decision: "+node.decision.name);
				AINode child = new AINode(node);
				scoreTree(child,depth - 1);
				madeMove = true;
			}
			if(node.decision == null)
				System.err.println(node.ourActive.name+" did not make a move!");
			else
				node = score(node);
		}
		return node;
	}
	
	public static AINode score(AINode node)
	{
		node.children = null;
		node.decision.score = node.damageDoneToEnemy - node.damageDoneToUs - node.count;
		System.out.println("Branch: "+node.decision.name+".");
		System.out.println("This branch did "+node.damageDoneToEnemy+" damage to our enemy ("+node.enemyActive.name+") in "+node.count+" turns.");
		System.out.println(node.enemyActive.name+" did "+node.damageDoneToUs+" damage to us ("+node.ourActive.name+")."); 
		System.out.println("Score: " +node.decision.score);
		return node;
	}
	
	public static void print(String text)
	{
		//Text to send to Showdown.
		System.err.println("Sending text to chat.");
		if(showdown == null)
			System.out.println("BATTLE CHAT: "+text);
		else
			showdown.sendMessage(text);
	}
	
	public static void setGenetic()
	{
		generic = false;
		genetic = true;
		miniMax = false;
	}
	
	public static void setGeneric()
	{
		generic = true;
		genetic = false;
		miniMax = false;
	}
	
	public static void setMiniMax()
	{
		generic = false;
		genetic = false;
		miniMax = true;
	}
}

