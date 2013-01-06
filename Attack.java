/*
 * An attack option, to be sent to Showdown or used in our virtual modeling.
 * @author TeamForretress
 */

package geniusect;

import geniusect.ai.GeniusectAI;

import java.awt.Point;

import com.seleniumhelper.ShowdownHelper;

public class Attack extends Action {
	public Move move;
	
	public Pokemon attacker;
	public Pokemon defender;
	
	public Attack(Move m, Pokemon attack, Pokemon defend, Battle b)
	{
		attacker = attack;
		defender = defend;
		battle = b;
		if(attacker.getLockedInto() != null)
		{
			move = attacker.getLockedInto();
			name = move.name;
			return;
		}
		if(m.disabled || m.name.toLowerCase().startsWith("struggle"))
		{
			//Double-check to make sure this is a legal move.
			Move[] newSet = new Move[4];
			Move[] currentSet = attack.getMoveset();
			boolean foundEnabledMove = false;
			for(int i = 0; i < currentSet.length; i++)
			{
				if(currentSet[i] == null || currentSet[i].disabled)
					continue;
				else
				{
					foundEnabledMove = true;
					newSet[i] = currentSet[i];
				}
			}
			if(foundEnabledMove)
				move = Pokequations.bestMove(attacker, defender, newSet);
			else if(m.name.toLowerCase().startsWith("struggle"))
				move = m;
			else
				move = new Move("struggle", attack);
		}
		else
			move = m;
		name = move.name;
	}
	
	public int deploy()
	{
		//Send next Attack to Showdown.
		if(sent)
			return defender.getDamageDone();
		ShowdownHelper showdown = battle.getShowdown();
		System.err.println(attacker.getName()+" used "+move.name+"!");
		sent = true;
		if(!sayOnSend.equals(""))
		{
			GeniusectAI.print(sayOnSend);
			sayOnSend = "";
		}
		if(showdown == null || GeniusectAI.isSimulating())
		{
			if(!attacker.isAlive()) //We can only attack if we're alive.
				return 0;
			Damage damageDone = new Damage(move, attacker, defender);
			int damage = damageDone.applyDamage();
			//int damageDone = defender.onNewAttack(this);
			if(defender.isAlive())
				System.err.println("Damage done: "+damage+"%");
			return damage;
		}
		else if(showdown != null)
		{
			try
			{
				showdown.doMove(move.name);
			}
			catch (Exception e)
			{
				System.err.println(attacker.getName()+" could not do move "+move.name+"! Exception data:\n"+e);
				GeniusectAI.print("Exception! "+attacker.getName()+" could not do move "+move.name+"!");
				Action a = onException(this, e, battle);
				if(a instanceof Attack)
					((Attack) a).deploy();
				else if(a instanceof Change)
					((Change) a).deploy();
			}
		}
		return 0;
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
