package geniusect.ai;

import geniusect.Action;
import geniusect.Attack;
import geniusect.Battle;
import geniusect.Change;
import geniusect.Move;
import geniusect.Pokemon;
import geniusect.Pokequations;
import geniusect.SQLHandler;

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
	 * Generic (scripted) behavior. 
	 * Finds the best move we can use or Pokemon we can switch to and returns the Action.
	 * @param b (Battle): The Battle to use.
	 * @return Action - the best choice in the given circumstance using generic scripted logic.
	 * @see geniusect.ai.GeniusectAI#bestMove(Pokemon, Pokemon)
	 */
	public static Action bestMove(Battle b)
	{
		return bestMove(b.getTeam(GeniusectAI.getTeamID()).getActive(),b.getTeam(GeniusectAI.getEnemyID()).getActive(), b);
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
	protected static Action bestMove(Pokemon user, Pokemon opponent, Pokemon[] userTeam, Battle b)
	{
		Action doNext;
		if(shouldSwitch(user,opponent))
		{
			Change sanityCheck = new Change(Change.bestChange(user, userTeam, opponent, Pokequations.bestMove(opponent, user),b.getShowdown()),b);
			if(sanityCheck.switchTo.getName().toLowerCase().startsWith(user.getName().toLowerCase()))
			{
				System.err.println("Switch found, but it was us. Attacking with "+ourBestMove.name);
				doNext = new Attack(ourBestMove,user,opponent,b);
			}
			else 
				doNext = sanityCheck;
		}
		else
		{
			doNext = new Attack(ourBestMove,user,opponent,b);
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
	 * Finds the best Pokemon we can switch to.
	 * Calculates best moves for both teams and the amount of turns it is projected to take to kill either side.
	 * @param user - The user's Pokemon.
	 * @param opponent - The opponent's Pokemon.
	 * @return boolean - TRUE if we should switch, FALSE if we should stay in.
	 */
	private static boolean shouldSwitch(Pokemon user, Pokemon opponent)
	{
		theirBestMove = Pokequations.bestMove(opponent,user);
		
		if(theirBestMove == null)
			return false;
		ourBestMove = Pokequations.bestMove(user, opponent,theirBestMove);
		if(ourBestMove == null)
			return true;
		turnsToKillUs = Pokequations.turnsToKill(user.getHealth(), theirBestMove.getProjectedPercent(user).y + ourBestMove.recoilPercent);
		turnsToKillThem = Pokequations.turnsToKill(opponent.getHealth(), ourBestMove.getProjectedPercent(opponent).x + theirBestMove.recoilPercent);
		
		deficit = turnsToKillUs - turnsToKillThem;
		double damageType1 = Pokequations.damageMultiplier(opponent.getType(0), user.getTypes());
		double damageType2 = Pokequations.damageMultiplier(opponent.getType(1), user.getTypes());
		double damageMult = damageType1 * damageType2;
		if(damageMult > 2)
			return true;
		if(damageMult == 2 && opponent.isFasterThan(user));
		if(deficit < 0 || deficit == 0 && opponent.isFasterThan(user))
			return true;
		else return false;
	}

}
