/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.blaze.agent;

import com.blaze.api.Api;
import com.blaze.api.ApiV3Impl;
import com.blaze.runner.CIStatus;
import com.blaze.runner.Constants;
import com.blaze.runner.JsonConstants;
import com.blaze.runner.TestStatus;
import com.blaze.testresult.TestResult;
import com.blaze.utils.Utils;
import com.google.common.collect.LinkedHashMultimap;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class BzmBuild {

    private static final int CHECK_INTERVAL = 60000;
    private BuildProgressLogger logger;
    private Api api;
    private String masterId;
    private String testId;

    public BzmBuild(String userKey,String serverUrl,String testId,String httplf,BuildProgressLogger logger){
        this.api=new ApiV3Impl(userKey,serverUrl,httplf);
        this.testId=testId;
        this.logger=logger;
    };

    public boolean validateInput() throws IOException, MessagingException {
        LinkedHashMultimap<String, String> tests = api.testsMultiMap();
        Collection<String> values = tests.values();
        if (tests != null) {
            if (!values.contains(testId)) {
                logger.warning(Constants.PROBLEM_WITH_VALIDATING);
                logger.warning("Server url=" + this.api.getServerUrl());
                logger.warning("UserKey=" + this.api.getApiKey().substring(0, 4) + "...");
                logger.warning("Check the following settings: serverUrl, userKey, proxy settings at buildAgent");
                return false;
            }
        }
        return true;
    }

    public String startTest(String testId, BuildProgressLogger logger) throws IOException, JSONException {
        String masterId = null;
        HashMap<String, String> startTestResp = new HashMap<String, String>();
        try {
            boolean collection = collection(testId, this.api);
            String testId_num= Utils.getTestId(testId);
            startTestResp = api.startTest(testId_num, collection);
            this.masterId = startTestResp.get(JsonConstants.ID);
        } catch (Exception e) {
            logger.error("Exception while starting BlazeMeter Test: " + e.getMessage());
            logger.exception(e);
        }
        return this.masterId;
    }


    public static boolean collection(String testId,Api api) throws Exception{
        boolean exists=false;
        boolean collection=false;

        LinkedHashMultimap tests = api.testsMultiMap();
        Set<Map.Entry> entries = tests.entries();
        for (Map.Entry e : entries) {
            int point = ((String) e.getValue()).indexOf(".");
            if (testId.contains(((String) e.getValue()).substring(0,point))) {
                collection = (((String) e.getValue()).substring(point+1)).contains("multi");
                exists=true;
            }
            if (collection) {
                break;
            }
        }
        if(!exists){
            throw new Exception("Test with test id = "+testId+" is not present on server");
        }
        return collection;
    }


    public void junitXml(String masterId, File junitDir) {
        String junitReport = "";
        logger.message("Requesting junit report from server, masterId = " + masterId);
        try {
            junitReport = this.api.retrieveJUNITXML(masterId);
            logger.message("Received junit report from server");
            logger.message("Saving junit report to " + junitDir.getAbsolutePath());
            Utils.saveJunit(junitReport, junitDir, masterId, logger);
        } catch (Exception e) {
            logger.message("Problems with receiving junit report from server, masterId = " + masterId + ": " + e.getMessage());
        }
    }



    public boolean active(String testId, BuildProgressLogger logger){
        return api.active(testId);
    }

    public boolean stopMaster(String masterId, BuildProgressLogger logger) {
        boolean terminate = false;
        try {
            int statusCode = api.getTestMasterStatusCode(masterId);
            if (statusCode < 100 & statusCode != 0) {
                api.terminateTest(masterId);
                terminate = true;
            }
            if (statusCode >= 100 | statusCode == -1 | statusCode == 0) {
                api.stopTest(masterId);
                terminate = false;
            }
        } catch (Exception e) {
            logger.warning("Error while trying to stop test with testId = " + masterId + ", " + e.getMessage());
        } finally {
            return terminate;
        }
    }

    public void jtlReports(String masterId, File jtlDir) throws IOException, JSONException {
        List<String> sessionsIds = this.api.getListOfSessionIds(masterId);
        for (String s : sessionsIds) {
            this.retrieveJtlForSession(s, jtlDir);
        }
    }


    public String getReportUrl(String masterId) {
        JSONObject jo = null;
        String publicToken = "";
        String reportUrl = null;
        try {
            jo = this.api.generatePublicToken(masterId);
            if (jo.get(JsonConstants.ERROR).equals(JSONObject.NULL)) {
                JSONObject result = jo.getJSONObject(JsonConstants.RESULT);
                publicToken = result.getString("publicToken");
                reportUrl = this.api.getServerUrl() + "/app/?public-token = " + publicToken + "#masters/" + masterId + "/summary";
            } else {
                logger.warning("Problems with generating public-token for report URL: " + jo.get(JsonConstants.ERROR).toString());
                reportUrl = this.api.getServerUrl() + "/app/#masters/" + masterId + "/summary";
            }

        } catch (Exception e) {
            logger.warning("Problems with generating public-token for report URL");
        } finally {
            return reportUrl;
        }
    }


    public void retrieveJtlForSession(String sessionId, File jtlDir) throws IOException, JSONException {
        JSONObject jo = this.api.retrieveJtlZip(sessionId);
        String dataUrl = null;
        try {
            JSONArray data = jo.getJSONObject(JsonConstants.RESULT).getJSONArray(JsonConstants.DATA);
            for (int i = 0; i < data.length(); i++) {
                String title = data.getJSONObject(i).getString("title");
                if (title.equals("Zip")) {
                    dataUrl = data.getJSONObject(i).getString(JsonConstants.DATA_URL);
                    break;
                }
            }
            String jtlFilePath = jtlDir.getAbsolutePath() + "/" + sessionId + ".zip";

            File jtlZip = new File(jtlFilePath);
            URL url = new URL(dataUrl);
            FileUtils.copyURLToFile(url, jtlZip);
            logger.message("Downloading jtl from " + url);
            logger.message("JTL zip location: " + jtlZip.getCanonicalPath());
        } catch (JSONException e) {
            logger.warning("Unable to get jtl: "+e.getMessage());
        } catch (MalformedURLException e) {
            logger.warning("Unable to get jtl: "+e.getMessage());
        } catch (IOException e) {
            logger.warning("Unable to get jtl: "+e.getMessage());
        } catch (NullPointerException e) {
            logger.warning("Unable to get jtl: "+e.getMessage());
        }
    }

    public TestStatus masterStatus(String testId) {
        TestStatus status=null;
        try {
            status=this.api.masterStatus(testId);
        } catch (NullPointerException e){
            logger.exception(e);
        }
        return status;
    }


    public CIStatus validateCIStatus(String masterId, BuildProgressLogger logger){
        CIStatus ciStatus= CIStatus.success;
        JSONObject jo;
        JSONArray failures=new JSONArray();
        JSONArray errors=new JSONArray();
        try {
            jo= api.getCIStatus(masterId);
            logger.message("Test status object = " + jo.toString());
            failures=jo.getJSONArray(JsonConstants.FAILURES);
            errors=jo.getJSONArray(JsonConstants.ERRORS);
        } catch (JSONException je) {
            logger.message("No thresholds on server: setting 'success' for CIStatus ");
        } catch (Exception e) {
            logger.message("No thresholds on server: setting 'success' for CIStatus ");
        }finally {
            if(errors.length()>0){
                logger.message("Having errors while test status validation...");
                logger.message("Errors: " + errors.toString());
                ciStatus=CIStatus.errors;
                logger.message("Setting CIStatus = "+CIStatus.errors.name());
                return ciStatus;
            }
            if(failures.length()>0){
                logger.message("Having failures while test status validation...");
                logger.message("Failures: " + failures.toString());
                ciStatus=CIStatus.failures;
                logger.message("Setting CIStatus = "+CIStatus.failures.name());
                return ciStatus;
            }
            logger.message("No errors/failures while validating CIStatus: setting "+CIStatus.success.name());
        }
        return ciStatus;
    }

    public void waitNotActive(String testId){

        boolean active=true;
        int activeCheck=1;
        while(active&&activeCheck<11){
            try {
                Thread.currentThread().sleep(CHECK_INTERVAL);
            } catch (InterruptedException e) {
                logger.warning("Thread was interrupted during sleep()");
                logger.warning("Received interrupted Exception: " + e.getMessage());
                break;
            }
            logger.message("Checking, if test is active, testId = "+testId+", retry # "+activeCheck);
            active=this.api.active(testId);
            activeCheck++;
        }
    }


    public TestResult getReport(BuildProgressLogger logger) {
        TestResult testResult = null;
        try {
            JSONObject aggregate = this.api.testReport(this.masterId);
            testResult = new TestResult(aggregate);
        } catch (JSONException e) {
            logger.warning("Failed to get aggregate report from server " + e);
        } catch (IOException e) {
            logger.warning("Failed to get aggregate report from server" + e);
        } catch (NullPointerException e) {
            logger.warning("Failed to get aggregate report from server" + e);
        } finally {
            return testResult;
        }
    }

    public String masterId() {
        return masterId;
    }
}
