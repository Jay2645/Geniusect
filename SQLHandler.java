/*
 * A class which hooks into an SQL server and returns the data within.
 * @author TeamForretress
 */

package geniusect;

import java.sql.*;

public class SQLHandler {
	
	private static Connection conn = null;
	
	public static void openConnection()
	{
		if(conn == null)
		{
			System.out.println("Opening connection to SQL server.");
			try
			{
				String userName = "root";
				String password = "securepassword";
				String url = "jdbc:mysql://localhost:3306/pokemon";
				Class.forName ("com.mysql.jdbc.Driver").newInstance ();
				conn = DriverManager.getConnection (url, userName, password);
				System.out.println ("Database connection established");
			}
			catch (Exception e)
			{
				System.err.println ("Cannot connect to database server, error: " +e);
			}
		}
	}
		
	public static Move queryMove(Move m)
	{
		openConnection();
		String move = m.name;
		System.out.println("Sending SQL query for move: " + move);
		//ArrayList hiddenPower = HiddenPowerCalculator.calculateHiddenPower();
		{
			try
			{
				System.out.println("Fetching Pokemon learnset");
				PreparedStatement s = conn.prepareStatement("SELECT type, power, accuracy, category, pp, target, move.desc FROM move WHERE name= ? ORDER BY name ASC");
				s.setString(1, move);
				s.executeQuery();
				ResultSet rs = s.getResultSet ();
				int count = 0;
				while (rs.next ())
				{
					m.type = Type.fromSQL(Integer.parseInt(rs.getString("type")));
					m.power = Integer.parseInt(rs.getString("power"));
					String moveCategory = rs.getString("category");
					if(moveCategory.toLowerCase().startsWith("special"))
						m.special = true;
					else if(moveCategory.toLowerCase().startsWith("status"))
						m.status = true;
					m.accuracy = Integer.parseInt(rs.getString("accuracy"));
					m.pp = Integer.parseInt(rs.getString("pp"));
					m.target = rs.getString("target");
					/*if(move.startsWith("Hidden Power"))
						{
						moveType = hiddenPower.get(1).toString();
						movePower = hiddenPower.get(0).toString();
						}*/
					System.out.println (
						"Name: " + move +
						", Type: " + m.type +
						", Power: " + m.power +
						", Special: " + m.special +
						", Status: " + m.status +
						", Accuracy: " + m.accuracy +
						", Target: " + m.target +
						", PP: " + m.pp);
					++count;
				}
				rs.close ();
				s.close ();
				System.out.println (count + " rows were retrieved");
			}
			catch (SQLException e)
			{
				System.err.println ("Error message: " + e.getMessage ());
				System.err.println ("Error number: " + e.getErrorCode ());
			}
		}
		closeConnection();
		return m;
	}
	
	public static double queryDamage(Type attack, Type defender)
	{
		double multiplier = 1;
		openConnection();
		//System.out.println("Sending SQL query to determine type effectiveness where the attacker is: " + attack.toString() +" and the defender is "+defender.toString());
		try
		{
			PreparedStatement s = conn.prepareStatement("SELECT dmg_mult, typeatk, typedef FROM type_effect WHERE typeatk= ? AND typedef= ? ORDER BY dmg_mult ASC");
			s.setString(1, attack.toSQLID()+"");
			s.setString(2, defender.toSQLID()+"");
			s.executeQuery();
			ResultSet rs = s.getResultSet ();
			//int count = 0;
			while (rs.next ())
			{
				multiplier = rs.getDouble("dmg_mult");
				//System.out.println(multiplier);
				//count++;
			}
			rs.close ();
			s.close ();
			//System.out.println (count + " rows were retrieved");
		}
		catch (SQLException e)
		{
			System.err.println ("Error message: " + e.getMessage ());
			System.err.println ("Error number: " + e.getErrorCode ());
		}
		catch (NumberFormatException nfe)
		{
			System.err.println("Number Format Exception error: " + nfe.getMessage());
		}
		closeConnection();
		return multiplier;
	}
	
	public static Pokemon queryPokemon(Pokemon p)
	{
		openConnection();
		String currentPokemon = p.name;
		System.out.println("Sending SQL query for Pokemon: " + currentPokemon);
		try
		{
			PreparedStatement s = conn.prepareStatement("SELECT pokemon.name, atk, spa, def, spd, hp, spe, pokemon.type0, pokemon.type1, tier FROM pokemon JOIN learnset ON (pokemon.id=learnset.pokemon) JOIN move ON (learnset.move=move.id) WHERE pokemon.name = ? ORDER BY name ASC");
			s.setString(1, currentPokemon); // set the first '?' in the query to the currentPokemon
			s.executeQuery(); // everything else is the same from here on
			ResultSet rs = s.getResultSet();
			int count = 0;
			int type1Holder = -1;
			int type2Holder = -1;
			while (rs.next ())
			{
				p.base[Stat.HP.toInt()] = Integer.parseInt(rs.getString("hp"));
				p.base[Stat.Atk.toInt()] = Integer.parseInt(rs.getString("atk"));
				p.base[Stat.Def.toInt()] = Integer.parseInt(rs.getString("def"));
				p.base[Stat.SpA.toInt()] = Integer.parseInt(rs.getString("spa"));
				p.base[Stat.SpD.toInt()] = Integer.parseInt(rs.getString("spd"));
				p.base[Stat.Spe.toInt()] = Integer.parseInt(rs.getString("spe"));
				p.tier = rs.getString("tier");
				type1Holder = Integer.parseInt(rs.getString("type0"));
				type2Holder = Integer.parseInt(rs.getString("type1"));
				++count;
			}
			p.types[0] = Type.fromSQL(type1Holder);
			p.types[1] = Type.fromSQL(type2Holder);
			System.out.println("Pokemon name: " + currentPokemon);
			System.out.println("Pokemon type 1: " + p.types[0]);
			System.out.println("Pokemon type 2: " + p.types[1]);
			System.out.println("Tier: " + 					p.tier);
			System.out.println("Base HP: " + 				p.base[Stat.HP.toInt()]);
			System.out.println("Base Attack: " + 			p.base[Stat.Atk.toInt()]);
			System.out.println("Base Defense: " + 			p.base[Stat.Def.toInt()]);
			System.out.println("Base Special Attack: " + 	p.base[Stat.SpA.toInt()]);
			System.out.println("Base Special Defense: " +	p.base[Stat.SpD.toInt()]);
			System.out.println("Base Speed: " +		 		p.base[Stat.Spe.toInt()]);
			rs.close ();
			s.close ();
			System.out.println (count + " rows were retrieved");
		}
		catch (SQLException e)
		{
			System.err.println ("Error message: " + e.getMessage ());
			System.err.println ("Error number: " + e.getErrorCode ());
		}
		catch (NumberFormatException nfe)
		{
			System.err.println("Number Format Exception error: " + nfe.getMessage());
		}
		/*
		 * TODO: Resistance lookup (old one was too Jaycode-y to decipher).
		p.hyperEffective = quadrupleDamageLookup(p.types);
		p.superEffective = doubleDamageLookup(p.types);
		p.resistances = halfDamageLookup(p.types);
		p.doubleResistances = quarterDamageLookup(p.types);
		*/
		closeConnection();
		return p;
	}
	
	private static void closeConnection()
	{
		/*if (conn != null)
		{
			System.out.println("Closing connection to SQL server.");
			try
			{
				conn.close ();
				System.out.println ("Database connection terminated");
			}
			catch (Exception e) { }
		}*/
	}
}
