/*
 * A special class for the move Hidden Power.
 * Used to determine the type and power of Hidden Power used.
 * @author Team Forretress
 */

package geniusect;

import java.util.List;

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
	
	public List<Type> resists; //What we know this resists this.
	public List<Type> strengths; //What we know this is super-effective against.
	
	public List<Type> possible;
	
	public void addResist(Type t)
	{
		resists.add(t);
		recalculateType();
	}
	
	public void addStrength(Type t)
	{
		strengths.add(t);
		recalculateType();
	}
	
	public void recalculateType()
	{
		//TODO: Recalculating type logic.
	}
}
