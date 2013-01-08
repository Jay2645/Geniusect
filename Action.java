/*
 * A generic action class which defines what we will do in a turn.
 * @author TeamForretress
 */

package geniusect;

import geniusect.ai.GenericAI;
import geniusect.ai.MinimaxNode;
import geniusect.ai.GeniusectAI;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.seleniumhelper.ShowdownHelper;
import com.seleniumhelper.ShowdownHelper.TurnEndStatus;

public class Action 
{
	public String name;
	protected String sayOnSend = "";
	protected boolean crit = false;
	protected boolean sent = false;
	public int score = 0; //The minimax score for this action.
	public int attempt = 0; //How many times we've attempted to try this.
	protected Battle battle = null;
	protected TurnEndStatus nextTurn;
	
	public static int changeCount = 0; //How many times in a row have we changed? (Sanity check.)
	
	
	public void updateLastTurn(Battle b)
	{
		battle = b;
		ShowdownHelper showdown = battle.getShowdown();
		if(showdown != null)
		{
			String lastTurn = showdown.getLastTurnText();
			if(lastTurn.contains("~printLogic"))
				GeniusectAI.lastTurnLogic();
			boolean switched = findPokemon(lastTurn);
			findMove(lastTurn);
			//TODO:	Update Pokemon of both teams.
			if(switched && this instanceof Attack)
			{
				Attack a = (Attack)this;
				a.defenderSwap(battle.getTeam(1).getActive());
			}
		}
	}
	
	/**
	 * Sets a child's enemy Pokemon or adds the damage done by this Action to the child.
	 * @param node (MinimaxNode): The child node.
	 */
	public int sendToShowdown(MinimaxNode node)
	{
		if(this instanceof Change)
		{
			Change c = (Change)this;
			c.deploy();
			return Change.calculateSwitchDamagePercent(c.switchTo);
		}
		else
		{
			Attack a = (Attack) this;
			return a.deploy();
		}
	}
	
	public void sendToShowdown(Battle b)
	{
		battle = b;
		ShowdownHelper showdown = b.getShowdown();
		if(this instanceof Change)
		{
			Change c = (Change)this;
			changeCount++;
			c.deploy();
		}
		else if(this instanceof Attack)
		{
			Attack a = (Attack) this;
			changeCount = 0;
			a.deploy();
		}
		if(b.isFirstTurn())
			GeniusectAI.displayIntro();
		b.isFirstTurn(false);
		if(showdown != null)
		{
			nextTurn = showdown.waitForNextTurn(5);
			System.out.println(nextTurn);
			if(nextTurn == TurnEndStatus.ATTACK || nextTurn == TurnEndStatus.UNKNOWN)
				b.newTurn();
			else if(nextTurn == TurnEndStatus.SWITCH)
			{
				Pokemon change = b.getTeam(0).getActive().onDie();
				if(change == null)
				{
					System.err.println("Could not find Pokemon to switch to!");
					showdown.leaveBattle();
				}
				else
				{
					try
					{
						System.out.println("We have died! Switching to "+change.getName());
						showdown.switchTo(change.getName(), false);
						b.newTurn();
					}
					catch(Exception e)
					{
						onException(this,e,b);
					}
				}
			}
			else if (nextTurn == TurnEndStatus.WON)
			{
				b.getTeam(0).getActive().ragequits++;
				b.gameOver(true);
			}
			else if (nextTurn == TurnEndStatus.LOST)
				b.gameOver(false);
		}
	}
	
	public void say(String text)
	{
		//Says some text at the end of a turn.
		GeniusectAI.print(text);
	}
	
	/*public static void main(String[] args) {
		String log = "Turn 14" +
				"\nThe foe's Cloyster used Shell Smash!" +
				"\nThe foe's Cloyster's Attack sharply rose!" +
				"\nThe foe's Cloyster's Special Attack sharply rose!" +
				"\nThe foe's Cloyster's Speed sharply rose!" +
				"\nThe foe's Cloyster's Defense fell!" +
				"\nThe foe's Cloyster's Special Defense fell!" +
				"\nThe foe's Cloyster restored its status using White Herb!" +
				"\nLatios used Draco Meteor!" +
				"\nA critical hit! The foe's Cloyster lost 75% of its health!" +
				"\nLatios's Special Attack harshly fell!" +
				"\nThe foe's Cloyster fainted!";
		
		findMove(log);
	}*/
	
	protected void findMove(String text)
	{	//Takes a string of the last turn's events, finds all moves used and their damage, then updates HP and Pokemon.
		Pattern p = Pattern.compile("(.+) used (.+)!", Pattern.MULTILINE);
		Matcher m = p.matcher(text);
		Team t = null;
		//Team.players[0] = new Team(0);
		//Team.players[1] = new Team(1);
		while(m.find())
		{
			String tempname = text.substring(m.start(1), m.end(1));
			if(tempname.contains("The foe's"))
			{
				t = battle.getTeam(1);
				tempname = stripFoe(tempname);
			}
			else
				t = battle.getTeam(0);
			Pokemon poke = t.addPokemon(tempname, battle.getShowdown());
			String tempmove = text.substring(m.start(2), m.end(2));
			String moveDamage = text.substring(m.end(2));
			boolean crit = false;
			int dmg = 0;
			Pattern dmgP = Pattern.compile("(.+) lost (.+)%");
			Matcher dmgM = dmgP.matcher(moveDamage);
			if(dmgM.find())
			{
				String damage = moveDamage.substring(dmgM.start(1),dmgM.end(2));
				if(!damage.contains(tempname))
				{
					crit = damage.contains("critical hit");
					damage = moveDamage.substring(dmgM.start(2),dmgM.end(2));
					dmg = Integer.parseInt(damage);
				}
			}
			System.out.println(tempname+" used "+tempmove+" for "+dmg+"% damage. Was it a crit? "+crit);
			System.err.println(tempname+"'s enemy is "+poke.getEnemy());
			poke.onNewTurn(tempmove, dmg, crit);
		}
		findDrops(text);
	}
	
	protected void findDrops(String text)
	{
		Pattern whP = Pattern.compile("(.+) restored its status using White Herb!");
		Matcher whM = whP.matcher(text);
		String[] restore = new String[2];
		while(whM.find())
		{
			String herb = text.substring(whM.start(1),whM.end(1));
			if(herb.contains("The foe's"))
			{
				herb = stripFoe(herb);
				restore[1] = herb;
			}
			else
				restore[0] = herb;
		}
		Pattern p = Pattern.compile("(.+)'s (.+) (fell|rose)!",Pattern.MULTILINE);
		Matcher m = p.matcher(text);
		Team t = null;
		while(m.find())
		{
			String temp = text.substring(m.start(1),m.end(1));
			String herb;
			if(temp.contains("The foe's"))
			{
				t = battle.getTeam(1);
				temp = stripFoe(temp);
				herb = restore[1];
			}
			else
			{
				t = battle.getTeam(0);
				herb = restore[0];
			}
			Pokemon poke = t.addPokemon(temp, battle.getShowdown());
			String stat = text.substring(m.start(2),m.end(2));
			int level = 1;
			if(stat.contains("harshly")||stat.contains("sharply"))
			{
				String twoStage;
				if(stat.contains("harshly"))
					twoStage = "harshly";
				else
					twoStage = "sharply";
				level = 2;
				stat = stat.substring(0, stat.indexOf(twoStage));
			}
			else if(stat.contains("drastically")||stat.contains("dramatically"))
			{
				String threeStage;
				if(stat.contains("drastically"))
					threeStage = "drastically";
				else
					threeStage = "dramatically";
				level = 3;
				stat = stat.substring(0,stat.indexOf(threeStage));
			}
			Stat st = Stat.fromString(stat);
			String rose = text.substring(m.start(3),m.end(3));
			if(rose.toLowerCase().startsWith("fell"))
			{
				if(herb == null)
					level *= -1;
				else
				{
					System.out.println(poke.getName()+" restored its negative stat drop!");
					level = 0;
				}
			}
			if(level != 0)
				poke.giveBoosts(st, level);
		}
	}
	
	protected static String stripFoe(String tempname)
	{	//Strips "The foe's " from a result to give you just the Pokemon name.
		String f = "";
		Pattern foeP = Pattern.compile("The foe's (.+)");
		Matcher foeM = foeP.matcher(tempname);
		foeM.find();
		if (foeM.end(1) - foeM.start(1) > 1)
		{
			f = foeM.group(1);
		}
		else
		{
			f = tempname.substring(0, foeM.start(1) - 2);
		}
		return f;
	}
	
	protected boolean findPokemon(String text)
	{	//Takes a string of the last turn's events, finds any Pokemon that were sent out, and marks them as active.
		boolean switched = false;
		String[] pokemon = new String[2];
		ShowdownHelper showdown = battle.getShowdown();
		Pattern faintP = Pattern.compile("(.+) fainted!", Pattern.MULTILINE);
		Matcher faintM = faintP.matcher(text);
		while(faintM.find())
		{
			String faint = text.substring(faintM.start(1), faintM.end(1));
			Team t;
			if(faint.contains("The foe's "))
			{
				faint = stripFoe(faint);
				t = battle.getTeam(1);
			}
			else
				t = battle.getTeam(0);
			Pokemon poke = t.getPokemon(faint);
			if(poke == null)
				System.err.println("Could not find Pokemon "+faint);
			else
				poke.onDie();
		}
		pokemon[0] = showdown.getCurrentPokemon(true);
		pokemon[1] = showdown.getCurrentOpponentPokemon(true);
		for(int i = 0; i < pokemon.length; i++)
		{
			if(pokemon[i] != null)
			{
				Pokemon poke = battle.getTeam(i).addPokemon(pokemon[i], showdown); //Not getPokemon because we don't know if we've seen it yet.
				poke.onSendOut();
				switched = true;
			}
		}
		return switched;
	}
	
	protected static void onException(Action failure, Exception e, Battle battle)
	{
		//Called if everything breaks.
		ShowdownHelper showdown = battle.getShowdown();
		if(failure.attempt == 0)
		{
			GeniusectAI.print("Here's what I tried to do:");
			GeniusectAI.lastTurnLogic();
			GeniusectAI.print(Battle.criticalErrors);
			Battle.criticalErrors = Battle.criticalErrors + "\n" + e;
			e.printStackTrace();
		}
		else if(failure.attempt > 4)
		{
			GeniusectAI.print("Failed!");
			GeniusectAI.print("Sorry! Geniusect is still in early development and is very buggy.");
			GeniusectAI.print("If I don't leave, please hit the 'Kick inactive player button.'");
			if(showdown != null)
				showdown.leaveBattle();
			battle.isPlaying(false);
			GeniusectAI.setBattle(null);
			return;
		}
		GeniusectAI.print("Attempting to rectify: attempt number "+failure.attempt+" / 4");
		if(showdown != null)
		{
			Team enemy = battle.getTeam(1);
			String activeEnemy = showdown.getCurrentPokemon(enemy.getUsername(),true);
			Pokemon enemyPoke = enemy.getPokemon(activeEnemy);
			if(enemyPoke != null)
				enemy.setActive(enemyPoke);
			//TODO: Recheck enemy team's alive/dead states.
			Team us = battle.getTeam(0);
			String activeUs = showdown.getCurrentPokemon(us.getUsername(),true);
			Pokemon poke = us.getPokemon(activeUs);
			if(poke != null)
				us.setActive(poke);
			//TODO: Recheck our team's alive/dead states.
			poke.resetMoves(showdown.getMoves());
		}
		Action a;
		if(battle.getTeam(0).getActive().isAlive())
		{
			a = GenericAI.bestMove(battle);
		}
		else
		{
			Pokemon p = Change.bestCounter(battle.getTeam(0).getPokemon(),battle.getTeam(1).getActive());
			a = new Change(p, battle);
		}
		a.attempt = failure.attempt + 1;
		a.sendToShowdown(battle);
	}
}
