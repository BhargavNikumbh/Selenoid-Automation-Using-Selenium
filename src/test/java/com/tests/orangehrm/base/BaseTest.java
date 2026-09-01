package com.tests.orangehrm.base;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.eventstreaming.Message;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;

import org.orangehrm.factory.DriverFactory;
import org.orangehrm.pages.DashboardPage;
import org.orangehrm.pages.LoginPage;
import org.orangehrm.pages.Navbar;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.asserts.SoftAssert;


import java.io.File;

import java.io.IOException;
import java.net.URL;

import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;


public class BaseTest {
    DriverFactory df;
    protected Properties prop;
    public WebDriver driver;

    protected LoginPage loginPage;
    protected Navbar navbar;
    protected DashboardPage dashboardPage;

    protected SoftAssert softAssert;

    @Parameters({"browser", "browserversion"})
    @BeforeTest
    public void setup(String browser, String browserVersion) {
        df = new DriverFactory();
        prop = df.init_prop();

        if (browser != null) {
            prop.setProperty("browser", browser);
            prop.setProperty("browserversion", browserVersion);
        }

        driver = df.init_driver(prop);
        loginPage = new LoginPage(driver);
        softAssert = new SoftAssert();
    }

    @AfterTest
    public void tearDown() throws InterruptedException {
        driver.close();
//        driver.switchTo().window(prop.getProperty("tcstudioid"));
//        Thread.sleep(3000);
        driver.quit();
    }

//    @AfterSuite
//    public void saveTestReport() {
//
//        String sourcePath = "build/TestExecutionReport.html";
//        String destinationPath = "reports/TestExecutionReport.html";
//
//        try {
//            File sourceFile = new File(sourcePath);
//            File destinationFile = new File(destinationPath);
//
//            // Create reports directory if it doesn't exist
//            FileUtils.forceMkdirParent(destinationFile);
//
//            // Copy report
//            FileUtils.copyFile(sourceFile, destinationFile);
//
//            System.out.println("======================================");
//            System.out.println("Test Execution Report:");
//            System.out.println(destinationFile.getAbsolutePath());
//            System.out.println("======================================");
//
//        } catch (IOException e) {
//            System.err.println("Failed to save test execution report");
//            e.printStackTrace();
//        }
//    }


//    @AfterSuite
//    public void sendTestReports() {
//
//        // Pass the name of the S3 bucket
//        String bucket_name = "nalreportbucket";
//        // Location of the report file from the project structure
//        String file_path = "build/TestExecutionReport.html";
//        String key_name = Paths.get(file_path).getFileName().toString();
//
//        // Instantiate an Amazon S3 client, which will make the service call with the
//        // supplied AWS credentials.
//        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new ProfileCredentialsProvider())
//                .withRegion(Regions.AP_SOUTH_1).build();
//
//        try {
//            // Upload the report to S3 bucket
//            try {
//                s3.putObject(bucket_name, key_name, new File(file_path));
//            } catch (AmazonServiceException e) {
//                System.err.println(e.getErrorMessage());
//                System.exit(1);
//            }
//
//            // Generate the S3 Pre-signed URL of the Test Execution Report
//            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket_name, key_name, HttpMethod.GET);
//            // The URL expires after one day - time in milliseconds
//            request.setExpiration(new Date(new Date().getTime() + 86400000));
//            URL url = s3.generatePresignedUrl(request);
//            System.out.println("======================================");
//            System.out.println("Test Execution Report:");
//            System.out.println(url);
//            System.out.println("======================================");
//        } catch (AmazonServiceException e) {
//            System.err.println("Failed to upload report to S3");
//            System.err.println(e.getErrorMessage());
//        }
//
//
//    }
}