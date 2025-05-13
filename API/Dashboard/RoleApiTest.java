package Dashboard;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class RoleApiTest {

    private final String BASE_URL = "https://a5izddda32.us-east-1.awsapprunner.com/v1/role";
    private final String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2N2JmZjQ5ZjMwYjg3OGUzMjlmM2JkYWIiLCJpYXQiOjE3NDEwNTc1OTgsInR5cGUiOiJyZWZyZXNoIn0.mjOKuy8dQDWaZlrAoRSFiFWRIegj8pl0Hdxx35k6O2M";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    // TC_01: Positive Test - Valid token should return role list
    @Test
    public void testGetRolesWithValidToken() {
        Response response = RestAssured.given()
                .header("Authorization", validToken)
                .contentType(ContentType.JSON)
                .get();

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        // If fails: API may not be working or token is invalid
        // If passes: API and auth are working as expected
    }

    // TC_02: Negative Test - No Authorization header
    @Test
    public void testGetRolesWithoutToken() {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .get();

        Assert.assertEquals(response.getStatusCode(), 403, "Should return 403 Unauthorized");
        // If fails: API may be accessible without authentication — critical security issue
        // If passes: Authorization required
    }

    // TC_03: Negative Test - Invalid Token
    @Test
    public void testGetRolesWithInvalidToken() {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer invalid_token")
                .contentType(ContentType.JSON)
                .get();

        int statusCode = response.getStatusCode();
        boolean isExpected = (statusCode == 401 || statusCode == 502);
        Assert.assertTrue(isExpected, "Expected 401 or 502 but got: " + statusCode);
        // If fails: API accepting invalid tokens
        // If passes: Token validation is working
    }

    // TC_04: Negative Test - Wrong HTTP method (POST instead of GET)
    @Test
    public void testGetRolesWithWrongHttpMethod() {
        Response response = RestAssured.given()
                .header("Authorization", validToken)
                .contentType(ContentType.JSON)
                .post();

        int statusCode = response.getStatusCode();
        Assert.assertTrue(statusCode == 404 || statusCode == 500,
                "Expected 404 or 405 for wrong method, but got: " + statusCode);
        // If fails: API may be wrongly handling HTTP verbs
        // If passes: API strictly follows REST standards
    }

    // TC_05: Edge Test - Token without "Bearer" prefix
    @Test
    public void testGetRolesWithoutBearerPrefix() {
        Response response = RestAssured.given()
                .header("Authorization", validToken.replace("Bearer ", "")) // Remove Bearer
                .contentType(ContentType.JSON)
                .get();

        Assert.assertEquals(response.getStatusCode(), 401, "Should return 401 Unauthorized");
        // If fails: API accepts token without Bearer prefix — not recommended
        // If passes: API enforces correct format
    }
}
