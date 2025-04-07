# Expense Sharing Backend
## Overview

This is a backend application built using Spring Boot to manage expenses between users. 
<br/> 
The system allows users <br/> 
        1.  to add expenses <br/> 
        2.  split the expense amount equally / unequally/ amount or percentage <br/> 
        3.  view their expenditures <br/> 
        4.  Get their final balances. <br/> 
Extra Feature:
        5.  create groups, add members, and split expenses among group members.



## Features

- **User Management**: Add and manage users.
- **Expense Management**: Create and manage shared expenses.
- **Balance Tracking**: Track balances between users.
- **Payment Settlement**: Settle payments between users.
- **Database Integration**: Uses PostgreSQL for data storage.

---

## Technologies Used In project

- **Java**: Programming language.
- **Spring Boot**: Backend framework.
- **PostgreSQL**: Relational database.
- **Gradle**: Build tool.

## For testing these can be used

- **JUnit 5**: Unit testing framework.
- **Mockito**: Mocking framework for testing.


---

## Prerequisites

Before running the project, ensure you have the following installed:

- **Java 17** or later
- **Gradle** (if not using the wrapper)
- **PostgreSQL** (version 12 or later)

---



## Setup Instructions

### 1. Clone the Repository

```
git clone https://github.com/your-username/splitwise-backend.git
cd splitwise-backend
```

2. Configure the Database
Create a PostgreSQL database named splitwise_db.
Update the database connection details in src/main/resources/application.properties:
```
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Make sure docker is running 

```
open -a Docker
```

3. Build the Project using docker 
```
 docker build -t splitwise-backend .
```

4. Run the Application
```
 docker run -p 8080:8080 splitwise-backend          
```

The application will start on http://localhost:8080

API Endpoints
1. User Management
   POST /users
```
Create User: POST /users
{
    "name": "John Doe",
    "email": "john.doe@example.com"
}
```

2. Expense Management

Create Expense: POST /expenses
```
{
    "description": "Dinner",
    "amount": 1000,
    "paidById": 1,
    "splitType": "EQUAL",
    "userShares": {
        "1": 500,
        "2": 500
    }
}
```

Get All Expenses: GET /expenses<br/>
Get All Expenses: GET /expenses/{expenseId}<br/>
Get Expenses by User ID: GET /expenses/user/{userId}<br/>

3. Balance Tracking
Get All Balances: GET /balances/all
Response:
```

{
    "1": "User 1 is with a profit of ₹6200.00",
    "2": "User 2 owes ₹7840.00",
    "3": "User 3 owes nothing",
    "4": "User 4 owes nothing",
    "5": "User 5 is with a profit of ₹400.00",
    "6": "User 6 owes ₹400.00",
    "8": "User 8 owes nothing",

}
```

GET /getBalance/{userId}
Response:
```
{
    "message": "User 8 owes nothing"
}
```
4. Payment Settlement
Settle Payment: POST /settle
```
{
    "senderId": 2,
    "receiverId": 1,
    "amount": 500
}
```

## Database Schema

The application uses the following database schema to manage users, expenses, balances, and settlements:

## Tables

1. **users**
   - `id`: Primary key
   - `name`: User's name
   - `email`: User's email

2. **expenses**
   - `id`: Primary key
   - `description`: Expense description
   - `amount`: Total amount
   - `paid_by`: User ID of the payer
   - `split_type`: Type of split (e.g., EQUAL)

3. **user_balance**
   - `id`: Primary key
   - `user_id`: User who owes money
   - `other_user_id`: User who is owed money
   - `balance_amount`: Amount owed

4. **settlements**
   - `id`: Primary key
   - `sender_id`: User who sent the payment
   - `receiver_id`: User who received the payment
   - `amount`: Amount settled


## Future Enhancements

- Write tests
- Set up Docker
- use refresh tokens -- authorization


## Future Enhancements

- Implement detailed transaction history for users.
- Create Groups

## Walkthrough apis

## Demo

Walk through api's in action:

[![Splitwise Backend Demo](https://img.youtube.com/vi/W52SS_iIlhE/0.jpg)](https://youtu.be/W52SS_iIlhE?si=0EcHpU1tlRLU2DOp)

Click [here](https://youtu.be/W52SS_iIlhE?si=0EcHpU1tlRLU2DOp) to watch the video on YouTube.
