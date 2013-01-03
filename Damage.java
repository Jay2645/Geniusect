package geniusect;

public class Damage {
	public Damage(){}
	public Damage(int damagePercent, Pokemon victim)
	{
		defender = victim;
	}
	int damage = 0;
	Attack attack = null;
	Pokemon defender = null;
	
	public void applyDamage()
	{
		if(defender == null)
			return;
		if(attack == null)
			defender.damage(attack);
		else if(damage > 0)
			defender.damage(damage);
		else if(damage < 0)
			defender.restoreHP(damage);
	}
}
