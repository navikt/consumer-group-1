# LEESAH Quiz Starter-app

**System overview**
```bash
┌──────────────┐       ┌──────────────┐
│              │       │              │
│              │       │              │
│  Quizboard   │       │  Quizmaster  │
│              │       │              │
│              │       │              │
└──────────────┘       └──────────────┘
│  ▲                   │  ▲
│  │                   │  │
│  │                   │  │
▼  │                   ▼  │
┌──────────────────────────────────────────────────────────────┐
│                      Kafka                                   │
└──────────────────────────────────────────────────────────────┘
▲    │                           ▲    │
│    │                           │    │
│    ▼                           │    ▼
┌──────────────┐                 ┌──────────────┐
│              │                 │              │
│              │                 │              │
│    Team 1    │      .  .  .    │    Team n    │
│              │                 │              │
│              │                 │              │
└──────────────┘                 └──────────────┘
```

## Prerequisites
- Java 17
- IDEA 
  - Plugins: [Kotlin](https://plugins.jetbrains.com/plugin/6954-kotlin), [Co-Author](https://plugins.jetbrains.com/plugin/10952-co-author)
- Gradle 7.x.x

For Java and Gradle [SDKman](https://sdkman.io/) is recommended for installing.

## Setup 📝

1. Click the [Use this template] button located on: https://github.com/navikt/leesah-game-starter-kotlin
2. Create a new public repository from the template with your team name with the navikt organisation as owner.
3. Clone your repository to your local machine
   - `git clone https://github.com/navikt/<your repository name>.git`


## Developing your quiz participant 🤖

Your challenge is to implement a QuizParticipant that answers all the question messages that are
published by the quizmaster 🧙. You are free to develop your application as you want but this starter project comes with
som useful boilerplate so you can focus on the fun part, answering questions! 🎉

The code you need to extend is all located in `src/main/kotlin/no/nav/QuizApplication.kt`.

From the command-line in the project root run:

**To build the app locally**
```bash
 ./gradlew build
```

**To run the app locally**
```bash
java -jar build/libs/app.jar
```

## First task
Deploy the application to NAIS!

Good luck! Remember to ask questions! ❤️