package com.kreait.bots.agile.domain.v2

import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.domain.v2.data.StandupSpec
import com.kreait.bots.agile.domain.v2.repository.StandupSpecRepository
import com.kreait.bots.olaph.dto.jackson.common.StandupSpecRequest
import com.kreait.bots.olaph.dto.jackson.standupspec.CreateStandupSpecResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.ErrorCreateStandupSpecResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.SuccessfulStandupSpecResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.sample
import io.restassured.RestAssured
import org.apache.commons.codec.binary.Base64
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [(TestApplication::class)], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("StandupSpec Controller Tests")
class StandupSpecIntegrationTest(@Value("\${authentication.key}") private val authKey: String,
                                 @Autowired private val standupSpecRepository: StandupSpecRepository) {

    @LocalServerPort
    var localPort: Int = 0

    lateinit var authKeyEncoded: String

    @BeforeEach
    fun before() {
        this.standupSpecRepository.deleteAll()
        RestAssured.port = localPort
        RestAssured.baseURI = "http://localhost"
        this.authKeyEncoded = Base64.encodeBase64String(this.authKey.toByteArray())
    }

    @Test
    @DisplayName("GET /standup-specs without account id")
    fun testGetAll() {
        this.standupSpecRepository.save(StandupSpec.sample())

        RestAssured.given()
                .log().all()
                .header(HttpHeaders.AUTHORIZATION, this.authKeyEncoded)
                .get("/standup-specs")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    @DisplayName("GET /standup-specs?withParticipant=someUserId")
    fun testGetWithParticipant() {
        val standupSpec = this.standupSpecRepository.save(StandupSpec.sample())

        RestAssured.given()
                .log().all()
                .header(HttpHeaders.AUTHORIZATION, this.authKeyEncoded)
                .get("/standup-specs?withParticipant=${standupSpec.participants.first().userId}&olaphAccountId=")
                .then()
                .statusCode(HttpStatus.OK.value())
    }

    @Test
    @DisplayName("GET /standup-specs?onDayOfWeek=TUESDAY&olaphAccountId=")
    fun testGetWithDayOfWeek() {
        val standupSpec = this.standupSpecRepository.save(StandupSpec.sample())
        RestAssured.given()
                .log().all()
                .header(HttpHeaders.AUTHORIZATION, this.authKeyEncoded)
                .get("/standup-specs?onDayOfWeek=${standupSpec.days.first().dayOfWeek}&olaphAccountId=")
                .then()
                .statusCode(HttpStatus.OK.value())
    }

    @Test
    @DisplayName("GET /standup-specs?olaphAccountId=accountId")
    fun testGetolaphAccountId() {
        val standupSpec = this.standupSpecRepository.save(StandupSpec.sample())
        RestAssured.given()
                .log().all()
                .header(HttpHeaders.AUTHORIZATION, this.authKeyEncoded)
                .get("/standup-specs?olaphAccountId=${standupSpec.accountId}")
                .then()
                .statusCode(HttpStatus.OK.value())
    }

    @Test
    @DisplayName("GET /standup-specs/someId")
    private fun testGet() {
        val standupSpec = this.standupSpecRepository.save(StandupSpec.sample())

        RestAssured.given()
                .log().all()
                .header(HttpHeaders.AUTHORIZATION, this.authKeyEncoded)
                .get("/standup-specs/${standupSpec.id}")
                .then()
                .statusCode(HttpStatus.OK.value())
    }

    @Test
    @DisplayName("POST /standup-specs")
    fun testPost() {
        val standupSpecResponse = RestAssured.given()
                .log().all()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .header(HttpHeaders.AUTHORIZATION, this.authKeyEncoded)
                .body(StandupSpecRequest.sample())
                .post("/standup-specs")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .response()
                .body.`as`<SuccessfulStandupSpecResponse>(SuccessfulStandupSpecResponse::class.java) as SuccessfulStandupSpecResponse

        assertTrue(this.standupSpecRepository.existsById(standupSpecResponse.standupSpecResponse.id))
    }

    @Test
    @DisplayName("GET /standup-specs with non-existend id")
    fun testGetNonExistendID() {
        assertTrue(RestAssured.given()
                .log().all()
                .header(HttpHeaders.AUTHORIZATION, this.authKeyEncoded)
                .get("/standup-specs/doesNotExist")
                .body.`as`<CreateStandupSpecResponse>(CreateStandupSpecResponse::class.java) is ErrorCreateStandupSpecResponse)
    }
}