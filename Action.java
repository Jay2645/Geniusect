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
	boolean sent = false;
	public int score = 0; //The minimax score for this action.
	
	public static int changeCount = 0; //How many times in a row have we changed? (Sanity check.)
	
	
	public void updateLastTurn()
	{
		if(GeniusectAI.showdown != null)
		{
			@SuppressWarnings("unused")
			String lastTurn = GeniusectAI.showdown.getLastTurnText();
			boolean switched = false;
			//TODO:	Regex to check if someone has switched.
			//		Get what they switched to
			//		Check if we've already seen it
			if(switched && this instanceof Attack)
			{
				Attack a = (Attack)this;
				a.defenderSwap(a.defender);
			}
		}
	}
	
	
	public void sendToShowdown(AINode node)
	{
		if(this instanceof Change)
		{
			Change c = (Change)this;
			changeCount++;
			node.ourActive = c.switchTo;
			c.deploy();
		}
		else if(this instanceof Attack)
		{
			changeCount = 0;
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
	}
	
	public void say(String text)
	{
		//Says some text at the end of a turn.
	}
}
