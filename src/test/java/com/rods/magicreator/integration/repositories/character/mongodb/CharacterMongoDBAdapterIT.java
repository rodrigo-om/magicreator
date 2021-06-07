package com.rods.magicreator.integration.repositories.character.mongodb;

import com.flextrade.jfixture.JFixture;
import com.rods.magicreator.domain.models.Character;
import com.rods.magicreator.domain.ports.out.IStoreCharacters;
import com.rods.magicreator.domain.ports.out.IStoreCharacters.ErrorSearchingCharactersException;
import com.rods.magicreator.domain.ports.out.IStoreCharacters.ErrorStoringCharacterException;
import com.rods.magicreator.repositories.character.mongodb.CharacterMongoDBAdapter;
import com.rods.magicreator.repositories.character.mongodb.CharacterRepository;
import com.rods.magicreator.repositories.character.mongodb.models.CharacterModel;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Testcontainers
@SpringBootTest
public class CharacterMongoDBAdapterIT {
    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");
    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CharacterMongoDBAdapter adapter;

    @Autowired
    private CharacterRepository repository;

    JFixture fixture = new JFixture();

    @BeforeEach
    public void init() {
        repository.deleteAll();
    }

    private Optional<Character> getCachedCharacter(String id) {
        return Optional.ofNullable(cacheManager.getCache("characters")).map(c -> c.get(id, Character.class));
    }

    @Test
    void Create_Should_FillIdCorrectly() throws ErrorStoringCharacterException {
        //Arrange
        Character character = fixture.create(Character.class);
        character.setId(null);

        //Act
        Character saved = adapter.create(character);

        //Assert
        assertThat(saved).usingRecursiveComparison().ignoringFields("id").isEqualTo(character);
        assertThat(saved.getId()).isNotNull();

        CharacterModel characterSaved = repository.findById(new ObjectId(saved.getId())).get();
        assertThat(characterSaved.getName()).isEqualTo(character.getName());
        assertThat(characterSaved.getRole()).isEqualTo(character.getRole());
        assertThat(characterSaved.getSchool()).isEqualTo(character.getSchool());
        assertThat(characterSaved.getHouse()).isEqualTo(character.getHouse());
        assertThat(characterSaved.getHouseName()).isEqualTo(character.getHouseName());
        assertThat(characterSaved.getPatronus()).isEqualTo(character.getPatronus());
    }

    @Test
    void FindById_Should_ConvertIdCorrectly_And_Find_Characters() throws ErrorSearchingCharactersException {
        //Arrange
        CharacterModel characterSaved = repository.save(fixture.create(CharacterModel.class));
        String expectedPreviousIdConversion = characterSaved.getId().toString();

        //Act
        Character found = adapter.findBy(expectedPreviousIdConversion).get();

        //Assert
        assertThat(found.getId()).isEqualTo(characterSaved.getId().toString());
        assertThat(found.getName()).isEqualTo(characterSaved.getName());
        assertThat(found.getRole()).isEqualTo(characterSaved.getRole());
        assertThat(found.getSchool()).isEqualTo(characterSaved.getSchool());
        assertThat(found.getHouse()).isEqualTo(characterSaved.getHouse());
        assertThat(found.getPatronus()).isEqualTo(characterSaved.getPatronus());
        assertThat(getCachedCharacter(found.getId()).get()).isEqualTo(found);
    }

    @Test
    void FindBy_Should_BuildFiltersCorrectly_WhenGivenExactMatchFields() throws ErrorSearchingCharactersException {
        //Arrange
        CharacterModel draco = new CharacterModel(null, "Draco Malfoy", "Student", "Hogwarts", "1234Sonserina", "None", "Sonserina");
        CharacterModel hermione = new CharacterModel(null, "Hermione Granger", "Student", "Hogwarts", "4321Gryffindor", "Otter", "Gryffindor");
        CharacterModel rony = new CharacterModel(null, "Rony Weasley", "Student", "Hogwarts", "4321Gryffindor", "Terrier", "Gryffindor");
        CharacterModel minerva = new CharacterModel(null, "Minerva McGonagall", "Professor", "Hogwarts", "4321Gryffindor", "Cat", "Gryffindor");
        repository.saveAll(List.of(draco, hermione, rony, minerva));

        //Act
        List<Character> students = adapter.findBy(null, "Student", null, null, null);
        List<Character> gryffindor = adapter.findBy(null, null, null, "4321Gryffindor", null);
        List<Character> gryffindorStudents = adapter.findBy(null, "Student", null, "4321Gryffindor", null);
        List<Character> noExactMatch = adapter.findBy(null, "Stu", null, "4321Gryf", null);

        //Assert
        assertThat(students).extracting("name").containsExactlyInAnyOrder("Draco Malfoy", "Hermione Granger", "Rony Weasley");
        assertThat(gryffindor).extracting("name").containsExactlyInAnyOrder("Hermione Granger", "Rony Weasley", "Minerva McGonagall");
        assertThat(gryffindorStudents).extracting("name").containsExactlyInAnyOrder("Hermione Granger", "Rony Weasley");
        assertThat(noExactMatch).isEmpty();
    }

    @Test
    void FindBy_Should_BuildFiltersCorrectly_WhenGivenFieldsThatMatchOnContain() throws ErrorSearchingCharactersException {
        //Arrange
        CharacterModel draco = new CharacterModel(null, "Draco Malfoy", "Student", "Hogwarts", "1234Sonserina", "None", "Sonserina");
        CharacterModel lucius = new CharacterModel(null, "Lucius Malfoy", "Death Eater", "Hogwarts", "1234Sonserina", "CantHaveOne", "Sonserina");
        CharacterModel cissy = new CharacterModel(null, "Narcissa Malfoy", "Death Eater", "Hogwarts", "1234Sonserina", "Terrier", "Sonserina");
        CharacterModel minerva = new CharacterModel(null, "Minerva McGonagall", "Professor", "Hogwarts", "4321Gryffindor", "Cat", "Gryffindor");
        repository.saveAll(List.of(draco, lucius, cissy, minerva));

        //Act
        List<Character> malfoy = adapter.findBy("Malfoy", null, null, null, null);
        List<Character> deathEaterMalfoys = adapter.findBy("Malfoy", "Death Eater", null, null, null);

        //Assert
        assertThat(malfoy).extracting("name").containsExactlyInAnyOrder("Draco Malfoy", "Lucius Malfoy", "Narcissa Malfoy");
        assertThat(deathEaterMalfoys).extracting("name").containsExactlyInAnyOrder("Lucius Malfoy", "Narcissa Malfoy");
    }

    @Test
    void Update_Should_UpdateCorrectlyAnyChangedField() throws ErrorStoringCharacterException {
        //Arrange
        CharacterModel harryFirstDayInSchool = repository.save(new CharacterModel(
                null, "Harry Potter", "Student", "Hogwarts", "4321Gryffindor", "None", "Gryffindor"
        ));

        Character harryAdult = new Character(
                harryFirstDayInSchool.getId().toString(),
                harryFirstDayInSchool.getName(),
                "Auror",
                harryFirstDayInSchool.getSchool(),
                harryFirstDayInSchool.getHouse(),
                harryFirstDayInSchool.getHouseName(),
                "Stag"
        );

        //Act
        adapter.update(harryAdult);

        //Assert
        CharacterModel harryNow = repository.findById(harryFirstDayInSchool.getId()).get();
        assertThat(harryNow.getRole()).isEqualTo("Auror");
        assertThat(harryNow.getPatronus()).isEqualTo("Stag");
        assertThat(harryNow.getHouse()).isEqualTo(harryFirstDayInSchool.getHouse());
        assertThat(harryNow.getSchool()).isEqualTo(harryFirstDayInSchool.getSchool());
    }

    @Test
    void CreateAndUpdate_Should_EvictFindByIdCache() throws ErrorSearchingCharactersException, ErrorStoringCharacterException, IStoreCharacters.ErrorDeletingCharacterException {
        //Arrange
        CharacterModel characterSaved = repository.save(fixture.create(CharacterModel.class));

        //Act
        //Assert
        Character found = adapter.findBy(characterSaved.getId().toString()).get();
        assertThat(getCachedCharacter(found.getId()).get()).isEqualTo(found);

        found.setHouse("new-house");
        Character updated = adapter.update(found);
        assertThat(getCachedCharacter(found.getId()).get()).isEqualTo(updated);

        adapter.delete(updated.getId());
        assertThat(getCachedCharacter(found.getId())).isEmpty();
    }
}