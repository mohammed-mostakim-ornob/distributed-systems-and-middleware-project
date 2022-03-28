# Beverage Store
A simple web application where managers can create new beverages (bottle & crate), edit them and customers can add the beverages into the cart for checking out eventually to place the order. 

## Features
  - Manager
    - Login with username and password
    - Add new beverages to the store
    - Edit existing beverages
    - View all the customers
    - View all the orders
    - View all the customer addresses
    - Regenerate the invoice for existing orders and auto-send via email
  - Customer
    - Register as new customer
    - Login with username and password
    - Add addresses
    - Add beverages to the cart
    - Checkout cart to create order and receive the invoice via email
    - Regenerate the invoice for existing orders and receive via email

## Architecture
Application is developed with microservices architecture. The Springboot application enables the customers to submit the order. The invoicing feature is implemented with two microservices. One of them (Google Cloud HTTP Function) generates the invoice PDF and stores that into Google Cloud Storage. The other one (Google Cloud Pub/Sub Trigger) sends that invoice PDF to the customer's email.

Orders are also stored in Google Cloud Firestore for further analysis.

## Springboot Application Deployment
  - Local Deployment<br/><br/>
    The local deployment currently does not support any machine. It requires the machine's IP address to be registered in the Google Cloud PostgreSql manged instance. Then it can be deployed as regular Springboot application with the following parameter.
    ```
    spring.profiles.active=common, local
    ```
    After the deployment, the application can accessed on the following URL.
    ```
    http://localhost:8080
    ```
  - Google Cloud App Engine Deployment<br/><br/>
    This deployment requires some changes in the <strong>build.gradle</strong> file of <strong>spring_boot</strong> project. The deployment is possible with the following gradle task.
    ```
    spring_boot -> app engine app.yaml based projects -> appengineDeploy
    ```

## Accessing the Application
Currently, the application is deployed on Google Cloud App Engine. The latest version can be access with the following URL.
```
http://dsam-group02-beverage-store.ey.r.appspot.com
```

## Demo Data
Currently, the application is pre-configured with some demo data including demo users of both Manager and Customer roles.

  - Manager
    ```
    Username: manager
    Password: manager
    ```
  - Customer 1
    ```
    Username: customer1
    Password: customer1
    ```
  - Customer 2
    ```
    Username: customer2
    Password: customer2
    ```

## Automated Testing
Automated testing features the Unit Tests and the Integration Tests. These tests are configured with in-memory (H2) database. 
  - Unit Testing - Unit Tests cover the business logic.<br/><br/>
  - Integration Testing - Integration Tests cover the following aspects of the application.
    - Security of the controller actions
    - Integration between services and controller actions
    - Integration between views and view controller actions
    - Integration between a client and REST controller actions

## Code Coverage
Code coverage reports can be generated with the following Gradle Task.

```
spring_boot -> verification -> jacocoTestReport
```

The generated reports can be found in the following directory.
```
/spring_boot/build/jacocoHtml
```

The tests are configured to cover minimum 50% of the codebase. This rule can be verified with the following Gradle Task.
```
spring_boot -> verification -> jacocoTestCoverageVerification
```

Currently, the existing tests cover around 98% of overall codebase excluding POJO, Bean, Demo Data, Exception and Property classes.

## Testing the Invoicing Feature
We have developed an unsecured API endpoint to test this feature with mock invoice.

```
Endpoint - http://dsam-group02-beverage-store.ey.r.appspot.com/api/invoice
Method - POST
Content-type - application/json
Example Request Body -
    
{
    "orderNumber": "ORD001",
    "orderDate": "2021-02-26",
    "customerName": "Customer Name",
    "customerEmailId": "customer@email.com",
    "deliveryAddress": {
        "street": "Pestalozzistraße",
        "houseNumber": "9F",
        "postalCode": "96052"
    },
    "billingAddress": {
        "street": "Pestalozzistraße",
        "houseNumber": "9F",
        "postalCode": "96052"
    },
    "items": [
        {
            "position": 1,
            "name": "Coca-cola",
            "type": "Bottle",
            "quantity": 1,
            "price": 1.00
        },
        {
            "position": 2,
            "name": "Sprite Crate",
            "type": "Crate",
            "quantity": 1,
            "price": 10.00
        }
    ]
}
```

## Design Principals
1. As real world prices are usually float, all the prices are designed as float.
2. In order details page, order items are not retrieved with the order using entity graph, because a second level relationship (order item bottle and crate) retrieving is required.
3. Self-registration feature is developed only for customers as it is not practical for a business to let any user register as manager.
