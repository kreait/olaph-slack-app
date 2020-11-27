package com.kreait.bots.agile.domain.response

import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.domain.common.data.RandomResponses
import com.kreait.bots.agile.domain.common.data.RandomResponsesRepository
import io.restassured.RestAssured
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [(TestApplication::class)], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Random Response Endpoint Tests")
class RandomResponseEndpointTests @Autowired constructor(private val randomResponsesRepository: RandomResponsesRepository) {

    @LocalServerPort
    var port: Int = 0

    @Test
    @DisplayName("Random Response List ")
    fun randomResponseList() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.given()
                .log().all()
                .`when`()
                .headers(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET, HttpHeaders.ORIGIN, "http://www.google.com")
                .options("http://localhost:$port/random-answers")
                .then()
                .statusCode(HttpStatus.OK.value())

        RestAssured.given()
                .log().all()
                .`when`()
                .get("http://localhost:$port/random-answers")
                .then()
                .statusCode(HttpStatus.OK.value())
    }

    @Test
    @DisplayName("Random Response Get ")
    fun randomResponseGet() {

        val sampleId = "4"

        randomResponsesRepository.save(RandomResponses(sampleId, listOf()))

        RestAssured.given()
                .log().all()
                .`when`()
                .headers(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET, HttpHeaders.ORIGIN, "http://www.google.com")
                .options("http://localhost:$port/random-answers/$sampleId")
                .then()
                .statusCode(HttpStatus.OK.value())

        RestAssured.given()
                .log().all()
                .`when`()
                .get("http://localhost:$port/random-answers/$sampleId")
                .then()
                .statusCode(HttpStatus.OK.value())

        this.randomResponsesRepository.deleteById(sampleId)

    }
}
