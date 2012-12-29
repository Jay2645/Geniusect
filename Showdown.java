/*
 * Showdown hookup.
 * @author Rissole
 */

package geniusect;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
 
public class Showdown {
       
        public String rooturl = "http://play.pokemonshowdown.com";
        private WebDriver driver;
       
        public Showdown(WebDriver driver) {
                this.driver = driver;
        }
       
        public void login() {
                login("geniusecttest", "test123");
        }
       
        public void login(String username, String password) {
            driver.findElement(By.xpath("(//button[@onclick=\"return rooms['lobby'].formRename()\"])[595]")).click();
            driver.findElement(By.id("overlay_name")).clear();
            driver.findElement(By.id("overlay_name")).sendKeys(username);
            driver.findElement(By.cssSelector("button[type=\"submit\"]")).click();
           
            (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return isElementPresent(By.id("overlay_password"));
            }
        });
           
            driver.findElement(By.id("overlay_password")).clear();
            driver.findElement(By.id("overlay_password")).sendKeys(password);
            driver.findElement(By.cssSelector("button[type=\"submit\"]")).click();
        }
       
        private boolean isElementPresent(By by) {
                try {
                        driver.findElement(by);
                        return true;
                } catch (Exception e) {
                        return false;
                }
          }
       
}