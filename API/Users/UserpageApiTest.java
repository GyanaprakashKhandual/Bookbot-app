package Users;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.http.Header;

public class UserpageApiTest {

    private static final String BASE_URL = "https://a5izddda32.us-east-1.awsapprunner.com";
    private static final String AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2ODBmMTZhZTM0MzA1OTAwMWVkODE1MWIiLCJpYXQiOjE3NDU4MzQ0OTYsInR5cGUiOiJyZWZyZXNoIn0.sc1PYsCl7gaXRBSYJBSW76-YbMOiEQW4Hjb8yac1rCA";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    // Test for response headers
    @Test(description = "Verify all response headers")
    public void testResponseHeaders() {
        given()
            .header(new Header("Authorization", "Bearer " + AUTH_TOKEN))
            .queryParam("page", 1)
            .queryParam("limit", 10)
        .when()
            .get("/v1/users")
        .then()
            .statusCode(200)
            .header("access-control-allow-origin", equalTo("*"))
            .header("content-type", containsString("application/json"));
    }

    // Test for response body structure
    @Test(description = "Verify response body structure with detailed assertions")
    public void testResponseBodyStructure() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .queryParam("page", 1)
            .queryParam("limit", 10)
        .when()
            .get("/v1/users")
        .then()
            .statusCode(200)
            .body("results", not(empty()));
    }

    // Test for security headers
    @Test(description = "Verify security headers")
    public void testSecurityHeaders() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .queryParam("page", 1)
            .queryParam("limit", 10)
        .when()
            .get("/v1/users")
        .then()
            .statusCode(200)
            .header("content-security-policy", containsString("default-src 'self'"))
            .header("x-xss-protection", equalTo("0"))
            .header("x-content-type-options", equalTo("nosniff"))
            .header("strict-transport-security", containsString("max-age=15552000"))
            .header("x-frame-options", equalTo("SAMEORIGIN"));
    }

    // Test for CORS headers
    @Test(description = "Verify CORS headers")
    public void testCorsHeaders() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .header("Origin", "https://main.du8ei0eipqn81.amplifyapp.com")
            .queryParam("page", 1)
            .queryParam("limit", 10)
        .when()
            .get("/v1/users")
        .then()
            .statusCode(200)
            .header("access-control-allow-origin", equalTo("*"))
            .header("vary", containsString("Accept-Encoding"));
    }

    // Test for performance
    @Test(description = "Verify response time is acceptable")
    public void testResponseTime() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .queryParam("page", 1)
            .queryParam("limit", 10)
        .when()
            .get("/v1/users")
        .then()
            .statusCode(200)
            .time(lessThan(3000L)); // Response should be faster than 3 second
    }

    // Test for ETag header functionality
    @Test(description = "Verify ETag header and caching")
    public void testETagFunctionality() {
        // First request to get ETag
        String eTag = given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .queryParam("page", 1)
            .queryParam("limit", 10)
        .when()
            .get("/v1/users")
        .then()
            .statusCode(200)
            .extract()
            .header("ETag");

        // Second request with If-None-Match header
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .header("If-None-Match", eTag)
            .queryParam("page", 1)
            .queryParam("limit", 10)
        .when()
            .get("/v1/users")
        .then()
            .statusCode(304); // Not Modified
    }

    // Test for request headers
    @Test(description = "Verify request headers are properly handled")
    public void testRequestHeaders() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .header("Accept", "application/json")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Accept-Language", "en-US,en;q=0.9")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36")
            .queryParam("page", 1)
            .queryParam("limit", 10)
        .when()
            .get("/v1/users")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    // Test for pagination
    @Test(description = "Verify pagination works correctly")
    public void testPagination() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .queryParam("page", 2)
            .queryParam("limit", 5)
        .when()
            .get("/v1/users")
        .then()
            .statusCode(200)
            .body("results.size()", lessThanOrEqualTo(5));
    }

    // Test for unauthorized access
    @Test(description = "Verify unauthorized access is blocked")
    public void testUnauthorizedAccess() {
        given()
            .queryParam("page", 1)
            .queryParam("limit", 10)
        .when()
            .get("/v1/users")
        .then()
            .statusCode(403);
    }

    // Test for invalid token
    @Test(description = "Verify invalid token is rejected")
    public void testInvalidToken() {
        given()
            .header("Authorization", "Bearer invalid_token_123")
            .queryParam("page", 1)
            .queryParam("limit", 10)
        .when()
            .get("/v1/users")
        .then()
            .statusCode(401);
    }
}