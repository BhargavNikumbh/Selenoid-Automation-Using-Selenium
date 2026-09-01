package org.orangehrm.pages;


import org.orangehrm.factory.DriverFactory;
import org.orangehrm.utils.Constants;
import org.orangehrm.utils.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardPage {

    private WebDriver driver;
    private ElementUtil elementUtil;

    private By dashboardTitle = By.cssSelector("h6[class='oxd-text oxd-text--h6 oxd-topbar-header-breadcrumb-module']");

    public static Logger log = LoggerFactory.getLogger(DashboardPage.class);

    public DashboardPage(WebDriver driver){
        this.driver = driver;
        elementUtil = new ElementUtil(this.driver);
    }

    public String getDashboardPageTitle(){
        return elementUtil.waitForElementVisible(dashboardTitle, Constants.DEFAULT_TIME_OUT).getText();
    }
}
