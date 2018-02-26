package com.cathay.test.selenium;

import java.io.File;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ValidationPointHelperFK extends ValidationPointHelper {

    private static Logger log = Logger.getLogger(ValidationPointHelperFK.class);

    public ValidationPointHelperFK(WebDriver _driver, SeleniumHelper seleniumHelper) {
        super(_driver, seleniumHelper);
    }

    /**
     * ���ҩ����^�ǰT��
     * @param checkMsg
     * @return
     */
    @Override
    public ValidResult validBottomMessage(String checkMsg) {

        if (checkMsg == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o��null");
        }

        try {
            WebElement elem = seleniumHelper.getBottomMessageElem();
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
        }
    }

    /**
     * ���ҩ����^�ǰT��
     * @param checkMsg
     * @return
     */
    @Override
    public ValidResult validContainBottomMessage(String[] checkContainMsgs) {

        if (checkContainMsgs == null) {
            return new ValidResult(StepResult.RESULT_EXCEPTION, "���Ҧr�ꤣ�o��null");
        }

        try {
            WebElement elem = seleniumHelper.getBottomMessageElem();
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
        }
    }
}