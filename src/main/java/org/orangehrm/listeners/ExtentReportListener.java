package org.orangehrm.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.orangehrm.factory.DriverFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;

public class ExtentReportListener implements ITestListener {

    private static final String REPORT_FOLDER = "./reports/";
    private static final String REPORT_FILE = "TestExecutionReport.html";

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    /**
     * Initialize ExtentReports
     */
    private static ExtentReports init() {

        Path reportPath = Paths.get(REPORT_FOLDER);

        try {
            Files.createDirectories(reportPath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create report directory", e);
        }

        ExtentSparkReporter sparkReporter =
                new ExtentSparkReporter(REPORT_FOLDER + REPORT_FILE);

        sparkReporter.config()
                .setReportName("Orange HRM Automation Test Results");

        ExtentReports extentReports = new ExtentReports();

        extentReports.attachReporter(sparkReporter);

        extentReports.setSystemInfo("System", "Windows");
        extentReports.setSystemInfo("Author", "Bhargav");
        extentReports.setSystemInfo("Build#", "1.1");
        extentReports.setSystemInfo("Team", "OMS");
        extentReports.setSystemInfo("Customer Name", "NAL");

        return extentReports;
    }

    static {
        extent = init();
    }

    @Override
    public void onStart(ITestContext context) {
        DriverFactory.log.info("Test Suite started!");
    }

    @Override
    public void onTestStart(ITestResult result) {

        String methodName = result.getMethod().getMethodName();

        DriverFactory.log.info(methodName + " started!");

        ExtentTest extentTest = extent.createTest(
                methodName,
                result.getMethod().getDescription()
        );

        extentTest.assignCategory(
                result.getTestContext().getSuite().getName()
        );

        test.set(extentTest);

        test.get()
                .getModel()
                .setStartTime(getTime(result.getStartMillis()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        DriverFactory.log.info(result.getMethod().getMethodName() + " passed!");
        if (test.get() != null) {
            try {
                String screenshotPath = new DriverFactory().getScreenshot();
                DriverFactory.log.info("Screenshot captured: " + screenshotPath);
                test.get().pass(
                        "Test passed",
                        MediaEntityBuilder
                                .createScreenCaptureFromPath(screenshotPath)
                                .build()
                );
            } catch (Exception e) {
                DriverFactory.log.error("Unable to capture screenshot", e);
                test.get().pass("Test passed");
            }
            test.get()
                    .getModel()
                    .setEndTime(getTime(result.getEndMillis()));
        }
    }


    @Override
    public void onTestFailure(ITestResult result) {

        DriverFactory.log.info(
                result.getMethod().getMethodName() + " failed!"
        );

        if (test.get() != null) {

            try {

                String screenshotPath =
                        new DriverFactory().getScreenshot();

                test.get().fail(
                        result.getThrowable(),
                        MediaEntityBuilder
                                .createScreenCaptureFromPath(screenshotPath)
                                .build()
                );

            } catch (Exception e) {

                test.get().fail(result.getThrowable());

                DriverFactory.log.error(
                        "Unable to attach screenshot",
                        e
                );
            }

            test.get()
                    .getModel()
                    .setEndTime(getTime(result.getEndMillis()));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {

        DriverFactory.log.info(
                result.getMethod().getMethodName() + " skipped!"
        );

        if (test.get() != null) {

            try {

                String screenshotPath =
                        new DriverFactory().getScreenshot();

                test.get().skip(
                        result.getThrowable(),
                        MediaEntityBuilder
                                .createScreenCaptureFromPath(screenshotPath)
                                .build()
                );

            } catch (Exception e) {

                test.get().skip(result.getThrowable());

                DriverFactory.log.error(
                        "Unable to attach screenshot",
                        e
                );
            }

            test.get()
                    .getModel()
                    .setEndTime(getTime(result.getEndMillis()));
        }
    }

    @Override
    public void onFinish(ITestContext context) {

        DriverFactory.log.info("Test Suite is ending!");

        /*
         * VERY IMPORTANT:
         * This writes the final HTML report.
         */
        extent.flush();

        DriverFactory.log.info(
                "Extent report generated at: "
                        + Paths.get(REPORT_FOLDER + REPORT_FILE)
                        .toAbsolutePath()
        );

        test.remove();
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        DriverFactory.log.info(
                "Test failed but within success percentage: "
                        + result.getMethod().getMethodName()
        );
    }

    private Date getTime(long millis) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(millis);

        return calendar.getTime();
    }
}
