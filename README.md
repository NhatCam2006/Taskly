# Taskly - AI-Integrated Task Management System

[![Java Version](https://img.shields.io/badge/Java-21%2B-oracle.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17%2B-orange.svg)](https://openjfx.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15%2B-blue.svg)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.x-red.svg)](https://maven.apache.org/)

Taskly is a modern, desktop-based Task Management application built using **Java** and **JavaFX**. It features secure user registration and login (with email verification), priority-based task categorization, progress tracking, visual performance charts, and a built-in **AI Chatbot (Gemini 2.0 Flash)** that can automatically parse chat conversations and dynamically register tasks into the database.

---

## 🌟 Key Features

1. **User Authentication & Session Management:**
   * Secure user registration with email OTP verification sent via Gmail ([EmailService.java](src/main/java/service/EmailService.java)).
   * Logged-in user session persistence and authorization control ([SessionManager.java](src/main/java/utils/SessionManager.java)).
2. **Interactive Task Management:**
   * Intuitive dashboard to create, edit, delete, and list tasks.
   * Task categorization by priority level (Priority 1 - 3).
   * Real-time progress tracking (%) and completion audio notification.
3. **AI Chatbot Assistant (Gemini 2.0 Flash):**
   * Instant chatting to ask questions and discuss task details.
   * **NLP Task Parsing:** When you request the chatbot to add a task (e.g., *"Add task: Study Java for 2 hours with high priority"*), the AI assistant will automatically parse and save the task directly into your database.
4. **Performance Statistics:**
   * Graphical charts visualizing tasks categorized by priority level for efficiency tracking.

---

## 🛠️ Tech Stack

* **Programming Language:** Java 21+ / JDK 23
* **Grahpical User Interface:** JavaFX 17 (FXML-based views)
* **Database Management:** PostgreSQL (Driver `org.postgresql:postgresql:42.7.3`)
* **Build tool:** Maven
* **HTTP Client:** OkHttp 4 (for Gemini API interactions)
* **Email Client:** Jakarta Mail 2.0 (for OTP email dispatch)

---

## 📥 Prerequisites & Setup

### 1. Requirements
* **JDK 21** or higher installed on your local machine.
* **PostgreSQL** database server running locally or remotely.

### 2. Database Schema Initialization
Open your database management tool (e.g., pgAdmin, DBeaver, or psql console) and execute the following SQL script to set up the database and tables:

```sql
-- 1. Create Database
CREATE DATABASE TaskManagerDB;

-- Make sure to connect to 'TaskManagerDB' database before executing the table creations below

-- 2. Create 'users' table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    verified INT DEFAULT 1
);

-- 3. Create 'tasks' table
CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    content VARCHAR(255) NOT NULL,
    description TEXT,
    category_level INT,
    time_to_complete INT,
    start_time TIMESTAMP,
    status VARCHAR(50),
    progress INT,
    remaining_time INT,
    user_id INT REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Create 'conversations' table (for Chatbot conversations)
CREATE TABLE conversations (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL
);

-- 5. Create 'chat_history' table (for saving chat logs)
CREATE TABLE chat_history (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    conversation_id INT REFERENCES conversations(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    sender VARCHAR(50) NOT NULL, -- 'user' or 'bot'
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## ⚙️ Configuration Guide

Before running the application, update the configuration credentials in the source code files:

### 1. Database Configuration
Open [DatabaseConnection.java](src/main/java/database/DatabaseConnection.java) and set your PostgreSQL credentials:
```java
private static final String URL = "jdbc:postgresql://localhost:5432/TaskManagerDB";
private static final String USER = "postgres";      // Your PostgreSQL username
private static final String PASSWORD = "123";       // Your PostgreSQL password
```

### 2. Email SMTP Configuration
Open [EmailService.java](src/main/java/service/EmailService.java) and input your Gmail account details and App Password:
```java
private static final String EMAIL = "nhatcam2006@gmail.com";
private static final String PASSWORD = "your-gmail-app-password"; // Your Gmail App Password
```

### 3. Gemini API Key Configuration
Open [ChatbotController.java](src/main/java/controller/ChatbotController.java) and specify your Gemini API key (retrieve it from Google AI Studio):
```java
private static final String API_KEY = "your-gemini-api-key"; // Your Gemini API Key
```

---

## 🏃 How to Run the Application

### Method 1: Running from Command Line (Terminal)
Navigate to the project root directory and execute the following commands:
```powershell
# Navigate to the project folder
cd d:\_Study\Home\Java\Project

# Clean project and launch JavaFX application
.\mvnw.cmd clean javafx:run
```

### Method 2: Running in IntelliJ IDEA
1. Open IntelliJ IDEA -> Select **Open** -> Navigate to the project folder and select the [pom.xml](pom.xml) file to import it as a Maven project.
2. Verify JDK settings in **File** -> **Project Structure** -> **Project** -> Choose **JDK 23** or **JDK 21**.
3. If dependencies are unresolved, right-click [pom.xml](pom.xml) -> **Maven** -> **Reload Project**.
4. Open [Main.java](src/main/java/application/Main.java) -> right-click inside the file -> select **Run 'Main.main()'** to launch the login window.
