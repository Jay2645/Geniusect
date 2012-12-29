/*
 * The AI logic.
 * Can currently adjust behavior based upon minimax (minimize losses) or generic (use best attack) modes.
 * TODO: Genetic AI based upon common spreads.
 * @author TeamForretress
 */

package geniusect;

public class GeniusectAI {
	
	public static Team us;
	public static Team enemy;
	
	public static Move ourBestMove;	
	public static Move theirBestMove;
	
	public static int turnsToKillThem;
	public static int turnsToKillUs;
	
	public static int deficit;
	public static Action nextTurn;
	public static Action lastTurn;
	
	private static boolean genetic = false;
	private static boolean miniMax = false;
	private static boolean generic = true;
	
	public static int turnCount = 0;
	
	public static String version = "0.0.1";
	
	public static void battleStart(String load)
	{
		//Called when the battle begins.
		//Can load a team from an importable.
		print("Geniusect AI version "+version+" initialized.");
		print("You are playing against an AI. If you suspect the AI is not responding, feel free to hit 'Kick Inactive Player.'");
		us = new Team(load);
		enemy = new Team();
		//TODO: If we can choose lead, do so and log names of all enemy Pokemon.
		String pokemonName = "Genesect"; //TODO: Replace with the name of the enemy lead.
		enemy.addPokemon(pokemonName);
		us.active = us.team[0];
		enemy.active = enemy.team[0];
		us.active.changeEnemy(enemy.active);
		enemy.active.changeEnemy(us.active);
		enemy.active.moveset[0] = new Move("U-Turn", enemy.active);
		enemy.active.moveset[1] = new Move("Ice Beam", enemy.active);
		enemy.active.moveset[2] = new Move("Thunderbolt", enemy.active);
		enemy.active.moveset[3] = new Move("Fire Blast", enemy.active);
		for(int i = 0; i < enemy.team.length; i++)
		{
			if(enemy.team[i].name.equalsIgnoreCase(pokemonName))
			{
				enemy.active = enemy.team[i];
				break;
			}
		}
		newTurn();
	}
	
	public static void newTurn()
	{
		turnCount++;
		if(turnCount % 5 == 0)
			lastTurnLogic();
		lastTurn = nextTurn;
		//TODO:	FETCH:
		//		- Enemy Pokemon
		//		- Our HP
		//		- Enemy HP
		//		- Enemy move used (and if any boosts were obtained)
		//		- The PP of the move we just used (and if any boosts were obtained)
		//		CHECK:
		//		- If we died (and change using generic logic if so)
		//		- Actual damage done (predicted versus actual)
		//		- If move was crit.
		//		- Status inflicted
		//		- Entry hazards placed
		if(generic)
			nextTurn = bestMove();
		else if(miniMax || genetic)
			nextTurn = minimax(1);
		//else if(genetic)
			//nextTurn == TODO;
			
		nextTurn.sendToShowdown();
	}
	
	public static void gameOver(boolean won)
	{
		if(won)
		{
			print("We won! Bow before your new robot overlords.");
		}
		else
		{
			print("GG. :(");
		}
		if(turnCount % 5 != 0)
			lastTurnLogic();
	}
	
	public static Action bestMove()
	{
		return bestMove(us.active,enemy.active);
	}
	
	public static Action bestMove(Pokemon ourGuy, Pokemon theirGuy)
	{
		Action doNext;
		if(shouldSwitch(ourGuy,theirGuy))
		{
			Change sanityCheck = new Change(Change.bestChange(ourGuy, ourGuy.team.team, theirGuy, Pokequations.bestMove(theirGuy, ourGuy)));
			if(sanityCheck.switchTo == ourGuy)
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
		print("Their best move is "+theirBestMove.name+", which will kill in about "+turnsToKillUs+" turns.");
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
			theirBestMove = Pokequations.bestMove(enemy.active, us.active, ourBestMove);
			if(theirBestMove == null)
				turnsToKillUs++;
			else
				turnsToKillUs = Pokequations.turnsToKill(us.active.hpPercent, theirBestMove.getProjectedPercent(us.active).y + ourBestMove.recoilPercent);
			turnsToKillThem = Pokequations.turnsToKill(enemy.active.hpPercent, ourBestMove.getProjectedPercent(enemy.active).x + theirBestMove.recoilPercent);
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
		print("Here's what I know about the enemy's "+enemy.active.name);
		print("It is level "+enemy.active.level);
		print("Its types are "+enemy.active.types[0]+" and "+enemy.active.types[1]);
		print("Its nature is "+enemy.active.nature.toString());
		if(enemy.active.ability != null)
			print("Its ability is "+enemy.active.ability.name);
		if(!enemy.active.immunities.isEmpty())
		{
			print("It is immune to:");
			for(int i = 0; i < enemy.active.immunities.size(); i++)
			{
				print(enemy.active.immunities.get(i).toString());
			}
		}
		print("It knows:");
		for(int i = 0; i <enemy.active.moveset.length; i++)
		{
			if(enemy.active.moveset[i] == null)
			{
				print("I am unsure what it has in moveslot "+(i + 1));
				continue;
			}
			print(enemy.active.moveset[i].name);
		}
		print("I believe its stats are about:");
		for(int i = 0; i < enemy.active.stats.length; i++)
		{
			print(Stat.fromInt(i)+": "+enemy.active.stats[i]);
		}
		print("I think it has an EV spread of:");
		for(int i = 0; i < enemy.active.evs.length; i++)
		{
			print(Stat.fromInt(i)+": "+enemy.active.evs[i]);
		}
		print("If I use my best move against them, "+ourBestMove.name+", it will do about "+ ourBestMove.getProjectedDamage(enemy.active, true) +" HP worth of damage ("+ourBestMove.getProjectedPercent(enemy.active, true)+" percent).");
		print("I project it will take about "+turnsToKillThem+" turns to kill their "+enemy.active.name+".");
		print("If they use their best move against me, "+theirBestMove.name+", it will do about "+ theirBestMove.getProjectedDamage(us.active, true) +" HP worth of damage ("+theirBestMove.getProjectedPercent(us.active, true)+" percent).");
		print("I project it will take them about "+turnsToKillUs+" turns to kill my " +us.active.name+".");
		if(nextTurn instanceof Change)
		{
			Change switching = (Change) nextTurn;
			print("It is advisable that I switch, as they can kill me faster than I can kill them.");
			print("I plan to change to "+switching.switchTo.name+", who will take "+theirBestMove.getProjectedDamage(switching.switchTo, true)+" HP worth of damage ("+theirBestMove.getProjectedPercent(switching.switchTo, true)+" percent).");
		}
		else if(nextTurn instanceof Attack)
		{
			Attack attacking = (Attack) nextTurn;
			print("I decided that the best course of action is an attack. "+us.active.name+" will use "+attacking.move.name);
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
		AINode node = new AINode(us.team,enemy.team,us.active,enemy.active);
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
					node.ourActive.onWithdraw();
					node.ourTeam[i].onSendOut();
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
		System.out.println(text);
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

