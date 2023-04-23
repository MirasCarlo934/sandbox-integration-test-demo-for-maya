cd src/test/resources/stubs
curl -X POST localhost:8080/__admin/mappings -H "Content-Type: application/json" -d @walletservice-create-new-wallet-200.json
