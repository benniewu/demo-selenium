package com.cathay.test.selenium;

import java.net.URL;
import java.util.Arrays;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

public class WebDriverHelper {

    private static String driverRoot = "D:\\Selenium\\WebDriver\\";

    public static final String DRIVER_DEVELOP = "DEVELOP";

    public static final String DRIVER_LOCAL = "LOCAL";

    public static final String DRIVER_REMOTE = "REMOTE";

    public static final String BROWSER_IE = "IE";

    public static final String BROWSER_CHROME = "CHROME";

    public static final String BROWSER_FIREFOX = "FIREFOX";

    public static final String BROWSER_SAFARI = "SAFARI";

    public static final String BROWSER_EDGE = "EDGE";

    /**
     * ¾켹 IE WebDriver
     * @param browserType
     * @return
     * @throws Exception
     */
    public static WebDriver getWebDriver() throws Exception {
        return getWebDriver("IE");
    }

    /**
     * ¾켹 WebDriver
     * @param browserType
     * @return
     * @throws Exception
     */
    public static WebDriver getWebDriver(String browserType) throws Exception {

        browserType = browserType.toUpperCase();

        if (browserType.startsWith(BROWSER_IE)) {
            System.setProperty("webdriver.ie.driver", driverRoot + "IEDriverServer.exe");
            return new InternetExplorerDriver(getDesiredCapabilities(BROWSER_IE));
        } else if (BROWSER_CHROME.equals(browserType)) {
            System.setProperty("webdriver.chrome.driver", driverRoot + "chromedriver.exe");
            return new ChromeDriver(getDesiredCapabilities(BROWSER_CHROME));
        } else if (BROWSER_FIREFOX.equals(browserType)) {
            System.setProperty("webdriver.firefox.bin", "D:\\Selenium\\firefox-46.0.1.win64.sdk\\bin\\firefox.exe");
            return new FirefoxDriver(getDesiredCapabilities(BROWSER_FIREFOX));
        } else if (BROWSER_EDGE.equals(browserType)) {
            System.setProperty("webdriver.edge.bin", driverRoot + "MicrosoftWebDriver.exe");
            return new EdgeDriver(getDesiredCapabilities(BROWSER_EDGE));
        } else if (BROWSER_SAFARI.equals(browserType)) {
            return new SafariDriver(getDesiredCapabilities(BROWSER_SAFARI));
        } else {
            throw new Exception("Unsupported Browser Type : " + browserType);
        }
    }

    /**
     * ¾켹 Local Remote WebDriver
     * @param browserType
     * @return
     * @throws Exception
     */
    public static WebDriver getLocalWebDriver(String browserType, String localIp) throws Exception {

        DesiredCapabilities capability = getDesiredCapabilities(browserType.toUpperCase());
        return new RemoteWebDriver(new URL("http://" + localIp + ":4444/wd/hub"), capability);
    }

    /**
     * ¾켹 Remote WebDriver
     * @param browserType
     * @return
     * @throws Exception
     */
    public static WebDriver getRemoteWebDriver(String browserType, String remoteIp) throws Exception {

        DesiredCapabilities capability = getDesiredCapabilities(browserType.toUpperCase());
        if (browserType.startsWith(BROWSER_IE)) {
            return new RemoteWebDriver(new URL("http://" + remoteIp + ":4444/wd/hub"), capability);
        } else {
            return new RemoteWebDriver(new URL("http://" + remoteIp + ":4444/wd/hub"), capability);
        }
    }

    /**
     * ¾켹 WebDriver
     * @param browserType
     * @return
     * @throws Exception
     */
    public static WebDriver getWebDriver(String driverType, String browserType) throws Exception {
        return getWebDriver(driverType, null, browserType);
    }

    /**
     * ¾켹 WebDriver
     * @param browserType
     * @return
     * @throws Exception
     */
    public static WebDriver getWebDriver(String driverType, String remoteIp, String browserType) throws Exception {

        if (DRIVER_DEVELOP.equals(driverType.toUpperCase())) {
            return WebDriverHelper.getWebDriver(browserType);
        } else if (DRIVER_LOCAL.equals(driverType.toUpperCase())) {
            return WebDriverHelper.getLocalWebDriver(browserType, remoteIp);
        } else {
            return WebDriverHelper.getRemoteWebDriver(browserType, remoteIp);
        }
    }

    private static DesiredCapabilities getDesiredCapabilities(String browserType) throws Exception {

        DesiredCapabilities capability = null;

        if (browserType.startsWith(BROWSER_IE)) {
            capability = DesiredCapabilities.internetExplorer();
            capability.setPlatform(Platform.WINDOWS);
            capability.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
        } else if (BROWSER_CHROME.equals(browserType)) {
            capability = DesiredCapabilities.chrome();
            capability.setPlatform(Platform.WINDOWS);
            capability.setCapability("chrome.switches", Arrays.asList("--ignore-certificate-errors")); //
            capability.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

            ChromeOptions options = new ChromeOptions();
            options.addArguments("test-type"); // for chrome v36
            options.addArguments("start-maximized");
            options.addArguments("--js-flags=--expose-gc");
            options.addArguments("--enable-precise-memory-info");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-default-apps");
            options.addArguments("test-type=browser");
            options.addArguments("disable-infobars");
            capability.setCapability(ChromeOptions.CAPABILITY, options);
        } else if (BROWSER_FIREFOX.equals(browserType)) {
            capability = DesiredCapabilities.firefox();
        } else if (BROWSER_EDGE.equals(browserType)) {
            capability = DesiredCapabilities.edge();
        } else if (BROWSER_SAFARI.equals(browserType)) {
            capability = DesiredCapabilities.safari();
            capability.setBrowserName("safari");
            capability.setPlatform(org.openqa.selenium.Platform.ANY);
        } else {
            throw new Exception("Unsupported Browser Type : " + browserType);
        }

        return capability;
    }
}