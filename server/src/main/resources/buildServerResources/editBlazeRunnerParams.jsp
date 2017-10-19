<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="api" class="com.blaze.api.ApiImpl"/>
<jsp:useBean id="url" class="com.blaze.api.urlmanager.UrlManagerImpl"/>

<c:set target="${api}" property="apiKeyID" value="${propertiesBean.defaultProperties['API_KEY_ID']}"/>
<c:set target="${api}" property="apiKeySecret" value="${propertiesBean.defaultProperties['API_KEY_SECRET']}"/>
<c:set target="${url}" property="serverUrl" value="${propertiesBean.defaultProperties['BLAZEMETER_URL']}"/>

<c:set target="${api}" property="urlManager" value="${url}"/>

<c:set var="isFirstOptionItem" value="true"/>
<l:settingsGroup title="BlazeMeter">
    <tr>
        <th><label>BlazeMeter tests:</label></th>
        <td>

            <props:selectProperty name="all_tests">
                <c:forEach var="test" items="${api.testsMultiMap}">
                    <c:forEach var="value" items="${test.value}">

                        <c:choose>
                            <c:when test="${test.key.contains('.workspace')}">
                                <c:choose>
                                    <c:when test="${isFirstOptionItem}">
                                        <c:set var="isFirstOptionItem" value="false"/>
                                    </c:when>
                                    <c:otherwise>
                                        </optgroup>
                                    </c:otherwise>
                                </c:choose>
                                <optgroup label="${value}">
                            </c:when>
                            <c:otherwise>
                                <props:option value="${value}" selected="false" title="${test.key}" id="${value}">
                                    ${value}
                                </props:option>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </c:forEach>
                </optgroup>

            </props:selectProperty>
            <span class="error" id="error_all_tests"></span>
            <span class="smallNote">Select the test to execute.</span>
        </td>
    </tr>
<tr>
    <th><label>Download JUnit report:</label></th>
    <td>
        <props:checkboxProperty name="blazeMeterPlugin.request.junit" treatFalseValuesCorrectly="${true}"
                                uncheckedValue="false"/>
    </td>
</tr>
<tr>
    <th><label>Download JTL report:</label></th>
    <td>
        <props:checkboxProperty name="blazeMeterPlugin.request.jtl" treatFalseValuesCorrectly="${true}"
                                uncheckedValue="false"/>
    </td>
</tr>
<tr>
    <th><label>JUnit report path:</label></th>
    <td>
        <props:textProperty name="blazeMeterPlugin.request.junit.path"/>
    </td>
</tr>
<tr>
    <th><label>JTL report path:</label></th>
    <td>
        <props:textProperty name="blazeMeterPlugin.request.jtl.path"/>
    </td>
</tr>
<tr>
    <th><label>Notes:</label></th>
    <td>
        <props:multilineProperty name="blazeMeterPlugin.notes" linkTitle="" cols="35" rows="4" expanded="true"/>
    </td>
</tr>
<tr>
    <th><label>JMeter propeties:</label></th>
    <td>
        <props:multilineProperty name="blazeMeterPlugin.jmeter.properties" linkTitle="" cols="35" rows="2" expanded="true"/>
    </td>
</tr>
</l:settingsGroup>