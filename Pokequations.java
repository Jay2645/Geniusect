/*
 * A bunch of game constants, formulas, and the like.
 * @author TeamForretress
 */

package geniusect;
import java.awt.Point;

public class Pokequations {
	
	public static Point calculateDamagePercent(Pokemon attacker, Move move, Pokemon defender)
	{
		Point percentage = calculateDamage(attacker, move, defender);
		//System.out.println(percentage.x+", "+defender.boostedStat(Stat.HP));
		percentage.x = percentage.x / (defender.boostedStat(Stat.HP) / 100);
		percentage.y = percentage.y / (defender.boostedStat(Stat.HP) / 100);
		return percentage;
	}
	public static Point calculateDamage(Spread attacker, Move move, Spread defender)
	{
		//Make sure we can't use the exact calculations unless we believe we know everything about both sides.
		return calculateDamage(attacker,move,defender);
	}
	
	public static Point calculateDamage(Pokemon attacker, Move move, Pokemon defender)
	{
		//System.out.println("Calculating the damage if "+attacker.name+" uses "+move.name+" on "+defender.name);
		//Returns damage dealt as a point(minValue, maxValue).
		if(move.status)
		{
			//System.out.println(move.name+" is a status-inflicting move, so it doesn't do any damage.");
			return new Point(0,0);
		}
		boolean stab = false;
		if(attacker.types[0] == move.type || attacker.types[1] == move.type)
			stab = true;
		
		int level = attacker.level;
		int attackPower = move.power;
		int attackStat;
		int defenseStat;
		if(move.special)
		{
			attackStat = attacker.boostedStat(Stat.SpA);
			defenseStat = defender.boostedStat(Stat.SpD);
		}
		else
		{
			attackStat = attacker.boostedStat(Stat.Atk);
			defenseStat = defender.boostedStat(Stat.Def);
		}
		double multiplier = damageMultiplier(move.type, defender.types);
		
		return calculateDamage(level, attackStat, attackPower, defenseStat, stab, multiplier);
	}
	
	private static Point calculateDamage(int level, int attackStat, int attackPower, int defenseStat, boolean stab, double multiplier)
	{
		//Returns damage dealt as a point(minValue, maxValue).
		double bonus = 1;
		if(stab)
			bonus = 1.5;
		
		Point p = new Point();
		p.y = (int)Math.floor(((((2 * level / 5 + 2) * attackStat * attackPower / defenseStat) / 50) + 2) * bonus * multiplier);
		p.x = (int)Math.ceil(p.y * 0.85);
		//System.out.println("Max damage is "+p.y);
		return p;
	}
	
	
	
	public static int calculateAtkStat(Pokemon attacker, Move move, Pokemon defender, int percentageLost)
	{
		int attackPower = move.power;
		int level = attacker.level;
		int defenseStat;
		if(move.special)
			defenseStat = defender.boostedStat(Stat.SpD);
		else
			defenseStat = defender.boostedStat(Stat.Def);
		double multiplier = damageMultiplier(move.type, defender.types);
		double bonus = 1;
		if(attacker.types[0] == move.type || attacker.types[1] == move.type)
			bonus = 1.5;
		int damage = calculateHPDamage(percentageLost,defender.boostedStat(Stat.HP));
		
		return (int)Math.floor(50 * damage * defenseStat / (bonus * multiplier * attackPower * (2 * level / 5 + 2)) - 100 * defenseStat / (attackPower * (2 * level / 5 + 2)));
	}
	
	public static int calculateDefStat(Pokemon attacker, Move move, Pokemon defender, int percentageLost)
	{
		/*int attackPower = move.power;
		int level = attacker.level;
		int defenseStat;
		if(move.special)
			defenseStat = defender.stats[4];
		else
			defenseStat = defender.stats[2];
		double multiplier = damageMultiplier(move.type, defender.types);
		double bonus = 1;
		if(attacker.types[0] == move.type || attacker.types[1] == move.type)
			bonus = 1.5;
		int damage = calculateHPDamage(percentageLost,defender.stats[0]);
		return (int)Math.floor(50 * damage * defenseStat / (bonus * multiplier * attackPower * (2 * level / 5 + 2)) - 100 * defenseStat / (attackPower * (2 * level / 5 + 2)));
		*/
		//TODO: Work out what to do about this.
		return 0;
	}
	
	public static int calculateHPDamage(int percentage, int hp)
	{
		//Returns the amount of HP lost based upon our known HP value and a percentage of lost HP.
		return Math.round((percentage / 100) * hp);
	}
	
	public static double damageMultiplier(Type move, Type[] enemy)
	{
		if(move == Type.None)
			return 1;
		//Returns the damage multiplier value for a type matchup.
		double first = SQLHandler.queryDamage(move, enemy[0]);
		if(enemy[1] == Type.None)
			return first;
		double second = SQLHandler.queryDamage(move, enemy[1]);
		return first * second;
	}
	
	public static int[] calculateStat(Pokemon pokemon)
	{
		int[] stats = new int[6];
		for(int i = 0; i < 6; i++)
		{
			stats[i] = calculateStat(Stat.fromInt(i),pokemon);
		}
		return stats;
	}
	
	public static int calculateStat(Stat type, Pokemon pokemon)
	{
		if(pokemon.base == null || pokemon.base[type.toInt()] == 0)
			return 0;
		return calculateStat(type, pokemon.nature, pokemon.base[type.toInt()], pokemon.ivs[type.toInt()],pokemon.evs[type.toInt()], pokemon.level);
	}
	
	public static int calculateStat(Stat type, Nature nature, int base, int iv, int ev, int level)
	{
		return calculateStat(type,nature.multiplier(type),base,iv,ev,level);
	}
	
	public static int calculateStat(Stat type, double natureValue, int base, int iv, int ev, int level)
	{
		//Returns any non-HP stat as an int.
		if(type == Stat.HP)
			return calculateHP(base,iv,ev,level);
		else return (int) Math.ceil((((iv + 2 * base + (ev/4) ) * level/100 ) + 5) * natureValue);
	}
	public static int calculateHP(int base, int iv, int ev, int level)
	{
		//Returns the HP stat as an int.
		return (int) Math.ceil(((iv + 2 * base + (ev/4) ) * level/100 ) + 10 + level);
	}
	
	public static int calculateEVs(Stat stat, Pokemon pokemon)
	{
		return calculateEVs(pokemon.nature,stat,pokemon.base[stat.toInt()],pokemon.level,pokemon.stats[stat.toInt()],pokemon.ivs[stat.toInt()]);
	}
	
	public static int calculateEVs(Nature nature, Stat stat, int base, int level, int statValue, int iv)
	{
		double natureValue = nature.multiplier(stat);
		return (int)Math.ceil(-(4 *(natureValue * (2 * base*level+level * iv+500)-100 * statValue))/(natureValue * level));
	}
	
	public static int statBoost(int level, int stat)
	{
		//Adjusts a stat for a certain number of boosts, then returns the adjusted stat.
		double adjust = 1;
		if(level < -6)
			level = -6;
		else if(level > 6)
			level = 6;
		switch(level) {
			case -6:	adjust = 0.25;
						break;
			case -5:	adjust = 0.285;
						break;
			case -4:	adjust = 0.33;
						break;
			case -3:	adjust = 0.4;
						break;
			case -2:	adjust = 0.5;
						break;
			case -1:	adjust = 0.66;
						break;
			case 0:		adjust = 1;
						break;
			case 1:		adjust = 1.5;
						break;
			case 2:		adjust = 2;
						break;
			case 3:		adjust = 2.5;
						break;
			case 4:		adjust = 3;
						break;
			case 5:		adjust = 3.5;
						break;
			case 6:		adjust = 4;
						break;
		}
		return (int)Math.round(stat*adjust);
	}
	
	public static Move bestMove(Pokemon attacker, Pokemon defender, Move enemyMove, int turnsUntilDead)
	{
		//Proper calculation for Wobbuffet, Wynaut, etc.
		boolean foundMove = false;
		for(int i = 0; i < attacker.moveset.length; i++)
		{
			if(attacker.moveset[i] == null || attacker.moveset[i].disabled)
				continue;
			foundMove = true;
			if(turnsUntilDead > 1)
			{
				if(attacker.moveset[i].name.equalsIgnoreCase("counter") && !enemyMove.special || attacker.moveset[i].name.equalsIgnoreCase("mirror coat") && enemyMove.special)
				{
					attacker.moveset[i].getProjectedDamage(defender).x = enemyMove.getProjectedDamage(attacker).x * 2;
					attacker.moveset[i].getProjectedDamage(defender).y = enemyMove.getProjectedDamage(attacker).y * 2;
				}
				if(turnsUntilDead == 2 && !attacker.isFasterThan(defender))
				{
					if(attacker.moveset[i].name.equalsIgnoreCase("destiny bond"))
					{
						attacker.moveset[i].getProjectedDamage(defender).x = Integer.MAX_VALUE - 1;
						attacker.moveset[i].getProjectedDamage(defender).y = Integer.MAX_VALUE;
						attacker.moveset[i].getProjectedPercent(defender).x = 99;
						attacker.moveset[i].getProjectedPercent(defender).y = 100;
					}
				}
				else if(attacker.moveset[i].name.equalsIgnoreCase("destiny bond"))
				{
					attacker.moveset[i].getProjectedDamage(defender).x = 0;
					attacker.moveset[i].getProjectedDamage(defender).y = 0;
					attacker.moveset[i].getProjectedPercent(defender).x = 0;
					attacker.moveset[i].getProjectedPercent(defender).y = 0;
				}
			}
			else
			{
				if(attacker.moveset[i].name.equalsIgnoreCase("counter")|| attacker.moveset[i].name.equalsIgnoreCase("mirror coat"))
				{
					attacker.moveset[i].getProjectedDamage(defender).x = 0;
					attacker.moveset[i].getProjectedDamage(defender).y = 0;
					attacker.moveset[i].getProjectedPercent(defender).x = 0;
					attacker.moveset[i].getProjectedPercent(defender).y = 0;
				}
				else if(turnsUntilDead == 1 && attacker.isFasterThan(defender) && attacker.moveset[i].name.equalsIgnoreCase("destiny bond"))
				{
					attacker.moveset[i].getProjectedDamage(defender).x = Integer.MAX_VALUE - 1;
					attacker.moveset[i].getProjectedDamage(defender).y = Integer.MAX_VALUE;
					attacker.moveset[i].getProjectedPercent(defender).x = 99;
					attacker.moveset[i].getProjectedPercent(defender).y = 100;
				}
			}
		}
		if(foundMove)
			return bestMove(attacker,defender);
		else return new Move("Struggle", attacker);
	}
	
	public static Move bestMove(Pokemon attacker, Pokemon defender)
	{
		return bestMove(attacker, defender, attacker.moveset);
	}
	
	public static Move bestMove(Pokemon attacker, Pokemon defender, Move[] moveset)
	{
		Point damage = new Point(Integer.MIN_VALUE, Integer.MAX_VALUE);
		Move use = null;
		for(int i = 0; i < moveset.length; i++)
		{
			if(moveset[i] == null || moveset[i].disabled)
			{
				continue;
			}
			Point moveDamage = calculateDamage(attacker, moveset[i],defender);
			if(moveDamage.y > damage.y)
			{
				damage=moveDamage;
				use = moveset[i];
			}
		}
		if(use == null)
		{
			use = new Move("Struggle", attacker);
		}
		return use;
	}
	
	
	public static int turnsToKill(int health, int damage)
	{
		if(damage == 0)
			return Integer.MAX_VALUE;
		return (int)Math.floor(health / damage);
	}
	
	public static void printParentRecursive(AINode node)
	{
		if(node.parent != null)
		{
			System.err.println(node.ourActive.name+": "+node.decision.name);
			printParentRecursive(node.parent);
		}
	}
	
	public static Action miniMax(AINode node, int depth)
	{
		depth = 16;
		//Calculates all worst-case scenarios, then returns steps to minimize losses.
		if(depth <= 0 || node.children == null)
		{
			//printParentRecursive(node);
			return node.decision;
		}
		/*int alpha = -node.player * Integer.MAX_VALUE;
		for(int i = 0; i < node.children.length; i++)
		{
			int miniResult = miniMax(node.children[i], depth - 1);
			if(node.player == 1)
			{
				if(alpha >= miniResult)
				{
					alpha = miniResult;
					node.result = node.children[i];
					System.out.println("Current result for player 1 is "+alpha);
				}
				//System.out.println("Alpha is "+alpha+", miniResult (larger than alpha) is "+miniResult+", depth is "+depth);
			}
			else
			{
				if(alpha <= miniResult)
				{
					alpha = miniResult;
					node.result = node.children[i];
					System.out.println("Current result for player -1 is "+alpha);
				}
				//System.out.println("Alpha is "+alpha+", miniResult (smaller than alpha) is "+miniResult+", depth is "+depth);
			}
		}*/
		int alpha = Integer.MIN_VALUE;
		Action decision = null;
		for(int i = 0; i < node.children.length; i++)
		{
			Action miniResult = miniMax(node.children[i], depth - 1);
			int miniScore = -miniResult.score;
			if(miniResult.name.equalsIgnoreCase("Heat Wave"))
				System.err.println("Heat Wave has a score of "+miniResult+", we need to beat"+alpha+".");
			if(miniScore > alpha)
			{
				System.out.println(node.decision.name+" has a score of "+miniResult+", to be achieved in "+node.count+" turns.");
				decision = miniResult;
				alpha = miniScore;
			}
		}
		return decision;
	}
}