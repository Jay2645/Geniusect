/*
 * A class representing a team.
 * @author TeamForretress
 */

package geniusect;

public class Team {
	public Team(){}
	public Team(String importable)
	{
		//TODO: Mark end of one importable and beginning of another.
		//int importableLength = 6;
		//for(int i = 0; i < importableLength; i++)
		//{
			@SuppressWarnings("unused")
			Spread spread = new Spread(addPokemon(Pokemon.loadFromText(importable, this)), false);
			//TODO: Lookup GA data using specified Spread.
		//}
	}
	
	public Pokemon[] team = new Pokemon[6];
	public Pokemon active;
	
	public Pokemon addPokemon(String s)
	{
		return addPokemon(new Pokemon(s, this));
	}
	public Pokemon addPokemon(Pokemon p)
	{
		//Adds Pokemon without marking them as active.
		//(Used if you know in advance what Pokemon a team has.)
		for(int i = 0; i < team.length; i++)
		{
			if(team[i] == null)
			{
				team[i] = p;
				break;
			}
		}
		return p;
	}
	
	public void activePokemon(String activate)
	{
		//Adds Pokemon and marks them as active (on the battlefield).
		activePokemon(new Pokemon(activate,this));
	}
	
	public void activePokemon(Pokemon activate)
	{
		active = activate;
		for(int i = 0; i < team.length; i++)
		{
			if(activate == team[i])
			{
				return;
			}
			else if(team[i] == null)
			{
				team[i] = activate;
				break;
			}
		}
	}
}
