# Advanced Programming Practices: TubeLytics

This project is a Java-based application designed to enhance the way users search, explore, and understand content from YouTube. It leverages the YouTube Data API to fetch video information, offers real-time updates, and provides unique insights into video readability and channel content.

## Features
1. **Intelligent Video Search**: Search YouTube for videos based on specific queries and display comprehensive information for each relevant post.
2. **Real-time Data Updates**: Utilizes socket programming to provide live updates, instantly reflecting new video uploads related to active searches without requiring a manual refresh.
3. **Content Readability Scoring**: Calculates readability scores for video descriptions, offering quick insights into the complexity and accessibility of the content.
4. **Channel Navigation & Recent Videos**: Seamlessly navigate from individual videos to their respective channels to view the most recent uploads.
5. **Sentiment Analysis (Advanced)**: Analyzes the sentiment of video descriptions, providing an additional layer of insight into content tone.



## Installation
To run this project locally:

1. Clone this repository:
    ```bash
    git clone https://github.com/haniehst/tubelytics.git
    ```

2. Run the project using `sbt`:
    ```bash
    sbt run
    ```
3. Run tests:
    ```bash
    sbt clean test
    ```
4. Test coverage report:
    ```bash
    sbt jacoco
    ```
## Usage
After starting the project, you can access the application via `localhost:9000` in your browser.

## Technologies Used

- **Programming Language**: Java (JDK 17)
- **Frameworks**: Play Framework
- **Libraries**:
    - `sbt` (for build management)
    - `Mockito` (for testing and mocking)
    - `Stanford CoreNLP` (for sentiment analysis)
    - `Apache Commons Text` (for text processing utilities)
    - `JaCoCo` (for code coverage reporting)


**Note**: The project is developed using [JDK 17](https://jdk.java.net/17/). Please ensure JDK 17 is installed for compatibility.

## Team 400 Bad Request (Divided)

- **Hanieh Salmantaheri**
- **Adriana Lilia Guevara Contreras**
