Simple book shelf demo created in Spring Boot and MySql

To run application 

Prerequisites
 - docker with docker compose
 - java 11
 
Steps:
 - clone repository
 - in src/main/resources execute `docker-compose up`
 - in main directory execute `mvn clean install`
 - execute `mvn spring-boot:run`

Available endpoints (under localhost:8080/books)
    - (GET) all books fetching
    - (POST) new book creation
    - (GET) /{id} fetching book by id
    - (PUT) /{id} updating book by id
    - (DELETE) /{id} deleting book by id

Operations visible in BookController.class

Instead of running via curl You can use Restlet Client (Chrome extension)
https://chrome.google.com/webstore/detail/restlet-client-rest-api-t/aejoelaoggembcahagimdiliamlcdmfm
Just install it in your browser and import demo-bookshelf.json schema available in main directory