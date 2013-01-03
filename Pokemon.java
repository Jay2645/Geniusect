
/*
 * A class representing a Pokemon.
 * Keeps track of type, name, resistances, etc.
 * Also fills itself out with data on an unknown Pokemon as the match goes on.
 * @author TeamForretress
 */

package geniusect;


import geniusect.abilities.Ability;
import geniusect.ai.GeniusectAI;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seleniumhelper.ShowdownHelper;

public class Pokemon {
	protected String name;
	protected String nickName;
	protected int id = -1;
	protected Item item;
	protected Team team;
	protected int level = 100;
	protected Type[] types = {Type.None, Type.None};
	protected Ability ability;
	protected Nature nature = Nature.Hardy;
	protected String tier;
	
	protected ArrayList<Type> immunities = new ArrayList<Type>();
	
	protected Move[] moveset = new Move[4];
	
	protected int[] base = {0,0,0,0,0,0};
	protected int[] ivs = {31,31,31,31,31,31};
	protected int[] evs = {0,0,0,0,0,0};
	protected int[] stats = new int[6]; //Stats before boosts.
	protected int[] boosts = {0,0,0,0,0,0};
	protected int[] boostedStats = new int[6]; // Stats after boosts.
	protected int fullHP; //How big of an HP stat we have at full HP.
	protected int hpPercent = 100; //Our current HP percent.
	protected int evsLeft = 510; //Legality check for our EV calculations.
	
	protected boolean lead = false; //Is this the lead?
	protected boolean alive = true; //Is this Pokemon alive?
	protected boolean active = false; //Is this Pokemon active?
	protected boolean canMove = true; //Can we move?
	protected boolean canSwitch = true; //Can we switch?
	protected boolean charged = false; //Is our move recharged?
	protected Move lockedInto = null; //Are we locked into a move (i.e. Outrage, Choice, etc.)?
	protected Status status = Status.None; //What permanent status do we have (i.e. Poison, Burn, etc.)?
	protected ArrayList<VolatileStatus> effects = new ArrayList<VolatileStatus>(); //What temporary status do we have (i.e. Confused, Taunt, etc.)?
	protected Pokemon enemy;
	protected Team enemyTeam;
	protected int damageDoneLastTurn;
	
	protected ShowdownHelper showdown;
	
	public Pokemon() {}
	
	public Pokemon(Pokemon p)
	{
		clone(p);
	}
	
	public Pokemon(String n, String nick, Team t)
	{
		name = n;
		team = t;
		enemyTeam = Team.getEnemyTeam(team.getTeamID());
		query();
	}
	
	public void query()
	{
		SQLHandler.queryPokemon(this);
		stats = Pokequations.calculateStat(this);
		boostedStats = stats;
		fullHP = boostedStats[Stat.HP.toInt()];
		if(team == null && enemy == null && showdown != null)
		{
			if(nickName == null || nickName.isEmpty())
				showdown.getTeam(name);
			else
				showdown.getTeam(nickName); //TODO: Make sure this isn't an identical match.
		}
		else if(team == null)
			team = enemy.enemyTeam;
		onSendOut();
	}
	
	public void onSendOut()
	{
		//Called when this Pokemon enters the battle.
		active = true;
		enemyTeam = Team.getEnemyTeam(team.getTeamID());
		if(enemyTeam != null)
			enemy = enemyTeam.getActive();
		if(enemy != null)
			enemy.changeEnemy(this);
		team.setActive(this);
		showdown = team.getShowdown();
		if(ability != null)
			ability.onSendOut();
		getMoves();
		wobbuffet(true);
		status.resetActive();
	}
	
	public void onWithdraw()
	{
		//Called when this Pokemon withdraws from the battle.
		active = false;
		team.setActive(null);
		resetBoosts();
		effects.clear();
		lockedInto = null;
		wobbuffet(false);
	}
	
	public Pokemon onDie()
	{
		//Called when the Pokemon dies.
		hpPercent = 0;
		if(ability != null)
			ability.onFaint();
		System.err.println(name+" has died!");
		alive = false;
		if(!active)
			onWithdraw();
		Change change = GeniusectAI.onPokemonDeath(this);
		if(change == null)
			return null;
		else return change.switchTo;
	}
	
	public void getMoves()
	{
		showdown = team.getShowdown();
		if(team.getTeamID() == 0 && showdown != null)
		{
			List<String> moves = showdown.getMoves();
			for(int i = 0; i < moves.size(); i++)
			{
				addMove(moves.get(i));
			}
		}
	}
	
	public int onNewAttack(Attack a)
	{
		//Called when predicting future events.
		//Whereas the method below takes the moves that THIS Pokemon did, this method takes a move the OTHER Pokemon did.
		//It returns the amount of damage done in this turn.
		int preHP = hpPercent;
		damage(a);
		for(int i = 0; i < effects.size(); i++)
		{
			effects.get(i).onNewTurn();
		}
		damage((int)Math.round(status.onNewTurn()));
		//System.err.println("PreHP: "+preHP+", HP percent: "+hpPercent);
		damageDoneLastTurn = preHP - hpPercent;
		System.out.println(name+" took "+damageDoneLastTurn+"% damage.");
		a.attacker.onNewTurn(a.name, damageDoneLastTurn, false);
		return damageDoneLastTurn;
	}
	
	public void onNewTurn(String n, int damageDone, boolean crit)
	{
		//Called when this Pokemon uses a move.
		//Keeps track of what moves THIS Pokemon has done (if unknown) and what damage they did to the enemy.
		if(enemy == null)
			enemy = enemyTeam.getActive(); //So we can properly simulate the right team.
		Move moveUsed = addMove(n);
		if(moveUsed != null)
			moveUsed.onMoveUsed(enemy, damageDone, crit);
		status.onNewTurn();
	}
	
	public int restoreHP(int restorePercent)
	{
		int restoreAmount = restorePercent + hpPercent;
		if(restoreAmount > 100)
		{
			int difference = restoreAmount - 100;
			restoreAmount -= difference;
		}
		hpPercent = restoreAmount;
		return restoreAmount;
	}
	
	public boolean damage(int damagePercent)
	{
		hpPercent -= damagePercent;
		if(hpPercent < 0)
			hpPercent = 0;
		return isAlive();
	}
	
	public boolean damage(Attack attack)
	{
		return damage(attack.move.getProjectedPercent(this).y);
	}
	
	public void removeStatus(VolatileStatus status)
	{
		effects.remove(status);
	}
	
	/*
	 * 
	 * Query methods below here.
	 * 
	 */
	
	public int checkHP()
	{
		if(hpPercent <= 0 && alive)
		{
			onDie();
		}
		return hpPercent;
	}
	
	public boolean isAlive()
	{
		checkHP();
		return alive;
	}
	
	public boolean teamExists()
	{
		return team == null;
	}
	
	public boolean canMove()
	{
		return canMove;
	}
	
	public void canMove(boolean can)
	{
		canMove = can;
	}
	
	public boolean isFasterThan(Pokemon compare)
	{
		return compare.boostedStats[Stat.Spe.toInt()] < boostedStats[Stat.Spe.toInt()];
	}
	
	public boolean isFasterThan(int speed)
	{
		//Useful for making self faster than "Magic Numbers" when building a team.
		return speed < boostedStats[Stat.Spe.toInt()];
	}
	
	public int[] boostedStats()
	{
		return boostedStats;
	}
	
	public int boostedStat(Stat stat)
	{
		return boostedStats[stat.toInt()];
	}
	
	public void changeEnemy(Pokemon e)
	{
		enemy = e;
	}
	
	public int getHealth()
	{
		return hpPercent;
	}
	
	/**
	 * Adds the move to the moveset. If the moveset is full, returns null. If the move is already in the moveset, returns that move.
	 * @param moveName (String): The name of the move we're trying to add.
	 * @return The move to add.
	 */
	public Move addMove(String moveName)
	{
		Move move = null;
		if(team == null)
			team = Team.lookupPokemon(this);
		else
			showdown = team.getShowdown();
		for(int i = 0; i < moveset.length; i++)
		{
			for(int n = 0; n < moveset.length; n++)
			{
				//First iterate through all moves to make sure we don't already have this move.
				if(moveset[n] != null && moveset[n].name.toLowerCase().startsWith(moveName.toLowerCase()))
				{
					move = moveset[n];
					return move;
				}
			}
			if(moveset[i] == null || moveset[i].name.toLowerCase().startsWith("struggle") && !moveName.toLowerCase().startsWith("struggle"))
			{
				System.err.println("Adding "+moveName+" to "+name+"'s move list.");
				moveset[i] = new Move(moveName,this);
				if(moveName.toLowerCase().startsWith("hidden power"))
				{
					moveset[i] = new HiddenPower(moveset[i]); 
					if(team != null && team.getTeamID() == 0 && showdown != null)
					{
						List<String> hpType = showdown.getMoves();//Will need to be changed later.
						for(int r = 0; r < hpType.size(); r++)
						{
							if(hpType.get(r).toLowerCase().startsWith("hidden power"))
							{
								System.err.println(hpType.get(r));
							}
						}
					}
				}
				move = moveset[i];
			}
		}
		return move;
	}
	
	/**
	 * Returns this Pokemon's item.
	 * @return This Pokemon's held item.
	 */
	public Item getItem()
	{
		return item;
	}
	
	/**
	 * Gets the type at the specified index.
	 * @param id - The index of the type. Can be 0 or 1.
	 * @return Type - the type at that index.
	 */
	public Type getType(int id)
	{
		return types[id];
	}
	
	/**
	 * Returns our nature.
	 * @return Nature - our Nature.
	 */
	public Nature getNature()
	{
		return nature;
	}
	
	/**
	 * Returns our ability.
	 * @return Ability - our Ability.
	 */
	public Ability getAbility()
	{
		return ability;
	}
	
	public Type[] getImmunities()
	{
		if(immunities.isEmpty())
			return new Type[0];
		Type[] immune = new Type[1];
		immune = (Type[]) immunities.toArray(immune);
		return immune;
	}
	
	public void addImmunity(Type immunity)
	{
		immunities.add(immunity);
	}
	
	public int getDamageDone()
	{
		return damageDoneLastTurn;
	}
	
	public boolean hasMove(String moveName)
	{
		for(int i = 0; i < moveset.length; i++)
		{
			if(moveset[i] == null)
				continue;
			if(moveset[i].name.toLowerCase().startsWith(moveName))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sets a move as the move we are forced to use.
	 * @param move (Move): The move we are forced to use.
	 */
	public void setLockedInto(Move move)
	{
		lockedInto = move;
	}
	
	/**
	 * Gets the move we are locked into, if any.
	 * @return The move we are locked into.
	 */
	public Move getLockedInto() {
		return lockedInto;
	}
	
	/**
	 * Returns the move in our moveset at index i. If we can't use that move, returns null.
	 * @param i - The index of this Pokemon, as an int.
	 * @return Move - the move in slot i.
	 */
	public Move getMove(int i)
	{
		if(moveset[i] == null || moveset[i].disabled || moveset[i].name.toLowerCase().startsWith("struggle"))
			return null;
		return moveset[i];
	}
	
	/**
	 * Sets this Pokemon's ShowdownHelper to the specified ShowdownHelper.
	 * @param helper (ShowdownHelper): The helper to use.
	 */
	public void setHelper(ShowdownHelper helper)
	{
		showdown = helper;
	}
	
	/**
	 * Sets the team to the specified team.
	 * @param t (Team): The Team to set our team to.
	 */
	public void setTeam(Team t)
	{
		team = t;
	}
	
	/**
	 * Returns all the moves in our moveset.
	 * @return Move[] - The moves in our moveset.
	 */
	public Move[] getMoveset()
	{
		return moveset;
	}
	
	/**
	 * Returns all our EVs.
	 * @return int[] - Our EVs.
	 */
	public int[] getEVs()
	{
		return evs;
	}
	
	/**
	 * Gets the enemy of this Pokemon.
	 * @return Pokemon - The enemy of this Pokemon.
	 */
	public Pokemon getEnemy()
	{
		return enemy;
	}
	
	/**
	 * Gets the Team of this Pokemon.
	 * @return Team - The team of this Pokemon.
	 */
	public Team getTeam()
	{
		return team;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean nameIs(String s)
	{
		return name.toLowerCase().startsWith(s.toLowerCase());
	}
	
	public void chargeMove()
	{
		inflictStatus(VolatileStatus.Charging);
		canMove = false;
		canSwitch = false;
	}
	
	public void setAbility(String a)
	{
		ability = new Ability(a, this);
	}
	
	public void setAbility(Ability a)
	{
		ability = a;
	}
	
	public int getFullHP()
	{
		return fullHP;
	}
	
	public double getSTAB()
	{
		if(ability == null)
			return 1.5;
		return ability.getSTAB();
	}
	
	public double getAbilityModifier()
	{
		if(ability == null)
			return 1;
		return ability.getModifier();
	}
	
	public Pokemon[] getPokemonTeam()
	{
		if(team == null)
			query();
		return team.getPokemon();
	}
	
	/**
	 * Takes a stat and returns the base stat for that stat.
	 * @param stat (Stat): The Stat to get the base stat for.
	 * @return (int): The base stat for that stat.
	 */
	public int getBaseStat(Stat stat) 
	{
		return base[stat.toInt()];
	}

	/**
	 * Returns this Pokemon's level.
	 * @return (int): This Pokemon's level.
	 */
	public int getLevel() 
	{
		return level;
	}

	/**
	 * Takes a stat and returns the EV-adjusted stat for that stat.
	 * @param stat (Stat): The stat to get the adjusted stat for.
	 * @return (int): The EV-adjusted stat.
	 */
	public int getStats(Stat stat) 
	{
		return stats[stat.toInt()];
	}

	/**
	 * Takes a stat and returns the IV for that stat.
	 * @param stat (Stat): The stat to get the IV for.
	 * @return (int): The IV for that stat.
	 */
	public int getIVs(Stat stat) 
	{
		return ivs[stat.toInt()];
	}
	
	/**
	 * Takes a stat and returns the EVs for that stat.
	 * @param stat (Stat): The stat to get the EVs for.
	 * @return (int): The EVs for that stat.
	 */
	public int getEVs(Stat stat) 
	{
		return evs[stat.toInt()];
	}
	
	/**
	 * Returns what types we are.
	 * @return (Type[]): Our types.
	 */
	public Type[] getTypes() 
	{
		return types;
	}
	
	/**
	 * Sets this Pokemon's base stat.
	 * @param index (int): The index of the stat to set (use Stat.toInt() if you are unsure of a Stat's index).
	 * @param baseStat (int): The base stat to set it to.
	 * @see geniusect.Stat#toInt()
	 */
	public void setBaseStat(int index, int baseStat) {
		base[index] = baseStat;
	}

	/**
	 * Sets this Pokemon's tier.
	 * @param string This Pokemon's tier.
	 */
	public void setTier(String string) {
		tier = string;
	}

	/**
	 * Gets this Pokemon's tier.
	 * @return (String): This Pokemon's tier.
	 */
	public String getTier() {
		return tier;
	}

	/**
	 * Sets this Pokemon's types.
	 * @param type1 (Type): This Pokemon's primary type.
	 * @param type2 (Type): This Pokemon's secondary type (can be Type.None).
	 */
	public void setType(Type type1, Type type2) {
		types[0] = type1;
		types[1] = type2;
	}
	
	/**
	 * Gets our ID (position in the team, between 0 and 5 inclusive) and returns it.
	 * @return (int): Our ID.
	 */
	public int getID() 
	{
		return id;
	}
	
	/*
	 * 
	 * Logic methods below here.
	 * 
	 */
	
	public void adjustEVs(int[] newEVs)
	{
		//Adjust our EV spread.
		evsLeft = 510;
		evs = new int[6];
		for(int i = 0; i < evs.length; i++)
		{
			adjustEVs(i,newEVs[i]);
		}
	}
	
	public void adjustEVsNoCheck(int[] newEVs)
	{
		evs = newEVs;
		for(int index = 0; index > evs.length; index++)
		{
			evsLeft -= evs[index];
			Pokequations.calculateStat(Stat.fromInt(index),this);
			if(Stat.fromInt(index) == Stat.HP)
			{
				fullHP = stats[Stat.HP.toInt()];
				percentToHP();
			}
		}
	}
	
	public void adjustEVs(int index, int newEV)
	{
		if(Stat.fromInt(index) == Stat.HP)
		{
			stats[Stat.HP.toInt()] = fullHP;
		}
		evsLeft += (evs[index] - newEV);
		int check = evsLeft - newEV;
		System.err.println(check);
		if(check < 0)
		{
			System.err.println("EV spread is invalid!");
			return;
		}
		evs[index] = newEV;
		Pokequations.calculateStat(Stat.fromInt(index),this);
		if(Stat.fromInt(index) == Stat.HP)
		{
			fullHP = stats[Stat.HP.toInt()];
			percentToHP();
		}
	}
	
	public void inflictStatus(Status s)
	{
		if(status == Status.None || s == Status.Rest || s == Status.None)
		{
			status = s;
		}
	}
	
	public void inflictStatus(VolatileStatus s)
	{
		effects.add(s);
		s.inflict(this);
	}
	
	public void giveBoosts(Stat boost, int count)
	{
		boosts[boost.toInt()] += count;
		if(boosts[boost.toInt()] > 6)
			boosts[boost.toInt()] = 6;
		else if(boosts[boost.toInt()] < -6)
			boosts[boost.toInt()] = -6;
		boostedStats[boost.toInt()] = Pokequations.statBoost(boosts[boost.toInt()],stats[boost.toInt()]);
		System.err.println(name+"'s new "+boost+" stat is "+boosts[boost.toInt()]);
	}
	
	public int hpToPercent()
	{
		hpPercent = (int)Math.round(stats[Stat.HP.toInt()] / fullHP);
		return hpPercent;
	}
	
	public int hpToPercent(int hp)
	{
		return (int)Math.round((hp / fullHP) * 100);
	}
	
	public int percentToHP()
	{
		stats[Stat.HP.toInt()] = (int)Math.round(fullHP * (hpPercent / 100));
		return stats[Stat.HP.toInt()];
	}
	
	public void resetBoosts()
	{
		for(int i = 0; i < 6; i++)
		{
			boosts[i] = 0;
		}
		boostedStats = stats;
	}
	
	public void wobbuffet(boolean entering)
	{
		if(team.getTeamID() == 0 && name.toLowerCase().startsWith("wobbuffet") || name.toLowerCase().startsWith("wynaut"))
		{
			if(entering)
				GeniusectAI.setGeneric();
			//else
				//GeniusectAI.setMiniMax();
		}
	}
	
	public Pokemon clone(Pokemon clone)
	{
		System.out.println("Cloning "+clone.name);
		name = clone.name;
		id = clone.id;
		item = clone.item;
		team = clone.team;
		level = clone.level;
		types = clone.types;
		ability = clone.ability;
		nature = clone.nature;
		tier = clone.tier;
		
		immunities = clone.immunities;
		
		moveset[0] = new Move(clone.moveset[0]);
		moveset[1] = new Move(clone.moveset[1]);
		moveset[2] = new Move(clone.moveset[2]);
		moveset[3] = new Move(clone.moveset[3]);
		
		base = clone.base;
		ivs = clone.ivs;
		evs = clone.evs;
		stats = clone.stats;
		boosts = clone.boosts;
		boostedStats = clone.boostedStats;
		fullHP = clone.fullHP;
		hpPercent = clone.hpPercent;
		evsLeft = clone.evsLeft;
		
		lead = clone.lead;
		alive = clone.alive;
		canMove = clone.canMove;
		status = clone.status;
		effects = clone.effects;
		enemy = clone.enemy;
		return this;
	}
	
	
	
	/*
	 * 
	 * Static methods below here.
	 * 
	 */
	
	public static Pokemon loadFromText(String importable, Team t, int count)
	{
		if(importable.isEmpty())
			return null;
		System.out.println("Loading importable: " + importable);
		Pokemon found = null;
		Boolean evsFound = false;
		
		
		Pattern p = Pattern.compile("(.+) @ (.+)\\s+Trait: (.+)\n.+\\s+(.+)Nature\\s+", Pattern.MULTILINE);
		Matcher m = p.matcher(importable);
		if(m.find()) // only need 1 find for this
		{
			found = new Pokemon();
			found.id = count;
			
			// name: Hard To Please (Ninetales) (F)
			String tempname = importable.substring(m.start(1), m.end(1));
			if (tempname.contains("("))
			{
				Pattern nameP = Pattern.compile(" \\((.+?)\\)");
				Matcher nameM = nameP.matcher(tempname);
				nameM.find();
				if (nameM.end(1) - nameM.start(1) > 1)
				{
					found.name = nameM.group(1);
				}
				else
				{
					found.name = tempname.substring(0, nameM.start(1) - 2);
				}
			}
			else
			{
				found.name = tempname;
			}
			
			found.item = new Item(importable.substring(m.start(2), m.end(2)));
			found.setAbility(importable.substring(m.start(3), m.end(3)));
			found.nature = Nature.fromString(importable.substring(m.start(4), m.end(4)));
							
			System.out.println("name: " + found.name);
			System.out.println("item: " + found.item.name);
			System.out.println("trait: " + found.ability.getName());
			System.out.println("nature: " + found.nature.toString());
		}
		String[] evP = {"(\\d+) HP","(\\d+) Atk","(\\d+) Def","(\\d+) SAtk","(\\d+) SDef","(\\d+) Spd"};
		int[] evDist = new int[6];
		for (int i = 0; i < 6; ++i)
		{
			evDist[i] = 0;
			Matcher m2 = Pattern.compile(evP[i], Pattern.MULTILINE).matcher(importable);
			if (m2.find())
			{
				evDist[i] = Integer.parseInt(importable.substring(m2.start(1), m2.end(1)));
				evsFound = true;
			}
		}
		System.out.println("Stats: ");
		System.out.printf("%d hp, %d atk, %d def, %d spa, %d spd, %d spe\n", evDist[0], evDist[1], evDist[2], evDist[3], evDist[4], evDist[5]);
					
		Pattern moveP = Pattern.compile("- (.+)\\n*", Pattern.MULTILINE);
		m = moveP.matcher(importable);
		String moves[] = new String[4];
		System.out.println("moves: ");
		int i = 0;
		while (m.find())
		{
			moves[i] = importable.substring(m.start(1), m.end(1));
			found.addMove(moves[i]);
			i++;
		}
		
		if(found == null){
			System.out.format("No Pokemon was found.%n");
			return null;
		}
		found.team = t;
		if(evsFound)
		{
			found.evs = evDist;
		}
		else
		{
			System.out.format("No EVs were found.%n");
		}
		found.query();
		System.err.println(found.getMove(0).name);
		return found;
	}
}
