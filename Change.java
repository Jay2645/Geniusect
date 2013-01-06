/*
 * A change ("switch") option, to be sent to Showdown or used in our virtual modeling.
 * @author TeamForretress
 */

package geniusect;

import com.seleniumhelper.ShowdownHelper;
import geniusect.ai.GeniusectAI;

public class Change extends Action {
	
	public Pokemon switchTo;
	
	public Change(Pokemon changeTo, Battle b)
	{
		switchTo = changeTo;
		battle = b;
		name = changeTo.getName();
	}
	
	public void deploy()
	{
		//Send next Pokemon to Showdown.
		if(sent)
			return;
		//System.err.println("Sending to showdown on turn "+GeniusectAI.turnCount / 2);
		System.err.println(switchTo.getTeam().getUsername()+" (TeamID "+switchTo.getTeam().getTeamID()+"): Go, "+switchTo.getName()+"!");
		sent = true;
		ShowdownHelper showdown = battle.getShowdown();
		if(!sayOnSend.equals(""))
		{
			GeniusectAI.print(sayOnSend);
			sayOnSend = "";
		}
		if(showdown == null || GeniusectAI.isSimulating())
		{
			switchTo.onSendOut();
		}
		else if(showdown != null)
		{
			try
			{
				showdown.switchTo(switchTo.getName(), false);
			}
			catch (Exception e)
			{
				System.err.println("Could not switch to "+switchTo.getName()+"! Exception data:\n"+e);
				GeniusectAI.print("Exception! Could not switch to "+switchTo.getName()+"!");
				Action a = onException(this, e, battle);
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
	
	public static Pokemon bestCounter(Pokemon[] ourTeam, Pokemon enemy, Pokemon removeFromCalc)
	{
		//Returns the best response to a threat.
		int damage = Integer.MAX_VALUE;
		Pokemon switcher = null;
		for(int i = 0; i < ourTeam.length; i++)
		{
			if(ourTeam[i] == null)
				continue;
			if(removeFromCalc != null)
			{
				if(ourTeam[i].getName().equals(removeFromCalc.getName()))
					continue;
			}
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
	
	public static Pokemon bestCounter(Pokemon[] ourTeam, Pokemon enemy)
	{
		return bestCounter(ourTeam,enemy,null);
	}
	
	public static Pokemon bestChange(Pokemon us, Pokemon[] ourTeam, Pokemon enemy, Move predictedMove, ShowdownHelper showdown)
	{
		if(changedRecently(showdown) || us.hasMove("Destiny Bond")) //Make sure this is a sane thing to do or try to take opponent with us if we can.
			return us;
		int damageStayIn = Pokequations.calculateDamagePercent(enemy, predictedMove, us).y;
		Pokemon change = us;
		int predictedDamage = damageStayIn;
		for(int i = 0; i < ourTeam.length; i++)
		{
			if(ourTeam[i] != null && ourTeam[i].isAlive() && ourTeam[i] != us)
			{
				if(ourTeam[i].getFullHP() == 0)
					ourTeam[i].query();
				int switchDamage = Pokequations.bestMove(enemy, ourTeam[i]).getProjectedPercent(ourTeam[i], true) + calculateSwitchDamagePercent(ourTeam[i]);
				if(ourTeam[i].getHealth() - switchDamage <= 0)
					continue;				
				if(change == us) //If we have not found someone to change to.
				{
					if(us.getHealth() - predictedDamage <= 0) 
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
	
	private static boolean changedRecently(ShowdownHelper showdown)
	{
		int sanity = changeCount;
		if(showdown == null)
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
		if(	secondTurnBestMove.getProjectedPercent(saneSwitch).y + switchDamage - saneSwitch.getHealth() > 0 || 
			secondTurnOurBestMove.getProjectedPercent(enemy).x - enemy.getHealth() <= 0 && saneSwitch.isFasterThan(enemy))
			return true;
		return false;
	}
}
