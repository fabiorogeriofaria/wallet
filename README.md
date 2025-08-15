# wallet
Wallet Service Assignment

### Installation

Download and install the following tools:

Java SDK: https://www.oracle.com/br/java/technologies/downloads/#jdk24-windows

Maven: https://maven.apache.org/download.cgi

Postman: https://www.postman.com/downloads/

Set up the environment variables (JAVA_HOME for Java home path and M2_HOME for Maven home path) in your environment, according to the requirements of the operating system you are using

### Execution

To build and execute the microservice, run the following command line:

```bash
mvn package
mvn spring-boot:run
```

### Testing the microservice

After starting it, the service must be available at `http://localhost:8080`

Use **Postman** to test the endpoints after locally starting the microservice.

The following endpoints will be available:

> `POST /api/wallets (to create a new wallet)`

body sample:

```jason
{
  "ownerName": "Usuario 1"
}
```

> `GET /api/wallets (to retrieve all created wallets)`

> `GET /api/wallets/{walletId}/balance (to retrieve the current balance of a specific wallet)`

> `GET /api/wallets/{walletId}/balance/history?date={yyyy-mm-dd} (to retrieve the history balance of a specific wallet)`

> `POST /api/wallets/{walletId}/deposit?amount={value} (to deposit values into a specific wallet)`

> `POST /api/wallets/{walletId}/withdraw?amount={value} (to withdraw values from a specific wallet)`

> `POST /api/wallets/transfer (to transfer values between wallets) `

body sample:

```jason
{
  "fromWalletId": "fccbf82a-e8b5-4116-b182-3ff275a700cf",
  "toWalletId": "258ab821-9c34-4db8-a98e-6b25edfd075c",
  "amount": 20.00
}
```




