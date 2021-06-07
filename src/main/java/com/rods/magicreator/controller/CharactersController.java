package com.rods.magicreator.controller;

import com.rods.magicreator.domain.models.Character;
import com.rods.magicreator.domain.ports.in.IManageCharacters;
import com.rods.magicreator.domain.ports.in.IManageCharacters.CouldNotCreateCharacterException;
import com.rods.magicreator.domain.ports.in.IManageCharacters.CouldNotDeleteCharacterException;
import com.rods.magicreator.domain.ports.in.IManageCharacters.CouldNotSearchCharactersException;
import com.rods.magicreator.domain.ports.in.IManageCharacters.CouldNotUpdateCharacterException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
public class CharactersController {

    private final IManageCharacters charactersManager;

    public CharactersController(IManageCharacters charactersManager) {
        this.charactersManager = charactersManager;
    }

    @PostMapping("/character")
    public ResponseEntity<Character> create(@RequestBody CreateCharacterRequest request) {
        try {
            return new ResponseEntity<Character>(charactersManager.create(toCharacter(request)), HttpStatus.CREATED);
        } catch (CouldNotCreateCharacterException e) {
            if (e.contains(IllegalArgumentException.class))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMostSpecificCause().getMessage(), e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMostSpecificCause().getMessage(), e);
        }
    }

    @PutMapping("/character")
    public Character update(@RequestBody Character request) {
        try {
            return charactersManager.update(request);
        } catch (CouldNotUpdateCharacterException e) {
            if (e.contains(IllegalArgumentException.class))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMostSpecificCause().getMessage(), e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMostSpecificCause().getMessage(), e);
        }
    }

    @DeleteMapping("/character/{id}")
    public void delete(@PathVariable String id) {
        try {
            charactersManager.delete(id);
        } catch (CouldNotDeleteCharacterException e) {
            if (e.contains(IllegalArgumentException.class))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMostSpecificCause().getMessage(), e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMostSpecificCause().getMessage(), e);
        }
    }

    @GetMapping("/character/{id}")
    public Optional<Character> findById(@PathVariable String id) throws CouldNotSearchCharactersException {
        try {
            return charactersManager.findBy(id);
        } catch (CouldNotSearchCharactersException e) {
            if (e.contains(IllegalArgumentException.class))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMostSpecificCause().getMessage(), e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMostSpecificCause().getMessage(), e);
        }
    }

    @GetMapping("/characters")
    public Page<Character> findAll(@RequestParam int page) {
        try {
            return charactersManager.findAll(page);
        } catch (CouldNotSearchCharactersException e) {
            if (e.contains(IllegalArgumentException.class))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMostSpecificCause().getMessage(), e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMostSpecificCause().getMessage(), e);
        }
    }

    @GetMapping("/character")
    public List<Character> findBy(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String house,
            @RequestParam(required = false) String patronus) {
        try {
            return charactersManager.findBy(name, role, school, house, patronus);
        } catch (CouldNotSearchCharactersException e) {
            if (e.contains(IllegalArgumentException.class))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMostSpecificCause().getMessage(), e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMostSpecificCause().getMessage(), e);
        }
    }

    private Character toCharacter(CreateCharacterRequest request) {
        return Character.builder()
                .name(request.getName())
                .role(request.getRole())
                .school(request.getSchool())
                .house(request.getHouse())
                .patronus(request.getPatronus())
                .build();
    }
}


