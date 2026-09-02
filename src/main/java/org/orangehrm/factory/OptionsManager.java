package org.orangehrm.factory;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.Map;
import java.util.Properties;

public class OptionsManager {

    private Properties prop;
    private ChromeOptions co;
    private FirefoxOptions fo;

    public OptionsManager(Properties prop){this.prop = prop;}

    public ChromeOptions getChromeOptions(){
        co = new ChromeOptions();

        if(Boolean.parseBoolean(prop.getProperty("remote"))){
//            co.setCapability("enableVNC", true);
            co.setCapability("selenoid:options", Map.of(
                    "enableVNC", true
            ));
//            co.setBrowserVersion(prop.getProperty("browserversion"));
            String browserVersion = prop.getProperty("browserversion");

            if (browserVersion != null && !browserVersion.trim().isEmpty()) {
                co.setBrowserVersion(browserVersion.trim());
            }

        }
        if (Boolean.parseBoolean(prop.getProperty("headless")))
            co.addArguments("--headless=new");

        if (Boolean.parseBoolean(prop.getProperty("incognito")))
            co.addArguments("--incognito");
        return co;
    }

    public FirefoxOptions getFirefoxOptions() {

        fo = new FirefoxOptions();
        if (Boolean.parseBoolean(prop.getProperty("remote"))) {
            fo.setCapability("enableVNC", true);
            fo.setBrowserVersion(prop.getProperty("browserversion"));
        }
        if (Boolean.parseBoolean(prop.getProperty("headless"))) {
            fo.addArguments("-headless");
        }
        if (Boolean.parseBoolean(prop.getProperty("incognito"))) {
            fo.addArguments("-private");
        }
        return fo;
    }
}
