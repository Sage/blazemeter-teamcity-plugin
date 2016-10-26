<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/blazeRunnerController.html"/>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="bzmServiceManager" class="com.blaze.BzmServiceManager"/>
<c:choose>
    <c:when test="${not empty propertiesBean.properties['USER_KEY']}">
        <c:set target="${bzmServiceManager}" property="userKey" value="${propertiesBean.properties['USER_KEY']}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${bzmServiceManager}" property="userKey" value="${userKey}"/>
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${not empty propertiesBean.properties['BLAZEMETER_URL']}">
        <c:set target="${bzmServiceManager}" property="blazeMeterUrl"
               value="${propertiesBean.properties['BLAZEMETER_URL']}"/>
    </c:when>
    <c:otherwise>
        <c:set target="${bzmServiceManager}" property="blazeMeterUrl" value="${blazeMeterUrl}"/>
    </c:otherwise>
</c:choose>

<l:settingsGroup title="BlazeMeter">
    <tr>
        <th><label for="${bzmServiceManager.debugKey}">BlazeMeter tests:</label></th>
        <td>
            <props:selectProperty name="all_tests">
                <c:forEach var="test" items="${bzmServiceManager.testsAsMap}">
                    <c:forEach var="value" items="${test.value}">
                    <props:option value="${value}" selected="false" title="${test.key}" id="${value}">
                        ${value} -> ${test.key}
                    </props:option>
                    </c:forEach>
                </c:forEach>
            </props:selectProperty>
            <span class="error" id="error_all_tests"></span>
            <span class="smallNote">Select the test to execute.</span>
        </td>
    </tr>

<tr>
    <th><label>Request junit:</label></th>
    <td>
        <props:checkboxProperty name="blazeMeterPlugin.request.junit" treatFalseValuesCorrectly="${true}"
                                uncheckedValue="false"/>
    </td>
</tr>

<tr>
    <th><label>Request jtl:</label></th>
    <td>
        <props:checkboxProperty name="blazeMeterPlugin.request.jtl" treatFalseValuesCorrectly="${true}"
                                uncheckedValue="false"/>
    </td>
</tr>


<tr>
    <th><label>Junit report path:</label></th>
    <td>
        <props:textProperty name="blazeMeterPlugin.request.junit.path"/>
    </td>
</tr>
<tr>
    <th><label>Jtl report path:</label></th>
    <td>
        <props:textProperty name="blazeMeterPlugin.request.jtl.path"/>
    </td>
</tr>

</l:settingsGroup>