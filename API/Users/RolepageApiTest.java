package Users;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class RolepageApiTest {

    String baseUrl = "https://a5izddda32.us-east-1.awsapprunner.com/v1/role";
    String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2N2JmZjQ5ZjMwYjg3OGUzMjlmM2JkYWIiLCJpYXQiOjE3NDEwNTc1OTgsInR5cGUiOiJyZWZyZXNoIn0.mjOKuy8dQDWaZlrAoRSFiFWRIegj8pl0Hdxx35k6O2M";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = baseUrl;
    }

    // TC_01: Positive Test - Valid token with pagination params
    @Test
    public void testGetRolesWithValidTokenAndParams() {
        Response response = RestAssured.given()
                .header("Authorization", validToken)
                .queryParam("page", 1)
                .queryParam("limit", 10)
                .contentType(ContentType.JSON)
                .get();

        Assert.assertEquals(response.statusCode(), 200, "Expected 200 OK");
        // If fails: Either token or pagination is not accepted correctly
        // If passes: Auth and params work properly
    }

    // TC_02: Negative Test - No Authorization header
    @Test
    public void testWithoutAuthorizationHeader() {
        Response response = RestAssured.given()
                .queryParam("page", 1)
                .queryParam("limit", 10)
                .get();

        Assert.assertEquals(response.statusCode(), 403, "Expected 403 Unauthorized");
        // If fails: API is not enforcing authentication
        // If passes: Proper auth enforcement
    }

    // TC_03: Negative Test - Invalid Token
    @Test
    public void testWithInvalidToken() {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer invalid_token")
                .queryParam("page", 1)
                .queryParam("limit", 10)
                .get();

        Assert.assertTrue(response.statusCode() == 401 || response.statusCode() == 502, "Should return 401 or 502");
        // If fails: Token validation might not be implemented correctly
        // If passes: Invalid tokens are being rejected as expected
    }

    // TC_04: Negative Test - Without Bearer prefix
    @Test
    public void testWithoutBearerPrefix() {
        String rawToken = validToken.replace("Bearer ", "");
        Response response = RestAssured.given()
                .header("Authorization", rawToken)
                .queryParam("page", 1)
                .queryParam("limit", 10)
                .get();

       int statusCode = response.getStatusCode();

    // Acceptable codes: 401 (Unauthorized) or 502 (if gateway handles it differently)
    boolean isExpected = (statusCode == 401 || statusCode == 502);

    Assert.assertTrue(isExpected, "Expected status code 401 or 502, but got: " + statusCode);
        // If passes: Bearer format properly required
    }

    // TC_05: Positive Test - Request with only limit param
    @Test
    public void testWithOnlyLimitParam() {
        Response response = RestAssured.given()
                .header("Authorization", validToken)
                .queryParam("limit", 5)
                .get();

        Assert.assertEquals(response.statusCode(), 200, "Expected 200 OK");
        // If fails: limit param handling is broken
        // If passes: API handles missing optional params gracefully
    }

    // TC_06: Edge Test - Zero limit value
    @Test
    public void testWithZeroLimit() {
        Response response = RestAssured.given()
                .header("Authorization", validToken)
                .queryParam("page", 1)
                .queryParam("limit", 0)
                .get();

        Assert.assertTrue(response.statusCode() == 400 || response.statusCode() == 422, "Should return error for limit=0");
        // If fails: limit validation may be missing
        // If passes: limit validation is implemented
    }

    // TC_07: Edge Test - Negative page value
    @Test
    public void testWithNegativePage() {
        Response response = RestAssured.given()
                .header("Authorization", validToken)
                .queryParam("page", -1)
                .queryParam("limit", 10)
                .get();

        Assert.assertTrue(response.statusCode() == 400 || response.statusCode() == 422, "Should return error for negative page");
        // If fails: page number is not validated
        // If passes: validation exists for page values
    }

    // TC_08: Negative Test - Method Not Allowed (POST instead of GET)
    @Test
    public void testPostMethodNotAllowed() {
        Response response = RestAssured.given()
                .header("Authorization", validToken)
                .contentType(ContentType.JSON)
                .post();

        Assert.assertTrue(response.statusCode() == 404 || response.statusCode() == 405, "Should return 404/405 for invalid method");
        // If fails: API may be accepting wrong HTTP methods
        // If passes: RESTful method rules respected
    }

    // TC_09: Edge Case - Large limit value
    @Test
    public void testWithLargeLimit() {
        Response response = RestAssured.given()
                .header("Authorization", validToken)
                .queryParam("page", 1)
                .queryParam("limit", 1000)
                .get();

        Assert.assertEquals(response.statusCode(), 200, "Expected 200 OK with large limit");
        // If fails: limit boundary not handled
        // If passes: pagination scaling works
    }

    // TC_10: Edge Test - No query params at all
    @Test
    public void testWithoutQueryParams() {
        Response response = RestAssured.given()
                .header("Authorization", validToken)
                .get();

        Assert.assertEquals(response.statusCode(), 200, "Expected 200 OK without query params");
        // If fails: query params might be required when not documented
        // If passes: API handles defaults well
    }
}
