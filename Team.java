/*
 * A class representing a team.
 * @author TeamForretress
 */

package geniusect;

import java.util.List;

import com.seleniumhelper.ShowdownHelper;

public class Team {
	public Team(Team clone)
	{
		clone(clone);
	}
	
	public Team(int id, Battle b)
	{
		teamID = id;
		if(id == 0)
			enemyID = 1;
		else
			enemyID = 0;
		battle = b;
	}
	
	public void importImportable(String input)
	{
		String imports = input;
		if(input.startsWith("Team Name: "))
		{
			int start = input.indexOf("Team Name: ") + 11;
			int end = input.indexOf("\n");
			String t = input.substring(start, end);
			teamName = t;
			//System.err.println(teamName);
			imports = input.substring(end + 1);
		}
		String[] importable = imports.split("\n\n", 6);
		for(int i = 0; i < importable.length; i++)
		{
			Pokemon spread = Pokemon.loadFromText(importable[i], this,i);
			//TODO: Lookup GA data using specified Spread.
			addPokemon(spread);
		}
		System.err.println(getActive().getMove(0));
	}
	
	public static Team getEnemyTeam(int id)
	{	//Gets the enemy of team "id".
		if(id == 0)
			return battle.getTeam(1);
		else return battle.getTeam(0);
	}
	
	private Pokemon[] team = new Pokemon[6];
	private Pokemon active = null;
	private int teamID = -1;
	private int enemyID = -1;
	private String teamName;
	private String userName = "";
	private boolean hasInitialized = false;
	private double teamDefModifier = 1;
	private double teamSpDModifier = 1;
	private static Battle battle;
	
	private ShowdownHelper showdown;
	
	public Pokemon addPokemon(String s, ShowdownHelper helper)
	{
		Pokemon p = null;
		showdown = helper;
		for(int i = 0; i < team.length; i++)
		{
			if(team[i] == null)
			{
				//System.err.println("Adding "+p.name+" to team ID "+teamID);
				p = new Pokemon(s, "", this); //TODO: Nicknames.
				team[i] = p;
				break;
			}
			else if(team[i].nameIs(s))
			{
				team[i].setTeam(this);
				p = team[i];
				break;
			}
		}
		hasInitialized = true;
		return p;
	}
	public Pokemon addPokemon(Pokemon p)
	{
		//Adds Pokemon to a team. If a Pokemon has already been added, returns that Pokemon.
		return addPokemon(p.getName(), showdown);
	}
	
	
	public void updateEnemyTeam()
	{
		 battle.updateTeamActive(enemyID,active);
	}
	
	public void updateEnemy(Pokemon p)
	{
		active.changeEnemy(p);
	}
	
	public Pokemon changePokemon(Pokemon p)
	{
		active.onWithdraw();
		active = p;
		active.onSendOut();
		updateEnemyTeam();
		return active;
	}
	
	public void setUserName(String s)
	{
		userName = s;
	}
	
	public Pokemon getPokemon(String name)
	{
		Pokemon p = null;
		for(int i = 0; i < team.length; i++)
		{
			if(team[i] == null)
				continue;
			if(team[i].nameIs(name))
			{
				p = team[i];
				break;
			}
		}
		return p;
	}
	
	/**
	 * Gets the Pokemon at index i, where i is an int between 0 and 5 (inclusive).
	 * @param i (int): The index to fetch the Pokemon from.
	 * @return Pokemon: The Pokemon at the index. NULL if index is out of bounds or Pokemon is not found.
	 */
	public Pokemon getPokemon(int i)
	{
		if(i > -1 && i < 7)
			return team[i];
		else return null;
	}
	
	/**
	 * Returns this team's username.
	 * @return This team's username.
	 */
	public String getUsername()
	{
		return userName;
	}
	
	/**
	 * Sets the battle to the specified battle.
	 * @param b (Battle): The battle to set our battle to.
	 */
	public void setBattle(Battle b)
	{
		battle = b;
	}
	
	/**
	 * Gets our current battle.
	 * @return (Battle): Our current battle.
	 */
	public Battle getBattle()
	{
		return battle;
	}
	
	/**
	 * Gets our Pokemon team.
	 * @return Pokemon[] - A CLONE of our Pokemon team.
	 */
	public Pokemon[] getPokemon()
	{
		return team.clone();
	}
	
	public int getPokemonID(Pokemon p)
	{
		for(int i = 0; i < team.length; i++)
		{
			if(team[i] == null)
				break;
			if(team[i] == p)
				return i;
		}
		return -1;
	}
	
	public ShowdownHelper getShowdown()
	{
		return showdown;
	}
	
	public int getTeamID()
	{
		return teamID;
	}
	
	public Pokemon getActive()
	{
		return active;
	}
	
	/**
	 * Sets the Pokemon as active.
	 * @param p (Pokemon): The Pokemon to mark as active.
	 */
	public void setActive(Pokemon p)
	{
		if(p == null)
			active = null;
		else if(active != null)
			active.onWithdraw();
		active = p;
	}
	/**
	 * Sets the Pokemon slot at the given ID to the given Pokemon.
	 * @param id (int): The Pokemon slot index (can be 0-5, inclusive)
	 * @param pokemon (Pokemon): The Pokemon to set the teamslot to.
	 */
	public void setPokemon(int id, Pokemon pokemon) 
	{
		if(id > -1 && id < 7)
			return;
		team[id] = pokemon;
	}
	/**
	 * Looks up the team of the specified Pokemon.
	 * @param pokemon (Pokemon): The Pokemon to look up.
	 * @return (Team): That Pokemon's team.
	 */
	public static Team lookupPokemon(Pokemon pokemon) 
	{
		Team team = null;
		for(int i = 0; i < 2; i++)
		{
			if(battle.getTeam(i).hasPokemon(pokemon))
			{
				System.out.println("Looking up team.");
				team = battle.getTeam(i);
				break;
			}
		}
		return team;
	}
	/**
	 * Looks up if this team has this Pokemon.
	 * @param pokemon (Pokemon): The Pokemon to look up.
	 * @return TRUE if it does have this Pokemon, else FALSE.
	 */
	private boolean hasPokemon(Pokemon pokemon) 
	{
		for(int i = 0; i < team.length; i++)
		{
			if(team[i] == null)
				continue;
			System.err.println(team[i].getName());
			if(team[i] == pokemon)
				return true;
			if(team[i].getName().equals(pokemon.getName())) //Sometimes for whatever reason it says we have a Pokemon when we don't.
			{
				boolean hasMoves = false;
				Move[] teamMoveset = team[i].getMoveset();
				Move[] pokeMoveset = pokemon.getMoveset();
				//Make sure this isn't a false positive.
				if((teamMoveset[0] == null && pokeMoveset[0] != null || teamMoveset[0] != null && pokeMoveset[0] == null) ||
					(teamMoveset[1] == null && pokeMoveset[1] != null || teamMoveset[1] != null && pokeMoveset[1] == null) ||
					(teamMoveset[2] == null && pokeMoveset[2] != null || teamMoveset[2] != null && pokeMoveset[2] == null) ||
					(teamMoveset[3] == null && pokeMoveset[3] != null || teamMoveset[3] != null && pokeMoveset[3] == null))
					hasMoves = false;
				else if((teamMoveset[0] == null && pokeMoveset[0] == null || teamMoveset[0].name.equals(pokeMoveset[0].name)) && 
						(teamMoveset[1] == null && pokeMoveset[1] == null || teamMoveset[1].name.equals(pokeMoveset[1].name)) &&
						(teamMoveset[2] == null && pokeMoveset[2] == null || teamMoveset[1].name.equals(pokeMoveset[2].name)) &&
						(teamMoveset[3] == null && pokeMoveset[3] == null || teamMoveset[1].name.equals(pokeMoveset[3].name)))
					hasMoves = true;
				return hasMoves;
			}
		}
		return false;
	}
	
	private void clone(Team clone)
	{
		team = clone.team.clone();
		active = team[clone.active.getID()];
		teamID = clone.teamID;
		enemyID = clone.enemyID;
		teamName = clone.teamName;
		userName = clone.userName;
		hasInitialized = clone.hasInitialized;
		teamDefModifier = clone.teamDefModifier;
		teamSpDModifier = clone.teamSpDModifier;
		
		showdown = clone.showdown;
		
	}
}
