# AUTH RESTFUL API

## User API Spec

### Register User

Endpoint : POST /api/users

Request Body :

```json
{
  "username" : "razzaq",
  "password" : "rahasia",
  "name" : "Abdul Razzaq",
  "email" : "razzaq@mail.com"
}
```

Response Body (Success) :

```json
{
  "data" : "OK",
  "errors": null
}
```

Response Body (Failed) :

```json
{
  "errors" : "Username must not blank, ???"
}
```

### Login User

Endpoint : POST /api/auth/login

Request Body :

```json
{
  "username" : "razzaq",
  "password" : "rahasia" 
}
```

Response Body (Success) :

```json
{
  "data" : {
    "accessToken": "0ea0832d-678a-43be-a27f-82b5319ef959",
    "refreshToken": "037f8697-2f21-40d2-a7e4-e1c08c185524",
    "expiredAt" : 123123123213 // milliseconds
  },
  "errors": null
}
```

Response Body (Failed, 401) :

```json
{
  "errors" : "Username or password wrong"
}
```

### Access Token User

Endpoint : POST /api/auth/token

Request Body :

```json
{
  "token" : "RefreshToken"
}
```

Response Body (Success) :

```json
{
  "data" : {
    "accessToken": "0ea0832d-678a-43be-a27f-82b5319ef959",
    "expiredAt" : 123123123213 // milliseconds
  },
  "errors": null
}
```

Response Body (Failed, 401) :

```json
{
  "errors" : "Unauthorize"
}
```

### Get User

Endpoint : GET /api/users/current

Request Header :

- X-API-TOKEN : Access Token (Mandatory)

Response Body (Success) :

```json
{
  "data" : {
    "username" : "razzaq",
    "name" : "Abdul Razzaq"
  },
  "errors": null
}
```

Response Body (Failed, 401) :

```json
{
  "errors" : "Unauthorized"
}
```

### Update User

Endpoint : PATCH /api/users/current

Request Header :

- X-API-TOKEN : Access Token (Mandatory)

Request Body :

```json
{
  "name" : "Abdul Razzaq", // put if only want to update name
  "password" : "new password" // put if only want to update password
}
```

Response Body (Success) :

```json
{
  "data" : {
    "username" : "razzaq",
    "name" : "Abdul Razzaq"
  },
  "errors": null
}
```

Response Body (Failed, 401) :

```json
{
  "errors" : "Unauthorized"
}
```

### Logout User

Endpoint : DELETE /api/auth/logout

Request Header :

- X-API-TOKEN : Access Token (Mandatory)

Response Body (Success) :

```json
{
  "data" : "OK",
  "errors": null
}
```

Response Body (Failed, 401) :

```json
{
  "errors" : "Unauthorized"
}
```