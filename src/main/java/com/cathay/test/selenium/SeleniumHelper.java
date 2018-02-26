package com.cathay.test.selenium;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cathay.common.hr.WorkDate;
import com.cathay.common.im.util.VOTool;
import com.cathay.common.util.CathayDate;
import com.cathay.common.util.DATE;
import com.cathay.common.util.http.HttpClientHelper;

@SuppressWarnings("rawtypes")
public class SeleniumHelper {

    private final static Logger log = Logger.getLogger(SeleniumHelper.class);

    public static final String SYS_CSR = "CSR";

    public static final String SYS_IM = "IM";

    public static final String SYS_EH = "EH";

    public static final String SYS_B2E = "B2E";

    public static final String SYS_STAFF = "STAFF";

    public static final String ENV_PROD = "";

    public static final String ENV_STAG = "s";

    public static final String ENV_TEST = "t";

    public static final String[] IS_TRIGGER_ARR = new String[] { "IS_TRIGGER_Y", "IS_TRIGGER_N" };

    protected WebDriver driver;

    private int elementVisibleWait = 5;

    private List<WebElement> highlightElems = new ArrayList<WebElement>();

    //原視窗(切換視窗時使用)
    protected String parentHandle = "";

    //系統別
    protected String thisSys = "";

    //環境
    protected String thisEnv = "";

    //Login人員
    protected String thisLoginId = "";

    //是否為開發測試
    protected boolean isDevTest = false;

    //儲存Alert訊息
    protected String storedAlertMsg = "";

    public SeleniumHelper(WebDriver _driver) {
        driver = _driver;
    }

    /**
     * 取得是否為開發測試
     * @return
     */
    public boolean isDevTest() {
        return isDevTest;
    }

    /**
     * 設定是否為開發測試
     * @param isDevTest
     */
    public void setDevTest(boolean isDevTest) {
        this.isDevTest = isDevTest;
    }

    /**
     * 
     * @return
     */
    public String getStoredAlertMsg() {
        return storedAlertMsg;
    }

    /**
     * 操作步驟執行
     * @param stepType
     * @param stepArgs
     */
    public StepResult doStep(String stepType, List<String> stepArgs) {

        StepResult result = new StepResult();

        try {
            switch (stepType) {
                case "LOGIN":
                    this.login(stepArgs.get(0), stepArgs.get(1), stepArgs.get(2), stepArgs.get(3));
                    break;
                case "RELOGIN":
                    this.logout();
                    this.login(stepArgs.get(0), stepArgs.get(1), stepArgs.get(2), stepArgs.get(3));
                    break;
                case "SYS_FUNC":
                    this.enterSys(stepArgs.get(0));
                    if (stepArgs.size() == 3) {
                        this.enterFunction(stepArgs.get(1), stepArgs.get(2));
                    } else {
                        this.enterFunctionBySeq(stepArgs.get(1), stepArgs.get(2), stepArgs.get(3));
                    }

                    break;
                case "SYS":
                    this.enterSys(stepArgs.get(0));
                    break;
                case "FUNC":
                    this.enterFunction(stepArgs.get(0), stepArgs.get(1));
                    break;
                case "REGEN_DATA":
                    this.reGenData(stepArgs.get(0));
                    break;
                case "ID_CHG":
                    this.changeID(stepArgs.get(0));
                    break;
                case "INPUT":
                    if (stepArgs.size() == 2) {
                        this.keyinById(stepArgs.get(0), formatInputValue(stepArgs.get(1)), "");
                    } else if (stepArgs.size() == 3) {
                        if (ArrayUtils.contains(IS_TRIGGER_ARR, stepArgs.get(2))) {
                            this.keyinById(stepArgs.get(0), formatInputValue(stepArgs.get(1)), stepArgs.get(2));
                        } else {
                            this.keyinByName(stepArgs.get(0), stepArgs.get(1), formatInputValue(stepArgs.get(2)), "");
                        }
                    } else {
                        this.keyinByName(stepArgs.get(0), stepArgs.get(1), formatInputValue(stepArgs.get(2)), stepArgs.get(3));
                    }
                    break;
                case "DATE_INPUT":
                    String inputDate = "";
                    if (stepArgs.size() == 5) {
                        inputDate = new StringBuilder().append(stepArgs.get(1)).append(stepArgs.get(2)).append(stepArgs.get(3)).toString();
                        this.keyinById(stepArgs.get(0), formatInputValue(inputDate), stepArgs.get(4));

                    } else if (stepArgs.size() == 6) {
                        inputDate = new StringBuilder().append(stepArgs.get(2)).append(stepArgs.get(3)).append(stepArgs.get(4)).toString();
                        this.keyinByName(stepArgs.get(0), stepArgs.get(1), formatInputValue(inputDate), stepArgs.get(5));
                    }
                    break;
                case "HIDE_INPUT":
                    if (stepArgs.size() == 2) {
                        this.setValByHideId(stepArgs.get(0), formatInputValue(stepArgs.get(1)), "");
                    } else if (stepArgs.size() == 3) {
                        if (ArrayUtils.contains(IS_TRIGGER_ARR, stepArgs.get(2))) {
                            this.setValByHideId(stepArgs.get(0), formatInputValue(stepArgs.get(1)), stepArgs.get(2));
                        } else {
                            this.setValByHideName(stepArgs.get(0), stepArgs.get(1), formatInputValue(stepArgs.get(2)), "");
                        }
                    } else {
                        this.setValByHideName(stepArgs.get(0), stepArgs.get(1), formatInputValue(stepArgs.get(2)), stepArgs.get(3));
                    }
                    break;
                case "TEXTAREA":
                    this.keyinById(stepArgs.get(0), stepArgs.get(1), "");
                    break;
                case "BUTTON":
                    if (isDevTest) {
                        result.setBeforeScreenshotImg(saveScreenshot());
                    }
                    if (stepArgs.size() == 1) {
                        this.clickById(stepArgs.get(0));
                    } else {
                        this.clickByName(stepArgs.get(0), stepArgs.get(1));
                    }
                    waitTime(2000);
                    //TimeUnit.SECONDS.sleep(Integer.parseInt(STEP_WAIT)); //暫定為按下按鈕的都先等待
                    if (isDevTest) {
                        waitTime(2000);
                        captureAlertMsg();
                        log.fatal("storedAlertMsg >>>>> " + storedAlertMsg);
                        if (StringUtils.isNotBlank(storedAlertMsg)) {
                            confirmAlert();
                        }
                        result.setAfterScreenshotImg(saveScreenshot());
                    }
                    break;
                case "CLICK":
                    if (isDevTest) {
                        result.setBeforeScreenshotImg(saveScreenshot());
                    }
                    if (stepArgs.size() == 1) {
                        this.clickScriptById(stepArgs.get(0));
                    } else {
                        this.clickScriptByName(stepArgs.get(0), stepArgs.get(1));
                    }
                    waitTime(2000);
                    //TimeUnit.SECONDS.sleep(Integer.parseInt(STEP_WAIT)); //暫定為按下按鈕的都先等待
                    if (isDevTest) {
                        waitTime(2000);
                        captureAlertMsg();
                        log.fatal("storedAlertMsg >>>>> " + storedAlertMsg);
                        if (StringUtils.isNotBlank(storedAlertMsg)) {
                            confirmAlert();
                        }
                        result.setAfterScreenshotImg(saveScreenshot());
                    }
                    break;
                case "SELECT":
                    if (stepArgs.size() == 2) {
                        this.selectTextById(stepArgs.get(0), stepArgs.get(1));
                    } else {
                        this.selectValueById(stepArgs.get(0), stepArgs.get(1));
                    }
                    break;
                case "RADIO":
                    if (stepArgs.size() == 1) {
                        this.checkRadioById(stepArgs.get(0));
                    } else {
                        this.checkRadioByName(stepArgs.get(0), stepArgs.get(1));
                    }
                    break;
                case "CHECKBOX":
                    if (stepArgs.size() == 1) {
                        this.checkRadioById(stepArgs.get(0));
                    } else {
                        this.checkRadioByName(stepArgs.get(0), stepArgs.get(1));
                    }
                    break;
                case "LINK":
                    this.clickLinkByText(stepArgs.get(0));
                    break;
                case "POPUP":
                    this.switchToPopupWin();
                    break;
                case "WINDOW":
                    this.switchToWindow();
                    break;
                case "CLOSE_WIN":
                    this.closeWindow();
                    break;
                case "BACK_WIN":
                    this.backToParentWin();
                    break;
                case "IFRAME":
                    this.switchToFrame(stepArgs.get(0));
                    break;
                case "MAINFRAME":
                    this.switchToMainFrame();
                    break;
                case "ALERT":
                    this.confirmAlert();
                    break;
                case "CONFIRM":
                    this.chooseConfirm(stepArgs.get(0));
                    break;
                case "WAIT":
                    this.waitTime(Long.valueOf(stepArgs.get(0)));
                    break;
            }

            result.setResultCode(StepResult.RESULT_SUCCESS);
        } catch (Exception e) {
            log.error("步驟執行發生錯誤", e);
            result.setResultCode(StepResult.RESULT_EXCEPTION);
            result.setExceptionMsg(e.getMessage());
            result.setScreenshotImg(saveScreenshot());
        }

        return result;
    }

    /**
     * 登入
     * @param sys
     * @param env
     * @param loginId
     */
    public void login(String sys, String env, String loginId) {
        login(sys, env, loginId, loginId);
    }

    /**
     * 登入
     * @param sys
     * @param env
     * @param loginId
     * @param loginPw
     * @return 
     */
    public void login(String sys, String env, String loginId, String loginPw) {

        thisSys = sys;
        thisEnv = env;
        thisLoginId = loginId;

        if (isDevTest) {
            driver.manage().window().setSize(new Dimension(1024, 768));
        } else {
            driver.manage().window().maximize();
        }
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        StringBuilder loginUrl = new StringBuilder();

        loginUrl.append("https://").append(env);

        if (SYS_CSR.equals(sys)) {
            loginUrl.append("was3.cathaylife.com.tw/CXLWeb/");
        } else if (SYS_IM.equals(sys)) {
            loginUrl.append("im.cathaylife.com.tw/IMWeb/");
        } else if (SYS_EH.equals(sys)) {
            loginUrl.append("staff.cathaylife.com.tw/EHWeb/login?sys=EH");
        } else if (SYS_B2E.equals(sys)) {
            driver.get(loginUrl.toString() + "w3.cathaylife.com.tw/eai/ZPWeb/login.jsp?");

            keyinById("UID", loginId, "");
            keyinById("KEY", loginPw, "");
            driver.findElement(By.id("btnLogin")).click();

            loginUrl.append("w3.cathaylife.com.tw/eai/CXPWeb");
            driver.get(loginUrl.toString());
            return;
        } else if (SYS_STAFF.equals(sys)) {
            loginUrl.append("staff.cathaylife.com.tw/CMWeb/");
        }

        driver.get(loginUrl.toString());
        keyinById("userID", loginId, "");
        keyinById("passWD", loginPw, "");
        driver.findElement(By.id("btn_submit")).click();
    }

    /**
     * 進入系統 
     * @param sysCht 系統名稱 ex:保全系統
     * @throws InterruptedException 
     * @throws Exception
     */
    public void enterSys(String sysCht) {
        log.error("****** 進入系統: " + sysCht + " ******");
        try {
            driver.switchTo().frame(getElement(By.name("leftFrame"), "左側iFrame"));
        } catch (Exception e) {
            log.error("進入子系統時 找不到左側選單 先回主畫面再RETRY");
            //等個3秒,For IM 多國語系(上方選單 load完)
            waitTime(3000);
            enterMainMenu();
            driver.switchTo().frame(getElement(By.name("leftFrame"), "左側iFrame"));
        }

        WebElement elem = getElement(By.xpath("//li[contains(text(), '" + sysCht + "')]"), "系統名稱" + sysCht);
        //elem 試3次
        for (int i = 1; i < 3; i++) {
            log.error("****** elem 試第: " + i + " 次******");
            if (elem.isDisplayed()) {
                try {
                    elem.click();
                } catch (TimeoutException e) {
                    log.error("****** Timeout視為正常,系統選單: " + sysCht + " ;TimeoutException: " + e);
                    //再等個3秒, 讓page load.
                    waitTime(3000);
                    continue;
                }
                break;
            } else {
                log.error("****** 系統: " + sysCht + " ,第" + i + "次沒出現, wait 3秒鐘 ******");
                waitTime(3000);
            }
        }
        waitTime(5000);
    }

    /**
     * 進入子系統>進入指定頁面>跳至主frame
     * @param subSysNm 子系統分類 
     * @param funcId 頁面名稱 
     * @throws Exception 
     */
    public void enterFunction(String subSysNm, String funcNm) {

        log.error("****** subSysNm: " + subSysNm + " ;funcNm: " + funcNm + " ******");
        try {
            driver.switchTo().frame(getElement(By.name("leftFrame"), "左側iFrame"));
        } catch (NoSuchElementException nsee) {
            //等個3秒重試.
            log.error("****** switchTo左側iFrame 3秒重試, NoSuchElementException: " + nsee);
            waitTime(3000);
            driver.switchTo().frame(getElement(By.name("leftFrame"), "左側iFrame"));
        } catch (TimeoutException toe) {
            //等個3秒重試.
            log.error("****** switchTo左側iFrame 3秒重試, TimeoutException: " + toe);
            waitTime(3000);
            driver.switchTo().frame(getElement(By.name("leftFrame"), "左側iFrame"));
        }

        WebElement menuDiv = getElement(By.cssSelector("li.folder[title*='" + subSysNm + "'] > div"), "子系統選單" + subSysNm);
        //menuDiv 試3次
        for (int i = 1; i < 3; i++) {
            log.error("****** menuDiv 試第: " + i + " 次******");
            if (menuDiv.isDisplayed()) {
                menuDiv.click();
                break;
            } else {
                log.error("****** menuDiv 第" + i + "次沒出現, wait 3秒鐘 ******");
                waitTime(3000);
            }
        }

        WebElement menuLink = null;
        try {
            menuLink = getElement(By.xpath("//li[contains(@title, '" + subSysNm + "')]/ul/li/a[text()='" + funcNm + "']"), "功能選單" + funcNm);
        } catch (NoSuchElementException nsee) {
            //等個3秒重試.
            log.error("****** menuLink 3秒重試, NoSuchElementException: " + nsee);
            waitTime(3000);
            menuLink = getElement(By.xpath("//li[contains(@title, '" + subSysNm + "')]/ul/li/a[contains(text(), '" + funcNm + "')]"),
                "功能選單" + funcNm);
        } catch (TimeoutException toe) {
            //等個3秒重試.
            log.error("****** menuLink 3秒重試, TimeoutException: " + toe);
            waitTime(3000);
            menuLink = getElement(By.xpath("//li[contains(@title, '" + subSysNm + "')]/ul/li/a[contains(text(), '" + funcNm + "')]"),
                "功能選單" + funcNm);
        }
        //menuLink 試3次
        for (int i = 1; i < 3; i++) {
            log.error("****** menuLink 試第: " + i + " 次******");
            if (menuLink.isDisplayed()) {
                try {
                    menuLink.click();
                } catch (TimeoutException e) {
                    log.error("****** Timeout視為正常,功能選單: " + funcNm + " ;TimeoutException: " + e);
                    //再等個3秒, 讓page load.
                    waitTime(3000);
                    continue;
                }
                break;
            } else {
                log.error("****** menuLink 第" + i + "次沒出現, wait 3秒鐘 ******");
                waitTime(3000);
            }
        }

        try {
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (Exception e) {
            log.error("初始沒有alert視窗 ，繼續執行....");
        }

        driver.switchTo().frame(getElement(By.id("mainFrame"), "主要iFrame"));
    }

    /**
     * 進入子系統>進入指定頁面(第seq個)>跳至主frame
     * @param subSysNm
     * @param funcNm
     * @param seq
     */
    public void enterFunctionBySeq(String subSysNm, String funcNm, String seq) {

        driver.switchTo().frame(getElement(By.name("leftFrame"), "左側iFrame"));

        WebElement menuDiv = getElement(By.cssSelector("li.folder[title*='" + subSysNm + "'] > div"), "子系統選單" + subSysNm);
        menuDiv.click();

        WebElement menuLink = null;
        try {
            List<WebElement> menuLinks = driver.findElements(By.xpath("//li[contains(@title, '" + subSysNm + "')]/ul/li/a[text()='"
                    + funcNm + "']"));
            menuLink = menuLinks.get(Integer.parseInt(seq) - 1);
            //menuLink = getElement(By.xpath("//li[contains(@title, '" + subSysNm + "')]/ul/li/a[text()='" + funcNm + "']"), "功能選單" + funcNm);
        } catch (Exception e) {
            String errMsg = new StringBuilder().append("無法於").append(funcNm).append("找到第").append(seq).append("個選單").toString();
            log.error(errMsg, e);
            throw new NoSuchElementException(errMsg);
        }
        menuLink.click();

        try {
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (Exception e) {
            log.error("初始沒有alert視窗 ，繼續執行....");
        }

        driver.switchTo().frame(getElement(By.id("mainFrame"), "主要iFrame"));
    }

    /**
     * 回到主選單
     */
    public void enterMainMenu() {

        //mainFrame<<<<<leftFrame
        driver.switchTo().parentFrame();
        //leftFrame<<<<<frameset
        driver.switchTo().parentFrame();
        //frameset>>>>>>topFrame
        driver.switchTo().frame(getElement(By.name("topFrame"), "上方選單"));
        By by = By.xpath("//ul[@id='menuBar']/li/a[contains(text(), '主選單')]");
        try {
            WebElement elem = getElement(by, "回首頁按鈕");
            elem.click();
        } catch (NoSuchElementException nsee) {
            log.info("無法找到回首頁按鈕，視為正常");
        }

        driver.switchTo().parentFrame();
    }

    /**
     * 登出
     */
    public void logout() {

        //mainFrame<<<<<leftFrame
        driver.switchTo().parentFrame();
        //leftFrame<<<<<frameset
        driver.switchTo().parentFrame();
        //frameset>>>>>>topFrame
        driver.switchTo().frame(getElement(By.name("topFrame"), "上方選單"));
        By by = By.xpath("//ul[@id='menuBar']/li/a[contains(text(), '登出')]");
        try {
            WebElement elem = getElement(by, "登出");
            elem.click();
        } catch (NoSuchElementException nsee) {
            log.info("無法找到登出按鈕，視為正常");
        }

        driver.switchTo().parentFrame();
    }

    /**
     * reGenData
     * @param caseNo
     * @throws Exception 
     */
    public void reGenData(String caseNo) throws Exception {

        try {
            log.info("環境 >>>>>>>> " + thisEnv);
            log.info("平台 >>>>>>>> " + thisSys);
            log.info("測試案例登入者 >>>>>>>> " + thisLoginId);

            //"https://twas3.cathaylife.com.tw/ZSWeb/api/app/exportData?CASE_NOs=" + caseNO + "&empId=" + thisLoginId;
            StringBuilder urlSb = new StringBuilder();
            urlSb.append("https://");
            if ("T".equals(thisEnv) || "S".equals(thisEnv)) {
                urlSb.append(thisEnv.toLowerCase());
                if (SYS_CSR.equals(thisSys)) {
                    urlSb.append("was3");
                } else if (SYS_IM.equals(thisSys)) {
                    urlSb.append("im");
                } else {
                    log.error("資料再生有誤, 非 CSR/IM 系統!");
                    return;
                }
            } else {
                log.error("非測試或平測環境, 無法執行資料再生! ");
                return;
            }
            urlSb.append(".cathaylife.com.tw/ZSWeb/api/app/exportData");
            String url = urlSb.toString();
            log.info("資料再生 WebService URL >>>>>>>> " + url);

            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("CASE_NOs", caseNo);
            paramMap.put("empId", thisLoginId);
            log.info("資料再生 WebService 參數 >>>>>>>> " + paramMap);

            HttpClientHelper helper = new HttpClientHelper();
            String result = helper.getHttpResponseAsString(url, paramMap, "utf-8");
            log.info("資料再生結果 >>>>>>>> " + result);
            Map resultMap = VOTool.jsonToMap(result);
            int returnCode = MapUtils.getIntValue(resultMap, "returnCode");
            log.error("returnCode >>>>>>>> " + returnCode);

            if (returnCode != 0) {
                throw new Exception(MapUtils.getString(resultMap, "detail"));
            }

        } catch (Exception e) {
            log.error("資料再生 WebService 呼叫失敗", e);
            throw e;
        }
    }

    /**
     *   
     * @param switchId
     */
    public void changeID(String switchId) {

        log.info("switchId >>>>>>>> " + switchId);
        waitTime(1000);
        enterSys("系統管理");
        waitTime(1000);
        enterFunction("平測人員專區", "使用者切換");
        waitTime(1000);
        WebElement switchID = getElement(By.name("switch_id"), "輸入切換ID" + switchId);
        switchID.clear();
        switchID.sendKeys(switchId);
        waitTime(1000);
        clickById("btnConfirm");
    }

    /**
     * 在ID或Name對應的元素輸入文字
     * @param id
     * @param context
     */
    public void keyinById(String id, String context) {
        keyinById(id, context, StringUtils.EMPTY);
    }

    /**
     * 在ID或Name對應的元素輸入文字
     * @param id
     * @param context
     * @param isTrigger
     */
    public void keyinById(String id, String context, String isTrigger) {

        WebElement elem = null;
        try {
            elem = getElement(By.id(id), "輸入欄位" + id);
        } catch (NoSuchElementException ne) {
            elem = getElement(By.name(id), "輸入欄位" + id);
        }

        //先用退格鍵 以免欄位自動觸發onChange事件       
        int text_size = elem.getAttribute("value").length();
        try {
            for (int i = 0; i < text_size; i++) {
                elem.sendKeys(Keys.BACK_SPACE);
            }
        } catch (Exception e) {
            log.fatal("BACK_SPACE退格有誤 改用clear", e);
            elem.clear();
        }
        elem.sendKeys(context);
        // 解決RB 即期匯率欄位輸入後清空問題，用Tab觸發連動事件
        if ("IS_TRIGGER_Y".equals(isTrigger)) {
            elem.sendKeys(Keys.TAB);
            waitTime(2000);
        }

    }

    /**
     * set隱藏欄位值by id
     * @param name
     * @param idx
     * @param context
     * @param isTrigger
     */
    public void setValByHideId(String id, String context, String isTrigger) {

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("return document.getElementById('" + id + "').value = '" + context + "';");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new NoSuchElementException("未找到id為" + id + "的元件", e);
        }
    }

    /**
     * set隱藏欄位值by name
     * @param name
     * @param idx
     * @param context
     * @param isTrigger
     */
    public void setValByHideName(String name, String idx, String context, String isTrigger) {
        waitTime(500);
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("return document.getElementsByName('" + name + "')[" + idx + "].value = '" + context + "';");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new NoSuchElementException("未找到name為" + name + "的元件", e);
        }

    }

    /**
     * Name對應的元素輸入文字
     * @param name
     * @param idx
     * @param context
     */
    public void keyinByName(String name, String idx, String context, String isTrigger) {
        waitTime(500);
        List<WebElement> elems = driver.findElements(By.name(name));
        if (elems.size() == 0) {
            throw new NoSuchElementException("無法找到" + name);
        } else {
            WebElement elem = elems.get(Integer.parseInt(idx));

            //先用退格鍵 以免欄位自動觸發onChange事件       
            int text_size = elem.getAttribute("value").length();
            try {
                for (int i = 0; i < text_size; i++) {
                    elem.sendKeys(Keys.BACK_SPACE);
                }
            } catch (Exception e) {
                log.fatal("BACK_SPACE退格有誤 改用clear", e);
                elem.clear();
            }
            elem.sendKeys(context);
            // 解決RB 即期匯率欄位輸入後清空問題，用Tab觸發連動事件
            if ("IS_TRIGGER_Y".equals(isTrigger)) {
                elem.sendKeys(Keys.TAB);
            }
        }

    }

    /**
     * 在ID對應的下拉選單元素選擇項目 (文字)
     * 
     * @param id
     * @param context
     */
    public void selectTextById(String id, String text) {

        WebElement elem = null;
        try {
            elem = getElement(By.id(id), "下拉選單" + id);
        } catch (NoSuchElementException ne) {
            elem = getElement(By.name(id), "下拉選單" + id);
        }
        new Select(elem).selectByVisibleText(text);
    }

    /**
     * 在ID對應的下拉選單元素選擇項目 (值)
     * @param id
     * @param val
     */
    public void selectValueById(String id, String val) {

        WebElement elem = null;
        try {
            elem = getElement(By.id(id), "下拉選單" + id);
        } catch (NoSuchElementException ne) {
            elem = getElement(By.name(id), "下拉選單" + id);
        }
        new Select(elem).selectByValue(val);
    }

    /**
     * 對ID對應的單選項目點選
     * 
     * @param id
     */
    public void checkRadioById(String id) {

        clickById(id);
    }

    /**
     * 對name對應checkBox項目點選
     * @param id
     */
    public void checkRadioByName(String name, String idx) {

        clickByName(name, idx);
    }

    /**
     * 對ID對應checkBox項目選取
     * @param id
     */
    public void checkBoxById(String id) {

        clickById(id);
    }

    /**
     * 對name對應的單選項目點選
     * @param id
     */
    public void checkBoxByName(String name, String idx) {

        clickByName(name, idx);
    }

    public void clickByName(String name, String idx) {

        waitTime(500);

        List<WebElement> elems = driver.findElements(By.name(name));
        if (elems.size() == 0) {
            throw new NoSuchElementException("無法找到" + name);
        } else {
            WebElement elem = elems.get(Integer.parseInt(idx));
            elem.click();
        }
    }

    /**
     * 對文字對應的單選項目點選
     * 
     * @param text
     */
    public void checkRadioByText(String text) {

        By by = By.xpath("//input/following-sibling::text()[contains(., '" + text + "')]/preceding-sibling::input");
        WebElement elem = getElement(by, text + "對應單選項目");
        elem.click();
    }

    /**
     * 對ID對應的單選項目點選
     * 
     * @param id
     */
    public void clickById(String id) {

        WebElement elem = null;
        if ("MP".equals(thisSys) && "query".equals(id)) { //MP的query暫無ID 先用XPATH代替
            driver.findElement(By.xpath("//*[@id='right11']/div/div[1]/div[8]/a")).click();
            return;
        }

        try {
            elem = getElement(By.id(id), "欲點選元件" + id);
        } catch (Exception ne) {
            elem = getElement(By.name(id), "欲點選元件" + id);
        }

        if (!isDevTest && "查詢".equals(elem.getAttribute("value"))) {
            elem.click();
            waitTime(3000);
            confirmAlert();
        } else {
            elem.click();
        }
    }

    /**
     * 使用javascript點選id
     * @param id
     */
    public void clickScriptById(String id) {
        WebElement elem = null;
        if ("MP".equals(thisSys) && "query".equals(id)) { //MP的query暫無ID 先用XPATH代替
            driver.findElement(By.xpath("//*[@id='right11']/div/div[1]/div[8]/a")).click();
            return;
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            js.executeScript("return document.getElementById('" + id + "').click();");
            elem = getElement(By.id(id), "欲點選元件" + id);
        } catch (Exception ne) {
            js.executeScript("return document.getElementsByName('" + id + "')[0].click();");
            elem = getElement(By.name(id), "欲點選元件" + id);
        }

        if (!isDevTest && "查詢".equals(elem.getAttribute("value"))) {
            elem.click();
            waitTime(3000);
            confirmAlert();
        } else {
            elem.click();
        }
    }

    /**
     * 使用javascript點選name
     * @param id
     */
    public void clickScriptByName(String id, String idx) {

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("return document.getElementsByName('" + id + "')[" + idx + "].click();");

    }

    /**
     * 對name對應的單選項目點選
     * @param id
     */
    public void clickByName(String name) {

        WebElement elem = getElement(By.name(name), "欲點選元件" + name);
        elem.click();
    }

    /**
     * 對文字對應的按鈕點擊
     * 
     * @param id
     * @param context
     */
    public void clickBtnByText(String text) {

        WebElement elem = null;

        try {
            By by = By.cssSelector("input[value='" + text + "']");
            elem = getElement(by, "欲點選元件" + text);
        } catch (TimeoutException e) {
            By by = By.xpath("//button[contains(text(), '" + text + "')]");
            elem = getElement(by, "欲點選元件" + text);
        }

        if (elem == null) {
            log.info("Can't get button by text full match : " + text);
            throw new NoSuchElementException("Unable to find button element with text == " + text);
        } else {
            try {
                elem.sendKeys(Keys.ENTER);
            } catch (Exception e) {
                elem.click();
            }
        }
    }

    /**
     * 切換到popupWin
     */
    public void switchToPopupWin() {
        //driver.switchTo().parentFrame();
        //driver.switchTo().frame(getElement(By.id("mainFrame"), "主要iFrame"));
        WebElement popup = getElement(By.xpath("//*[contains(@id, 'popupwin_')]"), "彈出視窗");
        driver.switchTo().frame(popup);
    }

    /**
     * 切換至新視窗
     */
    public void switchToWindow() {
        //將原視窗記起來
        parentHandle = driver.getWindowHandle();
        //切換到最後一個視窗
        for (String winHandle : driver.getWindowHandles()) {
            driver.switchTo().window(winHandle);
        }
    }

    /**
     * 關閉當下視窗
     */
    public void closeWindow() {
        driver.close();
    }

    /**
     * 回到原視窗
     */
    public void backToParentWin() {
        if (StringUtils.isBlank(parentHandle)) {
            return;
        }
        driver.switchTo().window(parentHandle);
        driver.switchTo().parentFrame();
        driver.switchTo().frame(getElement(By.name("leftFrame"), "左側iFrame"));
        driver.switchTo().frame(getElement(By.id("mainFrame"), "主要iFrame"));
    }

    /**
     * 切換至指定iFrame
     * @param id
     */
    public void switchToFrame(String id) {
        driver.switchTo().defaultContent();
        driver.switchTo().frame(getElement(By.name("leftFrame"), "左側iFrame"));
        driver.switchTo().frame(getElement(By.id("mainFrame"), "主要iFrame"));
        driver.switchTo().frame(getElement(By.id(id), "指定iFrame"));

    }

    /**
     * 切回原iframe
     */
    public void switchToMainFrame() {
        driver.switchTo().defaultContent();
        driver.switchTo().frame(getElement(By.name("leftFrame"), "左側iFrame"));
        driver.switchTo().frame(getElement(By.id("mainFrame"), "主要iFrame"));
    }

    public void clickLinkByText(String text) {

        List<WebElement> linkTextList = driver.findElements(By.linkText(text));
        if (linkTextList.size() == 0) {
            log.info("Can't get link by text full match : " + text);
        } else {
            linkTextList.get(0).click();
            return;
        }

        List<WebElement> xpathLinkTextList = driver.findElements(By.xpath("//a[contains(text(), '" + text + "')]"));
        if (xpathLinkTextList.size() == 0) {
            log.info("Can't get link by xpath text full match : " + text);
        } else {
            xpathLinkTextList.get(0).click();
            return;
        }

        List<WebElement> linkTitleList = driver.findElements(By.xpath("a[title='" + text + "']"));
        if (linkTitleList.size() == 0) {
            log.info("Can't get link by title full match : " + text);
        } else {
            linkTitleList.get(0).click();
            return;
        }

        List<WebElement> partialLinkTextList = driver.findElements(By.partialLinkText(text));
        if (partialLinkTextList.size() == 0) {
            log.info("Can't get link by title full match : " + text);
        } else {
            partialLinkTextList.get(0).click();
            return;
        }

        throw new NoSuchElementException("Unable to find link element with text == " + text);
    }

    /**
     * alert視窗確定
     */
    public void confirmAlert() {

        try {
            //CSRUtil產生的alert()
            clickBtnByText("確定");
        } catch (Exception e) {
            int i = 0;
            Alert alert = null;
            //若發生錯誤>>瀏覽器原生的alert
            do {
                try {
                    alert = driver.switchTo().alert();
                    alert.accept();
                    break;
                } catch (NoAlertPresentException ne) {

                    waitTime(500);
                    log.info("alert not found, wait for 500 ms and try again");
                    continue;
                }
            } while (i++ < 5);
        }
    }

    /**
     * Confirm視窗
     * @param type  Y/N
     */
    public void chooseConfirm(String type) {
        int i = 0;
        Alert alert = null;
        do {
            try {
                alert = driver.switchTo().alert();
                if ("Y".equals(type)) {
                    alert.accept();
                    break;
                } else {
                    alert.dismiss();
                    break;
                }
            } catch (NoAlertPresentException ne) {

                waitTime(500);
                log.info("alert not found, wait for 500 ms and try again");
                continue;
            }
        } while (i++ < 5);
    }

    public void skipAlert() {
        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException e) {

        }
    }

    /**
     * 等待時間
     * @param time
     */
    public void waitTime(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log.error("Thread sleep error", e);
        }
    }

    /**
     * confirm視窗確定
     */
    public void confirmDelete(String coonfirm_Delete) {
        int i = 0;
        Alert alert = null;
        log.info("***coonfirm_Delete: " + coonfirm_Delete);

        //若發生錯誤>>瀏覽器原生的alert
        do {
            try {
                alert = driver.switchTo().alert();

                if ("Y".equals(coonfirm_Delete)) {
                    alert.accept();
                    break;
                } else {
                    alert.dismiss();
                    break;
                }

            } catch (NoAlertPresentException ne) {
                waitTime(500);
                log.info("alert not found, wait for 500 ms and try again");
                log.info("", ne);
                continue;
            }
        } while (i++ < 2);
    }

    /**
     * 取得WebElement
     * @param by
     * @return
     * @throws NoSuchElementException 找不到元件
     * @throws TimeoutException 等待時間過長
     */
    public WebElement getElement(By by, String eleName) throws NoSuchElementException, TimeoutException {

        WebElement elem = null;
        try {
            elem = new WebDriverWait(driver, elementVisibleWait).until(ExpectedConditions.visibilityOfElementLocated(by));
        } catch (TimeoutException te) {

            if (te.getCause() instanceof NoSuchElementException) {
                log.info("無法找到 " + eleName);
                throw new NoSuchElementException("無法找到 " + eleName);
            }
            log.info(eleName + " 等待時間過長");
            throw new TimeoutException(eleName + " 等待時間過長");
        }
        if (elem == null) {
            log.info("無法找到 " + eleName);
            throw new NoSuchElementException("無法找到 " + eleName);
        }

        return elem;
    }

    /**
     * 取得底部訊息欄
     * @return
     * @throws Exception
     */
    protected WebElement getIMPopupElem() {

        waitTime(1000);
        driver.switchTo().parentFrame();
        driver.switchTo().frame(getElement(By.id("mainFrame"), "切換回mainFrame"));
        return getElement(By.id("msg-win_content"), "IM POPUP訊息");
    }

    /**
     * 取得底部訊息欄
     * @return
     * @throws Exception
     */
    protected WebElement getBottomMessageElem() {

        waitTime(1000);
        driver.switchTo().parentFrame();
        driver.switchTo().frame(getElement(By.id("bottomFrame"), "切換至 bottomFrame"));
        return getElement(By.id("cathay_common_msgBoard"), "底部訊息欄");
    }

    /**
     * 抓取Alert視窗訊息
     */
    public void captureAlertMsg() {

        storedAlertMsg = null;

        int i = 0;
        Alert alert = null;
        do {
            try {
                alert = driver.switchTo().alert();
                storedAlertMsg = replaceWrap(alert.getText());
                break;
            } catch (NoAlertPresentException ne) {
                waitTime(500);
                log.info("alert not found, wait for 500 ms and try again");
            }
        } while (i++ < 5);
    }

    /**
     * 去掉驗證文字的換行
     * @return String
     */
    private String replaceWrap(String str) {
        return str.replaceAll("(\r\n|\n\r|\r|\n)", " ");
    }

    /**
     * 強調元素
     * @param elem
     */
    @SuppressWarnings("unused")
    private void highLightElem(WebElement elem) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid red'", elem);
        highlightElems.add(elem);
    }

    /**
     * 取消強調當前強調元素
     */
    @SuppressWarnings("unused")
    private void unhighLightLastElems() {
        try {
            for (WebElement elem : highlightElems) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='0px'", elem);
            }
        } catch (StaleElementReferenceException sere) {
            // the page got reloaded, the element isn't there
        } finally {
            highlightElems.clear();
        }
    }

    /**
     * 執行中畫面擷圖存檔
     * @return
     */
    public File saveScreenshot() {

        File rtnFile = null;
        try {
            rtnFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        } catch (UnhandledAlertException uae) {
            try {
                File temp = File.createTempFile("temp_" + System.currentTimeMillis(), ".jpg");
                Robot robot = new Robot();
                Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage capture = robot.createScreenCapture(captureSize);
                ImageIO.write(capture, "jpg", temp);
            } catch (Exception e) {
                log.fatal("擷圖失敗", e);
            }
        } catch (Exception e) {
            log.fatal("擷圖失敗", e);
        }
        return rtnFile;
    }

    /**
     * format input內容
     * @param arg
     * @return
     * @throws ParseException 
     */
    private String formatInputValue(String arg) throws ParseException {
        if (StringUtils.isBlank(arg)) {
            return "";
        }

        String DB_DATE_MARK = "@{DB_DATE}";
        String ROC_DB_DATE_MARK = "@{ROC_DB_DATE}";
        String DB_TIMESTAMP_MARK = "@{DB_TIMESTAMP}";
        String SHUTDOWNDAY = "@{SHUTDOWNDAY}";
        if (arg.contains(DB_DATE_MARK)) {
            return calDate(DB_DATE_MARK, arg);
        } else if (arg.contains(SHUTDOWNDAY)) {
            return calDate(SHUTDOWNDAY, arg);
        } else if (arg.contains(ROC_DB_DATE_MARK)) {
            return DATE.formatToROCDate(calDate(ROC_DB_DATE_MARK, arg));
        } else if (arg.contains(DB_TIMESTAMP_MARK)) {
            return calTimeStamp();
        } else {
            return arg;
        }
    }

    /**
     * 計算系統日期+-天數
     * @param MARK
     * @param arg
     * @return
     * @throws ParseException 
     */
    private String calDate(String MARK, String arg) throws ParseException {

        String strAddDay = arg.replace(MARK, "");
        strAddDay = StringUtils.isBlank(strAddDay) ? "0" : strAddDay.trim();
        String DBDate = DATE.getDBDate();
        if (MARK.contains("@{SHUTDOWNDAY}")) {
            DBDate = new CathayDate().getShutdownDay();
        }
        //有指定是工作日
        if (strAddDay.endsWith("WD")) {
            strAddDay = strAddDay.replace("WD", "");
            boolean theDIR = true;
            if (strAddDay.startsWith("-")) {
                theDIR = false;
                strAddDay = strAddDay.replace("-", "");
            } else {
                strAddDay = strAddDay.replace("+", "");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date today = sdf.parse(DBDate);
            return sdf.format(WorkDate.getXWorkingDate(today, Integer.parseInt(strAddDay), theDIR));
        } else {
            try {
                int addDay = 0;
                addDay = Integer.parseInt(strAddDay);
                return DATE.addDate(DBDate, 0, 0, addDay);
            } catch (NumberFormatException e) {
                strAddDay = strAddDay.replace("+", "");
                String thisTime = DBDate + " " + strAddDay;
                if (DATE.isDBTimeStamp(thisTime)) {
                    return thisTime;
                } else {
                    return DBDate;
                }
            }
        }
    }

    /**
     * 計算系統時間
     * @param MARK
     * @param arg
     * @return
     */
    private String calTimeStamp() {
        return ObjectUtils.toString(new Timestamp(new Date().getTime()));
    }

    //    private static ExpectedCondition<WebElement> oneOfElementsLocatedVisible(By... args) {
    //        final List<By> byes = Arrays.asList(args);
    //        return new ExpectedCondition<WebElement>() {
    //            @Override
    //            public WebElement apply(WebDriver driver) {
    //                for (By by : byes) {
    //                    WebElement el;
    //                    try {
    //                        el = driver.findElement(by);
    //                    } catch (Exception r) {
    //                        continue;
    //                    }
    //                    if (el.isDisplayed())
    //                        return el;
    //                }
    //                return false;
    //            }
    //        };
    //    }
}