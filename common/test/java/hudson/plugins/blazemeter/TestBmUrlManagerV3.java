package hudson.plugins.blazemeter;

import com.blaze.api.urlmanager.BmUrlManager;
import com.blaze.api.urlmanager.BmUrlManagerV3Impl;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dzmitrykashlach on 9/01/15.
 */

public class TestBmUrlManagerV3 {
    private String userKey="881a84b35e97c4342bf11";
    private String appKey="jnk100x987c06f4e10c4";
    private String testId="123456789";
    private String masterId ="987654321";
    private String fileName="111111111";
    private BmUrlManager bmUrlManager= new BmUrlManagerV3Impl(TestConstants.mockedApiUrl);

    @Test
    public void getServerUrl(){
        Assert.assertTrue(bmUrlManager.getServerUrl().equals(TestConstants.mockedApiUrl));
    }

    @Test
    public void setServerUrl(){
        bmUrlManager.setServerUrl(TestConstants.mockedApiUrl);
        Assert.assertTrue(bmUrlManager.getServerUrl().equals(TestConstants.mockedApiUrl));
    }

    @Test
    public void testStatus(){
        String expTestGetStatus=bmUrlManager.getServerUrl()+"/api/latest/masters/"
                + masterId +"/status?events=false&api_key="+userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actTestGetStatus=bmUrlManager.masterStatus(appKey, userKey, masterId);
        Assert.assertEquals(expTestGetStatus, actTestGetStatus);
    }

    @Test
    public void getTests(){
    String expGetTestsUrl=bmUrlManager.getServerUrl()+"/api/web/tests?api_key="+userKey+
            "&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
    String actGetTestsUrl=bmUrlManager.tests(appKey, userKey);
        Assert.assertEquals(expGetTestsUrl, actGetTestsUrl);
    }


    @Test
    public void testStop_masters(){
        String expTestStop=bmUrlManager.getServerUrl()+"/api/latest/masters/"
                +testId+"/stop?api_key="+userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;

        String actTestStop=bmUrlManager.testStop(appKey, userKey, testId);
        Assert.assertEquals(expTestStop,actTestStop);
    }

    @Test
    public void testTerminate_masters(){
        String expTestTerminate=bmUrlManager.getServerUrl()+"/api/latest/masters/"
                +testId+"/terminate?api_key="+userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;

        String actTestTerminate=bmUrlManager.testTerminate(appKey, userKey, testId);
        Assert.assertEquals(expTestTerminate, actTestTerminate);
    }

    @Test
    public void testReport(){
        String expTestReport=bmUrlManager.getServerUrl()+"/api/latest/masters/"
                + masterId +"/reports/main/summary?api_key="+userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actTestReport=bmUrlManager.testReport(appKey, userKey, masterId);
        Assert.assertEquals(expTestReport, actTestReport);

    }

    @Test
    public void getUser(){
        String expGetUser=bmUrlManager.getServerUrl()+"/api/latest/user?api_key="+userKey+
                "&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actGetUser=bmUrlManager.getUser(appKey, userKey);
        Assert.assertEquals(expGetUser,actGetUser);
    }


    @Test
    public void getCIStatus(){
        String expCIStatus=bmUrlManager.getServerUrl()+"/api/latest/masters/"+ masterId +"/ci-status?api_key="
                +userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actCIStatus=bmUrlManager.ciStatus(appKey, userKey, masterId);
        Assert.assertEquals(expCIStatus,actCIStatus);
    }

    @Test
    public void getTestInfo(){
        String expGetTestInfo=bmUrlManager.getServerUrl()+"/api/latest/tests/"+testId+"?api_key="+userKey+"&app_key="+appKey
                +BmUrlManager.CLIENT_IDENTIFICATION;
        String actGetTestInfo=bmUrlManager.testConfig(appKey, userKey, testId);
        Assert.assertEquals(expGetTestInfo,actGetTestInfo);
    }

    @Test
    public void postJsonConfig(){
        String expPutTestInfo=bmUrlManager.getServerUrl()+"/api/latest/tests/"+testId+
                "/custom?custom_test_type=yahoo&api_key="+userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actPutTestInfo=bmUrlManager.postJsonConfig(appKey, userKey, testId);
        Assert.assertEquals(expPutTestInfo,actPutTestInfo);
    }

    @Test
    public void createTest(){
        String expCreateTest=bmUrlManager.getServerUrl()+"/api/latest/tests/custom?custom_test_type=yahoo&api_key="
                +userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actCreateTest=bmUrlManager.createTest(appKey, userKey);
        Assert.assertEquals(expCreateTest,actCreateTest);
    }

    @Test
    public void retrieveJUNITXML(){
        String expRetrieveJUNITXML=bmUrlManager.getServerUrl()+"/api/latest/masters/"+ masterId +
                "/reports/thresholds?format=junit&api_key="
                +userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actRetrieveJUNITXML=bmUrlManager.retrieveJUNITXML(appKey, userKey, masterId);
        Assert.assertEquals(expRetrieveJUNITXML,actRetrieveJUNITXML);
    }

    @Test
    public void generatePublicToken_masters(){
        String expGenPublicToken=bmUrlManager.getServerUrl()+"/api/latest/masters/"+ masterId +
                "/publicToken?api_key="
                +userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actGenPublicToken=bmUrlManager.generatePublicToken(appKey, userKey, masterId);
        Assert.assertEquals(expGenPublicToken,actGenPublicToken);
    }

    @Test
    public void listOfSessions(){
        String expListOfSessionIds=bmUrlManager.getServerUrl()+"/api/latest/masters/"+ masterId +
                "/sessions?api_key="+userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actListOfSessionsIds=bmUrlManager.listOfSessionIds(appKey, userKey, masterId);
        Assert.assertEquals(expListOfSessionIds,actListOfSessionsIds);
    }

    @Test
    public void activeTests(){
        String expActiveTests=bmUrlManager.getServerUrl()+"/api/latest/web/active?api_key="
                +userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actActiveTests=bmUrlManager.activeTests(appKey, userKey);
        Assert.assertEquals(expActiveTests,actActiveTests);
    }
}