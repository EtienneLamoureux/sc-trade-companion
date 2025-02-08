# Contributing
Thank you for your interest in the project!

## What to work on
If you are new to the project, take a look at issues tagged with ["Good first issue" or "Help wanted"](https://github.com/EtienneLamoureux/sc-trade-companion/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22good%20first%20issue%22%20label%3A%22help%20wanted%22): these would be the best issues to work on as a new contributor. That being said, [any issue in our backlog](https://github.com/EtienneLamoureux/sc-trade-companion/issues) is up for grabs, so feel free to pitch in on anything that catches your eye!

Please note that unsolicited contributions (features not in our backlog) may take a lot longer to get reviewed and merged, and may get rejected entirely.

## Guidelines
To ensure quality and consistency, we ask that contrbutions follow these guidelines: 
1. Your branch MUST compile through our [Java CI with Gradle](https://github.com/EtienneLamoureux/sc-trade-companion/actions/workflows/gradle.yml)
2. Code MUST be formatted according to our [code style](https://github.com/EtienneLamoureux/sc-trade-companion/blob/main/contributing/GoogleStyle.xml) and defined [import order](https://github.com/EtienneLamoureux/sc-trade-companion/blob/main/contributing/companion.importorder)
3. Files in `tools.sctrade.companion.domain` MUST NOT import files from outside the package, with the exception of `tools.sctrade.companion.utils`
4. Public classes and methods MUST be documented
5. Changes MUST be unit tested
6. Changes MUST NOT reduce the existing code coverage
7. Changes SHOULD respect the general structure of the project
8. Changes SHOULD be self-explanatory, using clear names and flow
9. Libraries SHOULD be abstracted behind a [facade](https://en.wikipedia.org/wiki/Facade_pattern)
10. Changes SHOULD NOT introduce new compiler warnings
11. Changes SHOULD NOT introduce new [CheckStyle](https://github.com/EtienneLamoureux/sc-trade-companion/blob/main/contributing/google_style.xml) warnings

## Testing
The test harness is still in development. For the moment, you can run the app locally and debug it when it processes screenshots as you would any java application.

## Submitting your contribution
The project builds locally and your changes do what they are supposed to? Great! You are ready to submit a pull request: 
1. Make sure the `gradlew clean build` completes successfully
2. Running `gradlew spotlessApply` will format the code and organize the imports correctly
3. Create a pull request towards the `main` branch
4. Feel free to let us know you have outstanding changes ready for review [in our slack channel](https://discord.com/channels/832608007313424444/1106015731516178483)

## Need help?
### Javadoc
All public class and and methods are thoroughly documented using standard javadoc comments. You can refer to that documentation directly in the code, or use the `gradlew javadoc` command to generate HTML help pages, accessible at `/sc-trade-companion/build/javadoc/index.html`.

### Ask away!
We're always happy to help would-be contributors to the project! If you run into issues or have comments/questions, feel free to reach out to the [#help channel](https://discord.com/channels/832608007313424444/832653828901568562) of [our Discord server](https://discord.gg/fdCxQAccpG). We're open to discuss anything code related, but we will not be able to provide assistance with environment setup or basic computer science fundamentals.