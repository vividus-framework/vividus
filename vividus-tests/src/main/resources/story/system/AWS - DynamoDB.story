Meta:
    @epic vividus-plugin-aws-dynamodb
    @requirementId 1175

Lifecycle:
Before:
Scope: STORY
When I execute query `
    INSERT INTO Music
    value {'Artist':'Roxette','SongTitle':'The Look'}
` against DynamoDB
When I execute query `
    INSERT INTO Music
    value {'Artist':'Roxette','SongTitle':'Real Sugar'}
` against DynamoDB
After:
Scope: STORY
When I execute query `
    DELETE FROM Music
    WHERE Artist='Roxette' and SongTitle='The Look'
` against DynamoDB
When I execute query `
    DELETE FROM Music
    WHERE Artist='Roxette' and SongTitle='Real Sugar'
` against DynamoDB

Scenario: SELECT statement
When I execute query `
    SELECT * FROM Music
    WHERE Artist='Roxette' and SongTitle='The Look'
` against DynamoDB and save result as JSON to scenario variable `song`
Then JSON element from `${song}` by JSON path `$` is equal to `
{
    "Artist": "Roxette",
    "SongTitle": "The Look"
}`

Scenario: UPDATE statement
When I execute query `
    UPDATE Music
    SET AwardsWon=1
    SET AwardDetail={'Grammis':[1989, 1990]}
    WHERE Artist='Roxette' and SongTitle='The Look'
` against DynamoDB
When I execute query `
    UPDATE Music
    SET AwardDetail.Grammis =list_append(AwardDetail.Grammis,[1991])
    WHERE Artist='Roxette' and SongTitle='The Look'
` against DynamoDB
When I execute query `
    UPDATE Music
    REMOVE AwardDetail.Grammis[2]
    WHERE Artist='Roxette' and SongTitle='The Look'
` against DynamoDB
When I execute query `
    UPDATE Music
    SET BandMembers =<<'Marie Fredriksson', 'Per Gessle'>>
    WHERE Artist='Roxette' and SongTitle='The Look'
` against DynamoDB
When I execute query `
    UPDATE Music
    SET BandMembers =set_add(BandMembers, <<'Christoffer Lundquist'>>)
    WHERE Artist='Roxette' and SongTitle='The Look'
` against DynamoDB
When I execute query `SELECT * FROM Music` against DynamoDB and save result as JSON to story variable `song`
Then JSON element from `${song}` by JSON path `$` is equal to `
[
    {
        "Artist": "Roxette",
        "AwardsWon": 1,
        "SongTitle": "The Look",
        "AwardDetail": {
            "Grammis": [1989, 1990]
        },
        "BandMembers": ["Christoffer Lundquist", "Marie Fredriksson", "Per Gessle"]
    },
    {
        "Artist": "Roxette",
        "SongTitle": "Real Sugar"
    }
]` ignoring array order

Scenario: Wait for the data
Meta:
    @requirementId 1235
When I save JSON element from `${song}` by JSON path `$[?(@.SongTitle == "The Look")].BandMembers` to scenario variable `bandMembers`
When I save JSON element from `${song}` by JSON path `$[0].length()` to scenario variable `numberOfBandMembers`
When I execute steps with delay `PT1S` at most 1 times while variable `numberOfBandMembers` is greater than `2`:
|step                                                                                                                                 |
|When I execute query `                                                                                                               |
     UPDATE Music                                                                                                                     |
     SET BandMembers =set_delete(BandMembers, <<'Christoffer Lundquist'>>)                                                            |
     WHERE Artist='Roxette' and SongTitle='The Look'                                                                                  |
| ` against DynamoDB                                                                                                                  |
|When I execute query `SELECT * FROM Music` against DynamoDB and save result as JSON to scenario variable `song`                      |
|When I save JSON element from `${song}` by JSON path `$[?(@.SongTitle == "The Look")].BandMembers` to scenario variable `bandMembers`|
|When I save JSON element from `${song}` by JSON path `$[0].length()` to scenario variable `numberOfBandMembers`                      |
Then `${numberOfBandMembers}` is equal to `2`
