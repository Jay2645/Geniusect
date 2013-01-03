package geniusect.abilities;

import geniusect.Pokemon;
import geniusect.Type;
import geniusect.ai.GeniusectAI;

/**
 * Class for the "Levitate" ability.
 * @author TeamForretress
 */
public class AbilityLevitate extends Ability
{
	public AbilityLevitate()
	{
		battle = GeniusectAI.getBattle();
	}
	public void setUser(Pokemon u)
	{
		user = u;
		user.addImmunity(Type.Ground);
	}
}
