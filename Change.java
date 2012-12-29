/*
 * A change ("switch") option, to be sent to Showdown or used in our virtual modeling.
 * @author TeamForretress
 */

package geniusect;

public class Change extends Action {
	
	public Pokemon switchTo;
	
	public Change(Pokemon changeTo)
	{
		switchTo = changeTo;
		name = changeTo.name;
	}

	public static int calculateSwitchDamage(Pokemon switcher)
	{
		int percent = calculateSwitchDamagePercent(switcher.hpToPercent());
		return switcher.fullHP * (percent / 100);
	}
	
	public static int calculateSwitchDamagePercent(int percent)
	{
		//TODO: Calculate entry hazard damage.
		return percent;
	}
	
	public static Pokemon bestCounter(Pokemon[] ourTeam, Pokemon enemy)
	{
		int damage = Integer.MAX_VALUE;
		Pokemon switcher = null;
		for(int i = 0; i < ourTeam.length; i++)
		{
			if(ourTeam[i] != null && ourTeam[i].isAlive())
				{
				Move theirBestMove = Pokequations.bestMove(enemy,ourTeam[i]);
				if(damage > theirBestMove.getProjectedPercent(ourTeam[i]).y)
				{
					damage = theirBestMove.getProjectedPercent(ourTeam[i]).y;
					switcher = ourTeam[i];
				}
			}
		}
		return switcher;
	}
	
	public static Pokemon bestChange(Pokemon us, Pokemon[] ourTeam, Pokemon enemy, Move predictedMove)
	{
		int damageStayIn = Pokequations.calculateDamagePercent(enemy, predictedMove, us).y;
		Pokemon change = us;
		int predictedDamage = damageStayIn;
		for(int i = 0; i < ourTeam.length; i++)
		{
			if(ourTeam[i] != null && ourTeam[i].isAlive() && ourTeam[i] != us)
			{
				int switchDamage = Pokequations.calculateDamagePercent(enemy, predictedMove, ourTeam[i]).y + calculateSwitchDamagePercent(ourTeam[i].hpPercent);
				if(switchDamage < predictedDamage && change == us || switchDamage < predictedDamage && ourTeam[i].hpPercent - switchDamage > change.hpPercent - switchDamage)
				{
					//Make sure that we won't take more damage switching in than we would by not.
					Move secondTurnBestMove = Pokequations.bestMove(enemy, ourTeam[i]);
					Move secondTurnOurBestMove = Pokequations.bestMove(ourTeam[i], enemy);
					if(	secondTurnBestMove.getProjectedPercent(ourTeam[i]).y + switchDamage - ourTeam[i].hpPercent > 0 || 
						secondTurnOurBestMove.getProjectedPercent(enemy).x - enemy.hpPercent <= 0 && ourTeam[i].isFasterThan(enemy))
					{
						//Sanity check: We don't want to come in and get killed the next turn, unless we can OHKO the enemy.
						change = ourTeam[i];
						predictedDamage = switchDamage;
					}
				}
			}
			else
				continue;
		}
		return change;
	}
}
