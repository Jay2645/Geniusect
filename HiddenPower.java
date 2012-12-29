/*
 * A special class for the move Hidden Power.
 * Used to determine the type and power of Hidden Power used.
 * @author Team Forretress
 */

package geniusect;

public class HiddenPower extends Move {
	public HiddenPower(Move clone)
	{
		name = "Hidden Power";
		user = clone.user;
		pp = clone.pp;
		accuracy = 100;
		target = Target.Normal;
		special =  true;
		status = false;
		boostChance = 0;
		recoilPercent = 0;
		disabled = clone.disabled;
		projectedDamage = clone.projectedDamage;
		projectedPercent = clone.projectedPercent;
	}
	
}
