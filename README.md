# Magicreator
Hello and welcome to the Magicreator, an application that lets you create your own database of Harry Potter characters with validations from a real PotterAPI. 

This README aims to help you in running the magicreator application and explaining a little about the way it was built and discuss some ideas. 

---
## How to run it
##### Let's get straight to the point:

There are 3 main ways to run it:  

1. From the contained docker-compose.yml file. It will deploy the application together with the database. *(RECOMMENDED)*
3. From your favorite IDE
4. From a terminal

Every method besides #1 requires you to generate your own api-key for accessing potter-api, so, for the sake of brevity, I will describe the recommended way here and jump to the next topics, but instructions for the other ways to run it will be described at the end of this README.

### 1. Docker-Compose
This one already has my own api-key bundled with the docker image so you don't have to worry about that. Doing this allowed me to avoid exposing the api-key openly on github and made running the application easier

##### Dependencies:
- [Docker]

##### Running it:
From inside the project root folder (if you see the docker-compose.yml, that's the place):
```sh
docker compose up
```
> Hint: the command may be "docker-compose up" if you have an older docker version.

And that's it. The application will be acessible by the local port 8080, and MongoDB will be acessible through 27017.

-----

## Testing the application

Inside the project root folder, there is a *Magicreator.postman_collection.json* file containing a Postman collection you can import. It contains a few scenarios ready and the the endpoints you can play around. 

![photo_2021-06-01_00-09-30.jpg](https://www.dropbox.com/s/813l93nl8x97lsw/photo_2021-06-07_04-59-44.jpg?dl=0&raw=1)

I highly recommend importing this, but if you don't want to do that now, it's ok, here are the main requests in curl version:

Create Character

```sh
curl --location --request POST 'http://localhost:8080/api/v1/character' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "Harry Potter",
    "role": "student",
    "school": "Hogwarts School of Witchcraft and Wizardry",
    "house": "1760529f-6d51-4cb1-bcb1-25087fce5bde",
    "patronus": "stag"
}'
```

Find By Id
```sh
curl --location --request GET 'http://localhost:8080/api/v1/character/insira-o-id-aqui'
```

Find All Accounts
```sh
curl --location --request GET 'http://localhost:8080/api/v1/characters?page=0'
```

Update a Character
```sh
curl --location --request PUT 'http://localhost:8080/api/v1/character' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": "60bdd0d2554d3a32235bc229",
    "name": "Thiago Potter",
    "role": "pai",
    "school": "Hogwarts School of Witchcraft and Wizardry",
    "house": "1760529f-6d51-4cb1-bcb1-25087fce5bde",
    "patronus": "stag"
}'
```

-----
## How it was built

![WhatsApp Image 2021-05-31 at 22.48.44.jpeg](https://www.dropbox.com/s/vai3jgcbj9z617h/photo_2021-06-07_05-14-39.jpg?dl=0&raw=1)

Core technology versions:
- [Java OpenJDK14]
- [Spring Boot 2.5] (https://spring.io/)

Following a [Hexagonal Architecture], I've built Inbound and Outbound ports to keep domain isolated, and technology-specific adapters (except for the IN controllers, I thought building adapters for them would be overkill)

#### Tests
Building it through TDD, the project is *almost* completely unit and integration tested. You can run these tests in the project root folder with:
```sh
./gradlew test
```

Or you can use your favorite IDE.

Some of these tests are integration tests, but it's ok, [they have been configured to start a mongodb container](https://www.testcontainers.org/) when necessary and to kill it after.

----

## Challenges + known issues

I was not up to date with Spring for quite a few years, having only used it in very small PoCs years ago, so it was quite challenging but an interesting experience learning more about it, it sure makes development faster when you get a grip on it. Having said that, I may have failed in some standard Spring good practices here and there from lack of experience with it, so feel free to point any of them. 

Another point I think is worth talking about is the Exceptions flow in a hexagonal architecture. Coming from a funcional language where exceptions are nothing more but "Failed returns", and thus harder to invade domain boundaries, I had to do a bit of thinking when organizing possible checked exceptions and wrapping them so the Domain API was understandable enough for code outside of it to integrate properly. I saw a lot of debate about wheter exceptions should be wrapped or not, if they should be checked or not... and I went with what I thought was more "secure" to the domain. But again, please feel free to disagree and suggest a better approach, I was a bit bothered by how "crowded" the could get with this solution.

----

## Other ways to run the application
I almost forgot this!
The other 2 ways you can run the application are:

### 2. From your favorite IDE
##### Dependencies:
- [IDE]
- [Java JDK 14]
- [Docker]

##### Running it:
Very simple, you just need to import the project on your favorite IDE with Spring Framework support enabled, and hit Execute! Hopefully everything will go smoothly. For the PotterAPI apikey, you can [generate it](#generating-your-api-key) and place it on application.properties in the potterapi.apikey

Oh, and before I forget, one also requires the mongodb container running, so I've set up a docker-compose-mongo-only.yml file for these situations. So make sure to 
```sh
docker compose -f .\docker-compose-mongo-only.yml up
```

And voi'lá!

### 3. From a terminal
##### Dependencies:
- [Java OpenJDK14]

##### Running it:
Pretty similar to the previous one. You will also have to [generate a key](#generate-your-api-key) and place it on application.properties + run docker-compose-mongo-only.yml, but this time, from the root project folder you will run

```sh
java -jar build/libs/magicreator-1.0.jar com.rods.magicreator.MagicreatorApplication
```

If all things are good, the application should start running on port 8080 correctly, otherwise, we will have to google the issue :(

----

## Generating your API Key

1 - User creation:
```
Method: POST
Host: http://us-central1-rh-challenges.cloudfunctions.net/potterApi/users
Body: {
	"email": "SEU_EMAIL_AQUI",
	"password": "DEFINA_UMA_SENHA_AQUI",
	"name": "SEU_NOME_AQUI"
}
```

The response will contain your api key, save it!
```
{
  "user": {
    "email": "email",
    "password": "sua senha criptografada",
    "id": "id",
    "apiKey": "sua apikey",
    "name": "nome"
  }
}
```

# Thank You!

It was a fun challenge and I hope it lived up to your expectations and that this README was clear enough to make testing the application a little bit easier. 
