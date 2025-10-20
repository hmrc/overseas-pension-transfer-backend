
# Overseas Pension Transfer Backend

This is the backend microservice application that is part of the Managing Pensions Schemes service concerned with moving pensions abroad. It is connected to the MPS dashboard.


## Running the service

1. Make sure you run all the dependant services through the service manager:

   > `sm2 --start OVERSEAS_PENSION_TRANSFER_ALL`

2. Stop the backend microservice from the service manager and run it locally with **test-only routes**:

   > `sm2 --stop OVERSEAS_PENSION_TRANSFER_BACKEND`

   > `sbt run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes`

The service runs on port `15600` by default.

---

## Seeding the Save-For-Later Database

These **test-only** endpoints allow developers to insert, generate, or clear data in the Save-For-Later MongoDB collection when running the service locally or in CI.

> ⚠️ These routes are **not** available in production and require starting the app with:  
> `sbt run -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes`

### 1. Seed a single in-progress/amend-in-progress record

**POST** `/test-only/in-progress/seed`  
Creates one in-progress transfer entry in the Save-For-Later collection.

**POST** `/test-only/amend-in-progress/seed`
Creates one amend-in-progress transfer entry in the Save-For-Later collection.

**Request body example**
```json
{
  "pstr": "24000001IN",
  "transferReference": "T-1",
  "lastUpdated": "2025-10-01T12:34:56Z",
  "nino": "AA000001A",
  "firstName": "Jane",
  "lastName": "Doe"
}
```

**Responses**
- `201 Created` – Record successfully inserted
- `400 Bad Request` – Invalid or missing fields

---

### 2. Bulk-seed multiple in-progress/amend-in-progress records

**POST** `/test-only/in-progress/bulk`  
Accepts an array of `SeedInProgress` objects to insert several records at once.

**POST** `/test-only/amend-in-progress/bulk`  
Accepts an array of `SeedAmendInProgress` objects to insert several records at once.

**Request body example**
```json
[
  {
    "pstr": "24000001IN",
    "transferReference": "T-1",
    "lastUpdated": "2025-09-10T10:15:30Z",
    "nino": "AA000001A",
    "firstName": "Alice",
    "lastName": "Brown"
  },
  {
    "pstr": "24000001IN",
    "transferReference": "T-2",
    "lastUpdated": "2025-09-20T14:22:05Z",
    "nino": "AA000002A",
    "firstName": "Bob",
    "lastName": "Jones"
  },
  {
    "pstr": "24000001IN",
    "transferReference": "T-3",
    "lastUpdated": "2025-09-28T08:45:00Z",
    "nino": "AA000003A",
    "firstName": "Carol",
    "lastName": "Smith"
  }
]
```

**Responses**
- `201 Created` – All records inserted successfully
- `400 Bad Request` – One or more records failed validation

---

### 3. Generate random test data

**POST** `/test-only/in-progress/generate/:pstr/:n`  
Automatically generates `n` fake records for a given `pstr`, using random names and timestamps within the last 31 days.

**POST** `/test-only/amend-in-progress/generate/:pstr/:n`  
Automatically generates `n` fake records for a given `pstr`, using random names and timestamps within the last 31 days.

**Response**
- `201 Created` – `n` random records successfully created

---

### 4. Clear all records for a PSTR

**DELETE** `/test-only/in-progress/clear/:pstr`  
Removes all in-progress Save-For-Later entries matching the specified PSTR.

**DELETE** `/test-only/amend-in-progress/clear/:pstr`  
Removes all amend-in-progress Save-For-Later entries matching the specified PSTR.

**Response**
- `204 No Content` – Records deleted successfully

---

### Notes for test-only endpoints

- These endpoints directly manipulate MongoDB and should **only be used in test or development environments**.
- All timestamps must be valid ISO-8601 (`yyyy-MM-dd'T'HH:mm:ss'Z'`) strings.
- To view seeded data, connect to your local Mongo instance and inspect the Save-For-Later collection.

---

## Running tests

### Unit tests

> `sbt test`

### Integration tests

> `sbt it/test`

---

## Scalafmt and Scalastyle

To check if all the scala files in the project are formatted correctly:
> `sbt scalafmtCheckAll`

To format all the scala files in the project correctly:
> `sbt scalafmtAll`

To check if there are any scalastyle errors, warnings or infos:
> `sbt scalastyle`
>
 
---

## All tests and checks

This is an sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report:
> `sbt runAllChecks`

---

## Decrypt the MongoDB Values

The `decrypt.sh` script is used to **decrypt the `data` field** inside MongoDB documents that were stored in an encrypted format.

### Steps to use

1. **Make the script executable (first time only):**
   > `chmod +x decrypt.sh`

2. **Run the script with your encrypted JSON document as input.**  
   
3. **Pass the full MongoDB document containing the `_id`, `referenceId`, and `data` fields.**

   Example with only the encrypted string:
   > `sh decrypt.sh 'ENCRYPTED_JSON_DATA'`

   Example with a full MongoDB document:
   > `sh decrypt.sh '{ "_id": { "$oid": "68d29dff44e574ac97b990cb" }, "referenceId": "Int-b963d6ce-3951-43e7-8f77-b0a39cd18162", "data": "56fKQrZrynult7fNkrbxDP7waSHqbaVOKf9cbDzrVfvTd1ZGE9sOKE86EZ1npmzo2ef3xZ8y71/Q3boTF7YBN11u+LAWUh+p+d/tFddYjQgf+2xq5pB/AHp0MgyxENIoNHZFo1mdzugaEes95LanmEbtDfpPRMbdu9dqtClLGzgL8NvRn8W21ZLkd5OBums=", "lastUpdated": { "$date": "2025-09-23T13:17:47.458Z" } }'`

3. **Check the terminal output.**  
   The script will return the same JSON, but with the `data` field decrypted.

### Example Output

```json
{
  "_id" : {
    "$oid" : "68d29dff44e574ac97b990cb"
  },
  "referenceId" : "Int-b963d6ce-3951-43e7-8f77-b0a39cd18162",
  "data" : {
    "transferDetails" : {
      "typeOfAssets" : {
        "moreAsset" : "No",
        "otherAssets" : [ {
          "assetValue" : 123,
          "assetDescription" : "123"
        } ]
      }
    }
  },
  "lastUpdated" : {
    "$date" : "2025-09-23T13:17:47.458Z"
  }
}
```

### Notes
- Make sure to copy the JSON exactly (with quotes escaped) when passing it into the command.
- If the input format is wrong, the script will fail to parse the JSON.

---

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
