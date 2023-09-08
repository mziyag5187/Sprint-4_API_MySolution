package com.library.steps;

import com.library.pages.BookPage;
import com.library.pages.DashboardPage;
import com.library.pages.LoginPage;
import com.library.utility.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ApiStepDefs {

    String token;
    Response response;
    RequestSpecification reqSpec;

    // US-01 ========================================

    @Given("I logged Library api as a {string}")
    public void i_logged_library_api_as_a(String userType) {
        token = LibraryAPI_Util.getToken(userType);
    }

    @Given("Accept header is {string}")
    public void accept_header_is(String acceptHeader) {
        reqSpec = given().accept(ContentType.JSON);
    }

    @When("I send GET request to {string} endpoint")
    public void i_send_get_request_to_endpoint(String endPoint) {
        response = given().spec(reqSpec).header("x-library-token", token).
                when().get(ConfigurationReader.getProperty("library.baseUri") + endPoint);
    }

    @Then("status code should be {int}")
    public void status_code_should_be(int expectedStatusCode) {
        //Assert.assertEquals(expectedStatusCode, response.getStatusCode());
        response.then().assertThat().statusCode(expectedStatusCode);
    }

    @Then("Response Content type is {string}")
    public void response_content_type_is(String expectedContentType) {
        //Assert.assertEquals(expectedContentType,response.contentType());
        response.then().assertThat().contentType(expectedContentType);
    }

    @Then("{string} fields should not be null")
    public void fields_should_not_be_null(String field) {
        response.then().body(field, everyItem(notNullValue()));
    }

    // US-02 ======================

    int pathParamId;

    @Given("Path param is {int}")
    public void path_param_is(int id) {
        reqSpec = given().pathParam("id", id);
        pathParamId = id;
    }

    @Then("{string} field should be same with path param")
    public void field_should_be_same_with_path_param(String expectedId) {
        response.then().assertThat().body(expectedId, is(String.valueOf(pathParamId)));
    }

    @Then("following fields should not be null")
    public void following_fields_should_not_be_null(List<String> expectedFields) {
        response.then().assertThat().body(expectedFields.get(0), notNullValue(),
                expectedFields.get(1), notNullValue(),
                expectedFields.get(2), notNullValue());
    }

    static Map<String, Object> randomNewItemInfo_API;

    // US-03-1 ===============================================
    @Given("Request Content Type header is {string}")
    public void request_content_type_header_is(String reqContentType) {
        reqSpec = given().contentType(reqContentType);
    }

    @Given("I create a random {string} as request body")
    public void i_create_a_random_as_request_body(String postBody) {
        randomNewItemInfo_API = LibraryAPI_Util.getRandomNewItemInfo(postBody);
    }

    @When("I send POST request to {string} endpoint")
    public void i_send_post_request_to_endpoint(String endPoint) {
        if(endPoint.contains("decode")){
            response = given().spec(reqSpec).header("x-library-token", token)
                    .post(ConfigurationReader.getProperty("library.baseUri") + endPoint);
        }else {
            response = given().spec(reqSpec).formParams(randomNewItemInfo_API).header("x-library-token", token)
                    .post(ConfigurationReader.getProperty("library.baseUri") + endPoint);
        }
    }

    @Then("{string} field should not be null")
    public void field_should_not_be_null(String field) {
        response.then().body(field, notNullValue());
    }

    @Then("the field value for {string} path should be equal to {string}")
    public void the_field_value_for_path_should_be_equal_to(String fieldName, String expectedValue) {
        response.then().assertThat().body(fieldName, equalTo(expectedValue));
    }

    // US-03-2 ===============================================
    @Given("I logged in Library UI as {string}")
    public void i_logged_in_library_ui_as(String userType) {
        new LoginPage().login("librarian");
    }

    @Given("I navigate to {string} page")
    public void i_navigate_to_page(String pageUI) {
        new DashboardPage().navigateModule("Books");
    }

    @Then("UI, Database and API created book information must match")
    public void ui_database_and_api_created_book_information_must_match() {
        // GET EXPECTED BOOK DATA FROM API - POST BODY
        String expectedBookName_API = (String) randomNewItemInfo_API.get("name");
        String expectedISBN_API = (String) randomNewItemInfo_API.get("isbn");
        int expectedYear_API = (int) randomNewItemInfo_API.get("year");
        String expectedAuthor_API = (String) randomNewItemInfo_API.get("author");

        // GET DATA FROM DB
        String query = "SELECT b.name as 'bookName' , isbn, year, author, bc.name as 'bookCategoryName'\n" +
                "FROM books b\n" +
                "         inner join book_categories bc\n" +
                "                    on b.book_category_id = bc.id\n" +
                "where b.author = '" + expectedAuthor_API + "';";
        DB_Util.runQuery(query);

        String actualBookName_DB = DB_Util.getCellValue(1, 1);
        String actualISBN_DB = (DB_Util.getCellValue(1, 2));
        int actualYear_DB = Integer.parseInt(DB_Util.getCellValue(1, 3));
        String actualAuthor_DB = DB_Util.getCellValue(1, 4);
        String actualBookCatName_DB = DB_Util.getCellValue(1, 5);

        Map<String, Object> actualBookMapDB = new LinkedHashMap<>();
        actualBookMapDB.put("bookName", actualBookName_DB);
        actualBookMapDB.put("isbn", actualISBN_DB);
        actualBookMapDB.put("year", actualYear_DB);
        actualBookMapDB.put("author", actualAuthor_DB);
        actualBookMapDB.put("bookCategoryName", actualBookCatName_DB);

        // GET DATA FROM UI
        Map<String, Object> actualBookMapUI = new BookPage().getNewCreatedBookInfo_UI(expectedBookName_API);

        // COMPARE API-DB
        Assert.assertEquals(expectedBookName_API, actualBookName_DB);
        Assert.assertEquals(expectedAuthor_API, actualAuthor_DB);
        Assert.assertEquals(expectedISBN_API, actualISBN_DB);
        Assert.assertEquals(expectedYear_API, actualYear_DB);

        //COMPARE UI-DB
        Map<String, Object> expectedBookMapDB = actualBookMapDB;
        Assert.assertEquals(actualBookMapUI, expectedBookMapDB);
    }


    Map<String, Object> userMap_DB;

    //// US-04-2 ===============================================
    @Then("created user information should match with Database")
    public void created_user_information_should_match_with_database() {
        // GET EXPECTED USER DATA FROM API - POST BODY
        String expectedFullName_API = (String) randomNewItemInfo_API.get("full_name");
        String expectedEmail_API = (String) randomNewItemInfo_API.get("email");
        String expectedPassword_API = (String) randomNewItemInfo_API.get("password");
        int expectedUserGroupId_API = (int) randomNewItemInfo_API.get("user_group_id");
        String expectedStatus_API = (String) randomNewItemInfo_API.get("status");
        String expectedStartDate_API = (String) randomNewItemInfo_API.get("start_date");
        String expectedEndDate_API = (String) randomNewItemInfo_API.get("end_date");
        String expectedAddress_API = (String) randomNewItemInfo_API.get("address");


        // GET DATA FROM DB
        int userId = Integer.parseInt(response.path("user_id"));
        String query = "SELECT full_name,email,password,user_group_id, status, start_date, end_date,address FROM users\n" +
                "WHERE id = " + userId + ";";
        DB_Util.runQuery(query);
        userMap_DB = DB_Util.getRowMap(1);

        // COMPARE API - DB
        Assert.assertEquals(expectedFullName_API, userMap_DB.get("full_name"));
        Assert.assertEquals(expectedEmail_API, userMap_DB.get("email"));
        Assert.assertTrue(expectedUserGroupId_API == Integer.parseInt((String) userMap_DB.get("user_group_id")));
        Assert.assertEquals(expectedStatus_API, userMap_DB.get("status"));
        Assert.assertEquals(expectedStartDate_API, userMap_DB.get("start_date"));
        Assert.assertEquals(expectedEndDate_API, userMap_DB.get("end_date"));
        Assert.assertEquals(expectedAddress_API, userMap_DB.get("address"));
    }

    @Then("created user should be able to login Library UI")
    public void created_user_should_be_able_to_login_library_ui() {
        new LoginPage().login((String) userMap_DB.get("email"), (String) randomNewItemInfo_API.get("password"));
        BrowserUtil.waitFor(3);
        String expectedDashboardUrl = "https://library2.cydeo.com/#dashboard";
        String actualDashboardUrl = Driver.getDriver().getCurrentUrl();
        Assert.assertEquals(expectedDashboardUrl,actualDashboardUrl);
    }

    @Then("created user name should appear in Dashboard Page")
    public void created_user_name_should_appear_in_dashboard_page() {
        String expectedFullName_DB = (String) userMap_DB.get("full_name");
        String actualFullName_UI = new DashboardPage().accountHolderName.getText();

        Assert.assertEquals(expectedFullName_DB, actualFullName_UI);
    }

    //// US-05 ===============================================

    @Given("I logged Library api with credentials {string} and {string}")
    public void i_logged_library_api_with_credentials_and(String email, String password) {
         token = LibraryAPI_Util.getToken(email, password);
    }

    @Given("I send token information as request body")
    public void i_send_token_information_as_request_body() {
        reqSpec = given().formParams("token",token);
    }

}
