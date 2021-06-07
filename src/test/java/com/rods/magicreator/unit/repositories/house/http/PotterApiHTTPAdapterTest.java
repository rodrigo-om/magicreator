package com.rods.magicreator.unit.repositories.house.http;

import com.flextrade.jfixture.JFixture;
import com.rods.magicreator.domain.models.House;
import com.rods.magicreator.domain.ports.out.IObtainHousesInfo.ErrorObtainingHousesException;
import com.rods.magicreator.repositories.house.http.PotterApiClient;
import com.rods.magicreator.repositories.house.http.PotterApiHttpAdapter;
import com.rods.magicreator.repositories.house.http.models.HouseModel;
import com.rods.magicreator.repositories.house.http.models.HouseModelRoot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class PotterApiHTTPAdapterTest {

    public static PotterApiClient houseApiMock = Mockito.mock(PotterApiClient.class);

    private final PotterApiHttpAdapter adapter = new PotterApiHttpAdapter(houseApiMock);

    JFixture fixture = new JFixture();

    @BeforeEach
    public void init() {
        reset(houseApiMock);
    }

    @Test
    void getHouseById_GivenAnIdShouldGetItFromTheHousesList() throws ErrorObtainingHousesException {
        //Arrange
        HouseModel house1 = fixture.create(HouseModel.class);
        HouseModel house2 = fixture.create(HouseModel.class);
        HouseModelRoot response = HouseModelRoot.builder().houses(List.of(house1, house2)).build();

        when(houseApiMock.getHouses()).thenReturn(response);

        //Act
        Optional<House> house = adapter.getHouseById(house1.getId());

        //Assert
        assertThat(house.get().getId()).isEqualTo(house1.getId());
    }

    @Test
    void getHouseById_ShouldReturnEmptyIfIdsDontMatch() throws ErrorObtainingHousesException {
        //Arrange
        HouseModel house1 = fixture.create(HouseModel.class);
        HouseModel house2 = fixture.create(HouseModel.class);
        HouseModelRoot response = HouseModelRoot.builder().houses(List.of(house1, house2)).build();

        when(houseApiMock.getHouses()).thenReturn(response);

        //Act
        Optional<House> house = adapter.getHouseById("not-included-id");

        //Assert
        assertThat(house).isEmpty();
    }

    @Test
    void getHouseById_ShouldReturnEmptyIfNoHousesAreFoundInApi() throws ErrorObtainingHousesException {
        //Arrange
        when(houseApiMock.getHouses()).thenReturn(HouseModelRoot.builder().houses(Collections.emptyList()).build());

        //Act
        Optional<House> house = adapter.getHouseById("not-included-id");

        //Assert
        assertThat(house).isEmpty();
    }

    @Test
    void getHouseById_ShouldWrapExceptionsInAppropriateException() {
        //Arrange
        when(houseApiMock.getHouses()).thenThrow(new RuntimeException());

        //Act
        //Assert
        assertThrows(ErrorObtainingHousesException.class, () -> {
            adapter.getHouseById("some-exception-will-be-thrown");
        });
    }
}