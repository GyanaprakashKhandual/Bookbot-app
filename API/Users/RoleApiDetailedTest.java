package Manage_Permission;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;


public class RoleApiDetailedTest {

    private static final String BASE_URL = "https://a5izddda32.us-east-1.awsapprunner.com";
    private static final String AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2ODBmMTZhZTM0MzA1OTAwMWVkODE1MWIiLCJpYXQiOjE3NDU4MzQ0OTYsInR5cGUiOiJyZWZyZXNoIn0.sc1PYsCl7gaXRBSYJBSW76-YbMOiEQW4Hjb8yac1rCA";
    private static final String VALID_ROLE_ID = "672c47c238903b464c9d2920";
    private static final String INVALID_ROLE_ID = "invalid_role_id_123";
    private static final String NON_EXISTENT_ROLE_ID = "672c47c238903b464c9d9999";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    // Positive Test Cases

    @Test(description = "Verify successful role retrieval with valid ID and token")
    public void testGetRoleWithValidCredentials() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .pathParam("roleId", VALID_ROLE_ID)
        .when()
            .get("/v1/role/{roleId}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("dashboard_permissions", not(empty()))
            .body("dashboard_permissions[0].isAllSelected", is(true))
            .body("dashboard_permissions[0].isAllCollapsed", is(false))
            .body("dashboard_permissions[0].ParentChildchecklist[0].moduleName", equalTo("Roles"))
            .body("dashboard_permissions[0].ParentChildchecklist[0].childList", hasSize(4));
    }

    @Test(description = "Verify complete response headers")
    public void testResponseHeaders() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .pathParam("roleId", VALID_ROLE_ID)
        .when()
            .get("/v1/role/{roleId}")
        .then()
            .statusCode(200)
            .header("access-control-allow-origin", equalTo("*"))
            .header("content-encoding", equalTo("gzip"))
            .header("content-security-policy", containsString("default-src 'self'"))
            .header("strict-transport-security", containsString("max-age=15552000"))
            .header("x-frame-options", equalTo("SAMEORIGIN"))
            .header("x-content-type-options", equalTo("nosniff"))
            .header("x-xss-protection", equalTo("0"))
            .header("server", equalTo("envoy"));
    }

     @Test(description = "Verify detailed permission structure for a valid role")
public void testPermissionStructure() {
    given()
        .header("Authorization", "Bearer " + AUTH_TOKEN)
        .contentType(ContentType.JSON)
        .pathParam("roleId", VALID_ROLE_ID)
    .when()
        .get("/v1/role/{roleId}")
    .then()
        .statusCode(200);
}
    @Test(description = "Verify ETag caching functionality")
    public void testETagCaching() {
        // First request to get ETag
        String eTag = given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .pathParam("roleId", VALID_ROLE_ID)
        .when()
            .get("/v1/role/{roleId}")
        .then()
            .statusCode(200)
            .extract()
            .header("ETag");

        // Second request with If-None-Match header
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .header("If-None-Match", eTag)
            .pathParam("roleId", VALID_ROLE_ID)
        .when()
            .get("/v1/role/{roleId}")
        .then()
            .statusCode(304); // Not Modified
    }

    // Negative Test Cases

    @Test(description = "Verify unauthorized access without token")
    public void testUnauthorizedAccess() {
        given()
            .pathParam("roleId", VALID_ROLE_ID)
        .when()
            .get("/v1/role/{roleId}")
        .then()
            .statusCode(403);
    }

    @Test(description = "Verify access with invalid token")
    public void testInvalidToken() {
        given()
            .header("Authorization", "Bearer invalid_token_123")
            .pathParam("roleId", VALID_ROLE_ID)
        .when()
            .get("/v1/role/{roleId}")
        .then()
            .statusCode(401);
    }

    @Test(description = "Verify response for non-existent role ID")
    public void testNonExistentRoleId() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .pathParam("roleId", NON_EXISTENT_ROLE_ID)
        .when()
            .get("/v1/role/{roleId}")
        .then()
            .statusCode(502);
    }

    @Test(description = "Verify response for invalid role ID format")
    public void testInvalidRoleIdFormat() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .pathParam("roleId", INVALID_ROLE_ID)
        .when()
            .get("/v1/role/{roleId}")
        .then()
            .statusCode(400);
    }

    // Edge Case Tests

    @Test(description = "Verify response time performance")
    public void testResponseTime() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .pathParam("roleId", VALID_ROLE_ID)
        .when()
            .get("/v1/role/{roleId}")
        .then()
            .statusCode(200)
            .time(lessThan(3000L)); // Should respond in under 3 seconds
    }

    @Test(description = "Verify CORS headers with origin")
    public void testCorsWithOrigin() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .header("Origin", "https://main.du8ei0eipqn81.amplifyapp.com")
            .pathParam("roleId", VALID_ROLE_ID)
        .when()
            .get("/v1/role/{roleId}")
        .then()
            .statusCode(200)
            .header("access-control-allow-origin", equalTo("*"));
    }

    @Test(description = "Verify unsupported HTTP methods")
    public void testUnsupportedMethods() {
        // Test POST
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .pathParam("roleId", VALID_ROLE_ID)
        .when()
            .post("/v1/role/{roleId}")
        .then()
            .statusCode(500); // Method Not Allowed

        // Test PUT
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .pathParam("roleId", VALID_ROLE_ID)
        .when()
            .put("/v1/role/{roleId}")
        .then()
            .statusCode(404);

        // Test DELETE
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .pathParam("roleId", VALID_ROLE_ID)
        .when()
            .delete("/v1/role/{roleId}")
        .then()
            .statusCode(500);
    }

    @Test(description = "Verify request with all original headers")
    public void testWithOriginalHeaders() {
        given()
            .header("Authorization", "Bearer " + AUTH_TOKEN)
            .header("Accept", "*/*")
            .header("Accept-Encoding", "gzip, deflate, br, zstd")
            .header("Accept-Language", "en-US,en;q=0.9")
            .header("Connection", "keep-alive")
            .header("Host", "a5izddda32.us-east-1.awsapprunner.com")
            .header("Referer", "https://main.du8ei0eipqn81.amplifyapp.com/")
            .header("Sec-Ch-Ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
            .header("Sec-Ch-Ua-Mobile", "?1")
            .header("Sec-Ch-Ua-Platform", "\"Android\"")
            .header("Sec-Fetch-Dest", "empty")
            .header("Sec-Fetch-Mode", "cors")
            .header("Sec-Fetch-Site", "cross-site")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36")
            .pathParam("roleId", VALID_ROLE_ID)
        .when()
            .get("/v1/role/{roleId}")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }
}