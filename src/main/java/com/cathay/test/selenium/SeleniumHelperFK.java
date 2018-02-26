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
     * �n�J
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
        //������FK �bIE�|�����ҿ��~ �G�j���I�~���s�������� CHROME�w�b�_�l�Ѽ�ignore-certificate-errors
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
     * �i�J�t�� 
     * @param sysCht �t�ΦW�� ex:�O���t��
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
     * �i�J�l�t��>�i�J���w����>���ܥDframe
     * @param subSysNm �l�t�Τ��� 
     * @param funcId �����W�� 
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
        	   WebElement elem = getElement(by, "���I�露��" + subSysNm);
               elem.click();
               subMenu = true;
               break;
           }catch(Exception e){
        	   log.error("�䤣��l�ؿ��A�N�ؿ����k��", e);
        	   //�Y�O���b�k�� �S���
        	   By by2 = By.id("subMenuRight");
        	   try{
        		   WebElement elem = getElement(by2, "���I�露��" + "subMenuRight");
            	   elem.click();
            	   waitTime(1000);
        	   }catch(Exception e2){
        		   log.error("�䤣�쩹�k���s", e2);
        		   subMenu = false;
        		   break;
        	   }
           }
        } while (i++ < 5);
        
        if(!subMenu){
        	throw new NoSuchElementException("�䤣��l�ؿ�" + subSysNm);
        }
        
        //By by = By.xpath("//li[contains(text(), '" + subSysNm + "')]");
        //WebElement elem = getElement(by, "���I�露��" + subSysNm);
        //clickLinkByText(funcNm);
        List<WebElement> xpathLinkTextList2 = driver.findElements(By.linkText(funcNm));
		WebElement menuElement2 = xpathLinkTextList2.get(0);
		((JavascriptExecutor) driver).executeScript("arguments[0].click()", menuElement2);
        waitTime(2000);
    }

    /**
     * �^��D���
     */
    @Override
    public void enterMainMenu() {

        By by = By.cssSelector("li[class='logo']>a");
        WebElement elem = getElement(by, "�^�������s");
        elem.click();
    }

    /**
     * ��ID���������������I��
     * 
     * @param id
     */
    @Override
    public void clickById(String id) {

        WebElement elem = null;
        try {
            elem = getElement(By.id(id), "���I�露��" + id);
        } catch (Exception ne) {
            elem = getElement(By.name(id), "���I�露��" + id);
        }
        elem.click();
    }

    /**
     * ��ID��������ﶵ���I��
     * 
     * @param id
     */
    @Override
    public void checkRadioById(String id) {

        WebElement elem = null;
        By by = By.xpath("//input[@id='" + id + "']/parent::*");
        elem = getElement(by, "���I�露��" + id);

        elem.click();
    }

    /**
     * ��name����checkBox�����I��
     * @param id
     */
    @Override
    public void checkRadioByName(String name, String idx) {

        WebElement elem = null;
        StringBuilder sb = new StringBuilder().append("(//input[@name='").append(name).append("']/parent::*)[")
                .append(Integer.parseInt(idx) + 1).append("]");
        By by = By.xpath(sb.toString());
        elem = getElement(by, "���I�露��" + name);
        elem.click();
    }

    /**
     * ��ID����checkBox���ؿ��
     * @param id
     */
    @Override
    public void checkBoxById(String id) {

        WebElement elem = null;
        By by = By.xpath("//input[@id='" + id + "']/parent::*");
        elem = getElement(by, "���I�露��" + id);
        elem.click();
    }

    /**
     * ��name��������ﶵ���I��
     * @param id
     */
    @Override
    public void checkBoxByName(String name, String idx) {

        WebElement elem = null;
        StringBuilder sb = new StringBuilder().append("(//input[@name='").append(name).append("']/parent::*)[")
                .append(Integer.parseInt(idx) + 1).append("]");
        By by = By.xpath(sb.toString());
        elem = getElement(by, "���I�露��" + name);
        elem.click();
    }

    /**
     * �^������
     */
    @Override
    public void backToParentWin() {
        if (StringUtils.isBlank(parentHandle)) {
            return;
        }
        driver.switchTo().window(parentHandle);
    }

    /**
     * ���o�����T����
     * @return
     * @throws Exception
     */
    @Override
    protected WebElement getBottomMessageElem() {

        waitTime(2000);
        return getElement(By.cssSelector("div[class*='notifyjs-bootstrap-base'] > span"), "�����T����");
    }
}