/*
 * A generic action class which defines what we will do in a turn.
 * @author TeamForretress
 */

package geniusect;

public class Action 
{
	public String name = "";
	protected String sayOnSend = "";
	boolean crit = false;
	public int score = 0; //The minimax score for this action.
	
	public void sendToShowdown(AINode node)
	{
		if(this instanceof Change)
		{
			Change c = (Change)this;
			node.ourActive = c.switchTo;
			c.deploy();
		}
		else if(this instanceof Attack)
		{
			Attack a = (Attack) this;
			a.defender = node.ourActive;
			node.damageDoneToUs += a.deploy();
		}
	}
	
	public void sendToShowdown()
	{
		if(this instanceof Change)
		{
			Change c = (Change)this;
			c.deploy();
		}
		else if(this instanceof Attack)
		{
			Attack a = (Attack) this;
			a.defender.team.team[a.defender.id] = a.defender;
			a.deploy();
		}
		if(!sayOnSend.equals(""))
		{
			GeniusectAI.print(sayOnSend);
			sayOnSend = "";
		}
		if(GeniusectAI.showdown == null)
		{
			GeniusectAI.swapSides();
		}
	}
	
	public void say(String text)
	{
		//Says some text at the end of a turn.
	}
}
