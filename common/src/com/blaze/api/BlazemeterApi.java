package com.blaze.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.blaze.entities.TestInfo;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.blaze.runner.BlazeMeterConstants;

/**
 * 
 * @author 
 *
 */
public class BlazemeterApi {
//    BuildProgressLogger logger=null;

	private String serverName;
	private int serverPort;
	private String username;
	private String password;
	
    public static final String APP_KEY = "tmcbzms4sbnsgb1z0hry";
    DefaultHttpClient httpClient;
    BmUrlManager urlManager;

    public BlazemeterApi(String serverName, int serverPort, String username, String password) {
    	this.serverName = serverName;
    	this.serverPort = serverPort;
    	this.username = username;
    	this.password = password;
//        this.logger = logger;
        urlManager = new BmUrlManager("https://a.blazemeter.com");
        try {
            httpClient = new DefaultHttpClient();
            configureProxy();
        } catch (Exception ex) {
            ex.printStackTrace();
//            logger.message("error Instantiating HTTPClient. Exception received: " + ex);
        }
    }
    
    private void configureProxy(){
    	// Configure the proxy if necessary
        if (getServerName() != null && !getServerName().isEmpty() && getServerPort() > 0) {
            if (getUsername() != null && !getUsername().isEmpty()){
            	Credentials cred = new UsernamePasswordCredentials(getUsername(), getPassword());
                httpClient.getCredentialsProvider().setCredentials(new AuthScope(getServerName(), getServerPort()), cred);
            }
            HttpHost proxyHost = new HttpHost(getServerName(), getServerPort());
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
        }    	
    }

    private HttpResponse getResponse(String url, JSONObject data) throws IOException {

//        logger.message("Requesting : " + url);
        if(data!=null){
//            logger.message("Request data : " + data.toString());
        }
        HttpPost postRequest = new HttpPost(url);
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Content-type", "application/json; charset=UTF-8");

        if (data != null) {
            postRequest.setEntity(new StringEntity(data.toString()));
        }

        HttpResponse response = null;
        try {
            response = this.httpClient.execute(postRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            String error = response.getStatusLine().getReasonPhrase();
            if ((statusCode >= 300) || (statusCode < 200)) {
                throw new RuntimeException(String.format("Failed : %d %s", statusCode, error));
            }
        } 
        catch (UnknownHostException uhe) {
        	System.err.format("Unknown host '" + getServerName() + "', check proxy settings! \n");
        }
        catch (Exception e) {
            System.err.format("Wrong response: %s\n", e);
        }

        return response;
    }

    @SuppressWarnings("deprecation")
	private HttpResponse getResponseForFileUpload(String url, File file) throws IOException {

//        logger.message("Requesting : " + url);
        HttpPost postRequest = new HttpPost(url);
        postRequest.setHeader("Accept", "application/json");
        postRequest.setHeader("Content-type", "application/json; charset=UTF-8");

        if (file != null) {
            postRequest.setEntity(new FileEntity(file, "text/plain; charset=\"UTF-8\""));
        }

        HttpResponse response = null;
        try {
            response = this.httpClient.execute(postRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            String error = response.getStatusLine().getReasonPhrase();
            if ((statusCode >= 300) || (statusCode < 200)) {
                throw new RuntimeException(String.format("Failed : %d %s", statusCode, error));
            }
        } catch (Exception e) {
            System.err.format("Wrong response: %s\n", e);
        }

        return response;
    }


    private JSONObject getJsonForFileUpload(String url, File file) {
        JSONObject jo = null;
        try {
            HttpResponse response = getResponseForFileUpload(url, file);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
//                logger.message(output);
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
//            logger.message("error decoding Json " + e);
        } catch (JSONException e) {
            e.printStackTrace();
//            logger.message("error decoding Json " + e);
        }
        return jo;
    }

    private JSONObject getJson(String url, JSONObject data) {
        JSONObject jo = null;
        try {
            HttpResponse response = getResponse(url, data);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
//                logger.message(output);
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
           e.printStackTrace();
//            logger.message("error decoding Json " + e);
        } catch (JSONException e) {
            e.printStackTrace();
//            logger.message("error decoding Json " + e);
        }
        return jo;
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
    public synchronized boolean uploadJmx(String userKey, String testId, String fileName, String pathName) {

        if (!validate(userKey, testId)) return false;

        String url = this.urlManager.scriptUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = getFileContents(pathName);

        try {
            jmxData.put("data", fileCon);
        } catch (JSONException e) {
            System.err.format(e.getMessage());
            return false;
        }

        getJson(url, jmxData);
        return true;
    }

    /**
     * @param userKey  - user key
     * @param testId   - test id
     * @param file     - the file (Java class) you like to upload
     * @return test id
     *         //     * @throws java.io.IOException
     *         //     * @throws org.json.JSONException
     */

    public synchronized JSONObject uploadBinaryFile(String userKey, String testId, File file) {

        if (!validate(userKey, testId)) return null;

        String url = this.urlManager.fileUpload(APP_KEY, userKey, testId, file.getName());

        return getJsonForFileUpload(url, file);
    }

    public synchronized JSONObject uploadFile(String userKey, String testId, String fileName, String pathName) {

        if (!validate(userKey, testId)) return null;

        String url = this.urlManager.fileUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = getFileContents(pathName);

        try {
            jmxData.put("data", fileCon);
        } catch (JSONException e) {
            System.err.format(e.getMessage());
        }

        return getJson(url, jmxData);
    } 


    private String getFileContents(String fn) {

        // ...checks on aFile are elided
        StringBuilder contents = new StringBuilder();
        File aFile = new File(fn);

        try {

            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(aFile));

            try {
                String line;    // not declared within while loop

                /*
                 *         readLine is a bit quirky : it returns the content of a line
                 *         MINUS the newline. it returns null only for the END of the
                 *         stream. it returns an empty String if two newlines appear in
                 *         a row.
                 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ignored) {
        }

        return contents.toString();
    }


    public TestInfo getTestRunStatus(String userKey, String testId) {
        TestInfo ti = new TestInfo();

        if (!validate(userKey, testId)) {
            ti.setStatus(BlazeMeterConstants.TestStatus.NotFound);
            return ti;
        }

        try {
            String url = this.urlManager.testStatus(APP_KEY, userKey, testId);
            JSONObject jo = getJson(url, null);

            if (jo.get("status") == "Test not found")
                ti.setStatus(BlazeMeterConstants.TestStatus.NotFound);
            else {
                ti.setId(jo.getString("test_id"));
                ti.setName( jo.getString("test_name"));
                ti.setStatus(jo.getString("status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
//            logger.message("error getting status " + e);
            ti.setStatus(BlazeMeterConstants.TestStatus.Error);
        }
        return ti;
    }

    public synchronized JSONObject startTest(String userKey, String testId) {

        if (!validate(userKey, testId)) return null;

        String url = this.urlManager.testStart(APP_KEY, userKey, testId);
//        logger.message("Requesting " + url);
        return getJson(url, null);
    }

    public int getTestCount(String userKey) throws JSONException, IOException {
        if (userKey == null || userKey.trim().isEmpty()) {
//            logger.message("getTests userKey is empty");
            return 0;
        }

        String url = getUrlForTestList(APP_KEY, userKey);

        JSONObject jo = getJson(url, null);
        String r = jo.get("response_code").toString();
        if (!r.equals("200"))
            return 0;
        JSONArray arr = (JSONArray) jo.get("tests");
        return arr.length();
    }

    private String getUrlForTestList(String appKey, String userKey) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return String.format("https://a.blazemeter.com/api/rest/blazemeter/getTests.json/?app_key=%s&user_key=%s&test_id=all", appKey, userKey);

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
    public JSONObject stopTest(String userKey, String testId) {
        if (!validate(userKey, testId)) return null;

        String url = this.urlManager.testStop(APP_KEY, userKey, testId);
        return getJson(url, null);
    }

    /**
     * @param userKey  - user key
     * @param reportId - report Id same as Session Id, can be obtained from start stop status.
     *                 //     * @throws IOException
     *                 //     * @throws ClientProtocolException
     */
    public JSONObject aggregateReport(String userKey, String reportId) {
        if (!validate(userKey, reportId)) return null;

        String url = this.urlManager.testAggregateReport(APP_KEY, userKey, reportId);
        return getJson(url, null);
    }

    public HashMap<String, String> getTestList(String userKey) throws IOException {

        LinkedHashMap<String, String> testListOrdered = null;

        if (userKey == null || userKey.trim().isEmpty()) {
//            logger.message("getTests userKey is empty");
        } else {
            String url = this.urlManager.getTests(APP_KEY, userKey);
//            logger.message(url);
            JSONObject jo = getJson(url, null);
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
