package geniusect.abilities;

import geniusect.Battle;
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
		rating = 3;
	}
	@Override
	public void setUser(Pokemon u)
	{
		u.addImmunity(Type.Fire);
		user = u;
	}
	
	//TODO: Boost the power of fire-type moves.
}
