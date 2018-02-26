package com.cathay.test.selenium;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestReportHelper {

    private Configuration configuration;

    private static final SimpleDateFormat wordCreated_sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    private static final SimpleDateFormat testTime_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final SimpleDateFormat fileNm_sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

    static {
        wordCreated_sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public TestReportHelper() {
        this("/com/cathay/qr/t0/template");
    }

    public TestReportHelper(String templatePathPrefix) {
        configuration = new Configuration();
        configuration.setDefaultEncoding("utf-8");
        configuration.setClassForTemplateLoading(TestReportHelper.class, templatePathPrefix);
    }

    public File createWordTestReport(Document testComponent, Document testResult, String wordCreater) throws TemplateException, IOException {

        Map<String, Object> dataMap = new HashMap<String, Object>();
        Date d = new Date();

        dataMap.put("AUTHOR", wordCreater);
        dataMap.put("CREATED", wordCreated_sdf.format(d));

        dataMap.put("OPR_NM", getDocString(testResult, "OPR_NM"));
        Date TEST_STR_TIME = testResult.getDate("TEST_STR_TIME");
        Date TEST_END_TIME = testResult.getDate("TEST_END_TIME");
        long diffTime = TEST_END_TIME.getTime() - TEST_STR_TIME.getTime();
        StringBuilder sb = new StringBuilder();
        sb.append(TimeUnit.MINUTES.convert(diffTime, TimeUnit.MILLISECONDS)).append(" 分 ")
                .append(TimeUnit.SECONDS.convert(diffTime, TimeUnit.MILLISECONDS) % 60).append(" 秒");
        dataMap.put("TEST_TIME", sb.toString());
        sb.setLength(0);
        dataMap.put("TEST_STR_TIME", testTime_sdf.format(TEST_STR_TIME));
        dataMap.put("TEST_END_TIME", testTime_sdf.format(TEST_END_TIME));

        String sysCdNm = getDocString(testResult, "SYS_CD") + getDocString(testResult, "SYS_NM");
        String caseNm = getDocString(testResult, "CASE_NM");
        dataMap.put("SYS_CD_NM", sysCdNm);
        dataMap.put("CASE_NM", caseNm);
        dataMap.put("FUNC_ID", getDocString(testResult, "FUNC_ID"));
        dataMap.put("BROWSER_TYPE", getDocString(testResult, "BROWSER_TYPE"));

        dataMap.put("STEP_SUCCESS_CNT", testResult.getInteger("STEP_SUCCESS_CNT"));
        dataMap.put("STEP_FAIL_CNT", testResult.getInteger("STEP_FAIL_CNT"));
        dataMap.put("STEP_NOTDO_CNT", testResult.getInteger("STEP_NOTDO_CNT"));
        dataMap.put("STEP_TOTAL_CNT", testResult.getInteger("STEP_TOTAL_CNT"));
        dataMap.put("VP_SUCCESS_CNT", testResult.getInteger("VP_SUCCESS_CNT"));
        dataMap.put("VP_FAIL_CNT", testResult.getInteger("VP_FAIL_CNT"));
        dataMap.put("VP_NOTDO_CNT", testResult.getInteger("VP_NOTDO_CNT"));
        dataMap.put("VP_TOTAL_CNT", testResult.getInteger("VP_TOTAL_CNT"));

        List<Map> caseResultList = new ArrayList<Map>();
        Map<String, Map> caseStepResult = (Map<String, Map>) testResult.get("CASE_STEP_RESULT");
        int imageSeq = 0;
        for (Entry<String, Map> entry : caseStepResult.entrySet()) {
            Map m = new HashMap();
            Map caseResult = entry.getValue();

            m.put("CASE_PURP_NM", MapUtils.getString(caseResult, "CASE_PURP_NM"));
            m.put("CASE_NM", MapUtils.getString(caseResult, "CASE_NM"));
            int CASE_VP_SUCCESS_CNT = MapUtils.getIntValue(caseResult, "CASE_VP_SUCCESS_CNT", 0);
            int CASE_VP_TOTAL_CNT = MapUtils.getIntValue(caseResult, "CASE_VP_TOTAL_CNT", 0);
            m.put("CASE_VP_SUCCESS_CNT", CASE_VP_SUCCESS_CNT);
            m.put("CASE_VP_TOTAL_CNT", CASE_VP_TOTAL_CNT);
            if (CASE_VP_TOTAL_CNT != 0 && CASE_VP_SUCCESS_CNT == CASE_VP_TOTAL_CNT) {
                m.put("CASE_IS_PASS", true);
            } else {
                m.put("CASE_IS_PASS", false);
            }

            List<Map> caseSteps = (List<Map>) caseResult.get("CASE_STEP");
            List<Map> stepResults = (List<Map>) caseResult.get("STEP_RESULT");
            for (int i = 0; i < stepResults.size(); i++) {
                Map stepResult = stepResults.get(i);
                boolean isSuccess = MapUtils.getBooleanValue(stepResult, "IS_SUCCESS");
                Map caseStep = caseSteps.get(i);
                caseStep.put("IS_SUCCESS", isSuccess);
                if (!isSuccess) {
                    caseStep.put("EXPECT", stepResult.get("EXPECT"));
                    caseStep.put("ACTUAL", stepResult.get("ACTUAL"));
                    caseStep.put("SCREENSHOT_IMG", stepResult.get("SCREENSHOT_IMG"));
                    caseStep.put("SCREENSHOT_IMG_NAME", "wordml://" + imageSeq + ".png");
                    imageSeq++;
                }

                String stepType = MapUtils.getString(caseStep, "STEP_TYPE");
                if (StringUtils.equalsIgnoreCase("BUTTON", stepType)) {
                    caseStep.put("BEFORE_SCREENSHOT_IMG", stepResult.get("BEFORE_SCREENSHOT_IMG"));
                    caseStep.put("BEFORE_SCREENSHOT_IMG_NAME", "wordml://" + imageSeq + ".png");
                    imageSeq++;
                    caseStep.put("AFTER_SCREENSHOT_IMG", stepResult.get("AFTER_SCREENSHOT_IMG"));
                    caseStep.put("AFTER_SCREENSHOT_IMG_NAME", "wordml://" + imageSeq + ".png");
                    imageSeq++;
                }
            }

            for (Map caseStep : caseSteps) {
                String stepType = MapUtils.getString(caseStep, "STEP_TYPE");
                Map component = (Map) testComponent.get(stepType);
                caseStep.put("NAME", component.get("name"));
                caseStep.put("COLOR", component.get("label-color"));
                caseStep.put("STEP_ARGS", ((List) caseStep.get("STEP_ARGS")).toString());
                if (!caseStep.containsKey("IS_SUCCESS")) {
                    caseStep.put("IS_NOT_DO", true);
                } else {
                    caseStep.put("IS_NOT_DO", false);
                }
            }
            m.put("CASE_STEP_RESULT", caseSteps);
            caseResultList.add(m);
        }
        dataMap.put("caseResultList", caseResultList);

        List<Map> stepResultList = (List<Map>) testResult.get("STEP_RESULT");
        for (Map stepResult : stepResultList) {
            String stepType = MapUtils.getString(stepResult, "STEP_TYPE");
            Map component = (Map) testComponent.get(stepType);
            stepResult.put("NAME", component.get("name"));
            stepResult.put("COLOR", component.get("label-color"));
            stepResult.put("STEP_ARGS", String.valueOf(((List) stepResult.get("STEP_ARGS"))));
            if (!stepResult.containsKey("IS_SUCCESS")) {
                stepResult.put("IS_NOT_DO", true);
            } else {
                stepResult.put("IS_NOT_DO", false);
                boolean isSuccess = MapUtils.getBooleanValue(stepResult, "IS_SUCCESS");
                if (!isSuccess) {
                    stepResult.put("SCREENSHOT_IMG_NAME", "wordml://" + imageSeq + ".png");
                    imageSeq++;
                }
            }

            if (StringUtils.equalsIgnoreCase("BUTTON", stepType)) {
                stepResult.put("BEFORE_SCREENSHOT_IMG_NAME", "wordml://" + imageSeq + ".png");
                imageSeq++;
                stepResult.put("AFTER_SCREENSHOT_IMG_NAME", "wordml://" + imageSeq + ".png");
                imageSeq++;
            }
        }

        dataMap.put("stepResultList", stepResultList);

        Template freemarkerTemplate = configuration.getTemplate("TestReport.xml");
        String outputWordFileNm = sb.append(fileNm_sdf.format(d)).append("_").append(sysCdNm).append("_").append(caseNm).append("$")
                .toString();
        File outputWordFile = File.createTempFile(outputWordFileNm, ".doc");
        outputWordFile.deleteOnExit();
        createWordFile(dataMap, freemarkerTemplate, outputWordFile);
        return outputWordFile;
    }

    /**
     * 以 Template XML 產生 Word
     * @param dataMap
     * @param template
     * @param outputWordFile
     * @throws TemplateException
     * @throws IOException
     */
    private void createWordFile(Map<?, ?> dataMap, Template template, File outputWordFile) throws TemplateException, IOException {
        Template t = template;
        Writer w = null;
        try {
            w = new OutputStreamWriter(new FileOutputStream(outputWordFile), "utf-8");
            t.process(dataMap, w);
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    /**
     * 取 MongoDB Document 裡的字串值
     * @param doc
     * @param key
     * @return
     */
    private String getDocString(Document doc, String key) {
        String val = doc.getString(key);
        if (val == null) {
            return "";
        }
        return val;
    }
}