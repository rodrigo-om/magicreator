package com.rods.magicreator.integration.repositories.house.http;

import com.rods.magicreator.repositories.house.http.PotterApiClient;
import com.rods.magicreator.repositories.house.http.models.HouseModelRoot;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;

public class PotterApiClientIT {

    public static MockWebServer mockServer;

    private final PotterApiClient api = new PotterApiClient(String.format("http://localhost:%s", mockServer.getPort()), "an-api-key");

    @BeforeAll
    static void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void GetHouses_ShouldCallApiCorrectly() throws InterruptedException {
        //Arrange
        mockServer.enqueue(new MockResponse()
                .setBody(housesStubResponse())
                .setHeader("Content-Type", "application/json")
        );

        //Act
        HouseModelRoot houses = api.getHouses();

        //Assert
        assertThat(mockServer.takeRequest().getHeader("apiKey")).isEqualTo("an-api-key");
        assertThat(houses.houses).hasSize(4);
        assertThat(houses.houses).extracting("id").containsExactlyInAnyOrder(
                "Gryffindor-Id-123","Hufflepuff-Id-45325","Ravenclaw-Id-12141","Slytherin-Id-12354258314"
        );
    }

    @Test
    void GetHouses_ShouldRetry_WhenHouseAPIReturnsError() {
        //Arrange
        mockServer.enqueue(new MockResponse()
                .setResponseCode(SERVICE_UNAVAILABLE.code()));
        mockServer.enqueue(new MockResponse()
                .setResponseCode(SERVICE_UNAVAILABLE.code()));
        mockServer.enqueue(new MockResponse()
                .setBody(housesStubResponse())
                .setHeader("Content-Type", "application/json")
        );

        //Act
        HouseModelRoot houses = api.getHouses();

        //Assert
        assertThat(houses.houses).hasSize(4);
        assertThat(mockServer.getRequestCount()).isEqualTo(3);
    }

    private String housesStubResponse() {
        return "{\n" +
                "    \"houses\": [\n" +
                "        {\n" +
                "            \"school\": \"Hogwarts School of Witchcraft and Wizardry\",\n" +
                "            \"colors\": [\n" +
                "                \"scarlet\",\n" +
                "                \"gold\"\n" +
                "            ],\n" +
                "            \"founder\": \"Goderic Gryffindor\",\n" +
                "            \"houseGhost\": \"Nearly Headless Nick\",\n" +
                "            \"headOfHouse\": \"Minerva McGonagall\",\n" +
                "            \"name\": \"Gryffindor\",\n" +
                "            \"mascot\": \"lion\",\n" +
                "            \"values\": [\n" +
                "                \"courage\",\n" +
                "                \"bravery\",\n" +
                "                \"nerve\",\n" +
                "                \"chivalry\"\n" +
                "            ],\n" +
                "            \"id\": \"Gryffindor-Id-123\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"Hufflepuff-Id-45325\",\n" +
                "            \"mascot\": \"badger\",\n" +
                "            \"colors\": [\n" +
                "                \"yellow\",\n" +
                "                \"black\"\n" +
                "            ],\n" +
                "            \"founder\": \"Helga Hufflepuff\",\n" +
                "            \"headOfHouse\": \"Pomona Sprout\",\n" +
                "            \"values\": [\n" +
                "                \"hard work\",\n" +
                "                \"patience\",\n" +
                "                \"justice\",\n" +
                "                \"loyalty\"\n" +
                "            ],\n" +
                "            \"school\": \"Hogwarts School of Witchcraft and Wizardry\",\n" +
                "            \"name\": \"Hufflepuff\",\n" +
                "            \"houseGhost\": \"The Fat Friar\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"founder\": \"Rowena Ravenclaw\",\n" +
                "            \"name\": \"Ravenclaw\",\n" +
                "            \"colors\": [\n" +
                "                \"blue\",\n" +
                "                \" bronze\"\n" +
                "            ],\n" +
                "            \"headOfHouse\": \"Fillius Flitwick\",\n" +
                "            \"values\": [\n" +
                "                \"intelligence\",\n" +
                "                \"creativity\",\n" +
                "                \"learning\",\n" +
                "                \"wit\"\n" +
                "            ],\n" +
                "            \"mascot\": \"eagle\",\n" +
                "            \"school\": \"Hogwarts School of Witchcraft and Wizardry\",\n" +
                "            \"id\": \"Ravenclaw-Id-12141\",\n" +
                "            \"houseGhost\": \"The Grey Lady\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"houseGhost\": \"The Bloody Baron\",\n" +
                "            \"headOfHouse\": \"Severus Snape\",\n" +
                "            \"founder\": \"Salazar Slytherin\",\n" +
                "            \"mascot\": \"serpent\",\n" +
                "            \"values\": [\n" +
                "                \"ambition\",\n" +
                "                \"cunning\",\n" +
                "                \"leadership\",\n" +
                "                \"resourcefulness\"\n" +
                "            ],\n" +
                "            \"name\": \"Slytherin\",\n" +
                "            \"colors\": [\n" +
                "                \"green\",\n" +
                "                \"silver\"\n" +
                "            ],\n" +
                "            \"id\": \"Slytherin-Id-12354258314\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
    }
}