package geniusectai.generic;

import geniusectai.GeniusectAI;
import geniusectsim.actions.Action;
import geniusectsim.actions.Attack;
import geniusectsim.actions.Change;
import geniusectsim.battle.Battle;
import geniusectsim.battle.Team;
import geniusectsim.bridge.Simulator;
import geniusectsim.constants.Pokequations;
import geniusectsim.moves.Move;
import geniusectsim.pokemon.Pokemon;
import geniusectsim.pokemon.Status;
import geniusectsim.pokemon.Stat;

//	An example of the generic AI in action:
// 	http://pastebin.com/tBcb6F2m

/**
 * All Generic AI logic.
 * Determines the most damaging move possible and performs it.
 * Does not (currently) take into account moves which boost stats nor recovery moves.
 * @author TeamForretress
 */
public class GenericAI {
	
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
	 * The Battle this AI is in.
	 */
	protected static Battle battle;
	
	/**
	 * Generic (scripted) behavior. 
	 * Finds the best move we can use or Pokemon we can switch to and returns the Action.
	 * @param b (Battle): The Battle to use.
	 * @return Action - the best choice in the given circumstance using generic scripted logic.
	 * @see geniusect.ai.GeniusectAI#bestMove(Pokemon, Pokemon)
	 */
	public static Action bestMove(Battle b)
	{
		return bestMove(b.getTeam(GeniusectAI.getTeamID(), true).getActive(),b.getTeam(GeniusectAI.getEnemyID(), true).getActive(), b);
	}
	
	/**
	 * Generic (scripted) behavior. 
	 * Finds the best move we can use or Pokemon we can switch to and returns the Action.
	 * @param b (Battle): The Battle to use.
	 * @param nextTurn (Action): The type of move the next turn must be (Change or Attack).
	 * @return Action - the best choice in the given circumstance using generic scripted logic.
	 * @see geniusect.ai.GeniusectAI#bestMove(Pokemon, Pokemon)
	 */
	public static Action bestMove(Battle b, Action nextTurn) 
	{
		if(nextTurn instanceof Change)
		{
			battle = b;
			((Change)nextTurn).changeTo(Change.bestCounter(b.getTeam(0, true).getPokemonTeam(), b.getTeam(0, true).getPokemonTeam()[0]));
			return nextTurn;
		}
		else
			return bestMove(b);
	}
	
	/**
	 * Generic (scripted) behavior. 
	 * Finds the best move we can use or Pokemon we can switch to considering our current team and returns the Action.
	 * @param user - the user's Pokemon.
	 * @param opponent - the opponent's Pokemon.
	 * @param b (Battle) - The Battle to use.
	 * @return Action - the best choice in the given circumstance using generic scripted logic.
	 * @see geniusect.ai.GeniusectAI#shouldSwitch(Pokemon, Pokemon)
	 */
	private static Action bestMove(Pokemon user, Pokemon opponent, Battle b)
	{
		return bestMove(user, opponent, user.getPokemonTeam(),b);
	}
	/**
	 * Generic (scripted) behavior. 
	 * Finds the best move we can use or Pokemon we can switch to and returns the Action.
	 * @param user - the user's Pokemon.
	 * @param opponent - the opponent's Pokemon.
	 * @param userTeam - The user's Pokemon team, as a Pokemon array.
	 * @param b (Battle): The Battle to use.
	 * @return Action - the best choice in the given circumstance using generic scripted logic.
	 * @see geniusect.ai.GeniusectAI#shouldSwitch(Pokemon, Pokemon)
	 */
	public static Action bestMove(Pokemon user, Pokemon opponent, Pokemon[] userTeam, Battle b)
	{
		battle = b;
		Action doNext;
		int switchConfidence = shouldSwitch(user,opponent);
		if(switchConfidence > 0 || ourBestMove == null)
		{
			Change sanityCheck = new Change();
			sanityCheck.changeTo(Change.bestChange(user, userTeam, opponent, Pokequations.bestMove(opponent, user)));
			if(switchConfidence <= 2 && ourBestMove != null)
			{
				ourBestMove = Pokequations.bestMove(user, opponent);
				theirBestMove = Pokequations.bestMove(opponent,user,ourBestMove);
				Pokemon sanityChange = sanityCheck.switchTo;
				int switchDamage = sanityChange.getHealth() - theirBestMove.getProjectedPercent(sanityChange, true) - Change.calculateSwitchDamagePercent(sanityChange);
				turnsToKillUs = Pokequations.turnsToKill(user.getHealth(), theirBestMove.getProjectedPercent(user).y + ourBestMove.recoilPercent);
				turnsToKillThem = Pokequations.turnsToKill(opponent.getHealth(), ourBestMove.getProjectedPercent(opponent).x + theirBestMove.recoilPercent);
				deficit = turnsToKillUs - turnsToKillThem;
				if(switchDamage < 0 || deficit > 0 || deficit == 0 && (ourBestMove.priority > theirBestMove.priority 
												|| ourBestMove.priority == theirBestMove.priority && user.isFasterThan(opponent)))
				{
					doNext = new Attack();
					((Attack)doNext).setMove(ourBestMove,user,opponent,battle);
				}
				else
					doNext = sanityCheck;
			}
			else if(switchConfidence > 2 && switchConfidence < 4)
			{
				if(ourBestMove != null && sanityCheck.switchTo.getName().toLowerCase().startsWith(user.getName().toLowerCase()))
				{
					System.err.println("Switch found, but it was us.");
					doNext = new Attack();
					((Attack)doNext).setMove(ourBestMove,user,opponent,battle);
				}
				else 
					doNext = sanityCheck;
			}
			else
			{
				ourBestMove = Pokequations.bestMove(user, opponent);
				theirBestMove = Pokequations.bestMove(opponent,user,ourBestMove);
				Pokemon sanityChange = sanityCheck.switchTo;
				int switchDamage = sanityChange.getHealth() - theirBestMove.getProjectedPercent(sanityChange, true) - Change.calculateSwitchDamagePercent(sanityChange);
				turnsToKillUs = Pokequations.turnsToKill(user.getHealth(), theirBestMove.getProjectedPercent(user).y + ourBestMove.recoilPercent);
				turnsToKillThem = Pokequations.turnsToKill(opponent.getHealth(), ourBestMove.getProjectedPercent(opponent).x + theirBestMove.recoilPercent);
				deficit = turnsToKillUs - turnsToKillThem;
				if(switchDamage <= 0 || deficit > 0 || deficit == 0 && (ourBestMove.priority > theirBestMove.priority 
												|| ourBestMove.priority == theirBestMove.priority && user.isFasterThan(opponent)))
				{
					doNext = new Attack();
					((Attack)doNext).setMove(ourBestMove,user,opponent,battle);
				}
				else if(sanityCheck.switchTo.getName().toLowerCase().startsWith(user.getName().toLowerCase()))
				{
					Pokemon attemptTwo = Change.bestCounter(userTeam, opponent,user);
					if(ourBestMove == null || attemptTwo != null && !attemptTwo.getName().startsWith(user.getName()))
					{
						System.err.println("Switch found, but it was us. Second attempt produced "+attemptTwo.getName());
						doNext = new Change();
						((Change)doNext).changeTo(attemptTwo);
					}
					else
					{
						System.err.println("Switch found, but it was us.");
						doNext = new Attack();
						((Attack)doNext).setMove(ourBestMove,user,opponent,battle);
					}
				}
				else 
					doNext = sanityCheck;
			}
		}
		else
		{
			doNext = new Attack();
			((Attack)doNext).setMove(ourBestMove,user,opponent,battle);
		}
		if(doNext instanceof Attack)
		{
			Attack a = (Attack)doNext;
			if(a.move == null);
				a.move = ourBestMove;
		}
		GeniusectAI.markDecision(doNext);
		return doNext;
	}
	
	/**
	 * Generic (scripted) behavior. 
	 * Calculates best moves for both teams and the amount of turns it is projected to take to kill either side.
	 * Then provides a boolean saying if it is better to switch or stay in.
	 * @param user - The user's Pokemon.
	 * @param opponent - The opponent's Pokemon.
	 * @return int - An int representing a rough estimate of the amount of danger we are in should we stay in.
	 * Higher numbers mean we should switch.
	 */
	private static int shouldSwitch(Pokemon user, Pokemon opponent)
	{
		theirBestMove = Pokequations.bestMove(opponent,user);
		if(user.getStatus() == Status.Sleep && !user.hasMove("Sleep Talk") && !user.hasMove("Snore") && !user.hasMove("Rest"))
			return 6;
		//Check if we're locked into a move:
		Move lockedInto = user.getLockedInto();
		if(lockedInto == null)
			ourBestMove = Pokequations.bestMove(user, opponent,theirBestMove);
		else
			ourBestMove = lockedInto;
		//First we check to see if we're trapped. If we are, we can't switch.
		if(!user.canSwitch())
			return Integer.MIN_VALUE;
		//If we've taken heavy stat drops, we should switch.
		int stats = 0;
		for(int i = 0; i < 6; i++)
		{
			stats += user.getBoosts(Stat.fromInt(i));
		}
		if(stats < 0)
			return -stats;
		//If we have no good moves to use, we should also switch.
		if(ourBestMove == null ||  Pokequations.damageMultiplier(ourBestMove.getType(), opponent.getTypes()) == 0)
			return 8;
		//We assume the opponent has one type of either STAB, then check if either is super effective against us.
		//If it is, we switch.
		double damageType1 = Pokequations.damageMultiplier(opponent.getType(0), user.getTypes());
		double damageType2 = Pokequations.damageMultiplier(opponent.getType(1), user.getTypes());
		double damageMult = damageType1 * damageType2;
		if(damageMult > 2 || damageMult == 2 && opponent.isFasterThan(user))
			return 8;
		//If we have a move to use, but it doesn't do any damage, we should switch.
		if(ourBestMove.getProjectedPercent(opponent).y <= 20)
			return 6;	
		//Now we check to see if we have anyone who better-prepared than us at killing the enemy. If so, we switch.
		//First calculate our best multiplier.
		Pokemon[] userTeam = user.getPokemonTeam();
		double userDamageMult = 0;
		Move[] userMoveset = user.getMoveset();
		if(userMoveset == null || userMoveset[0] == null)
		{
			userDamageMult = Math.max(	Pokequations.damageMultiplier(user.getType(0), opponent.getTypes()),
										Pokequations.damageMultiplier(user.getType(1), opponent.getTypes()));
		}
		else
		{
			for(int i = 0; i < 4; i++)
			{
				if(userMoveset[i] == null || userMoveset[i].disabled)
					continue;
				double damageType = Pokequations.damageMultiplier(userMoveset[i].getType(), opponent.getTypes());
				if(damageType > userDamageMult)
					userDamageMult = damageType;
			}
		}
		//If we have no super-effective moves to use, we check if anyone else has a good multiplier.
		if(userDamageMult < 2)
		{
			double teamDamageMult = 0;
			for(int i = 0; i < 6; i++)
			{
				if(userTeam[i] == null || userTeam[i].getName() == user.getName() || !userTeam[i].isAlive())
					continue;
				Move[] userMoves = userTeam[i].getMoveset();
				if(userMoves == null || userMoves[0] == null)
				{
					teamDamageMult = Math.max(	Pokequations.damageMultiplier(userTeam[i].getType(0), opponent.getTypes()),
												Pokequations.damageMultiplier(userTeam[i].getType(1), opponent.getTypes()));
				}
				else
				{
					for(int m = 0; m < 4; m++)
					{
						if(userMoves[m] == null || userMoves[m].disabled)
							continue;
						double damageType = Pokequations.damageMultiplier(userMoves[m].getType(), opponent.getTypes());
						if(damageType > teamDamageMult)
							userDamageMult = damageType;
					}
				}
			}
			//If someone has a higher multiplier than us, we should switch.
			if(teamDamageMult > userDamageMult)
				return (int)Math.floor((teamDamageMult * 2) - (userDamageMult * 2));
		}
		//Now we go back to our locked-into move and see if it's a good idea to stay in.
		if(lockedInto != null)
		{
			int mostDamage = lockedInto.getProjectedPercent(opponent).y;
			if(mostDamage <= 20)
				return 6;
			theirBestMove = Pokequations.bestMove(opponent,user,lockedInto);
			ourBestMove = lockedInto;
		}
		//If they don't have any good moves to use against us (and they have passed all prior checks), then we should stay in.
		if(theirBestMove == null)
			return -1;
		//Calculate how long it'll take to kill one another, then see if we can kill them faster than they can kill us.
		turnsToKillUs = Pokequations.turnsToKill(user.getHealth(), theirBestMove.getProjectedPercent(user).y + ourBestMove.recoilPercent);
		turnsToKillThem = Pokequations.turnsToKill(opponent.getHealth(), ourBestMove.getProjectedPercent(opponent).x + theirBestMove.recoilPercent);
		deficit = turnsToKillUs - turnsToKillThem;
		if(deficit == 0 && (ourBestMove.priority > theirBestMove.priority 
						|| ourBestMove.priority == theirBestMove.priority && user.isFasterThan(opponent)))
			return -1;
		if(deficit < 0 || deficit == 0 && opponent.isFasterThan(user))
		{
			Pokemon changeTo = Change.bestChange(user, userTeam, opponent, Pokequations.bestMove(opponent, user));
			if(Pokequations.turnsToKill(changeTo.getHealth(), theirBestMove.getProjectedPercent(changeTo).y) > 2)
				return 5;
			else return 3;
		}
		else return -deficit;
	}
	
	/**
	 * Called when a Pokemon dies. The AI finds the best Pokemon to change to.
	 * @param dead - The Pokemon that died.
	 * @return Change - The Change action specifying which Pokemon to switch to.
	 */
	public static Change onPokemonDeath(Pokemon dead)
	{
		Battle battle = GeniusectAI.getBattle();
		if(battle == null || battle.isPlaying())
		{
			//Called when a Pokemon gets killed.
			Pokemon change = null;
			change = Change.bestCounter(Simulator.getSwitchableTeam(), dead.getEnemy());
			if(change == null)
			{
				System.err.println(dead.getEnemy().getName()+" wins the game!");
				boolean won = dead.getTeam() == Team.getEnemyTeam(0);
				GeniusectAI.gameOver(won);
				return null;
			}
			else
			{
				Change c = new Change();
				c.changeTo(change);
				//c.say("Oh my god! You killed "+dead.getName()+"! D:");
				return c;
			}
		}
		return null;
	}
}
