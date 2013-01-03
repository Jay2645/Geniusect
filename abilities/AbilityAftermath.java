package geniusect.abilities;

import geniusect.Damage;
import geniusect.Move;
import geniusect.MoveType;
import geniusect.Pokemon;

public class AbilityAftermath extends Ability {
	public AbilityAftermath()
	{
		rating = 3;
	}
	public void onFaint(Pokemon attacker, Move move)
	{
		if (move.getType() != MoveType.Status && move.isContact) {
			onFaintDamage = new Damage(user.getFullHP()/4, attacker);
		}
	}
}
