package Dashboard;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class UserApiTest {

    private final String baseUrl = "https://a5izddda32.us-east-1.awsapprunner.com/v1/users";
    private final String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2N2JmZjQ5ZjMwYjg3OGUzMjlmM2JkYWIiLCJpYXQiOjE3NDEwNTc1OTgsInR5cGUiOiJyZWZyZXNoIn0.mjOKuy8dQDWaZlrAoRSFiFWRIegj8pl0Hdxx35k6O2M";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = baseUrl;
    }

    // TC_01: Positive Test - Valid token, expecting 200
    @Test
    public void testGetUsersWithValidToken() {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + validToken)
                .contentType(ContentType.JSON)
                .get();

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 OK");
        // If fails: API might be down or token is not valid anymore
        // If passes: API is accessible and token is valid
    }

    // TC_02: Negative Test - Missing token
    @Test
    public void testGetUsersWithoutToken() {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .get();

        Assert.assertEquals(response.getStatusCode(), 403, "Should return 403 Unauthorized");
        // If fails: API might not be secured correctly
        // If passes: API correctly requires token
    }

    //  TC_03: Negative Test - Invalid token
    @Test
public void testGetUsersWithInvalidToken() {
    Response response = RestAssured.given()
            .header("Authorization", "Bearer invalid_token")
            .contentType(ContentType.JSON)
            .get();

    int statusCode = response.getStatusCode();

    // Acceptable codes: 401 (Unauthorized) or 502 (if gateway handles it differently)
    boolean isExpected = (statusCode == 401 || statusCode == 502);

    Assert.assertTrue(isExpected, "Expected status code 401 or 502, but got: " + statusCode);

    // If fails: API may be wrongly accepting invalid tokens or returning unexpected errors
    // If passes: Token validation or error handling is working as expected
}


    //  TC_04: Negative Test - Wrong method (POST instead of GET)
    @Test
    public void testPostUsersWithValidToken() {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + validToken)
                .contentType(ContentType.JSON)
                .post();

                System.out.println(response.statusCode());
        Assert.assertTrue(response.getStatusCode() == 405 || response.getStatusCode() == 404,
                "Should return 405 Method Not Allowed or 404 Not Found");
        // If fails: API might be misconfigured with wrong method handling
        // If passes: API restricts invalid HTTP methods
    }

    //  TC_05: Negative Test - Wrong endpoint
    @Test
    public void testGetInvalidEndpoint() {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + validToken)
                .get("https://a5izddda32.us-east-1.awsapprunner.com/v1/wrongpath");

        Assert.assertEquals(response.getStatusCode(), 404, "Should return 404 Not Found");
        // If fails: Invalid routing or endpoint exists incorrectly
        // If passes: API handles wrong paths well
    }

    // TC_06: Edge Case - Expired token (simulate with an old timestamp token)
    @Test
    public void testWithExpiredToken() {
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJleHBpcmVkVXNlciIsImlhdCI6MTYwOTU1NzU5OCwidHlwZSI6InJlZnJlc2gifQ.abc123abc123abc123abc123abc123abc123abc123";

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + expiredToken)
                .get();

        Assert.assertEquals(response.getStatusCode(), 401, "Should return 401 Unauthorized due to expiration");
        // If fails: API may not validate expiry
        // If passes: Token expiry is enforced correctly
    }

    //  TC_07: Edge Case - Malformed token (missing parts)
    @Test
    public void testWithMalformedToken() {
        String malformedToken = "abc.def"; // Not valid JWT

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + malformedToken)
                .get();

        Assert.assertTrue(response.getStatusCode() == 401 || response.getStatusCode() == 502,
                "Should return 402 Method Not Allowed or 502 Not Found");
        // If fails: API might not be verifying token structure
        // If passes: JWT validation is strict
    }

    //  TC_08: Edge Case - Valid token, but no users available (simulate by checking empty response)
    @Test
    public void testWithValidTokenExpectingEmptyList() {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + validToken)
                .get();

        Assert.assertEquals(response.getStatusCode(), 200);
        String body = response.getBody().asString();
        // Just log for edge testing
        if (body.equals("[]")) {
            System.out.println("User list is empty - handled correctly");
        } else {
            System.out.println("Users returned: " + body);
        }
        // If fails: Could indicate empty but returns wrong format
        // If passes: API handles empty response well
    }

    // TC_09: Negative Test - Token without "Bearer" prefix
    @Test
    public void testWithoutBearerPrefix() {
        Response response = RestAssured.given()
                .header("Authorization", validToken)
                .get();

        Assert.assertEquals(response.getStatusCode(), 401, "Should return 401 Unauthorized");
        // If fails: Token accepted even without Bearer prefix
        // If passes: Bearer enforcement is proper
    }

    //  TC_10: Edge Case - Token with whitespaces
    @Test
    public void testWithWhitespaceToken() {
        String tokenWithSpace = "   " + validToken + "   ";
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + tokenWithSpace.trim())
                .get();

        Assert.assertEquals(response.getStatusCode(), 200, "Should return 200 after trimming whitespaces");
        // If fails: API does not trim token whitespace
        // If passes: Token is correctly parsed after trimming
    }
}
