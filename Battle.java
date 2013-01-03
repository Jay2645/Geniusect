
package geniusect;

import geniusect.ai.GeniusectAI;

import java.util.List;

import seleniumhelper.ShowdownHelper;

/**
 * A class representing a battle.
 * Battles consist of two teams of six Pokemon each. 
 * @author TeamForretress
 * @see geniusect.pokemon
 * @see geniusect.team
 */
public class Battle {
	public Battle(){}
	
	public Battle(Battle b)
	{
		b.showdown = null;
		clone(b);
	}
	
	/**
	 * A class representing a battle.
	 * Battles consist of two teams of six Pokemon each. 
	 * @param helper - The ShowdownHelper that this class sends commands to.
	 * @author TeamForretress
	 * @see geniusect.pokemon
	 * @see geniusect.team
	 */
	public Battle(ShowdownHelper helper)
	{
		showdown = helper;
	}
	/**
	 * The team the USER is going to use in the battle.
	 * The first line is the name of the team (so the ShowdownHelper can find which team to select).
	 * The rest of the line is the export data from Pokemon Showdown's teambuilder.
	 * @see #showdown
	 */
	private String importableUs =	"Team Name: The Jungle" +
									"\nNinetales @ Leftovers" +
									"\nTrait: Drought" +
									"\nEVs: 252 HP / 252 SAtk / 4 SDef" +
									"\nModest Nature" +
									"\n- Sunny Day" +
									"\n- SolarBeam" +
									"\n- Overheat" +
									"\n- Power Swap" +
									"\n" +
									"\nTangrowth @ Leftovers" +
									"\nTrait: Chlorophyll" +
									"\nEVs: 252 HP / 252 Spd / 4 Atk" +
									"\nNaive Nature" +
									"\n- Growth" +
									"\n- Power Whip" +
									"\n- Hidden Power" +
									"\n- Earthquake" +
									"\n" +
									"\nDugtrio @ Focus Sash" +
									"\nTrait: Arena Trap" +
									"\nEVs: 252 Spd / 4 Def / 252 Atk" +
									"\nJolly Nature" +
									"\n- Earthquake" +
									"\n- Sucker Punch" +
									"\n- Stone Edge" +
									"\n- Reversal" +
									"\n" +
									"\nHeatran @ Choice Scarf" +
									"\nTrait: Flash Fire" +
									"\nEVs: 252 Spd / 252 SAtk / 4 HP" +
									"\nModest Nature" +
									"\n- Overheat" +
									"\n- SolarBeam" +
									"\n- Earth Power" +
									"\n- Hidden Power" +
									"\n" +
									"\nDragonite @ Lum Berry" +
									"\nTrait: Multiscale" +
									"\nEVs: 252 Spd / 4 HP / 252 Atk" +
									"\nAdamant Nature" +
									"\n- Dragon Dance" +
									"\n- Fire Punch" +
									"\n- ExtremeSpeed" +
									"\n- Outrage" +
									"\n" +
									"\nDonphan @ Leftovers" +
									"\nTrait: Sturdy" +
									"\nEVs: 252 SDef / 28 HP / 228 Def" +
									"\nCareful Nature" +
									"\n- Rapid Spin" +
									"\n- Toxic" +
									"\n- Stealth Rock" +
									"\n- Earthquake";
	
			
	private String importableEnemy =	"Forretress @ Leftovers " +
										"\nTrait: Sturdy" +
										"\nEVs: 252 HP / 4 Atk / 252 Def" +
										"\nRelaxed Nature" +
										"\n- Toxic Spikes" +
										"\n- Gyro Ball" +
										"\n- Stealth Rock" +
										"\n- Rapid Spin" +
										"\n" +
										"\nGarchomp @ Choice Scarf" +
										"\nTrait: Rough Skin" +
										"\nEVs: 4 HP / 252 Atk / 252 Spd" +
										"\nAdamant Nature" +
										"\n- Outrage" +
										"\n- Earthquake" +
										"\n- Stone Edge" +
										"\n- Brick Break" +
										"\n" +
										"\nChansey @ Eviolite" +
										"\nTrait: Natural Cure" +
										"\nEVs: 248 HP / 252 Def / 8 Spd" +
										"\nBold Nature" +
										"\n- Toxic" +
										"\n- Protect" +
										"\n- Wish" +
										"\n- Seismic Toss" +
										"\n" +
										"\nLatios @ Choice Specs" +
										"\nTrait: Levitate" +
										"\nEVs: 6 HP / 252 SAtk / 252 Spd" +
										"\nTimid Nature" +
										"\n- Surf" +
										"\n- Psyshock" +
										"\n- Draco Meteor" +
										"\n- Hidden Power" +
										"\n" +
										"\nConkeldurr @ Leftovers" +
										"\nTrait: Guts" +
										"\nEVs: 120 HP / 252 Atk / 136 SDef" +
										"\nAdamant Nature" +
										"\n- Bulk Up" +
										"\n- Drain Punch" +
										"\n- Payback" +
										"\n- Mach Punch" +
										"\n" +
										"\nHeatran @ Choice Scarf" +
										"\nTrait: Flash Fire" +
										"\nEVs: 6 HP / 252 SAtk / 252 Spd" +
										"\nModest Nature" +
										"\n- Overheat" +
										"\n- Earth Power" +
										"\n- Hidden Power" +
										"\n- Dragon Pulse";

	private Team players[] = new Team[2];
	private int turnsToSimulate = 100; //How many turns we simulate, if Showdown is not running?
	private int turnCount = 1; //The current turn.
	private boolean playing = false; // TRUE if we have found a battle.
	private boolean firstTurn = true; // TRUE if it is the first turn.
	private Action nextTurn;
	private Action lastTurnUs;
	private Action lastTurnEnemy;
	private ShowdownHelper showdown = null;
	private Weather weather = Weather.None;
	
	public static String criticalErrors = "Errors:\n";
	
	public boolean findBattle()
	{
		//us = new Team(importableUs, 0);
		//if(us.teamName.equals(""))
		//{
			players[0] = new Team(0,this);
			return findBattle("Random Battle", "");
		//}
		//else return findBattle("OU", us.teamName);
	}
	
	public boolean findBattle(String type, String teamName)
	{
		if(showdown != null)
		{
			try
			{
				showdown.findBattle(type, teamName);
				showdown.waitForBattleStart();
			}
			catch(Exception e)
			{
				System.err.println("Geniusect battle search has failed! Exception data: "+e);
				try
				{
					showdown.leaveBattle();
				}
				catch (Exception l)
				{
					System.err.println("Could not leave battle. Exception data: "+e);
				}
				return false;
			}
		}
		return true;
	}
	
	public void battleStart()
	{
		//Called when the battle begins.
		turnsToSimulate *= 2;
		playing = true;
		if(showdown == null)
		{
			players[0] = new Team(0,this);
			players[1] = new Team(1,this);
			players[0].importImportable(importableUs);
			players[1].importImportable(importableEnemy);
		}
		else
		{
			if(players[0] == null)
				players[0] = new Team(0,this);
			players[1] = new Team(1,this);
		}
		populateTeams();
		newTurn();
	}
	
	public void populateTeams()
	{
		if(showdown!= null)
		{
			//Populate each team.
			for(int i = 0; i < 2; i++)
			{
				String userName = showdown.getUserName();
				players[i].setUserName(userName);
				List<String> ourPokes= showdown.getTeam(userName);
				//TODO: Get moves for each Pokemon, if known.
				for(int n = 0; n < ourPokes.size(); n++)
					players[i].addPokemon(ourPokes.get(n),showdown);
			}
		}
		//TODO: If we can choose lead, do so.
		players[0].getActive().changeEnemy(players[1].getActive());
		players[1].getActive().changeEnemy(players[0].getActive());
	}
	
	public void newTurn()
	{
		newTurn(players[0]);
	}
	
	public void newTurn(Team t)
	{
		if(!playing)
			return;
		int turnNumber;
		if(showdown == null || GeniusectAI.isSimulating())
		{
			turnCount++;
			turnNumber = turnCount / 2;
		}
		else
		{
			turnCount = showdown.getCurrentTurn();
			turnNumber = turnCount;
		}
		
		System.err.println("\n\n\n*******************************TEAM "+t.getTeamID()+", TURN "+(turnNumber)+"*******************************");
		System.err.println("**************************ACTIVE POKEMON: "+t.getActive().getName()+"**************************");
		System.err.println(criticalErrors);
		//if(GeniusectAI.showdown != null && turnCount % 5 == 0)
			//GeniusectAI.lastTurnLogic();
		if(showdown == null || GeniusectAI.isSimulating())
			lastTurnEnemy = nextTurn;
		else
		{
			if(nextTurn != null)
				nextTurn.updateLastTurn(this); //TODO: Remake lastTurnEnemy from the data here.
			lastTurnUs = nextTurn;
			//		- Enemy move used (and if any boosts were obtained)
			//		- The PP of the move we just used (and if any boosts were obtained)
			//		CHECK:
			//		- If we died (and change using generic logic if so)
			//		- Status inflicted
			//		- Entry hazards placed
		}
		if(GeniusectAI.isSimulating() || !playing)
			return;
		nextTurn = GeniusectAI.simulate();
		if(nextTurn == null) //Should never happen, but I'm being pedantic.
			return;
		if(showdown == null)
		{
			GeniusectAI.simulateTurn(this);
			if(playing)
			{
				turnsToSimulate--;
				if(turnsToSimulate > 0)
					newTurn(Team.getEnemyTeam(t.getTeamID()));
			}
		}
		else
			nextTurn.sendToShowdown(this);
	}
	
	public void gameOver(boolean won)
	{
		System.err.println("Game over.");
		System.err.println(Battle.criticalErrors);
		turnsToSimulate = 0;
		playing = false;
		GeniusectAI.gameOver(won);
	}
	
	public Weather getWeather()
	{
		return weather;
	}
	
	public void setWeather(Weather weatherType)
	{
		weather = weatherType;
	}
	
	/**
	 * Returns the team at index ID (0 == user, 1 == enemy).
	 * @param id - int: The TeamID.
	 * @return Team - The Team at index id.
	 */
	public Team getTeam(int id)
	{
		return players[id];
	}
	
	/**
	 * Returns the current turn count.
	 * @return int - the current turn count.
	 */
	public int getTurnCount()
	{
		return turnCount;
	}
	
	/**
	 * Returns our Showdown hookup.
	 * @return ShowdownHelper - Our Showdown hookup.
	 */
	public ShowdownHelper getShowdown()
	{
		return showdown;
	}
	
	/**
	 * Finds out if it is the first turn.
	 * @return TRUE if it is the first turn, else FALSE.
	 */
	public boolean isFirstTurn()
	{
		return firstTurn;
	}
	
	/**
	 * Sets if this is the battle's first turn.
	 * @param first (boolean) - TRUE if this is the first turn, else FALSE.
	 */
	public void isFirstTurn(boolean first)
	{
		firstTurn = first;
	}
	
	public void isPlaying(boolean play)
	{
		playing = play;
	}
	
	/**
	 * Returns our planned action next turn.
	 * @return Action - the action we've decided to take.
	 */
	public Action getNextTurn()
	{
		return nextTurn;
	}
	
	/**
	 * Sets what we will do next turn.
	 * @param doNext (Action): The Action we are going to take.
	 */
	public void setNextTurn(Action doNext)
	{
		nextTurn = doNext;
	}
	
	/**@return TRUE if the battle is active, else FALSE.*/
	public boolean isPlaying()
	{
		return playing;
	}
	
	public void clone(Battle clone)
	{
		showdown = clone.showdown;
		importableUs = clone.importableUs;
		importableEnemy = clone.importableEnemy;
		firstTurn = clone.firstTurn;
		lastTurnEnemy = clone.lastTurnEnemy;
		lastTurnUs = clone.lastTurnUs;
		nextTurn = clone.nextTurn;
		players = clone.players;
		playing = clone.playing;
		turnCount = clone.turnCount;
		turnsToSimulate = clone.turnsToSimulate;
		weather = clone.weather;
	}

	/**
	 * Returns the enemy's last turn.
	 * @return Action - What the enemy team did last.
	 */
	public Action getLastTurnEnemy() 
	{
		return lastTurnEnemy;
	}

	
	/**
	 * Updates the active Pokemon on the specified team.
	 * @param teamID (int): The ID of the Team to update.
	 * @param active (Pokemon): The Pokemon to mark as active.
	 */
	public void updateTeamActive(int teamID, Pokemon active)
	{
		updateTeamActive(players[teamID], active);
	}
	/**
	 * Updates the active Pokemon on the specified team.
	 * @param team (Team): The team to update.
	 * @param active (Pokemon): The Pokemon to mark as active.
	 */
	private void updateTeamActive(Team team, Pokemon active) 
	{
		team.updateEnemy(active);
	}

}
