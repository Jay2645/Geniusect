
package geniusect.abilities;

import geniusect.Pokemon;
import geniusect.Weather;

/**
 * @author Team Forretress
 * The ability subclass for when the abilities "Airlock" or "Cloud Nine" are in play.
 *
 */
public class AbilityRemoveWeather extends Ability {
	public AbilityRemoveWeather()
	{
		weather = battle.getWeather();
		rating = 3;
	}
	private Weather weather;
	
	public void onSendOut()
	{
		battle.setWeather(Weather.None);
	}
	
	public void onWithdraw()
	{
		battle.setWeather(weather);
	}
}
