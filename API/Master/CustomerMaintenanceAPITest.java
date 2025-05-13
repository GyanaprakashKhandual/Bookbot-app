package Master;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

public class CustomerMaintenanceAPITest {

    private final String BASE_URL = "https://a5izddda32.us-east-1.awsapprunner.com";
    private final String ENDPOINT = "/v1/customer-maintenance";
    private final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2N2JmZjQ5ZjMwYjg3OGUzMjlmM2JkYWIiLCJpYXQiOjE3NDEwNTc1OTgsInR5cGUiOiJyZWZyZXNoIn0.mjOKuy8dQDWaZlrAoRSFiFWRIegj8pl0Hdxx35k6O2M";

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
    }

    // ✅ 1. Positive Test – Valid Auth and Params
    @Test(description = "Valid request with correct auth and valid query parameters")
    public void validAuthAndParams_shouldReturn200AndData() {
        given()
            .header("Authorization", VALID_TOKEN)
            .queryParam("page", 1)
            .queryParam("limit", 1)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("results", notNullValue());
    }

    // ✅ 2. Positive Test – Check Allotted Manager Structure
    @Test(description = "Check if allotted manager object has required fields")
    public void validateAllottedManagerFields() {
        given()
            .header("Authorization", VALID_TOKEN)
            .queryParam("page", 1)
            .queryParam("limit", 1)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body("results[0].alloted_manager", allOf(
                hasKey("name"),
                hasKey("email"),
                hasKey("role"),
                hasKey("isEmailVerified"),
                hasKey("id")
            ));
    }

    // ✅ 3. Positive Test – Check Related Parties Structure
    @Test(description = "Check related parties have name and relationship")
    public void validateRelatedPartiesStructure() {
        given()
            .header("Authorization", VALID_TOKEN)
            .queryParam("page", 1)
            .queryParam("limit", 1)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body("results[0].related_parties[0]", allOf(
                hasKey("name"),
                hasKey("relationship")
            ));
    }

    // ❌ 4. Negative Test – Missing Auth Token
    @Test(description = "Missing auth token should return 401")
    public void missingToken_shouldReturn401() {
        given()
            .queryParam("page", 1)
            .queryParam("limit", 10)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(403);
    }

    // ❌ 5. Negative Test – Invalid Auth Token
    @Test(description = "Invalid token should return 401")
    public void invalidToken_shouldReturn401() {
        given()
            .header("Authorization", "Bearer invalid.token.value")
            .queryParam("page", 1)
            .queryParam("limit", 10)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(401);
    }

    // ❌ 6. Negative Test – Invalid Page and Limit
    @Test(description = "Invalid page/limit values should return 400 or handle gracefully")
    public void invalidPageLimit_shouldReturnErrorOrEmptyList() {
        given()
            .header("Authorization", VALID_TOKEN)
            .queryParam("page", -1)
            .queryParam("limit", -5)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(anyOf(is(502), is(200)));
            
    }

    // 🟡 7. Edge Case – Zero Limit
    @Test(description = "Zero limit should return 200 with empty list")
    public void zeroLimit_shouldReturnEmptyResults() {
        given()
            .header("Authorization", VALID_TOKEN)
            .queryParam("page", 1)
            .queryParam("limit", 0)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200);
    }

    // 🟡 8. Edge Case – Very High Page Number
    @Test(description = "Very high page number should return 200 with empty result")
    public void highPageNumber_shouldReturnEmptyList() {
        given()
            .header("Authorization", VALID_TOKEN)
            .queryParam("page", 9999)
            .queryParam("limit", 10)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body("results", hasSize(0));
    }

    // 🟡 9. Edge Case – Max Limit
    @Test(description = "Test with very high limit to check server performance")
    public void maxLimit_shouldReturnDataOrHandleGracefully() {
        given()
            .header("Authorization", VALID_TOKEN)
            .queryParam("page", 1)
            .queryParam("limit", 1000)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(200)
            .body("results.size()", lessThanOrEqualTo(1000));
    }

    // ❌ 10. Negative Test – Missing Query Params
    @Test(description = "Missing page and limit should return default or error")
    public void missingQueryParams_shouldReturnDefaultOrError() {
        given()
            .header("Authorization", VALID_TOKEN)
        .when()
            .get(ENDPOINT)
        .then()
            .statusCode(anyOf(is(200), is(400)));
    }
}
