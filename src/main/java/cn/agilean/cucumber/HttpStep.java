package cn.agilean.cucumber;

import static com.jayway.restassured.http.ContentType.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.util.StringUtils.hasText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.config.SSLConfig;
import com.jayway.restassured.config.SessionConfig;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;

/**
 * Cucumber 与 HTTP
 * <p>
 *
 *
 * @author Alex Lei.
 * @author Li Chun.
 *
 */
public class HttpStep {

    static final Logger logger = LoggerFactory.getLogger(HttpStep.class);

    private static final HashMap<String, ContentType> contentTypes = new HashMap<String, ContentType>();

    private String sutBaseUri;

    private RequestSpecification sutRequest;

    private ValidatableResponse sutResponse;

    private HashMap<String, Object> updata;

    private String sutSessionId;

    private String sutProxy;

    KeyValueParser textParser = new KeyValueParser();

    RestAssuredConfig config = RestAssuredConfig.config();

    /**
     * cucumber中设置的 header
     */
    Map<String, String> headers = new HashMap<String, String>();
    /**
     * cucumber设置的cookie
     */
    Map<String, String> cookies = new HashMap<String, String>();

    /**
     * 暴力传递COOKIE信息。
     * XXX 这个如果域不一样会出问题，操蛋的设置方式，改变httpclient失败，共享一个的时候会爆cliet没有关闭不能继续使用。下一次试试复用cookie store CookieStore
     */
    private Map<String, String> sessionCookies = Collections.EMPTY_MAP;

    public HttpStep() {
        sutRequest = RestAssured.given();
        System.out.println("new instance.");
        contentTypes.put("FORM", URLENC);
        contentTypes.put("JSON", JSON);
        resetRequest();
    }

    /**
     *
     */
    void afterResponse() {
        sessionCookies = this.sutResponse.extract().cookies();
    }

    @Before
    public void before_cucumber_scenario() {
        File f = new File("config.properties");
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(f));
            System.out.println("Loading config");
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            return;
        }
        withProperties(p);
    }

    @Given("^配置文件$")
    public void config_properties(String propertiesblock) throws IOException {
        Properties p = new Properties();
        p.load(new StringReader(propertiesblock));
        withProperties(p);
    }

    private void withProperties(Properties p) {
        if (p.containsKey("config.sessionConfig.sessionIdName")) {
            SessionConfig sessionConfig = this.config.getSessionConfig();
            sessionConfig = sessionConfig.sessionIdName(
                    p.getProperty("config.sessionConfig.sessionIdName"));
            this.config = this.config.sessionConfig(sessionConfig);
        }
        RestAssured.config = this.config;
        resetRequest();
    }

    @Given("^HEADER$")
    public void headers(String properties) throws IOException {
        Properties p = new Properties();
        p.load(new StringReader(properties));
        Set<Entry<Object, Object>> entrySet = p.entrySet();
        for (Entry<Object, Object> item : entrySet) {
            headers.put((String) item.getKey(), (String) item.getValue());
        }
    }

    @Given("^HEADER\\s+(.*?)\\s?=\\s?(.*?)$")
    public void header(String key, String value) {
        headers.put(key, value);
    }

    @Given("^COOKIE$")
    public void cookies(String properties) throws IOException {
        Properties p = new Properties();
        p.load(new StringReader(properties));
        Set<Entry<Object, Object>> entrySet = p.entrySet();
        for (Entry<Object, Object> item : entrySet) {
            cookies.put((String) item.getKey(), (String) item.getValue());
        }
    }

    @Given("^COOKIE\\s+(.*?)\\s?=\\s?(.*?)$")
    public void cookie(String key, String value) {
        cookies.put(key, value);
    }

    private RequestSpecification prepareSession() {
        if (sutSessionId != null)
            sutRequest = sutRequest.sessionId(sutSessionId);
        // else
        // System.out.println("[NO SESSION]");
        return sutRequest.headers(headers).cookies(cookies)
                .cookies(sessionCookies).log().all();
    }

    private void resetRequest() {
        sutRequest = RestAssured.given();
        if (sutProxy != null)
            this.sutProxy(sutProxy);
        if (sutBaseUri != null)
            this.sutBase(sutBaseUri);

        updata = new HashMap<String, Object>();
    }

    private void sutProxy(String proxy) {
        sutRequest.proxy(proxy);
    }

    private void let(String key, String value) {
        updata.put(key, value);
    }

    @Given("^LET ([^\\=|\\s]+) = \"(.*)\"$")
    public void quotedLet(String fieldName, String fieldValue)
            throws Throwable {
        let(fieldName, fieldValue);
    }

    @Given("^LET ([^\\=|\\s]+) = ([^\"].*|.*[^\"])$")
    public void normalLet(String fieldName, String fieldValue)
            throws Throwable {
        let(fieldName, fieldValue);
    }

    @Given("^GET (.*)")
    public void requestUsingGet(String url) throws Throwable {
        sutResponse = prepareSession().when().get(url).then().log().all();
        afterResponse();
        resetRequest();
    }

    @Given("^POST (FORM|JSON)? (.*)$")
    public void post(String contentType, String url,String body)  {
        if(hasText(body)){
            Map<String, String> requestParam = textParser.parse(body);
            updata.putAll(requestParam);
        }
        prepareSession().contentType(contentTypes.get(contentType));
        if (contentType.equals("FORM"))
            sutRequest.formParameters(updata);
        else
            sutRequest.body(updata);
        sutResponse = sutRequest.when().post(url).then();
        afterResponse();
        resetRequest();
    }

    @Given("^DELETE (.*)$")
    public void delete(String url) throws Throwable {
        sutResponse = prepareSession().when().delete(url).then();
        resetRequest();
    }

    @Given("^PUT (FORM|JSON)? (.*)$")
    public void put(String contentType, String url) throws Throwable {
        prepareSession().contentType(contentTypes.get(contentType));
        if (contentType.equals("FORM"))
            sutRequest.formParameters(updata);
        else
            sutRequest.body(updata);
        sutResponse = sutRequest.when().put(url).then();
        resetRequest();
    }

    @Given("^STATUS (\\d{3})$")
    public void status(int code) throws Throwable {
        sutResponse.statusCode(code);
    }

    @Given("^SESSION (START|END|PRINT)$")
    public void session(String action) {
        if (action.equals("START"))
            this.sutSessionId = sutResponse.extract().sessionId();
        else if (action.equals("END"))
            this.sutSessionId = null;
        else if (action.equals("PRINT"))
            System.out.println("CURRENT SESSION:" + sutSessionId);
        else
            this.sutSessionId = action;
    }

    @Given("^THEN$")
    public void then() throws Throwable {
        resetRequest();
        if (sutBaseUri != null)
            sutBase(sutBaseUri);
    }

    @Given("^JSONPATH (\\S+) (.*)$")
    public void jsonPath(String jsonPath, String jsonValue) throws Throwable {
        if (jsonValue.matches("\\d+")) {
            if (jsonValue.endsWith(".size()")) {
                sutResponse.defaultParser(Parser.JSON).body(jsonPath,
                        hasSize(Integer.parseInt(jsonValue)));
            } else {
                sutResponse.defaultParser(Parser.JSON).body(jsonPath,
                        is(Integer.parseInt(jsonValue)));
            }
        } else {
            if (jsonValue.startsWith("\"")) {
                jsonValue = jsonValue.substring(1);
            }
            if (jsonValue.endsWith("\"")) {
                jsonValue = jsonValue.substring(0, jsonValue.length() - 1);
            }

            sutResponse.defaultParser(Parser.JSON).body(jsonPath,
                    is(jsonValue));
        }
    }

    @Given("^XMLPATH (\\S+) (.*)$")
    public void xmlPath(String xPath, String xValue) throws Throwable {
        sutResponse.defaultParser(Parser.XML).body(xPath, is(xValue));
    }

    @Given("^BASE (.+)$")
    public void sutBase(String url) {
        if (url.startsWith("https://"))
            sutRequest.config(RestAssuredConfig.config().sslConfig(
                    SSLConfig.sslConfig().relaxedHTTPSValidation("SSL")));
        sutRequest.baseUri(sutBaseUri = url);
    }

    @Given("^PROXY (.*?)$")
    public void configProxy(String url) throws Throwable {
        sutRequest.proxy(url);
        this.sutProxy = url;
    }

    @Given("^CONTAINS (.+)$")
    public void contains(String content) {
        sutResponse.content(containsString(content));
    }

}
