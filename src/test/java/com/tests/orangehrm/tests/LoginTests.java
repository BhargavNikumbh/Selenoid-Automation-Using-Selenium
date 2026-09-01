package com.tests.orangehrm.tests;

import com.tests.orangehrm.base.BaseTest;
import org.orangehrm.pages.DashboardPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTests extends BaseTest {
    @Test(priority = 5)
    public void loginTest() {
        DashboardPage dashboardPage = loginPage.doLogin(prop.getProperty("username").trim(), prop.getProperty("password").trim());
        String dashboardPageTitle = dashboardPage.getDashboardPageTitle();
        Assert.assertEquals(dashboardPageTitle, "Dashboard");

    }
}
