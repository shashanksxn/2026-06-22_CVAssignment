**api.csv:** the api paths
'–' means not required, '+' and '++' means can be added in the future, 'NOT ALLOWED' means it is intentionally not allowed

**schema.csv:** schema for the DynamoDB

**pseudocode.txt:** pseudocode used to build the swagger.yaml file, and APIs

**swagger.yaml:** the swagger file for APIs

**postman_collection.json:** the Postman collection, can be imported in Postman and tested

###### To Run
The code is organized in two folders:
- therapy-app: it contains the code for building aws infrastructure
- therapy-lambda: this contains the implementation of the API

First compile the therapy-lambda using
```
therapy-lambda $ mvn package
```

Then compile and deploy the therapy-app
```
therapy-app $ mvn package
therapy-app $ cdk deploy
```
