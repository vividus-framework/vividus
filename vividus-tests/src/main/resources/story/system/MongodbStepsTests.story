Description: Integration tests for MongoDbSteps class

Meta:
  @group vividus-plugin-mongodb

Scenario: Set up
When I initialize the STORY variable `collectionName` with value `#{generate(regexify '[a-z]{15}')}`
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
                    "name" : "Kitty"
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
                    "value" : "Fluff"
                },
                {
                    "type" : "dinosaur",
                    "value" : "Ulad"
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

Scenario: Find and collect
When I execute commands
|command   |argument                                    |
|find      |{ age: { $gte: 25 }, "pets.type": "cat" }   |
|projection|{ name: 1, email: 1, "pets.$": 1, "_id": 0 }|
|collect   |                                            |
 in `${collectionName}` collection against `${db-name}` database on `${instance-key}` MongoDB instance and save result to SCENARIO variable `find`
Then a JSON element from '${find}' by the JSON path '$' is equal to '
[
   {
      "name":"Joanna Pierce",
      "email":"joannapierce@lingoage.com",
      "pets":[
         {
            "type":"cat",
            "name":"Kitty"
         }
      ]
   },
   {
      "name":"Buck Frazier",
      "email":"buckfrazier@autograte.com",
      "pets":[
         {
            "type":"cat",
            "value":"Fluff"
         }
      ]
   }
]'

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
Then a JSON element from '${native-find.cursor}' by the JSON path '$.firstBatch' is equal to '
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
]'

Scenario: Tear down
When I execute command `{ drop: "${collectionName}" }` against `${db-name}` database on `${instance-key}` MongoDB instance and save result to SCENARIO variable `drop`
Then `${drop.ok}` is equal to `1.0`
