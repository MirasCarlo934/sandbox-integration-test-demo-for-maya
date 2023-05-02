# set up db
mvn liquibase:update

# set up AWS
cd src/test/resources
sh aws.sh
cd ../../../

# set up wiremock stubs
cd src/test/resources/stubs
curl -X DELETE http://localhost:7000/__admin/mappings
curl -X POST localhost:7000/__admin/mappings -H "Content-Type: application/json" -d @walletservice-create-new-wallet-200.json
cd ../../../../
