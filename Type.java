/*
 * A list of all types in the game.
 * Can be cast as a string.
 * @author TeamForretress
 */

package geniusect;

public enum Type {
	Normal("Normal", 0, 17), Fighting("Fighting", 6, 1), Ghost("Ghost", 13, 7), Electric("Electric", 3, 12), Fire("Fire", 1, 9), Water("Water", 2, 10),
	Grass("Grass", 4, 11), Dark("Dark", 15, 16), Psychic("Psychic", 10, 13), Steel("Steel", 16, 8), Ground("Ground", 8, 4), 
	Rock("Rock", 12, 5), Dragon("Dragon", 14, 15), Ice("Ice", 5, 14), Bug("Bug", 11, 6), Flying("Flying", 9, 2), Poison("Poison", 7, 3), None("None", -1, -1);
	private String name;
	private int sqlID;
	private int gameID;
	
	private Type(String s, int sql, int g)
	{
		name = s;
		sqlID = sql;
		gameID = g;
	}
	
	public String toString()
	{
		return name;
	}
	
	public static Type fromSQL(int i)
	{
		Type type = Type.None;
		switch(i)
		{
			case 0:	type = Type.Normal;
					break;
			case 1:	type = Type.Fire;
					break;
			case 2:	type = Type.Water;
					break;
			case 3:	type = Type.Electric;
					break;
			case 4:	type = Type.Grass;
					break;
			case 5:	type = Type.Ice;
					break;
			case 6:	type = Type.Fighting;
					break;
			case 7:	type = Type.Poison;
					break;
			case 8:	type = Type.Ground;
					break;
			case 9:	type = Type.Flying;
					break;
			case 10:type = Type.Psychic;	
					break;
			case 11:type = Type.Bug;
					break;
			case 12:type = Type.Rock;
					break;
			case 13:type = Type.Ghost;
					break;
			case 14:type = Type.Dragon;
					break;
			case 15:type = Type.Dark;
					break;
			case 16:type = Type.Steel;
					break;
		}
		return type;
	}
	
	public int toSQLID()
	{
		return sqlID;
	}
	
	public int toGameID()
	{
		return gameID;
	}
}
