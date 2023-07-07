Description: Integration tests for MongoDbSteps class

Meta:
    @epic vividus-plugin-mongodb
    @requirementId 435

Scenario: Set up
Given I initialize story variable `collectionName` with value `#{generate(regexify '[a-z]{15}')}`
When I execute command `{ create: "${collectionName}", collation: { locale: "en" } }` against `${db-name}` database on `${instance-key}` MongoDB instance and save result to SCENARIO variable `create`
Then `${create.ok}` is equal to `1.0`
When I execute command `
{
    insert: "${collectionName}",
    documents: [
        {
            "age": 33,
            "name": "Joanna Pierce",
            "gender": "female",
            "company": "LINGOAGE",
            "email": "joannapierce@lingoage.com",
            "pets" : [
                {
                    "type" : "cat",
                    "nickname" : "Kitty"
                }
            ]
        },
        {
            "age": 40,
            "name": "Buck Frazier",
            "gender": "male",
            "company": "AUTOGRATE",
            "email": "buckfrazier@autograte.com",
            "pets" : [
                {
                    "type" : "cat",
                    "nickname" : "Fluff"
                },
                {
                    "type" : "dinosaur",
                    "nickname" : "Ulad"
                }
            ]
        },
        {
            "age": 20,
            "name": "Rowena Fitzpatrick",
            "gender": "female",
            "company": "IDETICA",
            "email": "rowenafitzpatrick@idetica.com"
        }
    ]
}
` against `${db-name}` database on `${instance-key}` MongoDB instance and save result to SCENARIO variable `insert`
Then `${insert.ok}` is equal to `1.0`
Given I initialize story variable `tableName` with value `#{generate(regexify '[a-z]{15}')}`
Given I initialize story variable `tableName` with value `#{generate(regexify '[a-z]{15}')}`
When I execute SQL query `
  CREATE TABLE ${tableName} (name VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, type VARCHAR (10) NOT NULL, nickname VARCHAR (10) NOT NULL);
  INSERT INTO ${tableName} (name, email, type, nickname) VALUES
   ('Joanna Pierce', 'joannapierce@lingoage.com', 'cat', 'Kitty'),
   ('Buck Frazier', 'buckfrazier@autograte.com', 'cat', 'Fluff');
` against `for-mongo`

Scenario: Find and collect
When I execute commands
| command    | argument                                  |
| find       | { age: { $gte: 25 }, "pets.type": "cat" } |
| projection | { name: 1, email: 1, "pets.$": 1 }        |
| collect    |                                           |
 in `${collectionName}` collection against `${db-name}` database on `${instance-key}` MongoDB instance and save result to SCENARIO variable `find`
Then JSON element from `${find}` by JSON path `$` is equal to `
[
   {
      "_id":"${json-unit.any-string}",
      "name":"Joanna Pierce",
      "email":"joannapierce@lingoage.com",
      "pets":[
         {
            "type":"cat",
            "nickname":"Kitty"
         }
      ]
   },
   {
      "_id":"${json-unit.any-string}",
      "name":"Buck Frazier",
      "email":"buckfrazier@autograte.com",
      "pets":[
         {
            "type":"cat",
            "nickname":"Fluff"
         }
      ]
   }
]`

Scenario: Find and count
When I execute commands
|command   |argument                                 |
|find      |{ age: { $gte: 25 }, "pets.type": "cat" }|
|projection|{ name: 1, email: 1, "pets.$": 1 }       |
|count     |                                         |
 in `${collectionName}` collection against `${db-name}` database on `${instance-key}` MongoDB instance and save result to SCENARIO variable `count`
Then `${count}` is equal to `2`

Scenario: Native find
When I execute command `
{
    find: "${collectionName}",
    filter: { age: { $gte: 1 } },
    sort: { age: 1 },
    projection: { age: 1, name: 1, _id: 0 }
}
` against `${db-name}` database on `${instance-key}` MongoDB instance and save result to SCENARIO variable `native-find`
Then `${native-find.ok}` is equal to `1.0`
Then JSON element from `${native-find.cursor}` by JSON path `$.firstBatch` is equal to `
[
   {
      "age": 20,
      "name": "Rowena Fitzpatrick"
   },
   {
      "age": 33,
      "name": "Joanna Pierce"
   },
   {
      "age": 40,
      "name": "Buck Frazier"
   }
]`

Scenario: Compare document from MongoDB with table from relational DB
When I execute SQL query `SELECT * FROM ${tableName}` against `for-mongo` and save result to SCENARIO variable `tableSource`
When I execute commands
|command   |argument                                    |
|find      |{ age: { $gte: 25 }, "pets.type": "cat" }   |
|projection|{ name: 1, email: 1, "pets.$": 1, "_id": 0 }|
|collect   |                                            |
 in `${collectionName}` collection against `${db-name}` database on `${instance-key}` MongoDB instance and save result to SCENARIO variable `documentSource`
Then `${tableSource}` is equal to table:
{transformer=FROM_JSON, variable=documentSource, columns=name=$.[*].name;email=$.[*].email;type=$.[*].pets.[0].type;nickname=$.[*].pets.[0].nickname}

Scenario: Tear down
When I execute command `{ drop: "${collectionName}" }` against `${db-name}` database on `${instance-key}` MongoDB instance and save result to SCENARIO variable `drop`
Then `${drop.ok}` is equal to `1.0`
When I execute SQL query `SHUTDOWN IMMEDIATELY` against `for-mongo`
