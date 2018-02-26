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

    //�����(���������ɨϥ�)
    protected String parentHandle = "";

    //�t�ΧO
    protected String thisSys = "";

    //����
    protected String thisEnv = "";

    //Login�H��
    protected String thisLoginId = "";

    //�O�_���}�o����
    protected boolean isDevTest = false;

    //�x�sAlert�T��
    protected String storedAlertMsg = "";

    public SeleniumHelper(WebDriver _driver) {
        driver = _driver;
    }

    /**
     * ���o�O�_���}�o����
     * @return
     */
    public boolean isDevTest() {
        return isDevTest;
    }

    /**
     * �]�w�O�_���}�o����
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
     * �ާ@�B�J����
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
                    //TimeUnit.SECONDS.sleep(Integer.parseInt(STEP_WAIT)); //�ȩw�����U���s����������
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
                    //TimeUnit.SECONDS.sleep(Integer.parseInt(STEP_WAIT)); //�ȩw�����U���s����������
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
            log.error("�B�J����o�Ϳ��~", e);
            result.setResultCode(StepResult.RESULT_EXCEPTION);
            result.setExceptionMsg(e.getMessage());
            result.setScreenshotImg(saveScreenshot());
        }

        return result;
    }

    /**
     * �n�J
     * @param sys
     * @param env
     * @param loginId
     */
    public void login(String sys, String env, String loginId) {
        login(sys, env, loginId, loginId);
    }

    /**
     * �n�J
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
     * �i�J�t�� 
     * @param sysCht �t�ΦW�� ex:�O���t��
     * @throws InterruptedException 
     * @throws Exception
     */
    public void enterSys(String sysCht) {
        log.error("****** �i�J�t��: " + sysCht + " ******");
        try {
            driver.switchTo().frame(getElement(By.name("leftFrame"), "����iFrame"));
        } catch (Exception e) {
            log.error("�i�J�l�t�ή� �䤣�쥪����� ���^�D�e���ARETRY");
            //����3��,For IM �h��y�t(�W���� load��)
            waitTime(3000);
            enterMainMenu();
            driver.switchTo().frame(getElement(By.name("leftFrame"), "����iFrame"));
        }

        WebElement elem = getElement(By.xpath("//li[contains(text(), '" + sysCht + "')]"), "�t�ΦW��" + sysCht);
        //elem ��3��
        for (int i = 1; i < 3; i++) {
            log.error("****** elem �ղ�: " + i + " ��******");
            if (elem.isDisplayed()) {
                try {
                    elem.click();
                } catch (TimeoutException e) {
                    log.error("****** Timeout�������`,�t�ο��: " + sysCht + " ;TimeoutException: " + e);
                    //�A����3��, ��page load.
                    waitTime(3000);
                    continue;
                }
                break;
            } else {
                log.error("****** �t��: " + sysCht + " ,��" + i + "���S�X�{, wait 3���� ******");
                waitTime(3000);
            }
        }
        waitTime(5000);
    }

    /**
     * �i�J�l�t��>�i�J���w����>���ܥDframe
     * @param subSysNm �l�t�Τ��� 
     * @param funcId �����W�� 
     * @throws Exception 
     */
    public void enterFunction(String subSysNm, String funcNm) {

        log.error("****** subSysNm: " + subSysNm + " ;funcNm: " + funcNm + " ******");
        try {
            driver.switchTo().frame(getElement(By.name("leftFrame"), "����iFrame"));
        } catch (NoSuchElementException nsee) {
            //����3����.
            log.error("****** switchTo����iFrame 3����, NoSuchElementException: " + nsee);
            waitTime(3000);
            driver.switchTo().frame(getElement(By.name("leftFrame"), "����iFrame"));
        } catch (TimeoutException toe) {
            //����3����.
            log.error("****** switchTo����iFrame 3����, TimeoutException: " + toe);
            waitTime(3000);
            driver.switchTo().frame(getElement(By.name("leftFrame"), "����iFrame"));
        }

        WebElement menuDiv = getElement(By.cssSelector("li.folder[title*='" + subSysNm + "'] > div"), "�l�t�ο��" + subSysNm);
        //menuDiv ��3��
        for (int i = 1; i < 3; i++) {
            log.error("****** menuDiv �ղ�: " + i + " ��******");
            if (menuDiv.isDisplayed()) {
                menuDiv.click();
                break;
            } else {
                log.error("****** menuDiv ��" + i + "���S�X�{, wait 3���� ******");
                waitTime(3000);
            }
        }

        WebElement menuLink = null;
        try {
            menuLink = getElement(By.xpath("//li[contains(@title, '" + subSysNm + "')]/ul/li/a[text()='" + funcNm + "']"), "�\����" + funcNm);
        } catch (NoSuchElementException nsee) {
            //����3����.
            log.error("****** menuLink 3����, NoSuchElementException: " + nsee);
            waitTime(3000);
            menuLink = getElement(By.xpath("//li[contains(@title, '" + subSysNm + "')]/ul/li/a[contains(text(), '" + funcNm + "')]"),
                "�\����" + funcNm);
        } catch (TimeoutException toe) {
            //����3����.
            log.error("****** menuLink 3����, TimeoutException: " + toe);
            waitTime(3000);
            menuLink = getElement(By.xpath("//li[contains(@title, '" + subSysNm + "')]/ul/li/a[contains(text(), '" + funcNm + "')]"),
                "�\����" + funcNm);
        }
        //menuLink ��3��
        for (int i = 1; i < 3; i++) {
            log.error("****** menuLink �ղ�: " + i + " ��******");
            if (menuLink.isDisplayed()) {
                try {
                    menuLink.click();
                } catch (TimeoutException e) {
                    log.error("****** Timeout�������`,�\����: " + funcNm + " ;TimeoutException: " + e);
                    //�A����3��, ��page load.
                    waitTime(3000);
                    continue;
                }
                break;
            } else {
                log.error("****** menuLink ��" + i + "���S�X�{, wait 3���� ******");
                waitTime(3000);
            }
        }

        try {
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (Exception e) {
            log.error("��l�S��alert���� �A�~�����....");
        }

        driver.switchTo().frame(getElement(By.id("mainFrame"), "�D�niFrame"));
    }

    /**
     * �i�J�l�t��>�i�J���w����(��seq��)>���ܥDframe
     * @param subSysNm
     * @param funcNm
     * @param seq
     */
    public void enterFunctionBySeq(String subSysNm, String funcNm, String seq) {

        driver.switchTo().frame(getElement(By.name("leftFrame"), "����iFrame"));

        WebElement menuDiv = getElement(By.cssSelector("li.folder[title*='" + subSysNm + "'] > div"), "�l�t�ο��" + subSysNm);
        menuDiv.click();

        WebElement menuLink = null;
        try {
            List<WebElement> menuLinks = driver.findElements(By.xpath("//li[contains(@title, '" + subSysNm + "')]/ul/li/a[text()='"
                    + funcNm + "']"));
            menuLink = menuLinks.get(Integer.parseInt(seq) - 1);
            //menuLink = getElement(By.xpath("//li[contains(@title, '" + subSysNm + "')]/ul/li/a[text()='" + funcNm + "']"), "�\����" + funcNm);
        } catch (Exception e) {
            String errMsg = new StringBuilder().append("�L�k��").append(funcNm).append("����").append(seq).append("�ӿ��").toString();
            log.error(errMsg, e);
            throw new NoSuchElementException(errMsg);
        }
        menuLink.click();

        try {
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (Exception e) {
            log.error("��l�S��alert���� �A�~�����....");
        }

        driver.switchTo().frame(getElement(By.id("mainFrame"), "�D�niFrame"));
    }

    /**
     * �^��D���
     */
    public void enterMainMenu() {

        //mainFrame<<<<<leftFrame
        driver.switchTo().parentFrame();
        //leftFrame<<<<<frameset
        driver.switchTo().parentFrame();
        //frameset>>>>>>topFrame
        driver.switchTo().frame(getElement(By.name("topFrame"), "�W����"));
        By by = By.xpath("//ul[@id='menuBar']/li/a[contains(text(), '�D���')]");
        try {
            WebElement elem = getElement(by, "�^�������s");
            elem.click();
        } catch (NoSuchElementException nsee) {
            log.info("�L�k���^�������s�A�������`");
        }

        driver.switchTo().parentFrame();
    }

    /**
     * �n�X
     */
    public void logout() {

        //mainFrame<<<<<leftFrame
        driver.switchTo().parentFrame();
        //leftFrame<<<<<frameset
        driver.switchTo().parentFrame();
        //frameset>>>>>>topFrame
        driver.switchTo().frame(getElement(By.name("topFrame"), "�W����"));
        By by = By.xpath("//ul[@id='menuBar']/li/a[contains(text(), '�n�X')]");
        try {
            WebElement elem = getElement(by, "�n�X");
            elem.click();
        } catch (NoSuchElementException nsee) {
            log.info("�L�k���n�X���s�A�������`");
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
            log.info("���� >>>>>>>> " + thisEnv);
            log.info("���x >>>>>>>> " + thisSys);
            log.info("���ծרҵn�J�� >>>>>>>> " + thisLoginId);

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
                    log.error("��ƦA�ͦ��~, �D CSR/IM �t��!");
                    return;
                }
            } else {
                log.error("�D���թΥ�������, �L�k�����ƦA��! ");
                return;
            }
            urlSb.append(".cathaylife.com.tw/ZSWeb/api/app/exportData");
            String url = urlSb.toString();
            log.info("��ƦA�� WebService URL >>>>>>>> " + url);

            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("CASE_NOs", caseNo);
            paramMap.put("empId", thisLoginId);
            log.info("��ƦA�� WebService �Ѽ� >>>>>>>> " + paramMap);

            HttpClientHelper helper = new HttpClientHelper();
            String result = helper.getHttpResponseAsString(url, paramMap, "utf-8");
            log.info("��ƦA�͵��G >>>>>>>> " + result);
            Map resultMap = VOTool.jsonToMap(result);
            int returnCode = MapUtils.getIntValue(resultMap, "returnCode");
            log.error("returnCode >>>>>>>> " + returnCode);

            if (returnCode != 0) {
                throw new Exception(MapUtils.getString(resultMap, "detail"));
            }

        } catch (Exception e) {
            log.error("��ƦA�� WebService �I�s����", e);
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
        enterSys("�t�κ޲z");
        waitTime(1000);
        enterFunction("�����H���M��", "�ϥΪ̤���");
        waitTime(1000);
        WebElement switchID = getElement(By.name("switch_id"), "��J����ID" + switchId);
        switchID.clear();
        switchID.sendKeys(switchId);
        waitTime(1000);
        clickById("btnConfirm");
    }

    /**
     * �bID��Name������������J��r
     * @param id
     * @param context
     */
    public void keyinById(String id, String context) {
        keyinById(id, context, StringUtils.EMPTY);
    }

    /**
     * �bID��Name������������J��r
     * @param id
     * @param context
     * @param isTrigger
     */
    public void keyinById(String id, String context, String isTrigger) {

        WebElement elem = null;
        try {
            elem = getElement(By.id(id), "��J���" + id);
        } catch (NoSuchElementException ne) {
            elem = getElement(By.name(id), "��J���" + id);
        }

        //���ΰh���� �H�K���۰�Ĳ�oonChange�ƥ�       
        int text_size = elem.getAttribute("value").length();
        try {
            for (int i = 0; i < text_size; i++) {
                elem.sendKeys(Keys.BACK_SPACE);
            }
        } catch (Exception e) {
            log.fatal("BACK_SPACE�h�榳�~ ���clear", e);
            elem.clear();
        }
        elem.sendKeys(context);
        // �ѨMRB �Y���ײv����J��M�Ű��D�A��TabĲ�o�s�ʨƥ�
        if ("IS_TRIGGER_Y".equals(isTrigger)) {
            elem.sendKeys(Keys.TAB);
            waitTime(2000);
        }

    }

    /**
     * set��������by id
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
            throw new NoSuchElementException("�����id��" + id + "������", e);
        }
    }

    /**
     * set��������by name
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
            throw new NoSuchElementException("�����name��" + name + "������", e);
        }

    }

    /**
     * Name������������J��r
     * @param name
     * @param idx
     * @param context
     */
    public void keyinByName(String name, String idx, String context, String isTrigger) {
        waitTime(500);
        List<WebElement> elems = driver.findElements(By.name(name));
        if (elems.size() == 0) {
            throw new NoSuchElementException("�L�k���" + name);
        } else {
            WebElement elem = elems.get(Integer.parseInt(idx));

            //���ΰh���� �H�K���۰�Ĳ�oonChange�ƥ�       
            int text_size = elem.getAttribute("value").length();
            try {
                for (int i = 0; i < text_size; i++) {
                    elem.sendKeys(Keys.BACK_SPACE);
                }
            } catch (Exception e) {
                log.fatal("BACK_SPACE�h�榳�~ ���clear", e);
                elem.clear();
            }
            elem.sendKeys(context);
            // �ѨMRB �Y���ײv����J��M�Ű��D�A��TabĲ�o�s�ʨƥ�
            if ("IS_TRIGGER_Y".equals(isTrigger)) {
                elem.sendKeys(Keys.TAB);
            }
        }

    }

    /**
     * �bID�������U�Կ�椸����ܶ��� (��r)
     * 
     * @param id
     * @param context
     */
    public void selectTextById(String id, String text) {

        WebElement elem = null;
        try {
            elem = getElement(By.id(id), "�U�Կ��" + id);
        } catch (NoSuchElementException ne) {
            elem = getElement(By.name(id), "�U�Կ��" + id);
        }
        new Select(elem).selectByVisibleText(text);
    }

    /**
     * �bID�������U�Կ�椸����ܶ��� (��)
     * @param id
     * @param val
     */
    public void selectValueById(String id, String val) {

        WebElement elem = null;
        try {
            elem = getElement(By.id(id), "�U�Կ��" + id);
        } catch (NoSuchElementException ne) {
            elem = getElement(By.name(id), "�U�Կ��" + id);
        }
        new Select(elem).selectByValue(val);
    }

    /**
     * ��ID��������ﶵ���I��
     * 
     * @param id
     */
    public void checkRadioById(String id) {

        clickById(id);
    }

    /**
     * ��name����checkBox�����I��
     * @param id
     */
    public void checkRadioByName(String name, String idx) {

        clickByName(name, idx);
    }

    /**
     * ��ID����checkBox���ؿ��
     * @param id
     */
    public void checkBoxById(String id) {

        clickById(id);
    }

    /**
     * ��name��������ﶵ���I��
     * @param id
     */
    public void checkBoxByName(String name, String idx) {

        clickByName(name, idx);
    }

    public void clickByName(String name, String idx) {

        waitTime(500);

        List<WebElement> elems = driver.findElements(By.name(name));
        if (elems.size() == 0) {
            throw new NoSuchElementException("�L�k���" + name);
        } else {
            WebElement elem = elems.get(Integer.parseInt(idx));
            elem.click();
        }
    }

    /**
     * ���r��������ﶵ���I��
     * 
     * @param text
     */
    public void checkRadioByText(String text) {

        By by = By.xpath("//input/following-sibling::text()[contains(., '" + text + "')]/preceding-sibling::input");
        WebElement elem = getElement(by, text + "������ﶵ��");
        elem.click();
    }

    /**
     * ��ID��������ﶵ���I��
     * 
     * @param id
     */
    public void clickById(String id) {

        WebElement elem = null;
        if ("MP".equals(thisSys) && "query".equals(id)) { //MP��query�ȵLID ����XPATH�N��
            driver.findElement(By.xpath("//*[@id='right11']/div/div[1]/div[8]/a")).click();
            return;
        }

        try {
            elem = getElement(By.id(id), "���I�露��" + id);
        } catch (Exception ne) {
            elem = getElement(By.name(id), "���I�露��" + id);
        }

        if (!isDevTest && "�d��".equals(elem.getAttribute("value"))) {
            elem.click();
            waitTime(3000);
            confirmAlert();
        } else {
            elem.click();
        }
    }

    /**
     * �ϥ�javascript�I��id
     * @param id
     */
    public void clickScriptById(String id) {
        WebElement elem = null;
        if ("MP".equals(thisSys) && "query".equals(id)) { //MP��query�ȵLID ����XPATH�N��
            driver.findElement(By.xpath("//*[@id='right11']/div/div[1]/div[8]/a")).click();
            return;
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            js.executeScript("return document.getElementById('" + id + "').click();");
            elem = getElement(By.id(id), "���I�露��" + id);
        } catch (Exception ne) {
            js.executeScript("return document.getElementsByName('" + id + "')[0].click();");
            elem = getElement(By.name(id), "���I�露��" + id);
        }

        if (!isDevTest && "�d��".equals(elem.getAttribute("value"))) {
            elem.click();
            waitTime(3000);
            confirmAlert();
        } else {
            elem.click();
        }
    }

    /**
     * �ϥ�javascript�I��name
     * @param id
     */
    public void clickScriptByName(String id, String idx) {

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("return document.getElementsByName('" + id + "')[" + idx + "].click();");

    }

    /**
     * ��name��������ﶵ���I��
     * @param id
     */
    public void clickByName(String name) {

        WebElement elem = getElement(By.name(name), "���I�露��" + name);
        elem.click();
    }

    /**
     * ���r���������s�I��
     * 
     * @param id
     * @param context
     */
    public void clickBtnByText(String text) {

        WebElement elem = null;

        try {
            By by = By.cssSelector("input[value='" + text + "']");
            elem = getElement(by, "���I�露��" + text);
        } catch (TimeoutException e) {
            By by = By.xpath("//button[contains(text(), '" + text + "')]");
            elem = getElement(by, "���I�露��" + text);
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
     * ������popupWin
     */
    public void switchToPopupWin() {
        //driver.switchTo().parentFrame();
        //driver.switchTo().frame(getElement(By.id("mainFrame"), "�D�niFrame"));
        WebElement popup = getElement(By.xpath("//*[contains(@id, 'popupwin_')]"), "�u�X����");
        driver.switchTo().frame(popup);
    }

    /**
     * �����ܷs����
     */
    public void switchToWindow() {
        //�N������O�_��
        parentHandle = driver.getWindowHandle();
        //������̫�@�ӵ���
        for (String winHandle : driver.getWindowHandles()) {
            driver.switchTo().window(winHandle);
        }
    }

    /**
     * ������U����
     */
    public void closeWindow() {
        driver.close();
    }

    /**
     * �^������
     */
    public void backToParentWin() {
        if (StringUtils.isBlank(parentHandle)) {
            return;
        }
        driver.switchTo().window(parentHandle);
        driver.switchTo().parentFrame();
        driver.switchTo().frame(getElement(By.name("leftFrame"), "����iFrame"));
        driver.switchTo().frame(getElement(By.id("mainFrame"), "�D�niFrame"));
    }

    /**
     * �����ܫ��wiFrame
     * @param id
     */
    public void switchToFrame(String id) {
        driver.switchTo().defaultContent();
        driver.switchTo().frame(getElement(By.name("leftFrame"), "����iFrame"));
        driver.switchTo().frame(getElement(By.id("mainFrame"), "�D�niFrame"));
        driver.switchTo().frame(getElement(By.id(id), "���wiFrame"));

    }

    /**
     * ���^��iframe
     */
    public void switchToMainFrame() {
        driver.switchTo().defaultContent();
        driver.switchTo().frame(getElement(By.name("leftFrame"), "����iFrame"));
        driver.switchTo().frame(getElement(By.id("mainFrame"), "�D�niFrame"));
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
     * alert�����T�w
     */
    public void confirmAlert() {

        try {
            //CSRUtil���ͪ�alert()
            clickBtnByText("�T�w");
        } catch (Exception e) {
            int i = 0;
            Alert alert = null;
            //�Y�o�Ϳ��~>>�s������ͪ�alert
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
     * Confirm����
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
     * ���ݮɶ�
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
     * confirm�����T�w
     */
    public void confirmDelete(String coonfirm_Delete) {
        int i = 0;
        Alert alert = null;
        log.info("***coonfirm_Delete: " + coonfirm_Delete);

        //�Y�o�Ϳ��~>>�s������ͪ�alert
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
     * ���oWebElement
     * @param by
     * @return
     * @throws NoSuchElementException �䤣�줸��
     * @throws TimeoutException ���ݮɶ��L��
     */
    public WebElement getElement(By by, String eleName) throws NoSuchElementException, TimeoutException {

        WebElement elem = null;
        try {
            elem = new WebDriverWait(driver, elementVisibleWait).until(ExpectedConditions.visibilityOfElementLocated(by));
        } catch (TimeoutException te) {

            if (te.getCause() instanceof NoSuchElementException) {
                log.info("�L�k��� " + eleName);
                throw new NoSuchElementException("�L�k��� " + eleName);
            }
            log.info(eleName + " ���ݮɶ��L��");
            throw new TimeoutException(eleName + " ���ݮɶ��L��");
        }
        if (elem == null) {
            log.info("�L�k��� " + eleName);
            throw new NoSuchElementException("�L�k��� " + eleName);
        }

        return elem;
    }

    /**
     * ���o�����T����
     * @return
     * @throws Exception
     */
    protected WebElement getIMPopupElem() {

        waitTime(1000);
        driver.switchTo().parentFrame();
        driver.switchTo().frame(getElement(By.id("mainFrame"), "�����^mainFrame"));
        return getElement(By.id("msg-win_content"), "IM POPUP�T��");
    }

    /**
     * ���o�����T����
     * @return
     * @throws Exception
     */
    protected WebElement getBottomMessageElem() {

        waitTime(1000);
        driver.switchTo().parentFrame();
        driver.switchTo().frame(getElement(By.id("bottomFrame"), "������ bottomFrame"));
        return getElement(By.id("cathay_common_msgBoard"), "�����T����");
    }

    /**
     * ���Alert�����T��
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
     * �h�����Ҥ�r������
     * @return String
     */
    private String replaceWrap(String str) {
        return str.replaceAll("(\r\n|\n\r|\r|\n)", " ");
    }

    /**
     * �j�դ���
     * @param elem
     */
    @SuppressWarnings("unused")
    private void highLightElem(WebElement elem) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid red'", elem);
        highlightElems.add(elem);
    }

    /**
     * �����j�շ�e�j�դ���
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
     * ���椤�e���^�Ϧs��
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
                log.fatal("�^�ϥ���", e);
            }
        } catch (Exception e) {
            log.fatal("�^�ϥ���", e);
        }
        return rtnFile;
    }

    /**
     * format input���e
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
     * �p��t�Τ��+-�Ѽ�
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
        //�����w�O�u�@��
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
     * �p��t�ήɶ�
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