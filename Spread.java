/*
 * A SPREAD is a Pokemon which is either commonly used or one we have encountered before.
 * We can identify spreads by comparing ones we have encountered in the past with the Pokemon we're facing now.
 * After we've reverse-engineered enough data about a Pokemon, we can establish a degree of certainty that we're facing a spread.
 * Once we've determined we're facing a spread, we can switch to Genetic Algorithms to know the best way to beat that spread.
 * @author TeamForretress
 */

package geniusect;

public class Spread extends Pokemon {
	public Spread(Pokemon p, boolean recheck)
	{
		clone(p);
		team.team[id] = this;
		improbable = recheck;
	}
	
	public Spread(Pokemon p, double prob, int encountered)
	{
		name = p.name;
		item = p.item;
		level = p.level;
		ability = p.ability;
		nature = p.nature;
		evs = p.evs;
		moveset = p.moveset;
		lead = p.lead;
		if(lead)
			probabilityLead = prob;
		else
			probabilityRegular = prob;
		timesEncountered = encountered;
	}
	
	public Spread(String n, Item i, int l, Ability a, Nature nat, int[] ev, Move[] set, boolean isLead, double p, int encountered)
	{
		name = n;
		item = i;
		level = l;
		ability = a;
		nature = nat;
		evs = ev;
		moveset = set;
		lead = isLead;
		if(lead)
			probabilityLead = p;
		else
			probabilityRegular = p;
		timesEncountered = encountered;
	}
	double probabilityRegular;
	double probabilityLead;
	int timesEncountered;
	int rageQuits; //How many times this Pokemon has made someone rageQuit.
	boolean improbable = false;
	
	public static Spread identifySpread(Pokemon p)
	{
		Spread found = null;
		//TODO: Get all spreads of p.name, then discard any which have incorrect moveset.
		//If we know the item, discard any which have an incorrect item.
		//If we know the ability, discard any which have the incorrect ability.
		//If we've used more than 3/4 our predicted EVs, see if any have around the same EVs.
		//If we can't find any with around the same EVs or we have multiples or we haven't determined enough EVs, check if it is a lead.
		//If it is a lead, return the most probable lead.
		//If it is not, return the most probable non-lead.
		//Log our probability estimate (1 / #found).
		//If we have no results at all, cast the Pokemon to a spread, mark it as improbable, and give it a probability of 0.01.
		//If we are reasonably confident (probability >= .5), switch to Genetic Algorithm while Pokemon is active.
		return found;
	}
	
	public void withdraw()
	{
		//Called when this Pokemon withdraws from the battle.
		if(active[team.teamID] == this)
			active[team.teamID] = null;
		boostedStats = stats;
		if(team.teamID == 0)
		{
			GeniusectAI.setMiniMax();
		}
	}
	
	public void wobbuffet(boolean entering)
	{
		if(team.teamID == 0 && name.toLowerCase().startsWith("wobbuffet") || name.toLowerCase().startsWith("wynaut"))
		{
			if(entering)
				GeniusectAI.setGeneric();
			//else
				//GeniusectAI.setGenetic();
		}
	}
}
