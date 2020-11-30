package com.kreait.bots.agile.integration

import com.kreait.bots.agile.IntegrationTest
import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.domain.common.data.RandomResponses
import com.kreait.bots.agile.domain.response.AddRandomResponsesRequest
import io.restassured.RestAssured
import org.apache.commons.codec.binary.Base64
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension

@IntegrationTest
@SpringBootTest(classes = [(TestApplication::class)], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Random Response Controller Tests")
class RandomResponseIntegrationTest(@Value("\${authentication.key}") private val authKey: String) {

    @LocalServerPort
    var port: Int = 0

    @DisplayName("GET-Endpoint")
    @Test
    fun testGetEndpoint() {
        // @formatter:off
        RestAssured.given().log().all()
                .get("http://localhost:$port/random-answers")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
        // @formatter:on
    }

    @DisplayName("GET-Endpoint with id")
    fun testGetSingleEndpoint(newResponse: String) {
        // @formatter:off

        RestAssured.given()
                .get("http://localhost:$port/random-answers/UnknownMessageResponses")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().`as`(RandomResponses::class.java).messages.contains(newResponse)
        // @formatter:on
    }

    @DisplayName("PUT-Endpoint with id")
    @Test
    fun testPutSingleEndpoint() {
        // @formatter:off
        val newResponse= "sampleResponse"
        val authKeyEncoded = Base64.encodeBase64String(this.authKey.toByteArray())
        RestAssured.given()
                .header(HttpHeaders.AUTHORIZATION,authKeyEncoded)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .body(AddRandomResponsesRequest(listOf(newResponse)))
                .put("http://localhost:$port/random-answers/UnknownMessageResponses")
                .then()
                .statusCode(HttpStatus.OK.value())
        testGetSingleEndpoint(newResponse)

        // @formatter:on
    }

    @DisplayName("PUT-Endpoint with id")
    @Test
    fun testWrongAuth() {
        // @formatter:off
        RestAssured.given()
                .header(HttpHeaders.AUTHORIZATION,"wrongKey")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .body(AddRandomResponsesRequest(listOf()))
                .put("http://localhost:$port/random-answers/UnknownMessageResponses")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
        // @formatter:on
    }
}
