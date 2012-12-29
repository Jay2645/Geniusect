/*
 * A list of all natures in the game, along with what they modify.
 * The modifier can be returned in multiple ways, or the enum can modify the stat directly.
 * @author TeamForretress
 */

package geniusect;

public enum Nature {
	Hardy("Hardy", Stat.Atk,Stat.Atk), Docile("Docile", Stat.Def,Stat.Def), Serious("Serious", Stat.Spe,Stat.Spe), 
	Bashful("Bashful", Stat.SpA,Stat.SpA),Quirky("Quirky",Stat.SpD,Stat.SpD), Lonely("Lonely", Stat.Atk,Stat.Def), 
	Brave("Brave", Stat.Atk,Stat.Spe), Adamant("Adamant", Stat.Atk,Stat.SpA), Naughty("Naughty", Stat.Atk,Stat.SpD), 
	Bold("Bold", Stat.Def,Stat.Atk), Relaxed("Relaxed", Stat.Def,Stat.Spe), Impish("Impish", Stat.Def,Stat.SpA), 
	Lax("Lax", Stat.Def,Stat.SpD), Modest("Modest", Stat.SpA, Stat.Atk), Mild("Mild", Stat.SpA, Stat.Def), Quiet("Quiet", Stat.SpA,Stat.Spe),
	Rash("Rash", Stat.SpA,Stat.SpD), Calm("Calm", Stat.SpD, Stat.Atk), Gentle("Gentle", Stat.SpD, Stat.Def), 
	Sassy("Sassy", Stat.SpD, Stat.Spe), Careful("Careful", Stat.SpD, Stat.SpA), Timid("Timid", Stat.Spe,Stat.Atk), 
	Hasty("Hasty", Stat.Spe,Stat.Def), Jolly("Jolly", Stat.Spe,Stat.SpA), Naive("Naive", Stat.Spe,Stat.SpD);
	private Stat[] mod = new Stat[2];
	private String name;
	private Nature(String name, Stat boost, Stat reduce)
	{
		mod[0] = boost;
		mod[1] = reduce;
	}
	
	public Stat[] multiplier()
	{
		return mod;
	}
	
	public double multiplier(Stat type)
	{
		if(type == mod[0] && type == mod[1])
			return 1;
		else if(type == mod[0])
			return 1.1;
		else if(type == mod[1])
			return 0.9;
		else return 1;
	}
	
	public int multiplier(int type, int stat)
	{
		return multiplier(Stat.fromInt(type), stat);
	}
	
	public int multiplier(Stat type, int stat)
	{
		if(mod[0] == type && mod[1] == type)
			return stat;
		else if(mod[0] == type)
			return (int)Math.round(stat * 1.1);
		else if(mod[1] == type)
			return (int)Math.round(stat * 0.9);
		else return stat;
	}
	
	public String toString()
	{
		return name;
	}
	
	public static Nature fromString(String n)
	{
		Nature nat = Nature.Hardy;
		switch(n)
		{
			case "Hardy":	nat = Nature.Hardy;
							break;
			case "Docile":	nat = Nature.Docile;
							break;
			case "Serious":	nat = Nature.Serious;
							break;
			case "Bashful":	nat = Nature.Bashful;
							break;
			case "Quirky":	nat = Nature.Quirky;
							break;
			case "Lonely":	nat = Nature.Lonely;
							break;
			case "Brave":	nat = Nature.Brave;
							break;
			case "Adamant":	nat = Nature.Adamant;
							break;
			case "Naughty":	nat = Nature.Naughty;
							break;
			case "Bold":	nat = Nature.Bold;
							break;
			case "Relaxed":	nat = Nature.Relaxed;
							break;
			case "Impish":	nat = Nature.Impish;
							break;
			case "Lax":		nat = Nature.Lax;
							break;
			case "Modest":	nat = Nature.Modest;
							break;
			case "Mild":	nat = Nature.Mild;
							break;
			case "Quiet":	nat = Nature.Quiet;
							break;
			case "Rash":	nat = Nature.Rash;
							break;
			case "Calm":	nat = Nature.Calm;
							break;
			case "Gentle":	nat = Nature.Gentle;
							break;
			case "Sassy":	nat = Nature.Sassy;
							break;
			case "Careful":	nat = Nature.Careful;
							break;
			case "Timid":	nat = Nature.Timid;
							break;
			case "Hasty":	nat = Nature.Hasty;
							break;
			case "Jolly":	nat = Nature.Jolly;
							break;
			case "Naive":	nat = Nature.Naive;
							break;
			case "":		nat = Nature.Hardy;
							break;
		}
		return nat;
	}
}
