package com.blaze.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.blaze.Utils;
import com.blaze.entities.TestInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.blaze.runner.Constants;

/**
 * 
 * @author 
 *
 */
public class BlazemeterApiV2Impl implements BlazemeterApi {
//    BuildProgressLogger logger=null;

	private String serverName;
	private int serverPort;
	private String username;
	private String password;
	
    public static final String APP_KEY = "tmcbzms4sbnsgb1z0hry";
    BzmHttpClient bzmHttpClient;
    BmUrlManager urlManager;

    public BlazemeterApiV2Impl(String serverName, int serverPort, String username, String password, String bzmUrl) {
    	this.serverName = serverName;
    	this.serverPort = serverPort;
    	this.username = username;
    	this.password = password;
//        this.logger = logger;
        urlManager = new BmUrlManager(bzmUrl);
        try {
            bzmHttpClient = new BzmHttpClient(this.serverName,this.username,this.password,this.serverPort);
            bzmHttpClient.configureProxy();
        } catch (Exception ex) {
            ex.printStackTrace();
//            logger.message("error Instantiating HTTPClient. Exception received: " + ex);
        }
    }




    /**
     * @param userKey  - user key
     * @param testId   - test id
     * @param fileName - test name
     * @param pathName - jmx file path
     *                 //     * @return test id
     *                 //     * @throws java.io.IOException
     *                 //     * @throws org.json.JSONException
     */
    @Override
    public synchronized boolean uploadJmx(String userKey, String testId, String fileName, String pathName) {

        if (!validate(userKey, testId)) return false;

        String url = this.urlManager.scriptUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = Utils.getFileContents(pathName);

        try {
            jmxData.put("data", fileCon);
        } catch (JSONException e) {
            System.err.format(e.getMessage());
            return false;
        }

        this.bzmHttpClient.getJson(url, jmxData);
        return true;
    }

    @Override
    public synchronized JSONObject uploadFile(String userKey, String testId, String fileName, String pathName) {

        if (!validate(userKey, testId)) return null;

        String url = this.urlManager.fileUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = Utils.getFileContents(pathName);

        try {
            jmxData.put("data", fileCon);
        } catch (JSONException e) {
            System.err.format(e.getMessage());
        }

        return this.bzmHttpClient.getJson(url, jmxData);
    }

    @Override
    public TestInfo getTestRunStatus(String userKey, String testId) {
        TestInfo ti = new TestInfo();

        if (!validate(userKey, testId)) {
            ti.setStatus(Constants.TestStatus.NotFound);
            return ti;
        }

        try {
            String url = this.urlManager.testStatus(APP_KEY, userKey, testId);
            JSONObject jo = this.bzmHttpClient.getJson(url, null);

            if (jo.get("status") == "Test not found")
                ti.setStatus(Constants.TestStatus.NotFound);
            else {
                ti.setId(jo.getString("test_id"));
                ti.setName( jo.getString("test_name"));
                ti.setStatus(jo.getString("status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
//            logger.message("error getting status " + e);
            ti.setStatus(Constants.TestStatus.Error);
        }
        return ti;
    }

    @Override
    public synchronized JSONObject startTest(String userKey, String testId) {

        if (!validate(userKey, testId)) return null;

        String url = this.urlManager.testStart(APP_KEY, userKey, testId);
//        logger.message("Requesting " + url);
        return this.bzmHttpClient.getJson(url, null);
    }


    private boolean validate(String userKey, String testId) {
        if (userKey == null || userKey.trim().isEmpty()) {
//            logger.message("startTest userKey is empty");
            return false;
        }

        if (testId == null || testId.trim().isEmpty()) {
//            logger.message("testId is empty");
            return false;
        }
        return true;
    }

    /**
     * @param userKey - user key
     * @param testId  - test id
     *                //     * @throws IOException
     *                //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject stopTest(String userKey, String testId) {
        if (!validate(userKey, testId)) return null;

        String url = this.urlManager.testStop(APP_KEY, userKey, testId);
        return this.bzmHttpClient.getJson(url, null);
    }

    /**
     * @param userKey  - user key
     * @param reportId - report Id same as Session Id, can be obtained from start stop status.
     *                 //     * @throws IOException
     *                 //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject aggregateReport(String userKey, String reportId) {
        if (!validate(userKey, reportId)) return null;

        String url = this.urlManager.testAggregateReport(APP_KEY, userKey, reportId);
        return this.bzmHttpClient.getJson(url, null);
    }

    @Override
    public HashMap<String, String> getTestList(String userKey) throws IOException {

        LinkedHashMap<String, String> testListOrdered = null;

        if (userKey == null || userKey.trim().isEmpty()) {
//            logger.message("getTests userKey is empty");
        } else {
            String url = this.urlManager.getTests(APP_KEY, userKey);
//            logger.message(url);
            JSONObject jo = this.bzmHttpClient.getJson(url, null);
            try {
                String r = jo.get("response_code").toString();
                if (r.equals("200")) {
                    JSONArray arr = (JSONArray) jo.get("tests");
                    testListOrdered = new LinkedHashMap<String, String>(arr.length());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject en = null;
                        try {
                            en = arr.getJSONObject(i);
                        } catch (JSONException e) {
//                            logger.message("Error with the JSON while populating test list, " + e);
                        }
                        String id;
                        String name;
                        try {
                            if (en != null) {
                                id = en.getString("test_id");
                                name = en.getString("test_name").replaceAll("&", "&amp;");
                                testListOrdered.put(name, id);

                            }
                        } catch (JSONException ie) {
//                            logger.message("Error with the JSON while populating test list, " + ie);
                        }
                    }
                }
            }
            catch (Exception e) {
//                logger.message("Error while populating test list, " + e);
            }
        }

        return testListOrdered;
    }
    
    

    public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
