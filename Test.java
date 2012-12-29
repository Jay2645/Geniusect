/*
 * Testing logins.
 * @author Rissole
 */

package geniusect;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
 
public class Test  {
    public static void main(String[] args) {
    	/*
        WebDriver driver = new FirefoxDriver();
        Showdown showdown = new Showdown(driver);
 
        driver.get(showdown.rooturl);
       
        showdown.login();        
        */
    	GeniusectAI.battleStart(	"Magikarp @ Air Balloon" +
    								"\nTrait: Swift Swim" +
    								"\nEVs: 252 Spd / 252 Atk / 4 HP" +
    								"\nAdamant Nature" +
    								"\n- Splash" +
    								"\n- Flail" +
    								"\n- Bounce" +
    								"\n- Tackle" +
    								"\n" +
    								"\nWobbuffet @ Leftovers" +
    								"\nTrait: Shadow Tag " +
    								"\nEVs: 200 Def / 56 SpD / 252 Spe " +
    								"\nTimid Nature (+Spe, -Atk) " +
    								"\n- Encore " +
    								"\n- Counter " +
    								"\n- Mirror Coat " +
    								"\n- Destiny Bond");
    }
}
