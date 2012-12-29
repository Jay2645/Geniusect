/*
 * The AI logic.
 * Can currently adjust behavior based upon minimax (minimize losses) or generic (use best attack) modes.
 * TODO: Genetic AI based upon common spreads.
 * @author TeamForretress
 */

package geniusect;

import seleniumhelper.ShowdownHelper;

public class GeniusectAI {
	
	public static Team us;
	public static Team enemy;
	
	public static Move ourBestMove;	
	public static Move theirBestMove;
	
	public static int turnsToKillThem;
	public static int turnsToKillUs;
	
	public static int deficit;
	public static Action nextTurn;
	public static Action lastTurnUs;
	public static Action lastTurnEnemy;
	
	private static boolean genetic = false;
	private static boolean miniMax = false;
	private static boolean generic = true;
	
	public static int turnCount = 1;
	public static int teamID = 0;
	public static int enemyID = 1;
	
	public static boolean playing = false; // TRUE if we have found a battle.
	public static boolean simulating = false; // TRUE if we are simulating future turns.
	
	public static ShowdownHelper showdown = null;
	public static String version = "0.0.1";

	public static int battleCount = 0;
	public static int winCount = 0;
	public static int lossCount = 0;
	
	public static int turnsToSimulate = 100; //How many turns we simulate, if Showdown is not running?
	
	public static void battleStart(String load)
	{
		turnsToSimulate *= 2;
		battleCount++;
		playing = true;
		//Called when the battle begins.
		//Can load a team from an importable.
		print("Geniusect version "+version+" has entered a battle. Session battle count: "+battleCount);
		print("Hello! You are playing against an AI. Occasionally I might get hung up while making a move.");
		print("If you suspect I'm not responding, feel free to hit 'Kick Inactive Player.'");
		us = new Team(load, teamID);
		enemy = new Team(enemyID);
		//TODO: If we can choose lead, do so and log names of all enemy Pokemon.
		String pokemonName = "Genesect"; //TODO: Replace with the name of the enemy lead.
		enemy.addPokemon(pokemonName);
		Pokemon.active[teamID].changeEnemy(Pokemon.active[enemyID]);
		Pokemon.active[enemyID].changeEnemy(Pokemon.active[teamID]);
		Pokemon.active[enemyID].moveset[0] = new Move("U-Turn", Pokemon.active[enemyID]);
		Pokemon.active[enemyID].moveset[1] = new Move("Ice Beam", Pokemon.active[enemyID]);
		Pokemon.active[enemyID].moveset[2] = new Move("Thunderbolt", Pokemon.active[enemyID]);
		Pokemon.active[enemyID].moveset[3] = new Move("Fire Blast", Pokemon.active[enemyID]);
		for(int i = 0; i < enemy.team.length; i++)
		{
			if(enemy.team[i].name.equalsIgnoreCase(pokemonName))
			{
				Pokemon.active[enemyID] = enemy.team[i];
				break;
			}
		}
		newTurn();
	}
	
	public static void newTurn()
	{
		turnCount++;		
		System.err.println("\n\n\n*******************************TEAM "+teamID+", TURN "+(turnCount / 2)+"*******************************");
		System.err.println("**************************ACTIVE POKEMON: "+Pokemon.active[teamID].name+"**************************");
		if(showdown != null && turnCount % 5 == 0)
			lastTurnLogic();
		if(showdown == null)
			lastTurnEnemy = nextTurn;
		else
		{
			lastTurnUs = nextTurn;
			//TODO:	FETCH:
			//		- Enemy Pokemon
			//		- Our HP
			//		- Enemy HP
			//		- Enemy move used (and if any boosts were obtained)
			//		- The PP of the move we just used (and if any boosts were obtained)
			//		CHECK:
			//		- If we died (and change using generic logic if so)
			//		- Actual damage done (predicted versus actual)
			//		- If move was a crit.
			//		- Status inflicted
			//		- Entry hazards placed
		}
		simulating = true;
		if(generic)
			nextTurn = bestMove();
		else if(miniMax || genetic)
			nextTurn = minimax(1);
		//else if(genetic)
			//nextTurn = TODO;
		simulating = false;
		nextTurn.sendToShowdown();
	}
	
	public static void swapSides()
	{
		//If there's no Showdown running, play against ourselves.
		if(showdown == null)
		{
			if(turnCount % 5 != 0)
				System.out.println("***THIS IS BEING PRINTED FOR DEBUGGING PURPOSES.***\n(It would not be printed in an actual battle.)");
			lastTurnLogic();
			int e = teamID;
			teamID = enemyID;
			enemyID = e;
			Team t = us;
			us = enemy;
			enemy = t;
			Pokemon.active[teamID].onNewTurn(nextTurn);
			if(playing)
			{
				turnsToSimulate--;
				if(turnsToSimulate > 0)
					newTurn();
			}
		}
	}
	
	public static Change onPokemonDeath(Pokemon dead)
	{
		if(playing)
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
		playing = false;
		turnsToSimulate = 0;
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
		print("Geniusect win/loss ratio: "+winCount +" : "+ lossCount);
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
		print("This is the logic I used last turn (turn "+(turnCount - 1)+").");
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
		print("Here's what I know about the enemy's "+Pokemon.active[enemyID].name);
		print("It is level "+Pokemon.active[enemyID].level);
		print("Its types are "+Pokemon.active[enemyID].types[0]+" and "+Pokemon.active[enemyID].types[1]);
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
		for(int i = 0; i <Pokemon.active[enemyID].moveset.length; i++)
		{
			if(Pokemon.active[enemyID].moveset[i] == null)
			{
				print("I am unsure what it has in moveslot "+(i + 1));
				continue;
			}
			print(Pokemon.active[enemyID].moveset[i].name);
		}
		print("I believe its stats are about:");
		for(int i = 0; i < Pokemon.active[enemyID].stats.length; i++)
		{
			print(Stat.fromInt(i)+": "+Pokemon.active[enemyID].stats[i]);
		}
		print("I think it has an EV spread of:");
		for(int i = 0; i < Pokemon.active[enemyID].evs.length; i++)
		{
			print(Stat.fromInt(i)+": "+Pokemon.active[enemyID].evs[i]);
		}
		print("If I use my best move against them, "+ourBestMove.name+", it will do about "+ ourBestMove.getProjectedDamage(Pokemon.active[enemyID], true) +" HP worth of damage ("+ourBestMove.getProjectedPercent(Pokemon.active[enemyID], true)+" percent).");
		print("I project it will take about "+turnsToKillThem+" turns to kill their "+Pokemon.active[enemyID].name+".");
		print("If they use their best move against me, "+theirBestMove.name+", it will do about "+ theirBestMove.getProjectedDamage(Pokemon.active[teamID], true) +" HP worth of damage ("+theirBestMove.getProjectedPercent(Pokemon.active[teamID], true)+" percent).");
		print("I project it will take them about "+turnsToKillUs+" turns to kill my " +Pokemon.active[teamID].name+".");
		if(nextTurn instanceof Change)
		{
			Change switching = (Change) nextTurn;
			print("It is advisable that I switch, as they can kill me faster than I can kill them.");
			print("I plan to change to "+switching.switchTo.name+", who will take about "+theirBestMove.getProjectedDamage(switching.switchTo, true)+" HP worth of damage ("+theirBestMove.getProjectedPercent(switching.switchTo, true)+" percent).");
		}
		else if(nextTurn instanceof Attack)
		{
			Attack attacking = (Attack) nextTurn;
			print("I decided that the best course of action is an attack. "+Pokemon.active[teamID].name+" will use "+attacking.move.name);
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
		//TODO: Showdown hookup.
		System.out.println("BATTLE CHAT: "+text);
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

