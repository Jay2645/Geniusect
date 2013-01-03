package geniusect.abilities;

import geniusect.Pokemon;
import geniusect.Type;
import geniusect.ai.GeniusectAI;

/**
 * A class representing the "Flash Fire" ability.
 * @author TeamForretress
 */
public class AbilityFlashFire extends Ability
{
	public AbilityFlashFire()
	{
		battle = GeniusectAI.getBattle();
	}
	@Override
	public void setUser(Pokemon u)
	{
		u.addImmunity(Type.Fire);
		user = u;
	}
}
