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
	
	public void deploy()
	{
		//Send next Pokemon to Showdown.
		if(sent)
			return;
		//System.err.println("Sending to showdown on turn "+GeniusectAI.turnCount / 2);
		System.err.println(switchTo.team.userName+" (TeamID "+switchTo.team.teamID+"): Go, "+switchTo.name+"!");
		sent = true;
		if(!sayOnSend.equals(""))
		{
			GeniusectAI.print(sayOnSend);
			sayOnSend = "";
		}
		if(GeniusectAI.showdown == null || GeniusectAI.simulating)
		{
			switchTo.onSendOut();
		}
		else if(GeniusectAI.showdown != null)
		{
			try
			{
				GeniusectAI.showdown.doMove(switchTo.name);
			}
			catch (Exception e)
			{
				System.err.println("Could not switch to "+switchTo.name+"! Exception data:\n"+e);
				GeniusectAI.print("Exception! Could not switch to "+switchTo.name+"!");
				Action a = onException(this, e);
				if(a instanceof Attack)
					((Attack) a).deploy();
				else if(a instanceof Change)
					((Change) a).deploy();
			}
		}
		switchTo.onSendOut();
	}
	
	public static int calculateSwitchDamagePercent(Pokemon change)
	{
		//Returns the amount of entry hazard damage an incoming Pokemon will take.
		int damage = 0;
		//TODO: Calculate entry hazard damage.
		return damage;
	}
	
	public static Pokemon bestCounter(Pokemon[] ourTeam, Pokemon enemy)
	{
		//Returns the best response to a threat.
		int damage = Integer.MAX_VALUE;
		Pokemon switcher = null;
		for(int i = 0; i < ourTeam.length; i++)
		{
			if(ourTeam[i] == null)
				continue;
			if(ourTeam[i].isAlive())
			{
				if(switcher == null)
					switcher = ourTeam[i]; //Make sure we will always return something if at least one Pokemon is alive.
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
		if(changedRecently() || us.hasMove("Destiny Bond")) //Make sure this is a sane thing to do, and then try to take opponent with us if we can.
			return us;
		int damageStayIn = Pokequations.calculateDamagePercent(enemy, predictedMove, us).y;
		Pokemon change = us;
		int predictedDamage = damageStayIn;
		for(int i = 0; i < ourTeam.length; i++)
		{
			if(ourTeam[i] != null && ourTeam[i].isAlive() && ourTeam[i] != us)
			{
				if(ourTeam[i].fullHP == 0)
					ourTeam[i].query();
				int switchDamage = Pokequations.calculateDamagePercent(enemy, predictedMove, ourTeam[i]).y + calculateSwitchDamagePercent(ourTeam[i]);
				if(change == us) //If we have not found someone to change to.
				{
					if(us.hpPercent - predictedDamage <= 0) //If we predict that this next attack will kill us:
					{
						change = ourTeam[i];
						predictedDamage = switchDamage;
					}
					else if(switchDamage < predictedDamage && sanityCheck(ourTeam[i], enemy, switchDamage))
					{	//A switch will take less damage by switching in and it's sane to come in:
						change = ourTeam[i];
						predictedDamage = switchDamage;
					}
				}
				else
				{
					if(sanityCheck(ourTeam[i], enemy, switchDamage))
					{
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
	
	private static boolean changedRecently()
	{
		int sanity = changeCount;
		if(GeniusectAI.showdown == null)
			sanity /= 2;
		if(sanity >= 3)
			return true;
		else return false;
	}
	
	private static boolean sanityCheck(Pokemon saneSwitch, Pokemon enemy, int switchDamage)
	{
		//Sanity check: We don't want to come in and get killed the next turn, unless we can OHKO the enemy.
		Move secondTurnBestMove = Pokequations.bestMove(enemy, saneSwitch);
		Move secondTurnOurBestMove = Pokequations.bestMove(saneSwitch, enemy);
		if(	secondTurnBestMove.getProjectedPercent(saneSwitch).y + switchDamage - saneSwitch.hpPercent > 0 || 
			secondTurnOurBestMove.getProjectedPercent(enemy).x - enemy.hpPercent <= 0 && saneSwitch.isFasterThan(enemy))
			return true;
		return false;
	}
}
