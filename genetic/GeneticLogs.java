/*
 * Keeps track of Pokemon we've seen in the past.
 * @author TeamForretress
 */

package geniusectai.genetic;

import geniusectsim.moves.Move;
import geniusectsim.pokemon.Pokemon;
import geniusectsim.pokemon.Spread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeneticLogs {
	
	public static Map<Spread, Map<String, ArrayList<String>>> matchupLog = new HashMap<Spread, Map<String, ArrayList<String>>>();
	
	/**
	 * Logs a matchup between <i>attacker</i> and <i>defender</i>. Converts <i>attacker</i> to a Spread.
	 * @param attacker - The Pokemon that is attacking.
	 * @param defender - The Pokemon that is defending.
	 * @param move - The move that <i>attacker</i> used against <i>defender</i>.
	 */
	public static void logMatchup(Pokemon attacker, Pokemon defender, Move move)
	{
		Spread match = new Spread(attacker, true);
		logMatchup(match, defender, move);
	}
	
	/**
	 * Logs a matchup between <i>attacker</i> and <i>defender</i>.
	 * @param attacker - The Spread that is attacking.
	 * @param defender - The Pokemon that is defending.
	 * @param move - The move that <i>attacker</i> used against <i>defender</i>.
	 */
	public static void logMatchup(Spread attacker, Pokemon defender, Move move)
	{
		Map<String, ArrayList<String>> log;
		if(matchupLog.containsKey(attacker))
		{
			log = matchupLog.get(attacker);
		}
		else
		{
			log = new HashMap<String, ArrayList<String>>();
			matchupLog.put(attacker, log);
		}
		ArrayList<String> movesUsed;
		if(log.containsKey(defender.getName()))
		{
			movesUsed =log.get(defender.getName());
			if(movesUsed.contains(move.name))
				return;
		}
		else
		{
			movesUsed = new ArrayList<String>();
		}
		movesUsed.add(move.name);
		log.put(defender.getName(), movesUsed);
		matchupLog.put(attacker, log);
	}
}
