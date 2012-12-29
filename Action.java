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
	
	public void sendToShowdown()
	{
		//TODO: Showdown hookup.
		if(this instanceof Change)
		{
			Change c = (Change)this;
			c.deploy();
		}
		else if(this instanceof Attack)
		{
			
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
