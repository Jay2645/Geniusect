package geniusect;

public class Battle {
	
	
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

	private Team us = null;
	
	public int turnsToSimulate = 50; //How many turns we simulate, if Showdown is not running?
	public int turnCount = 1; //The current turn.
	public boolean playing = false; // TRUE if we have found a battle.
	public Action nextTurn;
	public Action lastTurnUs;
	public Action lastTurnEnemy;
	
	public boolean findBattle()
	{
		//us = new Team(importableUs, 0);
		//if(us.teamName.equals(""))
		//{
			us = new Team(0);
			return findBattle("Random Battle", "");
		//}
		//else return findBattle("OU", us.teamName);
	}
	
	public boolean findBattle(String type, String teamName)
	{
		if(GeniusectAI.showdown != null)
		{
			try
			{
				GeniusectAI.showdown.findBattle(type, teamName);
				GeniusectAI.showdown.waitForBattleStart();
			}
			catch(Exception e)
			{
				System.err.println("Geniusect battle search has failed! Exception data: "+e);
				return false;
			}
		}
		return true;
	}
	
	public void battleStart()
	{
		System.err.println(GeniusectAI.showdown);
		turnsToSimulate *= 2;
		GeniusectAI.battleCount++;
		playing = true;
		//Called when the battle begins.
		//Can load a team from an importable.
		GeniusectAI.displayIntro();
		if(us == null)
			us = new Team(0);
		Team enemy;
		if(GeniusectAI.showdown == null)
			enemy = new Team(importableEnemy, 1);
		else
			enemy = new Team(1);
		GeniusectAI.populateTeams(us,enemy);
		newTurn();
	}
	
	public void newTurn()
	{
		newTurn(0);
	}
	
	public void newTurn(int teamID)
	{
		if(!playing)
			return;
		int enemyID = -1;
		if(teamID == 0)
			enemyID = 1;
		else
			enemyID = 0;
		int turnNumber;
		if(GeniusectAI.showdown == null)
		{
			turnCount++;
			turnNumber = turnCount / 2;
		}
		else
		{
			turnCount = GeniusectAI.showdown.getCurrentTurn();
			turnNumber = turnCount;
		}
		
		System.err.println("\n\n\n*******************************TEAM "+teamID+", TURN "+(turnNumber)+"*******************************");
		System.err.println("**************************ACTIVE POKEMON: "+Pokemon.active[teamID].name+"**************************");
		if(GeniusectAI.showdown != null && turnCount % 5 == 0)
			GeniusectAI.lastTurnLogic();
		if(GeniusectAI.showdown == null)
			lastTurnEnemy = nextTurn;
		else
		{
			lastTurnUs = nextTurn;
			//TODO:	FETCH:
			//		- Enemy Pokemon
			//		- Our HP
			//		- Enemy HP
			//		- Enemy move used (and if any boosts were obtained)
			//		- The PP of the move we just used (and if any boosts were obtained)
			//		CHECK:
			//		- If we died (and change using generic logic if so)
			//		- Actual damage done (predicted versus actual)
			//		- If move was a crit.
			//		- Status inflicted
			//		- Entry hazards placed
		}
		nextTurn = GeniusectAI.simulate();
		if(nextTurn == null) //Should never happen, but I'm being pedantic.
			return;
		if(GeniusectAI.showdown == null)
		{
			if(turnCount % 2 == 1)
			{
				if(nextTurn instanceof Change) //Always change first.
				{
					Change c = (Change)nextTurn;
					c.sendToShowdown(this);
					if(lastTurnEnemy instanceof Attack)
					{
						Attack a = (Attack)lastTurnEnemy;
						a.defenderSwap(c.switchTo);
					}
				}
				if(lastTurnEnemy instanceof Change)
				{
					Change c = (Change)lastTurnEnemy;
					c.sendToShowdown(this);
					if(nextTurn instanceof Attack)
					{
						Attack a = (Attack)nextTurn;
						a.defenderSwap(c.switchTo);
					}
				}
				if(Pokemon.active[teamID].isFasterThan(Pokemon.active[enemyID])) //Check who is faster.
				{	//Things won't send if they've already been sent, so we don't need to check if we've already sent it.
					nextTurn.sendToShowdown(this);
					lastTurnEnemy.sendToShowdown(this);
				}
				else
				{
					lastTurnEnemy.sendToShowdown(this);
					nextTurn.sendToShowdown(this);
				}
			}
			GeniusectAI.swapSides();
			if(playing)
			{
				turnsToSimulate--;
				if(turnsToSimulate > 0)
					newTurn(enemyID);
			}
		}
		else
			nextTurn.sendToShowdown(this);
	}
	
	public void gameOver(boolean won)
	{
		GeniusectAI.gameOver(won);
	}
}
