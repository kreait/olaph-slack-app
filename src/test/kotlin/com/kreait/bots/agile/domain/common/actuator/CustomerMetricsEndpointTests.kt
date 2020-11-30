package com.kreait.bots.agile.domain.common.actuator

import com.kreait.bots.agile.IntegrationTest
import com.kreait.bots.agile.TestApplication
import io.restassured.RestAssured
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@IntegrationTest
@SpringBootTest(classes = [(TestApplication::class)], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Customer Metrics Endpoint Tests")
internal class CustomerMetricsEndpointTests {

    @LocalServerPort
    var port: Int = 0

    @Test
    @DisplayName("Actuator Customers")
    fun customers() {
        RestAssured.given()
                .log().all()
                .`when`()
                .get("http://localhost:$port/actuator/customers")
                .then()
                .statusCode(HttpStatus.OK.value())
    }

    @Test
    @DisplayName("Actuator Customers CORS")
    fun customersCORS() {
        RestAssured.given()
                .log().all()
                .`when`()
                .headers(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET, HttpHeaders.ORIGIN, "http://www.google.com")
                .options("http://localhost:$port/actuator/customers")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
    }

}
