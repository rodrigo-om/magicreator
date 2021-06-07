package com.rods.magicreator.unit.domain.services;

import com.flextrade.jfixture.JFixture;
import com.rods.magicreator.domain.models.Character;
import com.rods.magicreator.domain.models.House;
import com.rods.magicreator.domain.ports.in.IManageCharacters;
import com.rods.magicreator.domain.ports.in.IManageCharacters.CouldNotCreateCharacterException;
import com.rods.magicreator.domain.ports.in.IManageCharacters.CouldNotUpdateCharacterException;
import com.rods.magicreator.domain.ports.in.IManageCharacters.InvalidHouseProvidedException;
import com.rods.magicreator.domain.ports.out.IObtainHousesInfo;
import com.rods.magicreator.domain.ports.out.IObtainHousesInfo.ErrorObtainingHousesException;
import com.rods.magicreator.domain.ports.out.IStoreCharacters;
import com.rods.magicreator.domain.ports.out.IStoreCharacters.ErrorStoringCharacterException;
import com.rods.magicreator.domain.services.CharactersService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
}