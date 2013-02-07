package geniusectai;

import geniusectai.generic.GenericAI;
import geniusectai.minimax.MinimaxAI;
import geniusectsim.abilities.Ability;
import geniusectsim.actions.Action;
import geniusectsim.actions.Attack;
import geniusectsim.actions.Change;
import geniusectsim.battle.Battle;
import geniusectsim.battle.Team;
import geniusectsim.battle.Type;
import geniusectsim.bridge.Simulator;
import geniusectsim.constants.Pokequations;
import geniusectsim.moves.Move;
import geniusectsim.pokemon.Pokemon;
import geniusectsim.pokemon.Stat;

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
	
	/**
	 * What action we will take next.
	 */
	protected static Action toDo = null;
	
	protected static int turnsToSimulate = 150;
	
	public static void main(String[] args) 
	{
		Simulator.isLocal(false);
		newBattle();
    }
	
	public static void newBattle()
	{
		battle = Simulator.onNewBattle();
		battleCount++;
		toDo = Simulator.battleStartAction();
		playMatch();
		/*swapSides();
		toDo = new Change();
		simulate(toDo);
		Simulator.newTurn(toDo);*/
	}
	
	private static void playMatch()
	{
		while(toDo != null)
		{
			toDo = simulate(toDo);
			toDo = Simulator.newTurn(toDo);
			newTurn(us);
		}
		gameOver(Simulator.getWon());
	}
	
	/**
	 * Prints the Geniusect intro to the chat.
	 */
	public static void displayIntro()
	{
		Simulator.print("Geniusect by rissole and Team Forretress found a battle. Session battle count: "+battleCount);
		Simulator.print("Hello! You are playing against Geniusect, an AI early in development, coded in Java.");
		Simulator.print("Source code is available at https://github.com/Jay2645/geniusect-ai and https://github.com/rissole/geniusect-selenium .");
		//Simulator.print("If you suspect I'm not responding, feel free to hit 'Kick Inactive Player.'");
	}
	
	private static Action simulate(Action nextTurn)
	{
		if(battle == null)
			return null;
		
		if(nextTurn instanceof Change)
		{
			us = battle.getTeam(teamID, true);
	    	enemy = battle.getTeam(enemyID, true);
			simulating = true;
			Simulator.toggleSend(!simulating);
			if(generic)
				nextTurn = GenericAI.bestMove(battle, nextTurn);
			else if(miniMax || genetic)
				nextTurn = MinimaxAI.minimax(4, battle, nextTurn);
			//else if(genetic)
				//nextTurn = TODO;
			simulating = false;
			Simulator.toggleSend(!simulating);
			return nextTurn;
		}
		else
			return simulate();
	}
	
	/**
	 * Simulates the next turn to determine the best action.
	 * @return Action - the decided upon action.
	 */
	public static Action simulate()
	{
		if(battle == null)
			return null;
    	us = battle.getTeam(teamID, true);
    	enemy = battle.getTeam(enemyID, true);
		Action nextTurn = null;
		simulating = true;
		Simulator.toggleSend(!simulating);
		if(generic)
			nextTurn = GenericAI.bestMove(battle);
		else if(miniMax || genetic)
			nextTurn = MinimaxAI.minimax(4, battle);
		//else if(genetic)
			//nextTurn = TODO;
		simulating = false;
		Simulator.toggleSend(!simulating);
		return nextTurn;
	}
	
	/**
	 * If we're running locally, switches sides to play against itself.
	 */
	protected static void swapSides()
	{
		//If there's no Showdown running, play against ourselves.
		if(battle == null || !battle.isPlaying())
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
	 * Resets the AI on game over.
	 * @param won - TRUE if the player won the game, FALSE if the player lost.
	 */
	public static void gameOver(boolean won)
	{
		if(simulating)
			return;
		Pokemon[] playerTeam = battle.getTeam(0, true).getPokemon();
		Pokemon[] enemyTeam = battle.getTeam(1, true).getPokemon();
		System.err.println("\nPlayer bodycount:");
		for(int i = 0; i < 6; i++)
		{
			if(playerTeam[i] == null)
				continue;
			playerTeam[i].printKills();
		}
		System.err.println("\nOpponent bodycount:");
		for(int i = 0; i < 6; i++)
		{
			if(enemyTeam[i] == null)
				continue;
			enemyTeam[i].printKills();
		}
		battle = null;
		if(won)
		{
			winCount++;
			Simulator.print("I won! I, for one, welcome our new robot overlords.");
			Simulator.print("Good game.");
		}
		else
		{
			Throwable t = new Throwable();
			t.printStackTrace();
			lossCount++;
			Simulator.print("GG. :(");
		}
		Simulator.print("Geniusect win : loss ratio is "+winCount +" : "+ lossCount);
		try 
		{
			Thread.sleep(1000);
		} 
		catch (InterruptedException e) {}
		Simulator.leaveBattle();
		try 
		{
			Thread.sleep(5000);
		} 
		catch (InterruptedException e){}
		newBattle();
	}
	
	/**
	 * Fills in the theirBestMove and ourBestMove values if we've decided without using shouldSwitch().
	 * @param decision - The Action we decided to take.
	 * @see geniusect.ai.GeniusectAI#shouldSwitch(Pokemon, Pokemon)
	 */
	public static void markDecision(Action decision)
	{
		if(decision instanceof Attack)
		{
			Move bestMoveUs = ((Attack) decision).move;
			Pokemon ourActive = us.getActive();
			if(ourActive == null)
			{
				Pokemon actualActive = us.getPokemon(((Attack) decision).attacker.getID());
				us.setActive(actualActive);
				ourActive = us.getActive();
			}
			Pokemon enemyActive = enemy.getActive();
			if(enemyActive == null)
			{
				Pokemon actualActive = enemy.getPokemon(((Attack) decision).defender.getID());
				enemy.setActive(actualActive);
				enemyActive = enemy.getActive();
			}
			ourBestMove = bestMoveUs;
			System.out.println("We are going to try to use "+bestMoveUs.name+ " ("+bestMoveUs.power+" BP, "+bestMoveUs.getType()+", "+bestMoveUs.getMoveType()+").");
			
			System.out.println("Enemy Speed: "+enemyActive.getBoostedStat(Stat.Spe));
			System.out.println("Our Speed: "+ourActive.getBoostedStat(Stat.Spe));
			Move bestMoveThem = Pokequations.bestMove(ourActive, enemyActive, ourBestMove);
			theirBestMove = bestMoveThem;
			if(bestMoveThem == null)
				turnsToKillUs++;
			else
				turnsToKillUs = Pokequations.turnsToKill(ourActive.getHealth(), theirBestMove.getProjectedPercent(ourActive).y + ourBestMove.recoilPercent);
			turnsToKillThem = Pokequations.turnsToKill(enemyActive.getHealth(), ourBestMove.getProjectedPercent(enemyActive).x + theirBestMove.recoilPercent);
		}
	}
	
	public static Action newTurn(Team t)
	{
		if(battle == null || t.getActive() == null)
			return null;
		int turnNumber = battle.getTurnCount();
		System.out.println("\n\n\n");
		if(simulating)
			System.err.println("***********************************SIMULATED***********************************");
		System.err.println("*******************************TEAM "+t.getTeamID()+", TURN "+(turnNumber)+"*******************************");
		System.err.println("**************************ACTIVE POKEMON: "+t.getActive().getName()+"**************************");
		System.err.println(battle.getErrors());
		//if(AIHandler.showdown != null && turnCount % 5 == 0)
			//AIHandler.lastTurnLogic();
		if(!simulating)
		{
			toDo = simulate();
			if(toDo == null) //Should never happen, but I'm being pedantic.
				return null;
		}
		return toDo;
	}
	
	/**
	 * Prints the AI debug log to the chat text.
	 * @see geniusect.ai.GeniusectAI#printEnemy
	 * @see geniusect.ai.GeniusectAI#Simulator.print(String)
	 */
	public static void lastTurnLogic()
	{
		int count = 0;
		count = battle.getTurnCount();
		Simulator.print("*********GENIUSECT DEBUG LOG*********");
		Simulator.print("This is the logic team "+teamID+" used last turn (turn "+count+").");
		if(genetic)
		{
			Simulator.print("I used Genetic Algorithms (http://en.wikipedia.org/wiki/Genetic_algorithm) to determine what to do.");
			Simulator.print("Unfortunately, those haven't been written yet, so I used MinimaxAI logic (http://en.wikipedia.org/wiki/Minimax) instead.");
		}
		else if(miniMax)
		{
			Simulator.print("I used MinimaxAI logic (http://en.wikipedia.org/wiki/Minimax) to determine what to do.");
		}
		else if(generic)
		{
			Simulator.print("I used scripted AI reasoning strategies to determine the best approach.");
		}
		printEnemy();
	}
	
	/**
	 * Prints what the AI knows about the enemy Pokemon.
	 * @see geniusect.ai.GeniusectAI#Simulator.print(String)
	 */
	protected static void printEnemy()
	{
		Pokemon usActive = us.getActive();
		Pokemon enemyActive = enemy.getActive();
		if(usActive == null)
			return;
		System.out.println("I am using a "+usActive.getName()+" with "+usActive.getHealth()+"% health.");
		if(enemyActive == null)
			return;
		Simulator.print("Here's what I know about the enemy's "+enemyActive.getName());
		Simulator.print("It has "+enemyActive.getHealth()+"% HP.");
		System.out.println("Its types are "+enemyActive.getType(0)+" and "+enemyActive.getType(1));
		Simulator.print("Its nature is "+enemyActive.getNature().toString());
		Ability ability = enemyActive.getAbility();
		if(ability != null)
			Simulator.print("Its ability is "+ability.getName());
		Type[] immunities = enemyActive.getImmunities();
		if(immunities.length > 0)
		{
			Simulator.print("It is immune to:");
			for(int i = 0; i < immunities.length; i++)
			{
				Simulator.print(immunities[i].toString());
			}
		}
		Simulator.print("It knows:");
		boolean knowsMoves = false;
		Move[] moveset = enemyActive.getMoveset();
		for(int i = 0; i < moveset.length; i++)
		{
			if(moveset[i] == null || moveset[i].name.toLowerCase().startsWith("struggle"))
				continue;
			knowsMoves = true;
			Simulator.print(moveset[i].name);
		}
		if(!knowsMoves)
			Simulator.print("I am unsure what moves it knows.");
		Simulator.print("I think it has an EV spread (format HP/Atk/Def/SpA/SpD/Spe) of:");
		int[] evs = enemyActive.getEVs();
		for(int i = 0; i < evs.length; i++)
		{
			Simulator.print(evs[i]+"");
		}
		/*if(ourBestMove != null)	//All this is wildly inaccurate.
		{
			Simulator.print("If I use my best move against them, "+ourBestMove.name+", it will do about "+ ourBestMove.getProjectedDamage(enemyActive, true) +" HP worth of damage ("+ourBestMove.getProjectedPercent(enemyActive, true)+" percent).");
			Simulator.print("I project it will take about "+turnsToKillThem+" turns to kill their "+enemyActive.getName()+".");
			Simulator.print("If they use their best move against me, "+theirBestMove.name+", it will do about "+ theirBestMove.getProjectedDamage(usActive, true) +" HP worth of damage ("+theirBestMove.getProjectedPercent(usActive, true)+" percent).");
			Simulator.print("I project it will take them about "+turnsToKillUs+" turns to kill my " +usActive.getName()+".");
			Action nextTurn = battle.getNextTurn();
			if(nextTurn instanceof Change)
			{
				Change switching = (Change) nextTurn;
				Simulator.print("I plan to change to "+switching.switchTo.getName()+", who will take about "+theirBestMove.getProjectedDamage(switching.switchTo, true)+" HP worth of damage ("+theirBestMove.getProjectedPercent(switching.switchTo, true)+" percent).");
			}
			else if(nextTurn instanceof Attack)
			{
				Attack attacking = (Attack) nextTurn;
				Simulator.print(usActive.getName()+" plans to use "+attacking.move.name+".");
			}
		}*/
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
	 * @see Geniusect.ai.GenericAI#bestMove(Battle b)
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
	public static int getTeamID() 
	{
		return teamID;
	}
	
	/**
	 * Returns our current enemyID.
	 * @return (int): Our current enemyID.
	 */
	public static int getEnemyID() 
	{
		return enemyID;
	}
}

