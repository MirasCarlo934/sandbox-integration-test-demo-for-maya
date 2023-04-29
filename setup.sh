# set up db
mvn liquibase:update

# set up wiremock stubs
cd src/test/resources/stubs
curl -X POST localhost:8080/__admin/mappings -H "Content-Type: application/json" -d @walletservice-create-new-wallet-200.json
