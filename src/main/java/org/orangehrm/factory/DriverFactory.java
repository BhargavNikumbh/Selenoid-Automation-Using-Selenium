package org.orangehrm.factory;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.safari.SafariDriver;
import org.orangehrm.customexception.FrameworkException;
import org.orangehrm.utils.Browser;
import org.orangehrm.utils.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class DriverFactory {

    private WebDriver driver;
    Properties prop;
    OptionsManager optionsManager;
    public static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public static Logger log = LoggerFactory.getLogger(DriverFactory.class);

    /**
     * this method is used to initialize the driver on the basis of given
     * browsername
     *
     * @param properties prop
     * @return this method will return the webdriver
     */
    public WebDriver init_driver(Properties prop){
        this.prop = prop;
        String browserName = prop.getProperty("browser").trim();
        log.info("browser name is: "+browserName);

        optionsManager = new OptionsManager(prop);

        if(browserName.equalsIgnoreCase(Browser.chrome.toString())){
            if(Boolean.parseBoolean(prop.getProperty("remote"))){
                init_remoteDriver("chrome");
            } else {
                log.info("Running Tests on Local.....");
                tlDriver.set(new ChromeDriver(optionsManager.getChromeOptions()));
            }
        }

        else if(browserName.equalsIgnoreCase(Browser.firefox.toString())){
            if (Boolean.parseBoolean(prop.getProperty("remote"))) {
                // remote execution:
                init_remoteDriver("firefox");
            }else {
                //local execution:
                tlDriver.set(new FirefoxDriver(optionsManager.getFirefoxOptions()));
            }
        }

        else if (browserName.equalsIgnoreCase(Browser.safari.toString())) {
            tlDriver.set(new SafariDriver());
        }

        else {
            System.out.println("please pass the right browser name... " + browserName);
            throw new FrameworkException("no browser found...");
        }
        // Common browser setup
        getDriver().manage().deleteAllCookies();
        getDriver().manage().window().maximize();

        getDriver().switchTo().newWindow(WindowType.TAB);
        getDriver().get(prop.getProperty("url"));

        return getDriver();
    }

    private void init_remoteDriver(String browserName){
        log.info("Running Tests in Grid....");
        if (browserName.equalsIgnoreCase(Browser.chrome.toString())) {
            try {
                tlDriver.set(
                        new RemoteWebDriver(new URL(prop.getProperty("huburl")), optionsManager.getChromeOptions()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else if (browserName.equalsIgnoreCase(Browser.firefox.toString())) {
            try {
                tlDriver.set(
                        new RemoteWebDriver(new URL(prop.getProperty("huburl")), optionsManager.getFirefoxOptions()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    public static WebDriver getDriver() {
        return tlDriver.get();
    }

    /**
     * This method is used to initialize the properties from the respective env
     * config file
     *
     * @return this returns properties class object with all the config properties
     */
    public Properties init_prop() {
        FileInputStream ip = null;
        prop = new Properties();

        // mvn command line arg:
        // mvn clean install -Denv="qa"

        String envName = System.getProperty("env");
        log.info("Running tests on environment: " + envName);

        if (envName == null) {
            log.info("No env is given ..... hence running it on QA");
            try {
                ip = new FileInputStream("./src/test/resources/configs/qa.config.properties");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        else {

            try {
                switch (envName.toLowerCase()) {
                    case Environment.ENV_QA:
                        ip = new FileInputStream("./src/test/resources/configs/qa.config.properties");
                        break;
                    case Environment.ENV_DEV:
                        ip = new FileInputStream("./src/test/resources/configs/dev.config.properties");
                        break;
                    case Environment.ENV_STAGE:
                        ip = new FileInputStream("./src/test/resources/configs/stage.config.properties");
                        break;
                    case Environment.ENV_UAT:
                        ip = new FileInputStream("./src/test/resources/configs/uat.config.properties");
                        break;
                    case Environment.ENV_PROD:
                        ip = new FileInputStream("./src/test/resources/configs/config.properties");
                        break;

                    default:
                        System.out.println("please pass the right environment value..." + envName);
                        log.error("please pass the right environment value..." + envName);
                        throw new FrameworkException("no env found...");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (FrameworkException e) {
                e.printStackTrace();
            }
        }

        try {
            prop.load(ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    /**
     * take screenshot
     *
     */

    public String getScreenshot() {

        File srcFile = ((TakesScreenshot) getDriver())
                .getScreenshotAs(OutputType.FILE);

        String screenshotDir = System.getProperty("user.dir")
                + "/screenshot/";

        File directory = new File(screenshotDir);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        String path = screenshotDir
                + System.currentTimeMillis()
                + ".png";

        File destination = new File(path);
        try {
            FileUtils.copyFile(srcFile, destination);
        } catch (IOException e) {
            log.error("Failed to capture screenshot", e);
        }
        return path;
    }


}
