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
    	GeniusectAI.battleStart("Zapdos @ Leftovers \nTrait: Pressure \nEVs: 252 SDef / 252 HP / 4 SAtk \nCalm Nature \n- Heat Wave \n- Roost \n- Thunderbolt \n- Drill Peck");
    }
}
