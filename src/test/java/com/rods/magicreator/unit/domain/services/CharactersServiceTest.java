package com.rods.magicreator.unit.domain.services;

import com.flextrade.jfixture.JFixture;
import com.rods.magicreator.domain.models.Character;
import com.rods.magicreator.domain.models.House;
import com.rods.magicreator.domain.ports.in.IDisplayHouses;
import com.rods.magicreator.domain.ports.in.IDisplayHouses.CouldNotSearchHousesException;
import com.rods.magicreator.domain.ports.in.IManageCharacters;
import com.rods.magicreator.domain.ports.in.IManageCharacters.*;
import com.rods.magicreator.domain.ports.out.IObtainHousesInfo;
import com.rods.magicreator.domain.ports.out.IObtainHousesInfo.ErrorObtainingHousesException;
import com.rods.magicreator.domain.ports.out.IStoreCharacters;
import com.rods.magicreator.domain.ports.out.IStoreCharacters.ErrorDeletingCharacterException;
import com.rods.magicreator.domain.ports.out.IStoreCharacters.ErrorSearchingCharactersException;
import com.rods.magicreator.domain.ports.out.IStoreCharacters.ErrorStoringCharacterException;
import com.rods.magicreator.domain.CharactersService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


public class CharactersServiceTest {
    public static IStoreCharacters charactersRepositoryMock = Mockito.mock(IStoreCharacters.class);
    public static IObtainHousesInfo housesRepositoryMock = Mockito.mock(IObtainHousesInfo.class);

    private final CharactersService service = new CharactersService(charactersRepositoryMock, housesRepositoryMock);

    private final JFixture fixture = new JFixture();
    private final String houseId;
    private final House house;
    private final Character characterReceived;

    @SneakyThrows
    public CharactersServiceTest() {
        houseId = fixture.create(String.class);
        house = fixture.create(House.class).withId(houseId);
        characterReceived = fixture.create(Character.class).toBuilder().id(null).houseName(null).house(houseId).build();
    }

    @BeforeEach
    public void init() {
        reset(charactersRepositoryMock);
        reset(housesRepositoryMock);
    }

    // ----------- CREATE() TESTS ---------------------------------

    @Test
    void Create_Should_CreateCorrectlyGivenRightConditions() throws ErrorStoringCharacterException, CouldNotCreateCharacterException, ErrorObtainingHousesException {
        //Arrange
        Character characterToSave = characterReceived.withHouseName(house.getName());
        Character characterSaved = characterToSave.withId(fixture.create(String.class));

        when(housesRepositoryMock.getHouseById(houseId)).thenReturn(Optional.of(house));
        when(charactersRepositoryMock.create(characterToSave)).thenReturn(characterSaved);

        //Act
        Character saved = service.create(characterToSave);

        //Assert
        assertThat(saved).usingRecursiveComparison().ignoringFields("id").isEqualTo(characterToSave);
        assertThat(saved.getId()).isEqualTo(characterSaved.getId());
    }

    @Test
    void Create_Should_NotCallStorageAndThrowAnExceptionIfProvidedCharacterHasIdNonNull() throws ErrorObtainingHousesException, ErrorStoringCharacterException {
        //Arrange
        Character characterToSaveWithInvalidHouse = characterReceived.withHouse(houseId).withId("a-non-null-id");

        //Act
        //Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.create(characterToSaveWithInvalidHouse);
        });
        assertThat(exception.getMessage()).contains("Character Id must be null - Did you mean to update it?");
        assertThat(exception.getCause()).isNull();
        verify(charactersRepositoryMock, never()).create(any(Character.class));
        verify(housesRepositoryMock, never()).getHouseById(any(String.class));
    }

    @Test
    void Create_Should_NotCallStorageAndThrowAnExceptionIfHouseDoesNotExist() throws ErrorObtainingHousesException, ErrorStoringCharacterException {
        //Arrange
        String houseId = "invalid-house";
        Character characterToSaveWithInvalidHouse = characterReceived.withHouse(houseId);

        when(housesRepositoryMock.getHouseById(houseId)).thenReturn(Optional.empty());

        //Act
        //Assert
        InvalidHouseProvidedException exception = assertThrows(InvalidHouseProvidedException.class, () -> {
            service.create(characterToSaveWithInvalidHouse);
        });
        assertThat(exception.getMessage()).isEqualTo("Invalid house provided as argument - House id: "+houseId);
        assertThat(exception.getCause()).isNull();
        verify(charactersRepositoryMock, never()).create(any(Character.class));
    }

    @Test
    void Create_Should_NotCallStorageAndThrowAnExceptionIfGettingHouseThrowsAnError() throws ErrorObtainingHousesException, ErrorStoringCharacterException {
        //Arrange
        Character characterToSaveWithInvalidHouse = characterReceived.withHouse("obtainer-throws-error");

        when(housesRepositoryMock.getHouseById("obtainer-throws-error")).thenThrow(new RuntimeException("Error retrieving Houses information"));

        //Act
        //Assert
        CouldNotCreateCharacterException exception = assertThrows(CouldNotCreateCharacterException.class, () -> {
            service.create(characterToSaveWithInvalidHouse);
        });
        assertThat(exception.getMessage()).contains("Error retrieving Houses information");
        assertThat(exception.getCause()).isInstanceOfAny(RuntimeException.class);
        verify(charactersRepositoryMock, never()).create(any(Character.class));
    }

    @Test
    void Create_Should_NotCallStorageAndThrowAnExceptionIfStorageThrowsAnError() throws ErrorStoringCharacterException, ErrorObtainingHousesException {
        //Arrange
        Character characterToSave = characterReceived.withHouse(houseId).withHouseName(house.getName());

        when(housesRepositoryMock.getHouseById(houseId)).thenReturn(Optional.of(house));
        when(charactersRepositoryMock.create(characterToSave)).thenThrow(new ErrorStoringCharacterException("Error storing new character", new RuntimeException()));

        //Act
        //Assert
        CouldNotCreateCharacterException exception = assertThrows(CouldNotCreateCharacterException.class, () -> {
            service.create(characterToSave);
        });
        assertThat(exception.getMessage()).contains("Error storing new character");
        assertThat(exception.getCause()).isInstanceOfAny(ErrorStoringCharacterException.class);
        assertThat(exception.getMostSpecificCause()).isInstanceOfAny(RuntimeException.class);
    }

    // ----------- UPDATE() TESTS ---------------------------------

    @Test
    void Update_Should_UpdateCorrectlyGivenRightConditions() throws ErrorStoringCharacterException, CouldNotUpdateCharacterException, ErrorObtainingHousesException {
        //Arrange
        Character character = characterReceived
                .withId(fixture.create(String.class))
                .withHouseName(house.getName())
                .withRole("updatedRole");

        when(housesRepositoryMock.getHouseById(houseId)).thenReturn(Optional.of(house));
        when(charactersRepositoryMock.update(character)).thenReturn(character);

        //Act
        Character updated = service.update(character);

        //Assert
        assertThat(updated).usingRecursiveComparison().isEqualTo(character);
        assertThat(updated.getId()).isEqualTo(character.getId());
    }

    @Test
    void Update_Should_NotCallStorageAndThrowAnExceptionIfProvidedCharacterHasIdNull() throws ErrorObtainingHousesException, ErrorStoringCharacterException {
        //Arrange
        Character character = characterReceived
                .withId(null);

        //Act
        //Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            service.update(character);
        });
        assertThat(exception.getMessage()).contains("Character Id must not be null - Did you mean to create it?");
        assertThat(exception.getCause()).isNull();
        verify(charactersRepositoryMock, never()).update(any(Character.class));
        verify(housesRepositoryMock, never()).getHouseById(any(String.class));
    }

    @Test
    void Update_Should_NotCallStorageAndThrowAnExceptionIfHouseDoesNotExist() throws ErrorObtainingHousesException, ErrorStoringCharacterException {
        //Arrange
        String houseId = "invalid-house";
        Character characterWithInvalidHouse = characterReceived.withId(fixture.create(String.class)).withHouse(houseId);

        when(housesRepositoryMock.getHouseById(houseId)).thenReturn(Optional.empty());

        //Act
        //Assert
        InvalidHouseProvidedException exception = assertThrows(InvalidHouseProvidedException.class, () -> {
            service.update(characterWithInvalidHouse);
        });
        assertThat(exception.getMessage()).isEqualTo("Invalid house provided as argument - House id: "+houseId);
        assertThat(exception.getCause()).isNull();
        verify(charactersRepositoryMock, never()).update(any(Character.class));
    }

    @Test
    void Update_Should_NotCallStorageAndThrowAnExceptionIfGettingHouseThrowsAnError() throws ErrorObtainingHousesException, ErrorStoringCharacterException {
        //Arrange
        Character characterWithErrorHouse = characterReceived.withId(fixture.create(String.class)).withHouse("obtainer-throws-error");

        when(housesRepositoryMock.getHouseById("obtainer-throws-error")).thenThrow(new RuntimeException("Error retrieving Houses information"));

        //Act
        //Assert
        CouldNotUpdateCharacterException exception = assertThrows(CouldNotUpdateCharacterException.class, () -> {
            service.update(characterWithErrorHouse);
        });
        assertThat(exception.getMessage()).contains("Error retrieving Houses information");
        assertThat(exception.getCause()).isInstanceOfAny(RuntimeException.class);
        verify(charactersRepositoryMock, never()).update(any(Character.class));
    }

    @Test
    void Update_Should_NotCallStorageAndThrowAnExceptionIfStorageThrowsAnError() throws ErrorStoringCharacterException, ErrorObtainingHousesException {
        //Arrange
        Character character = characterReceived.withId(fixture.create(String.class)).withHouse(houseId).withHouseName(house.getName());

        when(housesRepositoryMock.getHouseById(houseId)).thenReturn(Optional.of(house));
        when(charactersRepositoryMock.update(character)).thenThrow(new ErrorStoringCharacterException("Error updating a character", new RuntimeException()));

        //Act
        //Assert
        CouldNotUpdateCharacterException exception = assertThrows(CouldNotUpdateCharacterException.class, () -> {
            service.update(character);
        });
        assertThat(exception.getMessage()).contains("Error updating a character");
        assertThat(exception.getCause()).isInstanceOfAny(ErrorStoringCharacterException.class);
        assertThat(exception.getMostSpecificCause()).isInstanceOfAny(RuntimeException.class);
    }

    // ----------- FINDALL() TESTS ---------------------------------

    @Test
    void FindAll_Should_ReturnCorrectlyGivenThereAreCharacters() throws ErrorSearchingCharactersException, CouldNotSearchCharactersException {
        //Arrange
        Page<Character> characters = new PageImpl<>(List.of(
                fixture.create(Character.class),
                fixture.create(Character.class),
                fixture.create(Character.class)
        ));

        when(charactersRepositoryMock.findAll(1)).thenReturn(characters);

        //Act
        Page<Character> charactersRetrieved = service.findAll(1);

        //Assert
        assertThat(charactersRetrieved).usingRecursiveComparison().isEqualTo(characters);
    }

    @Test
    void FindAll_Should_ReturnCorrectlyEvenThoughThereAreNoCharacters() throws ErrorSearchingCharactersException, CouldNotSearchCharactersException {
        //Arrange
        Page<Character> noCharacters = new PageImpl<>(Collections.emptyList());

        when(charactersRepositoryMock.findAll(1)).thenReturn(noCharacters);

        //Act
        Page<Character> charactersRetrieved = service.findAll(1);

        //Assert
        assertThat(charactersRetrieved).usingRecursiveComparison().isEqualTo(noCharacters);
        assertThat(charactersRetrieved).isEmpty();
    }

    @Test
    void FindAll_Should_ThrowExceptionWhenSearchHasAnError() throws ErrorSearchingCharactersException, CouldNotSearchCharactersException {
        //Arrange
        when(charactersRepositoryMock.findAll(1)).thenThrow(new ErrorSearchingCharactersException(new RuntimeException("Error reaching db")));

        //Act
        //Assert
        CouldNotSearchCharactersException exception = assertThrows(CouldNotSearchCharactersException.class, () -> {
            service.findAll(1);
        });
        assertThat(exception.getMessage()).contains("Error searching characters");
        assertThat(exception.getCause()).isInstanceOfAny(ErrorSearchingCharactersException.class);
        assertThat(exception.getMostSpecificCause()).isInstanceOfAny(RuntimeException.class);
    }

    // ----------- FINDBY() TESTS ---------------------------------

    @Test
    void FindBy_Should_ReturnCorrectlyGivenThereIsACharacter() throws ErrorSearchingCharactersException, CouldNotSearchCharactersException {
        //Arrange
        Character character = fixture.create(Character.class);

        when(charactersRepositoryMock.findBy("1")).thenReturn(Optional.of(character));

        //Act
        Optional<Character> charactersRetrieved = service.findBy("1");

        //Assert
        assertThat(charactersRetrieved).isPresent();
        assertThat(charactersRetrieved.get()).usingRecursiveComparison().isEqualTo(character);
    }

    @Test
    void FindBy_Should_ReturnCorrectlyEvenThoughThereAreNoCharacters() throws ErrorSearchingCharactersException, CouldNotSearchCharactersException {
        //Arrange
        when(charactersRepositoryMock.findBy("1")).thenReturn(Optional.empty());

        //Act
        Optional<Character> charactersRetrieved = service.findBy("1");

        //Assert
        assertThat(charactersRetrieved).isEmpty();
    }

    @Test
    void FindBy_Should_ThrowExceptionWhenSearchHasAnError() throws ErrorSearchingCharactersException, CouldNotSearchCharactersException {
        //Arrange
        when(charactersRepositoryMock.findBy("1")).thenThrow(new ErrorSearchingCharactersException(new RuntimeException("Error reaching db")));

        //Act
        //Assert
        CouldNotSearchCharactersException exception = assertThrows(CouldNotSearchCharactersException.class, () -> {
            service.findBy("1");
        });
        assertThat(exception.getMessage()).contains("Error searching characters");
        assertThat(exception.getCause()).isInstanceOfAny(ErrorSearchingCharactersException.class);
        assertThat(exception.getMostSpecificCause()).isInstanceOfAny(RuntimeException.class);
    }

    // ----------- FINDBY() TESTS ---------------------------------

    @Test
    void FindByFilters_Should_ReturnCorrectlyGivenThereAreCharacters() throws ErrorSearchingCharactersException, CouldNotSearchCharactersException {
        //Arrange
        List<Character> characters = List.of(
                fixture.create(Character.class),
                fixture.create(Character.class)
        );

        when(charactersRepositoryMock.findBy("a-name", "a-role", "a-school", "a-house", "a-patronus"))
                .thenReturn(characters);

        //Act
        List<Character> charactersRetrieved = service.findBy("a-name", "a-role", "a-school", "a-house", "a-patronus");

        //Assert
        assertThat(charactersRetrieved).usingRecursiveComparison().isEqualTo(characters);
    }

    @Test
    void FindByFilters_Should_ReturnCorrectlyEvenThoughThereAreNoCharacters() throws ErrorSearchingCharactersException, CouldNotSearchCharactersException {
        //Arrange
        when(charactersRepositoryMock.findBy("a-name", "a-role", "a-school", "a-house", "a-patronus"))
                .thenReturn(Collections.emptyList());

        //Act
        List<Character> charactersRetrieved = service.findBy("a-name", "a-role", "a-school", "a-house", "a-patronus");

        //Assert
        assertThat(charactersRetrieved).isEmpty();
    }

    @Test
    void FindByFilters_Should_ThrowExceptionWhenSearchHasAnError() throws ErrorSearchingCharactersException, CouldNotSearchCharactersException {
        //Arrange
        when(charactersRepositoryMock.findBy("a-name", "a-role", "a-school", "a-house", "a-patronus"))
                .thenThrow(new ErrorSearchingCharactersException(new RuntimeException("Error reaching db")));

        //Act
        //Assert
        CouldNotSearchCharactersException exception = assertThrows(CouldNotSearchCharactersException.class, () -> {
            service.findBy("a-name", "a-role", "a-school", "a-house", "a-patronus");
        });
        assertThat(exception.getMessage()).contains("Error searching characters");
        assertThat(exception.getCause()).isInstanceOfAny(ErrorSearchingCharactersException.class);
        assertThat(exception.getMostSpecificCause()).isInstanceOfAny(RuntimeException.class);
    }

    // ----------- DELETE() TESTS ---------------------------------

    @Test
    void Delete_Should_ReturnCorrectlyNoErrorsOccur() throws ErrorDeletingCharacterException, CouldNotDeleteCharacterException {
        //Arrange
        doNothing().when(charactersRepositoryMock).delete("1");

        //Act
        //Assert
        assertDoesNotThrow(() -> service.delete("1"));
    }

    @Test
    void Delete_Should_ThrowExceptionWhenDeleteHasAnError() throws ErrorDeletingCharacterException {
        //Arrange
        doThrow(new ErrorDeletingCharacterException("Error deleting character", new RuntimeException("Error deleting in db"))).when(charactersRepositoryMock).delete("1");

        //Act
        //Assert
        CouldNotDeleteCharacterException exception = assertThrows(CouldNotDeleteCharacterException.class, () -> {
            service.delete("1");
        });
        assertThat(exception.getMessage()).contains("Error deleting character");
        assertThat(exception.getCause()).isInstanceOfAny(ErrorDeletingCharacterException.class);
    }

    // ----------- FINDHOUSES() TESTS ---------------------------------

    @Test
    void FindHouses_Should_ReturnCorrectlyGivenThereAreHouses() throws ErrorObtainingHousesException, CouldNotSearchHousesException {
        //Arrange
        List<House> houses = List.of(
                fixture.create(House.class),
                fixture.create(House.class),
                fixture.create(House.class)
        );

        when(housesRepositoryMock.getHouses()).thenReturn(houses);

        //Act
        List<House> housesRetrieved = service.findHouses();

        //Assert
        assertThat(housesRetrieved).usingRecursiveComparison().isEqualTo(houses);
    }

    @Test
    void FindHouses_Should_ReturnCorrectlyEvenThoughThereAreNoHouses() throws ErrorObtainingHousesException, CouldNotSearchHousesException {
        //Arrange
        when(housesRepositoryMock.getHouses()).thenReturn(Collections.emptyList());

        //Act
        List<House> housesRetrieved = service.findHouses();

        //Assert
        assertThat(housesRetrieved).isEmpty();
    }

    @Test
    void FindHouses_Should_ThrowExceptionWhenSearchHasAnError() throws ErrorObtainingHousesException {
        //Arrange
        when(housesRepositoryMock.getHouses()).thenThrow(new ErrorObtainingHousesException(new RuntimeException("Error reaching api")));

        //Act
        //Assert
        CouldNotSearchHousesException exception = assertThrows(CouldNotSearchHousesException.class, service::findHouses);
        assertThat(exception.getMessage()).contains("Could not search houses");
        assertThat(exception.getCause()).isInstanceOfAny(ErrorObtainingHousesException.class);
        assertThat(exception.getMostSpecificCause()).isInstanceOfAny(RuntimeException.class);
    }

}