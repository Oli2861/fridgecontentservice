# FridgeContentService
Microservice to store fridge contents (groceries) to be used within @Coding-Udum 's project Smart2Fridge (https://github.com/Coding-Udum/Smart2Fridge).

##### Endpoint descriptions
| Method | Endpoint                   | Description                                                               | Parameters                                                                                                          | Response                                                                                   |
|--------|----------------------------|---------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| GET    | /v1/item/bulk/{snapshotId} | Endpoint to retrieve a snapshot including all corresponding items.        | The id of the snapshot.                                                                                             | The date when the snapshot was saved and the ID of the snapshot and all items.             |
| DELETE | /v1/item/bulk/{snapshotId} | Endpoint to delete a snapshot and all corresponding items.                | The id of the snapshot.                                                                                             | The date when the now deleted snapshot was saved and the ID of the snapshot and all items. |
| POST   | /v1/item/bulk              | Endpoint for saving multiple elements, creates a snapshot in the process. | The name of the items to be saved: ```[{"description" : "Banana"},{"description" : "Apple"}]``` | URI to retrieve the saved items, including the id of the created snapshot.                 |
|        |                            |                                                                           |                                                                                                                     |                                                                                            |

##### Build & Run
```
cd fridgecontentservice && ./gradlew build && docker build . -t fridgecontentservice && cd ../ && docker-compose up
```