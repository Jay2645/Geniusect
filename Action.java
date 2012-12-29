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
		if(!sayOnSend.equals(""))
		{
			GeniusectAI.print(sayOnSend);
			sayOnSend = "";
		}
		GeniusectAI.lastTurnLogic();
	}
	
	public void say(String text)
	{
		//Says some text at the end of a turn.
	}
}
