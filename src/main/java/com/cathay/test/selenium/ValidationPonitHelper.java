package com.cathay.test.selenium;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.cathay.common.util.NumberUtils;

public class ValidationPonitHelper {

    private static Logger log = Logger.getLogger(ValidationPonitHelper.class);

    protected WebDriver driver;

    protected SeleniumHelper seleniumHelper;

    private List<WebElement> highlightElems = new ArrayList<WebElement>();

    //���ݮɶ�
    private int stepWait = 1;

    public ValidationPonitHelper(WebDriver _driver, SeleniumHelper _seleniumHelper) {
        driver = _driver;
        seleniumHelper = _seleniumHelper;
    }

    /**
     * �����I�B�J����
     * @param STEP_TYPE
     * @param STEP_ARGS
     * @return
     */
    public ValidResult doValid(String stepType, List<String> stepArgs) {

        ValidResult result = null;
        try {
            switch (stepType) {
                case "VP_BTM_MSG":
                    return this.validBottomMessage(stepArgs.get(0));
                case "VP_IM_POP_MSG":
                    return this.validIMPopupMessage(stepArgs.get(0));
                case "VP_ALERT_MSG":
                    return this.validAlertMessage(stepArgs.get(0));
                case "VP_AMT":
                    return this.validAmount(stepArgs.get(0), stepArgs.get(1));
                case "VP_TEXT":
                    return this.validText(stepArgs.get(0), stepArgs.get(1));
                case "VP_TD_MSG":
                    return this.validTdMessage(stepArgs.get(0), stepArgs.get(1));
                case "VP_TABLE_DATA":
                    return this.validTableData(stepArgs.get(0), stepArgs.get(1), stepArgs.get(2), stepArgs.get(3));
                case "VP_TUI_CNT":
                    return this.validTableUICount(stepArgs.get(0));
                case "VP_SELECT_LINKAGE":
                    return this.validSelectLinkage(stepArgs.get(0), stepArgs.get(1));
            }
        } catch (Exception e) {
            log.error("�����I����o�Ϳ��~", e);
            result = new ValidResult(StepResult.RESULT_EXCEPTION, e.getMessage(), getScreenshot());
        }

        return result;
    }

    /**
     * �����I�B�J����
     * @param STEP_TYPE
     * @param STEP_ARGS
     * @return
     */
    public ValidResult doPGValid(String stepType, List<String> stepArgs) {

        ValidResult result = null;
        try {
            switch (stepType) {
                case "PG_VP_INPUT_EMPTY":
                    return this.pgValidInputEmpty();
                case "PG_VP_FUNC_POSITIVE":
                    return this.pgValidFuncPositive();
                case "PG_VP_FUNC_NEGATIVE":
                    return this.pgValidFuncNegative();
            }
        } catch (Exception e) {
            log.error("�����I����o�Ϳ��~", e);
            result = new ValidResult(StepResult.RESULT_EXCEPTION, e.getMessage(), getScreenshot());
        }

        return result;
    }

    /**
     * ���o�����T����
     * @return
     * @throws Exception
     */
    protected WebElement getBottomMessageElem() throws Exception {

        Thread.sleep(1000);
        driver.switchTo().parentFrame();
        driver.switchTo().frame(seleniumHelper.getElement(By.id("bottomFrame"), "������ bottomFrame"));
        return seleniumHelper.getElement(By.id("cathay_common_msgBoard"), "�����T����");
    }

    /**
     * ���ҩ����^�ǰT��
     * @param checkMsg
     * @return
     */
    public ValidResult validBottomMessage(String checkMsg) {

        if (checkMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o��null");
        }

        try {
            WebElement elem = getBottomMessageElem();
            String rtnMsg = elem.getText();

            if (checkMsg.equals(rtnMsg)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkMsg, rtnMsg);
            } else {
                highLightElem(elem);
                File screenShotImg = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkMsg, rtnMsg, screenShotImg);
            }
        } catch (Exception e) {
            log.error("���ҩ����^�ǰT�����~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���ҩ����^�ǰT�����~ : " + e.getMessage());
        } finally {
            doWaitTime();
            driver.switchTo().parentFrame();
            driver.switchTo().frame(seleniumHelper.getElement(By.id("mainFrame"), "�����^mainFrame"));
        }
    }

    /**
     * ����TableUI�d�ߵ���
     * @param validCount
     * @return
     */
    public ValidResult validTableUICount(String checkCnt) {
        try {
            WebElement elem = null;
            if (checkCnt == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o��null");
            }
            Thread.sleep(500);
            elem = seleniumHelper.getElement(By.id("grid_pageInfo"), "TableUI�d�ߵ���");
            String rtnCnt = elem.getText();
            String totalCnt = rtnCnt.substring(rtnCnt.lastIndexOf("�@") + 1, rtnCnt.indexOf("��")).trim();
            if (checkCnt.equals(totalCnt)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkCnt, totalCnt);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkCnt, totalCnt, img);
            }
        } catch (Exception e) {
            log.error("����TableUI�d�ߵ��ƿ��~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "����TableUI�d�ߵ��ƿ��~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���Ҥ������B
     * @param id �����id
     * @param strAmt ���Ҥ�諸���B
     * @return
     */
    public ValidResult validAmount(String id, String checkAmt) {
        try {
            WebElement elem = null;
            if (id == null || checkAmt == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o��null");
            }
            Thread.sleep(500);
            checkAmt = checkAmt.replace(",", "");
            elem = seleniumHelper.getElement(By.id(id), "���Ҥ������B");
            String rtnAmt = elem.getText().replace(",", "");
            if (new BigDecimal(checkAmt).compareTo(new BigDecimal(rtnAmt)) == 0) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkAmt, rtnAmt);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkAmt, rtnAmt, img);
            }
        } catch (Exception e) {
            log.error("���Ҥ������B���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҥ������B���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���Ҥ�����r
     * @param id �����id
     * @param strAmt ���Ҥ�諸���B
     * @return
     */
    public ValidResult validText(String id, String checkText) {
        try {
            WebElement elem = null;
            if (id == null || checkText == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o��null");
            }
            Thread.sleep(500);
            elem = seleniumHelper.getElement(By.id(id), "���Ҥ�����r");
            String rtnAmt = elem.getText();

            if (checkText.equals(rtnAmt)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkText, rtnAmt);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkText, rtnAmt, img);
            }
        } catch (Exception e) {
            log.error("���Ҥ�����r���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҥ�����r���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���Ҥ��������k������r
     * @param fieldText ���W��
     * @param checkMsg ���Ҥ��r��
     * @return
     */
    public ValidResult validTdMessage(String fieldText, String checkMsg) {
        try {
            if (fieldText == null || checkMsg == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o��null");
            }
            WebElement elem = null;
            Thread.sleep(500);
            List<WebElement> eles = driver.findElements(By.xpath("//td[contains(text(), '" + fieldText + "')]/following-sibling::td[1]"));
            if (eles.size() == 0) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "�䤣������k����r ");
            }
            elem = eles.get(0);
            String rtnMsg = elem.getText();
            if (checkMsg.equals(rtnMsg)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkMsg, rtnMsg);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkMsg, rtnMsg, img);
            }
        } catch (Exception e) {
            log.error("���Ҥ��������k������r���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҥ��������k������r���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���Ҫ�椺�e
     * @param id ���id
     * @param rows �ĴX�C���
     * @param column �ĴX���ƩάO��Ƥ���NAME
     * @param checkMsg ���Ҥ��r��
     * @return
     */
    public ValidResult validTableData(String id, String rows, String column, String checkMsg) {
        try {
            if (id == null || rows == null || column == null || checkMsg == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҫ�id,��,�C�M�r�ꤣ�o��null");
            }
            WebElement elem = null;
            Thread.sleep(500);
            int col_nm = NumberUtils.toInt(column, 0); //�ǤJ���ONAME�D�Ʀr�N�ন0

            WebElement table = driver.findElement(By.xpath("//*[@id='" + id + "']//*//table[@name='main']"));
            //*[@id="grid"]//*//table[@name="main"]
            List<WebElement> allRows = table.findElements(By.tagName("tbody"));
            Thread.sleep(500);
            WebElement row = allRows.get(NumberUtils.toInt(rows) - 1);

            String rtnMsg;
            if (col_nm != 0) {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                elem = cells.get(col_nm - 1);
                rtnMsg = elem.getText();
            } else {
                rtnMsg = row.findElement(By.name(column)).getText();
            }
            if (checkMsg.equals(rtnMsg)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkMsg, rtnMsg);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkMsg, rtnMsg, img);
            }
        } catch (Exception e) {
            log.error("���Ҫ�椺�e���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҫ�椺�e���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ����IM POPUP�T��
     * @param checkMsg
     * @return
     */
    public ValidResult validIMPopupMessage(String checkMsg) {
        try {
            if (checkMsg == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o��null");
            }
            WebElement elem = null;
            Thread.sleep(500);
            driver.switchTo().parentFrame();
            driver.switchTo().frame(seleniumHelper.getElement(By.id("mainFrame"), "�����^mainFrame"));
            elem = seleniumHelper.getElement(By.id("msg-win_content"), "IM POPUP�T��");
            String rtnMsg = elem.getText();
            rtnMsg = replaceWrap(rtnMsg);
            if (checkMsg.equals(rtnMsg)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkMsg, rtnMsg);
            } else {
                highLightElem(elem);
                File img = getScreenshot();
                unhighLightLastElems();
                return new ValidResult(StepResult.RESULT_FAIL, checkMsg, rtnMsg, img);
            }
        } catch (Exception e) {
            log.error("����IM POPUP�T�����e���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "����IM POPUP�T�����e���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ����Alert�T��
     * @param checkMsg
     * @return
     */
    public ValidResult validAlertMessage(String checkMsg) {
        try {
            if (checkMsg == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o��null");
            }
            Thread.sleep(500);
            Alert alert = driver.switchTo().alert();
            String rtnMsg = alert.getText();
            rtnMsg = replaceWrap(rtnMsg);
            if (checkMsg.equals(rtnMsg)) {
                return new ValidResult(StepResult.RESULT_SUCCESS, checkMsg, rtnMsg);
            } else {
                File img = getScreenshot();
                return new ValidResult(StepResult.RESULT_FAIL, checkMsg, rtnMsg, img);
            }
        } catch (Exception e) {
            log.error("����Alert�T�����e���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "����Alert�T�����e���~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    /**
     * ���ҳs�ʿ��
     * @param id
     * @param keyword
     * @return
     */
    public ValidResult validSelectLinkage(String id, String keyword) {

        try {
            if (id == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "���ҳs�ʿ��ID���o����");
            }

            Thread.sleep(1000);
            WebElement elem = seleniumHelper.getElement(By.id(id), "���ҳs�ʿ��");

            List<WebElement> opts = new Select(elem).getOptions();
            for (WebElement opt : opts) {
                String optText = opt.getText();
                if (optText != null && optText.contains(keyword)) {
                    return new ValidResult(StepResult.RESULT_SUCCESS, keyword, optText);
                }
            }
            return new ValidResult(StepResult.RESULT_FAIL, keyword, "");
        } catch (Exception e) {
            log.error("���ҳs�ʿ����~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���ҳs�ʿ����~ : " + e.getMessage());
        } finally {
            doWaitTime();
        }
    }

    public ValidResult pgValidInputEmpty() {
        return validAlertMessage("�ˮֿ��~", new String[] { "�ݦ���", "���o����", "���i�ť�" });
    }

    public ValidResult pgValidInput(String keywords) {
        return validAlertMessage("�ˮֿ��~", new String[] { "�ˮֿ��~" });
    }

    public ValidResult pgValidFuncPositive() {
        return validBottomMessage(new String[] { "���\", "����" });
    }

    public ValidResult pgValidFuncNegative() {
        return validBottomMessage(new String[] { "�d�L���", "����", "���~", "���`" });
    }

    /**
     * ����Alert�T��
     * @param checkMsg
     * @return
     */
    public ValidResult validAlertMessage(String checkContainMsg, String[] optCheckContainMsgs) {
        try {
            if (checkContainMsg == null) {
                return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o��null");
            }
            Thread.sleep(500);
            Alert alert = driver.switchTo().alert();
            String rtnMsg = alert.getText();

            if (rtnMsg.contains(checkContainMsg)) {
                if (optCheckContainMsgs == null) {
                    return new ValidResult(StepResult.RESULT_SUCCESS, checkContainMsg, rtnMsg);
                }
                for (String optCheckContainMsg : optCheckContainMsgs) {
                    if (rtnMsg.contains(optCheckContainMsg)) {
                        return new ValidResult(StepResult.RESULT_SUCCESS, checkContainMsg + "," + Arrays.toString(optCheckContainMsgs),
                                rtnMsg);
                    }
                }
            }
            File img = getScreenshot();
            return new ValidResult(StepResult.RESULT_FAIL, checkContainMsg + "," + Arrays.toString(optCheckContainMsgs), rtnMsg, img);
        } catch (Exception e) {
            log.error("����Alert�T�����e���~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "����Alert�T�����e���~ : " + e.getMessage());
        } finally {
            seleniumHelper.confirmAlert();
            doWaitTime();
        }
    }

    /**
     * ���ҩ����^�ǰT��
     * @param checkMsg
     * @return
     */
    public ValidResult validBottomMessage(String[] checkContainMsgs) {

        if (checkContainMsgs == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o��null");
        }

        try {
            WebElement elem = getBottomMessageElem();
            String rtnMsg = elem.getText();

            for (String checkContainMsg : checkContainMsgs) {
                if (rtnMsg.contains(checkContainMsg)) {
                    return new ValidResult(StepResult.RESULT_SUCCESS, Arrays.toString(checkContainMsgs), rtnMsg);
                }
            }
            highLightElem(elem);
            File screenShotImg = getScreenshot();
            unhighLightLastElems();
            return new ValidResult(StepResult.RESULT_FAIL, Arrays.toString(checkContainMsgs), rtnMsg, screenShotImg);
        } catch (Exception e) {
            log.error("���ҩ����^�ǰT�����~", e);
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���ҩ����^�ǰT�����~ : " + e.getMessage());
        } finally {
            seleniumHelper.confirmAlert();
            doWaitTime();
            driver.switchTo().parentFrame();
            driver.switchTo().frame(seleniumHelper.getElement(By.id("mainFrame"), "�����^mainFrame"));
        }
    }

    /**
     * �j�դ���
     * @param elem
     */
    protected void highLightElem(WebElement elem) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid red'", elem);
        highlightElems.add(elem);
    }

    /**
     * �����j�շ�e�j�դ���
     */
    protected void unhighLightLastElems() {
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
    protected File getScreenshot() {

        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
    }

    /**
     * �����I�@���᪺���ݮɶ�
     * @return
     */
    protected void doWaitTime() {
        try {
            TimeUnit.SECONDS.sleep(stepWait);
        } catch (Exception e) {
            log.error("�����I������A���ݮɶ��o�Ͳ��`", e);
        }
    }
    
    /**
     * �h�����Ҥ�r������
     * @return String
     */
    private String replaceWrap(String str){
        log.debug("Strat replaceWrap");
        return str.replaceAll("(\r\n|\n\r|\r|\n)", " ");
    }
}