package org.orangehrm.pages;


import org.orangehrm.factory.DriverFactory;
import org.orangehrm.utils.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Navbar {

    private WebDriver driver;
    private ElementUtil elementUtil;

    private By navAdminPageLink = By.xpath("//span[normalize-space()='Admin']");
    private By navPimPageLink = By.xpath("//span[normalize-space()='PIM']");
    private By navLeavePageLink = By.xpath("//span[normalize-space()='Leave']");
    private By navTimePageLink = By.xpath("//span[normalize-space()='Time']");
    private By navRecruitmentPageLink = By.xpath("//span[normalize-space()='Recruitment']");
    private By navMyInfoPageLink = By.xpath("//span[normalize-space()='My Info']");
    private By navPerformancePageLink = By.xpath("//span[normalize-space()='Performance']");
    private By navDashboardPageLink = By.xpath("//span[normalize-space()='Dashboard']");
    private By navDirectoryPageLink = By.xpath("//span[normalize-space()='Directory']");
    private By navMaintanencePageLink = By.xpath("//span[normalize-space()='Maintenance']");
    private By navClaimPageLink = By.xpath("//span[normalize-space()='Claim']");
    private By navBuzzPageLink = By.xpath("//span[normalize-space()='Buzz']");


    public static Logger log = LoggerFactory.getLogger(Navbar.class);

    public Navbar(WebDriver driver){
        this.driver = driver;
        elementUtil = new ElementUtil(this.driver);
    }

    public DashboardPage goToDashboard(){
        elementUtil.doClick(navDashboardPageLink);
        return new DashboardPage(driver);
    }
}
