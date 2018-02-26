package com.cathay.test.selenium;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.openqa.selenium.WebDriver;

import com.cathay.common.util.DATE;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestHelper {

    private static Logger log = Logger.getLogger(TestHelper.class);

    private Map testResult = new ListOrderedMap();

    private WebDriver driver;

    private SeleniumHelper seleniumHelper;

    private ValidationPointHelper validHelper;

    private boolean isDevTest = false;

    public TestHelper(String testId, Document caseDoc, String testMchnType, String testMchnIp, String browserType, String oprId,
            String oprNm, String oprDiv) throws Exception {

        this(testId, caseDoc, testMchnType, testMchnIp, browserType, oprId, oprNm, oprDiv, false);
    }

    public TestHelper(String testId, Document caseDoc, String testMchnType, String testMchnIp, String browserType, String oprId,
            String oprNm, String oprDiv, boolean isDevTest) throws Exception {

        String sysCd = String.valueOf(caseDoc.get("SYS_CD"));

        testResult.put("TEST_ID", testId);
        testResult.put("CASE_ID", caseDoc.get("CASE_ID"));
        testResult.put("SYS_CD", sysCd);
        testResult.put("SYS_NM", caseDoc.get("SYS_NM"));
        testResult.put("CASE_NM", caseDoc.get("CASE_NM"));
        testResult.put("PLATFORM", caseDoc.get("PLATFORM"));
        testResult.put("ENV", caseDoc.get("ENV"));
        testResult.put("MNTN_DIV", caseDoc.get("MNTN_DIV"));
        testResult.put("MNTN_ID", caseDoc.get("MNTN_ID"));
        testResult.put("MNTN_NM", caseDoc.get("MNTN_NM"));

        testResult.put("TEST_MCHN_TYPE", testMchnType);
        testResult.put("TEST_MCHN_IP", testMchnIp);
        testResult.put("BROWSER_TYPE", browserType);
        testResult.put("IS_DEV_TEST", isDevTest);
        testResult.put("TEST_DT", DATE.getDBDate());
        testResult.put("TEST_STR_TIME", new Date(System.currentTimeMillis()));
        testResult.put("OPR_DIV", oprDiv);
        testResult.put("OPR_ID", oprId);
        testResult.put("OPR_NM", oprNm);

        driver = WebDriverHelper.getWebDriver(testMchnType, testMchnIp, browserType);
        seleniumHelper = SeleniumHelperFactory.createSeleniumHelper(sysCd, driver, isDevTest);
        validHelper = ValidationPointHelperFactory.createValidationPointHelper(sysCd, driver, seleniumHelper);
        this.isDevTest = isDevTest;
    }

    /**
     * 執行測試
     * @param caseDoc
     * @return
     */
    public Map test(Document caseDoc) {

        ResultCounter resultCounter = new ResultCounter();
        Map testResultMap = (Map) testStep(caseDoc, resultCounter);
        testResult.put("TEST_END_TIME", new Date(System.currentTimeMillis()));
        testResult.put("LOGIN_RESULT", testResultMap.get("LOGIN_RESULT"));
        testResult.put("STEP_RESULT", testResultMap.get("STEP_RESULT"));
        testResult.put("CASE_STEP_RESULT", testResultMap.get("CASE_STEP_RESULT"));
        calTestResult(testResult, resultCounter);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error("", e);
        }

        if (driver != null) {
            driver.quit();
        }

        return testResult;
    }

    /**
     * 執行步驟
     * @param caseDoc
     * @param resultCounter
     * @return
     */
    private Map testStep(Document caseDoc, ResultCounter resultCounter) {

        // 取得子案例在內所有步驟
        List<Document> fullCaseStep = (List<Document>) caseDoc.get("FULL_CASE_STEP");
        // 計算步驟和驗證點數
        Map caseStepCnt = calCaseStepCnt(fullCaseStep);
        resultCounter.setStepTotalCnt(MapUtils.getIntValue(caseStepCnt, "STEP_TOTAL_CNT") + 1);
        resultCounter.setVpTotalCnt(MapUtils.getIntValue(caseStepCnt, "VP_TOTAL_CNT"));
        Map<String, ResultCounter> caseCounter = MapUtils.getMap(caseStepCnt, "CASE_CNT_MAP");

        Map testResultMap = new HashMap();
        Map<String, Map> caseStepResults = new LinkedHashMap();
        List<Map> stepResults = new ArrayList<Map>();

        // 登入       
        //        Document loginStep = (Document) caseDoc.get("LOGIN_STEP");
        //        Map loginResult = new LinkedHashMap();
        //        loginResult.putAll(loginStep);
        //        try {
        //            List<String> stepArgs = (List<String>) loginStep.get("STEP_ARGS");
        //            seleniumHelper.login(stepArgs.get(0), stepArgs.get(1), stepArgs.get(2), stepArgs.get(3));
        //            resultCounter.stepSuccess();
        //            loginResult.put("IS_SUCCESS", true);
        //        } catch (Exception e) {
        //            log.error("登入失敗", e);
        //            resultCounter.stepFail();
        //            loginResult.put("IS_SUCCESS", false);
        //            loginResult.put("ERR_MSG", e.getMessage());
        //        }
        //        stepResults.add(loginResult);
        //        testResultMap.put("LOGIN_RESULT", loginResult);

        // 登入
        Map loginResultMap = new LinkedHashMap();
        Document loginStep = (Document) caseDoc.get("LOGIN_STEP");
        loginResultMap.putAll(loginStep);
        StepResult loginResult = seleniumHelper.doStep("LOGIN", (List<String>) loginStep.get("STEP_ARGS"));
        // 處理登入執行結果
        addStepResult("LOGIN", loginResult, loginResultMap, resultCounter);
        // 紀錄登入執行結果
        stepResults.add(loginResultMap);
        testResultMap.put("LOGIN_RESULT", loginResultMap);

        // 操作步驟
        Map resultMap = null;
        try {
            boolean doStep = true;
            StepResult result = null;
            String currentCaseKey = null;
            for (int i = 0; i < fullCaseStep.size(); i++) {
                Document caseStep = fullCaseStep.get(i);
                String stepType = caseStep.getString("STEP_TYPE");

                resultMap = new LinkedHashMap();
                resultMap.putAll(caseStep);
                if (doStep) {
                    if ("CASE".equals(stepType)) {

                        // 子案例另行統計執行結果
                        String caseKey = caseStep.getString("CASE_ID") + "_" + caseStep.getLong("TS_KEY");
                        caseStep.put("STEP_RESULT", new ArrayList<Map>());
                        caseStepResults.put(caseKey, caseStep);
                        currentCaseKey = caseKey;

                        // 執行子案例時，重新登入
                        if (caseStep.getBoolean("IS_CHANGE_LOGIN", false)) {

                            Map reLoginResultMap = new LinkedHashMap();
                            Document reloginStep = (Document) caseStep.get("CHANGE_LOGIN_STEP");
                            reLoginResultMap.putAll(reloginStep);
                            StepResult reLoginResult = seleniumHelper.doStep("RELOGIN", (List<String>) reloginStep.get("STEP_ARGS"));
                            if (reLoginResult.getResultCode() == StepResult.RESULT_EXCEPTION) {
                                log.fatal("重登失敗");
                            }
                            // 處理子案例登入執行結果
                            addStepResult("RELOGIN", reLoginResult, reLoginResultMap, resultCounter);
                            // 紀錄子案例登入執行結果
                            stepResults.add(reLoginResultMap);
                            ((List) caseStepResults.get(currentCaseKey).get("STEP_RESULT")).add(reLoginResultMap);
                        } else {
                            // 執行子案例前統一先回到主選單 沒重登的就回主選單
                            seleniumHelper.enterMainMenu();
                        }
                        continue;
                    }

                    List<String> stepArgs = (List<String>) caseStep.get("STEP_ARGS");
                    if (stepType.startsWith("VP")) {
                        result = validHelper.doValid(stepType, stepArgs);
                    } else if (stepType.startsWith("DEV_VP")) {
                        result = validHelper.doDevValid(stepType, stepArgs);
                    } else {
                        result = seleniumHelper.doStep(stepType, stepArgs);
                    }

                    Boolean isFromCase = caseStep.getBoolean("IS_FROM_CASE");
                    if (isFromCase != null && isFromCase) {
                        // 若為子案例步驟，紀錄執行結果和子案例執行結果
                        addStepResult(stepType, result, resultMap, resultCounter, caseCounter.get(currentCaseKey));
                        ((List) caseStepResults.get(currentCaseKey).get("STEP_RESULT")).add(resultMap);
                    } else {
                        // 紀錄執行結果
                        addStepResult(stepType, result, resultMap, resultCounter);
                    }
                    stepResults.add(resultMap);

                    // 若步驟執行異常則中止執行
                    if (result.getResultCode() == StepResult.RESULT_EXCEPTION) {
                        doStep = false;
                    }
                }
            }
        } catch (Exception e) {
            log.error("", e);
            // TODO 截圖
            // 發生異常，中止測試
            resultCounter.stepFail();
            resultMap.put("IS_SUCCESS", false);
            resultMap.put("ERR_MSG", e.getMessage());
        }

        testResultMap.put("STEP_RESULT", stepResults);
        for (String casekey : caseStepResults.keySet()) {
            ResultCounter caseResultCounter = caseCounter.get(casekey);
            caseStepResults.get(casekey).put("CASE_VP_SUCCESS_CNT", caseResultCounter.getVpSuccessCnt());
            caseStepResults.get(casekey).put("CASE_VP_TOTAL_CNT", caseResultCounter.getVpTotalCnt());
        }
        testResultMap.put("CASE_STEP_RESULT", caseStepResults);
        return testResultMap;
    }

    /**
     * 將 File 轉成 Base64 字串
     * @param screenshotImg
     * @return String
     */
    private String getBase64String(File screenshotImg) {
        byte[] data = null;
        try {
            data = Files.readAllBytes(Paths.get(screenshotImg.getPath()));
        } catch (IOException e) {
            log.error("讀取截圖File發生錯誤", e);
        }
        return new String(Base64.encodeBase64(data));
    }

    /**
     * 加入步驟執行結果
     * @param stepType
     * @param result
     * @param resultCounter
     * @param stepResultDoc
     */
    private void addStepResult(String stepType, StepResult result, Map stepResult, ResultCounter resultCounter) {
        addStepResult(stepType, result, stepResult, resultCounter, null);
    }

    /**
     * 加入步驟執行結果
     * @param stepType
     * @param result
     * @param resultCounter
     * @param stepResultDoc
     */
    private void addStepResult(String stepType, StepResult result, Map stepResult, ResultCounter resultCounter,
            ResultCounter caseResultCounter) {

        boolean isCntByCase = caseResultCounter != null;

        if (result.getResultCode() == StepResult.RESULT_SUCCESS) {
            resultCounter.stepSuccess();
            if (isCntByCase) {
                caseResultCounter.stepSuccess();
            }
            if (stepType.startsWith("VP") || stepType.startsWith("DEV_VP")) {
                resultCounter.vpSuccess();
                if (isCntByCase) {
                    caseResultCounter.vpSuccess();
                }
            }

            stepResult.put("IS_SUCCESS", true);
        } else {
            resultCounter.stepFail();
            if (isCntByCase) {
                caseResultCounter.stepFail();
            }
            if (stepType.startsWith("VP") || stepType.startsWith("DEV_VP")) {
                resultCounter.vpFail();
                if (isCntByCase) {
                    caseResultCounter.vpFail();
                }
            }

            stepResult.put("IS_SUCCESS", false);
            if (result.getResultCode() == StepResult.RESULT_EXCEPTION) {
                stepResult.put("ERR_MSG", result.getExceptionMsg());
            } else {
                if (result instanceof ValidResult) {
                    ValidResult validResult = (ValidResult) result;
                    log.error("Expected Result=" + validResult.getExpectStr());
                    log.error("Actual Result=" + validResult.getActualStr());

                    stepResult.put("EXPECT", validResult.getExpectStr());
                    stepResult.put("ACTUAL", validResult.getActualStr());
                } else {
                    // 執行錯誤結果處理 TODO
                }
            }

            // 執行錯誤/驗證錯誤截圖
            File screenshotImg = result.getScreenshotImg();
            if (screenshotImg != null) {
                stepResult.put("SCREENSHOT_IMG", getBase64String(screenshotImg));
            }
        }

        if (isDevTest) {
            // 開發測試用截圖 - 執行步驟前
            File beforeScreenshotImg = result.getBeforeScreenshotImg();
            if (beforeScreenshotImg != null) {
                stepResult.put("BEFORE_SCREENSHOT_IMG", getBase64String(beforeScreenshotImg));
            }
            // 開發測試用截圖 - 執行步驟前
            File afterScreenshotImg = result.getAfterScreenshotImg();
            if (afterScreenshotImg != null) {
                stepResult.put("AFTER_SCREENSHOT_IMG", getBase64String(afterScreenshotImg));
            }
        }
    }

    /**
     * 產生測試結果
     * @param resultDoc
     * @param resultCounter
     * @param stepResult
     */
    private void calTestResult(Map testResult, ResultCounter resultCounter) {

        testResult.put("TEST_END_TIME", new Date(System.currentTimeMillis()));

        int stepFailCnt = resultCounter.getStepFailCnt();
        int vpFailCnt = resultCounter.getVpFailCnt();
        if (stepFailCnt == 0 && vpFailCnt == 0) {
            testResult.put("TEST_SUCCESS", true);
        } else {
            testResult.put("TEST_SUCCESS", false);
        }

        testResult.put("STEP_TOTAL_CNT", resultCounter.getStepTotalCnt());
        testResult.put("STEP_SUCCESS_CNT", resultCounter.getStepSuccessCnt());
        testResult.put("STEP_FAIL_CNT", stepFailCnt);
        testResult.put("STEP_NOTDO_CNT", resultCounter.getStepNotdoCnt());
        testResult.put("VP_TOTAL_CNT", resultCounter.getVpTotalCnt());
        testResult.put("VP_SUCCESS_CNT", resultCounter.getVpSuccessCnt());
        testResult.put("VP_FAIL_CNT", vpFailCnt);
        testResult.put("VP_NOTDO_CNT", resultCounter.getVpNotdoCnt());
    }

    /**
     * 測試結果計數類別
     * @author i9300606
     *
     */
    private class ResultCounter {

        private int stepTotalCnt = 0;

        private int stepSuccessCnt = 0;

        private int stepFailCnt = 0;

        private int vpTotalCnt = 0;

        private int vpSuccessCnt = 0;

        private int vpFailCnt = 0;

        public int getStepTotalCnt() {
            return stepTotalCnt;
        }

        public void setStepTotalCnt(int stepTotalCnt) {
            this.stepTotalCnt = stepTotalCnt;
        }

        public int getVpTotalCnt() {
            return vpTotalCnt;
        }

        public void setVpTotalCnt(int vpTotalCnt) {
            this.vpTotalCnt = vpTotalCnt;
        }

        public void stepPlus() {
            stepTotalCnt++;
        }

        public void stepSuccess() {
            stepSuccessCnt++;
        }

        public void stepFail() {
            stepFailCnt++;
        }

        public void vpPlus() {
            vpTotalCnt++;
        }

        public void vpSuccess() {
            vpSuccessCnt++;
        }

        public void vpFail() {
            vpFailCnt++;
        }

        public int getStepSuccessCnt() {
            return stepSuccessCnt;
        }

        public int getStepFailCnt() {
            return stepFailCnt;
        }

        public int getStepNotdoCnt() {
            return stepTotalCnt - stepSuccessCnt - stepFailCnt;
        }

        public int getVpSuccessCnt() {
            return vpSuccessCnt;
        }

        public int getVpFailCnt() {
            return vpFailCnt;
        }

        public int getVpNotdoCnt() {
            return vpTotalCnt - vpSuccessCnt - vpFailCnt;
        }
    }

    /**
     * 計算步驟和驗證點個數
     * @param caseStepList
     * @return
     */
    private Map calCaseStepCnt(List<Document> caseStepList) {

        Map rtnMap = new HashMap();
        int stepTotalCnt = 0;
        int vpTotalCnt = 0;
        String currentCaseKey = null;
        for (int i = 0; i < caseStepList.size(); i++) {
            Document caseStep = caseStepList.get(i);
            String stepType = caseStep.getString("STEP_TYPE");
            stepTotalCnt++;
            if ("CASE".equals(stepType)) {
                String caseKey = caseStep.getString("CASE_ID") + "_" + caseStep.getLong("TS_KEY");
                currentCaseKey = caseKey;

                if (!rtnMap.containsKey("CASE_CNT_MAP")) {
                    rtnMap.put("CASE_CNT_MAP", new HashMap());
                }
                Map caseCntMap = MapUtils.getMap(rtnMap, "CASE_CNT_MAP");

                ResultCounter rc = new ResultCounter();
                rc.stepPlus();
                caseCntMap.put(caseKey, rc);
            } else {
                if (stepType.startsWith("VP") || stepType.startsWith("DEV_VP")) {
                    vpTotalCnt++;
                }

                Boolean isFromCase = caseStep.getBoolean("IS_FROM_CASE");
                if (isFromCase != null && isFromCase) {
                    ResultCounter caseCnt = (ResultCounter) MapUtils.getMap(rtnMap, "CASE_CNT_MAP").get(currentCaseKey);
                    caseCnt.stepPlus();
                    if (stepType.startsWith("VP") || stepType.startsWith("DEV_VP")) {
                        caseCnt.vpPlus();
                    }
                }
            }
        }

        rtnMap.put("STEP_TOTAL_CNT", stepTotalCnt);
        rtnMap.put("VP_TOTAL_CNT", vpTotalCnt);
        return rtnMap;
    }
}