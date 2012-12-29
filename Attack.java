/*
 * An attack option, to be sent to Showdown or used in our virtual modeling.
 * @author TeamForretress
 */

package geniusect;

import java.awt.Point;

public class Attack extends Action {
	public Move move;
	
	private Pokemon attacker;
	private Pokemon defender;
	
	public Attack(Move m, Pokemon attack, Pokemon defend)
	{
		name = m.name;
		attacker = attack;
		defender = defend;
		if(m.disabled || m.name.equalsIgnoreCase("struggle"))
		{
			//Double-check to make sure this is a legal move.
			Move[] newSet = new Move[4];
			boolean foundEnabledMove = false;
			for(int i = 0; i < attack.moveset.length; i++)
			{
				if(attack.moveset[i] == null || attack.moveset[i].disabled)
					continue;
				else
				{
					foundEnabledMove = true;
					newSet[i] = attack.moveset[i];
				}
			}
			if(foundEnabledMove)
				move = Pokequations.bestMove(attacker, defender, newSet);
			else if(m.name.equalsIgnoreCase("struggle"))
				move = m;
			else
				move = new Move("struggle", attack);
		}
		else
			move = m;
	}
	
	public void defenderSwap(Pokemon newDefend)
	{
		defender = newDefend;
	}
	
	public void resultPercent(int damageDone)
	{
		if(damageDone > move.getProjectedPercent(defender).y)
		{
			move.adjustProjectedPercent(new Point(move.getProjectedPercent(defender).x, damageDone), defender);
		}
		else if(damageDone < move.getProjectedPercent(defender).x)
		{
			move.adjustProjectedPercent(new Point(damageDone, move.getProjectedPercent(defender).y), defender);
		}
	}
}
