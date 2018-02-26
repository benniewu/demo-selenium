package com.cathay.test.selenium;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SeleniumHelperFK extends SeleniumHelper {

    private final static Logger log = Logger.getLogger(SeleniumHelperFK.class);
    
    public SeleniumHelperFK(WebDriver driver) {
        super(driver);
    }

    /**
     * 登入
     * @param sys
     * @param env
     * @param loginId
     * @param loginPw
     * @return 
     */
    @Override
    public void login(String sys, String env, String loginId, String loginPw) {

        thisSys = sys;
        thisEnv = env;
        thisLoginId = loginId;

        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS); 
        
        StringBuilder loginUrl = new StringBuilder();
        if("PP".equals(env.toUpperCase())){
        	loginUrl.append("http://10.95.18.96/FKWeb");
        }else if ("t".equals(env) || "T".equals(env)) {
            loginUrl.append("http://").append(env).append("go.linyuan.com.tw/FKWeb/");
        } else {
            loginUrl.append("https://").append(env).append("go.linyuan.com.tw/FKWeb/");
        }

        driver.get(loginUrl.toString());
        //平測的FK 在IE會有憑證錯誤 故強迫點繼續瀏覽此網站 CHROME已在起始參數ignore-certificate-errors
        if ("s".equals(env) || "S".equals(env)) {
            try {
                driver.navigate().to("javascript:document.getElementById('overridelink').click()");
            } catch (Exception e) {

            }
        }
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
    @Override
    public void enterSys(String sysCht) {

        waitTime(14000);
        //clickLinkByText(sysCht);
        List<WebElement> xpathLinkTextList = driver.findElements(By.linkText(sysCht));
		if (xpathLinkTextList.size() == 0) {
			return;
		} else {
			WebElement menuElement = xpathLinkTextList.get(0);
			((JavascriptExecutor) driver).executeScript("arguments[0].click()", menuElement);
		}
        waitTime(1000);
    }

    /**
     * 進入子系統>進入指定頁面>跳至主frame
     * @param subSysNm 子系統分類 
     * @param funcId 頁面名稱 
     * @throws Exception 
     */
    @Override
    public void enterFunction(String subSysNm, String funcNm) {

        waitTime(2000);
        int i = 0;
        boolean subMenu = false;
        do {
           try{
        	   By by = By.xpath("//li[contains(text(), '" + subSysNm + "')]");
        	   WebElement elem = getElement(by, "欲點選元件" + subSysNm);
               elem.click();
               subMenu = true;
               break;
           }catch(Exception e){
        	   log.error("找不到子目錄，將目錄往右移", e);
        	   //若是選單在右邊 沒顯示
        	   By by2 = By.id("subMenuRight");
        	   try{
        		   WebElement elem = getElement(by2, "欲點選元件" + "subMenuRight");
            	   elem.click();
            	   waitTime(1000);
        	   }catch(Exception e2){
        		   log.error("找不到往右按鈕", e2);
        		   subMenu = false;
        		   break;
        	   }
           }
        } while (i++ < 5);
        
        if(!subMenu){
        	throw new NoSuchElementException("找不到子目錄" + subSysNm);
        }
        
        //By by = By.xpath("//li[contains(text(), '" + subSysNm + "')]");
        //WebElement elem = getElement(by, "欲點選元件" + subSysNm);
        //clickLinkByText(funcNm);
        List<WebElement> xpathLinkTextList2 = driver.findElements(By.linkText(funcNm));
		WebElement menuElement2 = xpathLinkTextList2.get(0);
		((JavascriptExecutor) driver).executeScript("arguments[0].click()", menuElement2);
        waitTime(2000);
    }

    /**
     * 回到主選單
     */
    @Override
    public void enterMainMenu() {

        By by = By.cssSelector("li[class='logo']>a");
        WebElement elem = getElement(by, "回首頁按鈕");
        elem.click();
    }

    /**
     * 對ID對應的網頁元素點選
     * 
     * @param id
     */
    @Override
    public void clickById(String id) {

        WebElement elem = null;
        try {
            elem = getElement(By.id(id), "欲點選元件" + id);
        } catch (Exception ne) {
            elem = getElement(By.name(id), "欲點選元件" + id);
        }
        elem.click();
    }

    /**
     * 對ID對應的單選項目點選
     * 
     * @param id
     */
    @Override
    public void checkRadioById(String id) {

        WebElement elem = null;
        By by = By.xpath("//input[@id='" + id + "']/parent::*");
        elem = getElement(by, "欲點選元件" + id);

        elem.click();
    }

    /**
     * 對name對應checkBox項目點選
     * @param id
     */
    @Override
    public void checkRadioByName(String name, String idx) {

        WebElement elem = null;
        StringBuilder sb = new StringBuilder().append("(//input[@name='").append(name).append("']/parent::*)[")
                .append(Integer.parseInt(idx) + 1).append("]");
        By by = By.xpath(sb.toString());
        elem = getElement(by, "欲點選元件" + name);
        elem.click();
    }

    /**
     * 對ID對應checkBox項目選取
     * @param id
     */
    @Override
    public void checkBoxById(String id) {

        WebElement elem = null;
        By by = By.xpath("//input[@id='" + id + "']/parent::*");
        elem = getElement(by, "欲點選元件" + id);
        elem.click();
    }

    /**
     * 對name對應的單選項目點選
     * @param id
     */
    @Override
    public void checkBoxByName(String name, String idx) {

        WebElement elem = null;
        StringBuilder sb = new StringBuilder().append("(//input[@name='").append(name).append("']/parent::*)[")
                .append(Integer.parseInt(idx) + 1).append("]");
        By by = By.xpath(sb.toString());
        elem = getElement(by, "欲點選元件" + name);
        elem.click();
    }

    /**
     * 回到原視窗
     */
    @Override
    public void backToParentWin() {
        if (StringUtils.isBlank(parentHandle)) {
            return;
        }
        driver.switchTo().window(parentHandle);
    }

    /**
     * 取得底部訊息欄
     * @return
     * @throws Exception
     */
    @Override
    protected WebElement getBottomMessageElem() {

        waitTime(2000);
        return getElement(By.cssSelector("div[class*='notifyjs-bootstrap-base'] > span"), "底部訊息欄");
    }
}