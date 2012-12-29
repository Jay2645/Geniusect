package geniusect;

public enum Status {
	Poison(12.5), ToxicPoison(6.25), Burn(12.5), Freeze(0), Sleep(0), Paralysis(0), Rest(0), None(0);
	
	private Status(double d)
	{
		damage = d;
	}
	
	private double damage;
	private Pokemon victim;
	public int turnsActive = 0;
	
	public void inflict(Pokemon v)
	{
		victim = v;
		
		if(this == Status.Rest)
		{
			victim.restoreHP(100);
		}
		if(this == Status.Burn)
		{
			victim.giveBoosts(Stat.Atk, -2);
		}
		else if(this == Status.Paralysis)
		{
			victim.giveBoosts(Stat.Spe, -6);
		}
		if(this == Status.Freeze || this == Status.Rest || this == Status.Sleep || this == Status.Paralysis && turnsActive % 4 == 0)
		{
			victim.canMove(false);
		}
		else
		{
			victim.canMove(true);
		}
	}
	
	public double newTurn()
	{
		turnsActive++;
		if(this == Status.ToxicPoison)
		{
			damage = turnsActive / 16;
		}
		else if(this == Status.Poison || this == Status.Burn)
		{
			damage = 1 / 8;
		}
		if(this == Status.Rest && turnsActive == 2 || this == Status.Sleep && turnsActive == 3 || this == Status.Freeze && turnsActive == 5)
			victim.inflictStatus(Status.None);
		damage *= 100;
		return damage;
	}
	
	public void resetActive()
	{
		turnsActive = 0;
	}
}
