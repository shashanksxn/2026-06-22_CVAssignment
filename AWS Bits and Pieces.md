
Java Utilities
```
import java.util.Arrays;
package com.myorg; import software.amazon.awscdk.RemovalPolicy; import software.amazon.awscdk.Stack; import software.amazon.awscdk.StackProps; import software.amazon.awscdk.services.dynamodb.*; import software.constructs.Construct; import java.util.Collections;
```
###### Creating a Dynamo DB Table

Dependencies
```
<dependency>
	<groupId>software.amazon.awscdk</groupId>
	<artifactId>aws-cdk-lib</artifactId>
	<version>2.170.0</version>
</dependency>
```


TableV2 ==myTable== = TableV2.Builder.create(this, =="MyUserTable"==) 
	.tableName(=="users-table"==) 
	.partitionKey(Attribute.builder() 
		.name(=="userId"==)
		.type(AttributeType.==STRING==) 
		.build()) 
	.sortKey(Attribute.builder() 
		.name(=="createdAt"==) 
		.type(AttributeType.==STRING==) 
		.build())

To add a GSI
==myTable==.addGlobalSecondaryIndex(GlobalSecondaryIndexPropsV2.builder()
	.indexName(=="email-index"==) 
	.partitionKey(Attribute.builder() 
		.name(=="email"==) 
		.type(AttributeType.==STRING==) 
		.build()) 
	.projectionType(ProjectionType.==ALL==) 
	//.nonKeyAttributes(Arrays.asList(=="email", "status", "role"==))
	.build());

- Defining a sort key is optional
- What is projection type: 
	- ALL
	- KEYS_ONLY 
	- INCLUDE
- A PK+SK must be unique for every item. But GSI keys do not have to be unique, It will return all the items.
gem
#### To query the table

*Defining the Java class model*
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class ==User== {
    private String ==userid==;
    private String ==createdAt==;
    private String ==email==;
    //
    @DynamoDbPartitionKey
    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }
    //
    @DynamoDbSecondaryPartitionKey(indexNames = "createdAt-index")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    //
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

*Initialize the Client*
DynamoDbClient ddbClient = DynamoDbClient.create();
DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder() 
	.dynamoDbClient(ddbClient) 
	.build();

*What is difference between a normal client and enhanced client?*

*Map physical table to generated name*
DynamoDbTable\<User\> userTable = enhancedClient.table("users-table", TableSchema.fromBean(User.class));

###### Insert an Item
User newUser = new User();
newUser.setUserid("user_123");
newUser.setCreatedAt("2026-06-24T12:00:00Z");
newUser.setEmail("test@example.com");
userTable.**putItem**(newUser); // Inserts or overwrites entirely
###### Update an Item
User newUser = new User();
newUser.setUserid("user_123");
newUser.setCreatedAt("2026-06-24T12:00:00Z");
newUser.setEmail("test@example.com");
userTable.**putItem**(newUser);
###### Delete an Item
Key key = Key.builder().partitionValue("user_123").build();
userTable.**deleteItem**(key);
###### Query by Primary Key
Key key = Key.builder().partitionValue("user_123").build();
User user = userTable.**getItem**(key);
System.out.println("Found user: " + user.getEmail());
###### Query by GSI
import software.amazon.awssdk.enhanced.dynamodb.model.*;

// 1. Reference the GSI by its exact index name
DynamoDbIndex\<User\> createdAtIndex = userTable.index("createdAt-index");

// 2. Create the query conditional matching your GSI key value
QueryConditional queryConditional = QueryConditional
        .keyEqualTo(Key.builder().partitionValue("2026-06-24T12:00:00Z").build());

// 3. Execute query and iterate over pages of results
SdkIterable<Page\<User\>> results = createdAtIndex.query(QueryEnhancedRequest.builder()
        .queryConditional(queryConditional)
        .build());

results.forEach(page -> {
    page.items().forEach(userItem -> {
        System.out.println("User found via GSI: " + userItem.getUserid());
    });
});