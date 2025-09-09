# Weather API – Steg 1
## Kör
./mvnw spring-boot:run
## Testa
curl -i -H "Content-Type: application/json" -d '{"name":"Stockholm","lat":59.3293,"lon":18.0686}' http://localhost:8080/places
curl -i http://localhost:8080/places
