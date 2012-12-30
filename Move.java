/*
 * A class representing a move.
 * Keeps track of potential damage to known enemy Pokemon.
 * @author TeamForretress
 */

package geniusect;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class Move {
	public String name;
	public Pokemon user;
	public int pp;
	public int power;
	public int accuracy;
	public Type type;
	public Target target;
	public Status condition = null; //A status condition to inflict upon a target.
	public Map<VolatileStatus, Target> vol = new HashMap<VolatileStatus, Target>(); //The volatile status condition caused and its target.
	public Map<Stat, String> boosts = new HashMap<Stat, String>(); //What stats are boosted. <Stat, int> throws error for some reason?
	public boolean special;
	public boolean status;
	public int boostChance = 0;
	public int recoilPercent = 0;
	
	public boolean disabled = false;
	
	protected Map<Pokemon, Point> projectedDamage = new HashMap<Pokemon,Point>(6);
	protected Map<Pokemon, Point> projectedPercent = new HashMap<Pokemon, Point>(6);
	
	public Move(){}
	
	public Move(Move clone)
	{
		name = clone.name;
		user = clone.user;
		pp = clone.pp;
		power = clone.power;
		accuracy = clone.accuracy;
		type = clone.type;
		target = clone.target;
		boosts = clone.boosts;
		special = clone.special;
		status = clone.status;
		boostChance = clone.boostChance;
		recoilPercent = clone.recoilPercent;
		disabled = clone.disabled;
		projectedDamage = clone.projectedDamage;
		projectedPercent = clone.projectedPercent;
	}
	
	public Move(String n, Pokemon p)
	{
		int i = n.indexOf("\n");
		if(i == -1)
			name = n;
		else
			name = n.substring(0, i);
		user = p;
		if(name.toLowerCase().startsWith("Struggle"))
		{
			pp = Integer.MAX_VALUE;
			power = 50;
			accuracy = 100;
			type = Type.None;
			recoilPercent = 25;
		}
		else
			SQLHandler.queryMove(this);
	}
	
	public void onMoveUsed(Pokemon enemy, int damageDone, boolean wasCrit)
	{
		//Called when this move is used.
		if(enemy.ability == null)
		{
			pp--;
		}
		else
		{
			if(enemy.ability.name.toLowerCase().startsWith("pressure"))
				pp -=2;
			else
				pp--;
		}
		if(0 >= pp)
			disabled = true;
		if(!projectedPercent.containsKey(enemy));
		{
			adjustProjectedPercent(Pokequations.calculateDamagePercent(user,this,enemy),enemy);
		}
		if(!withinExpectedRange(damageDone,enemy,wasCrit))
		{
			//TODO: Adjust enemy EVs to make damage fall within expected range.
		}
	}
	
	public boolean withinExpectedRange(int damage, Pokemon p, boolean wasCrit)
	{
		if(wasCrit && user.ability.name.toLowerCase().startsWith("sniper"))
		{
			damage /= 3;
		}
		else if(wasCrit)
		{
			damage /= 2;
		}
		if(damage < projectedPercent.get(p).x || damage > projectedPercent.get(p).y)
			return false;
		else return true;
	}
	
	public void adjustProjectedPercent(Point newProjection, Pokemon p)
	{
		//Adjust our projected damage against a Pokemon.
		Point damage = new Point(Integer.MAX_VALUE, Integer.MIN_VALUE);
		if(projectedPercent.containsKey(p))
		{
			damage = projectedPercent.get(p);
		}
		damage.x = Math.min(newProjection.x, damage.x);
		damage.y = Math.max(damage.x, newProjection.y);
		projectedPercent.put(p, damage);
	}
	
	public int useMove(boolean bestCase,Pokemon us, Pokemon enemy)
	{
		int damage = 0;
		if(bestCase)
		{
			damage = Pokequations.calculateDamagePercent(us, this, enemy).y;
			for(int i = 0; i < 6; i++)
			{
				boostStats(Stat.fromInt(i));
			}
		}
		else
		{
			damage = Pokequations.calculateDamagePercent(us, this, enemy).x;
			if(boostChance == 1)
			{
				for(int i = 0; i < 6; i++)
				{
					boostStats(Stat.fromInt(i));
				}
			}
		}
		return damage;
	}
	
	public Point getProjectedPercent(Pokemon enemy)
	{
		
		if(projectedPercent.containsKey(enemy))
		{
			return projectedPercent.get(enemy);
		}
		else
		{
			Point damage = Pokequations.calculateDamagePercent(user, this, enemy);
			projectedPercent.put(enemy, damage);
			return damage;
		}
	}
	
	public int getProjectedPercent(Pokemon enemy, boolean most)
	{
		if(most)
			return getProjectedPercent(enemy).y;
		else return getProjectedPercent(enemy).x;
	}
	
	public Point getProjectedDamage(Pokemon enemy)
	{
		
		if(projectedDamage.containsKey(enemy))
		{
			return projectedDamage.get(enemy);
		}
		else
		{
			Point damage = Pokequations.calculateDamage(user, this, enemy);
			projectedDamage.put(enemy, damage);
			return damage;
		}
	}
	
	public int getProjectedDamage(Pokemon enemy, boolean most)
	{
		if(most)
			return getProjectedDamage(enemy).y;
		else return getProjectedDamage(enemy).x;
	}
	
	public void boostStats(Stat boost)
	{
		if(boosts.get(boost).equals("0"))
			return;
		user.giveBoosts(boost, Integer.parseInt(boosts.get(boost)));
	}
}
