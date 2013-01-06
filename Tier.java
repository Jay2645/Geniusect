package geniusect;

import java.util.ArrayList;

/**
 * Represents a list of all possible tiers for a Pokemon to be in.
 * @author TeamForretress
 */
public enum Tier 
{
	LC(0), NFE(1), NU(2), BL3(3), RU(4), BL2(5), UU(6), BL(7), OU(8), Limbo(9), Uber(10), G4CAP(11), G5CAP(12), Illegal(13);
	private int ranking;
	private Tier(int r)
	{
		ranking = r;
	}
	
	/**
	 * Gets this tier's ranking.
	 * @return (int): This tier's ranking.
	 */
	public int tierToRank()
	{
		return ranking;
	}
	
	/**
	 * Takes a String, returns the tier associated with that String.
	 * If none can be found, returns Tier.OU.
	 * @param tier (String): The tier to lookup.
	 * @return (Tier): The found Tier.
	 */
	public static Tier tierFromString(String tier)
	{
		tier = tier.toLowerCase();
		if(tier.contains("lc"))
			return Tier.LC;
		if(tier.contains("nfe"))
			return Tier.NFE;
		if(tier.contains("nu"))
			return Tier.NU;
		if(tier.contains("bl3"))
			return Tier.BL3;
		if(tier.contains("ru"))
			return Tier.RU;
		if(tier.contains("bl2"))
			return Tier.BL2;
		if(tier.contains("uu"))
			return Tier.UU;
		if(tier.contains("bl"))
			return Tier.BL;
		if(tier.contains("ou"))
			return Tier.OU;
		if(tier.contains("limbo"))
			return Tier.Limbo;
		if(tier.contains("uber"))
			return Tier.Uber;
		if(tier.contains("g4cap"))
			return Tier.G4CAP;
		if(tier.contains("g5cap"))
			return Tier.G5CAP;
		if(tier.contains("illegal"))
			return Tier.Illegal;
		else return Tier.OU;
	}
	
	/**
	 * Takes an int and returns a tier with that int as its rank.
	 * @param rank (int): The int to convert from.
	 * @return (Tier): The Tier with that rank. Returns OU if invalid.
	 */
	public static Tier tierFromInt(int rank)
	{
		switch(rank)
		{
			case 0: 	return Tier.LC;
			case 1: 	return Tier.NFE;
			case 2: 	return Tier.NU;
			case 3: 	return Tier.RU;
			case 4: 	return Tier.RU;
			case 5: 	return Tier.BL2;
			case 6: 	return Tier.UU;
			case 7: 	return Tier.BL;
			case 8: 	return Tier.OU;
			case 9: 	return Tier.Limbo;
			case 10:	return Tier.Uber;
			case 11:	return Tier.G4CAP;
			case 12:	return Tier.G5CAP;
			case 13:	return Tier.Illegal;
			default:	return Tier.OU;
		}
	}
	
	/**
	 * Returns all tiers between Tier one and Tier two. 
	 * The order in which they are listed does not matter.
	 * If Tier one and Tier two are the same, returns an array size one containing that tier.
	 * @param one (Tier): The first Tier to search between.
	 * @param two (Tier): The second Tier to search between.
	 * @return (Tier[]): An array of all tiers between the first and second (inclusive).
	 */
	public static Tier[] getBetween(Tier one, Tier two)
	{
		int rankOne = one.tierToRank();
		int rankTwo = two.tierToRank();
		int max = Math.max(rankOne, rankTwo);
		int min = Math.min(rankOne, rankTwo);
		Tier[] tierArray = new Tier[1];
		if(min == max)
		{
			tierArray[0] = tierFromInt(min);
			return tierArray;
		}
		ArrayList<Tier> tiers = new ArrayList<Tier>();
		for(int i = min; i <= max; i++)
		{
			tiers.add(tierFromInt(i));
		}
		tierArray = tiers.toArray(tierArray);
		return tierArray;
	}
}
