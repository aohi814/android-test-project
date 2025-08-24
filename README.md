# Drivemode Multiplatform Mobile Software Engineer Technical Test

Welcome to the Drivemode technical test assignment. First let's explain the project.

## Product Description

This app displays a list of books from a given subject and the book details. The user can type a subject and the app will search for all the books that fall within the subject and list them down. Tapping on an item will open a new screen that shows the details of the book.

## Technical requirements

1. Data source should be OpenLibrary API (https://openlibrary.org)
    * Note: OpenLibrary API is not documented well. You may need to explore the API to understand how to use it.
    * Test multiple tokens considering potential edge cases.
2. List View - the landing screen when you open the app. This screen should display the following
    * Search bar that allows you to search for a subject
    * Item view should display at least the book cover, title, and author
    * UI is pre-implemented in the `androidApp` and `iosApp` modules and is using the models in the `shared` module.
3. Detail View - this screen should display the following
    * Enlarged Book cover
    * Title
    * Author (or list of authors)
    * Publish date
    * Description
    * UI is pre-implemented in the `androidApp` and `iosApp` modules and is using the models in the `shared` module.
4. Offline-first. The app should be able to work offline. If the user has already searched for a subject, the app should be able to display the results even if the user is offline.
    * Please add a database library of your choice.  This database is the source of truth. Data displayed in the UI should come from this database.
    * Save all successful queries returned from the network to the database.
    * The app should be able to fetch data from database if the network is not available.
    * The first time the app is launched, it should display the last searched subject typed into the search bar, the list of books for that subject, and a toast message that displays the timestamp of the last search.
    * The timestamp should be localized to the user's timezone and formatted as `yyyy-MM-dd HH:mm:ss`.

## Deliverables

1. Create a separate branch and write the Pull Request. You can use any third party libraries to help you focus on what matters. Feature and UX parity between Android and iOS is a must.

## Evaluation Criteria
1. Functionality - the application has the functionalities described in the requirements above
2. Quality of code - no design issues, complies with the current conventions and standards of modern mobile application development, and with a clear and progressive commit logs
3. Architecture - maintainability, extensibility and testability
4. Provide Unit test for:
   *  Searching for a subject
   *  Retrieving book details
   *  Saving and retrieving data from the database
   *  Handling errors from the network response

## Bonus points
1. User interface design: feel free to use additional data, improve the UI, and implement new features to make the app more attractive
2. Production quality: consider what it takes to build a production-ready application, including handling of situations applicable to android mobile applications such as lack of network coverage, configuration changes, performance, etc.

## Notice
Please do not share this project or any details about this project with anyone else. We would like to keep this test private and only for the candidates who are applying for the position.
