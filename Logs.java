/*
 * Keeps track of Pokemon we've seen in the past.
 * @author TeamForretress
 */

package geniusect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Logs {
	
	public static Map<Spread, Map<String, ArrayList<String>>> matchupLog = new HashMap<Spread, Map<String, ArrayList<String>>>();
	
	public static void logMatchup(Pokemon attacker, Pokemon defender, Move move)
	{
		Spread match = new Spread(attacker, true);
		logMatchup(match, defender, move);
	}
	
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
		if(log.containsKey(defender.name))
		{
			movesUsed =log.get(defender.name);
			if(movesUsed.contains(move.name))
				return;
		}
		else
		{
			movesUsed = new ArrayList<String>();
		}
		movesUsed.add(move.name);
		log.put(defender.name, movesUsed);
		matchupLog.put(attacker, log);
	}
}
