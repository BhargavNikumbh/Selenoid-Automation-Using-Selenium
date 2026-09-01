package org.orangehrm.pages;


import org.orangehrm.factory.DriverFactory;
import org.orangehrm.utils.Constants;
import org.orangehrm.utils.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginPage {

    private WebDriver driver;
    private ElementUtil elementUtil;

    private By usernameTestBox = By.cssSelector("input[name='username']");
    private By passwordTextBox = By.cssSelector("input[name='password']");
    private By submitBtn = By.cssSelector("button[type='submit']");


    public static Logger log = LoggerFactory.getLogger(LoginPage.class);

    public LoginPage(WebDriver driver){
        this.driver = driver;
        elementUtil = new ElementUtil(this.driver);
    }

    public DashboardPage doLogin(String username, String password){
        log.info(username + ":" +password);
        elementUtil.waitForElementVisible(usernameTestBox, Constants.DEFAULT_TIME_OUT).sendKeys(username);
        elementUtil.doSendKeys(passwordTextBox, password);
        elementUtil.doClick(submitBtn);

        return new DashboardPage(driver);

    }

}
