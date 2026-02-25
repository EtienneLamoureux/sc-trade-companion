# Contributing
Thank you for your interest in the project!

## What to work on
If you are new to the project, take a look at issues tagged with ["Good first issue" or "Help wanted"](https://github.com/EtienneLamoureux/sc-trade-companion/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22good%20first%20issue%22%20label%3A%22help%20wanted%22): these would be the best issues to work on as a new contributor. That being said, [any issue in our backlog](https://github.com/EtienneLamoureux/sc-trade-companion/issues) is up for grabs, so feel free to pitch in on anything that catches your eye!

Please note that unsolicited contributions (features not in our backlog) may take a lot longer to get reviewed and merged, and may get rejected entirely.

## Guidelines
To ensure quality and consistency, we ask that contrbutions follow these guidelines: 
1. Your branch SHALL compile through our [Java CI with Gradle](https://github.com/EtienneLamoureux/sc-trade-companion/actions/workflows/gradle.yml)
1. Code SHALL be formatted according to our [code style](https://github.com/EtienneLamoureux/sc-trade-companion/blob/main/contributing/GoogleStyle.xml) and defined [import order](https://github.com/EtienneLamoureux/sc-trade-companion/blob/main/contributing/companion.importorder)
1. Files in `tools.sctrade.companion.domain` SHALL NOT import files from outside the package, with the exception of `tools.sctrade.companion.utils`
1. Public classes and methods SHALL be documented
1. Changes SHALL be unit tested
1. Changes SHALL NOT reduce the existing code coverage
1. Changes SHOULD respect the general structure of the project
1. Changes SHOULD be self-explanatory, using clear names and flow
1. Libraries SHOULD be abstracted behind a [facade](https://en.wikipedia.org/wiki/Facade_pattern)
1. Changes SHOULD NOT introduce new compiler warnings
1. Changes SHOULD NOT introduce new [CheckStyle](https://github.com/EtienneLamoureux/sc-trade-companion/blob/main/contributing/google_style.xml) warnings

## Dependencies
This project leverages [oneocr-wrapper](https://github.com/EtienneLamoureux/oneocr-wrapper). In order to build and test locally, you will need to acquire the [following DLLs](https://github.com/EtienneLamoureux/oneocr-wrapper?tab=readme-ov-file#dependencies) and copy them in the [`bin/oneocr` folder](https://github.com/EtienneLamoureux/sc-trade-companion/tree/main/bin/oneocr).

## Testing
The test harness allows you to see if your changes are an incremental improvement on the existing processing. Run `CommoditySubmissionFactoryITest` and see if the app now scores higher than the previous accuracy value. Please note that you have to define the list of image manipulations to use in the test class itself. This allows experimenting without changing the current production behaviour.

You can also run the app locally and debug it when it processes screenshots as you would any java application.

## Submitting your contribution
Your changes do what they are supposed to? Great! You are ready to submit a pull request: 
1. Make sure the `gradlew clean build` completes successfully
    1. Running `gradlew spotlessApply` will format the code and organize the imports correctly
1. Create a pull request towards the `main` branch
    1. Feel free to let us know you have outstanding changes ready for review [in our slack channel](https://discord.com/channels/832608007313424444/1106015731516178483)

## Need help?
### Javadoc
All public types and methods are thoroughly documented using standard javadoc comments. You can refer to that documentation directly in the code, or use the `gradlew javadoc` command to generate HTML help pages, accessible at `/build/javadoc/index.html`.

### Ask away!
We're always happy to help would-be contributors to the project! If you run into issues or have comments/questions, feel free to reach out to the [#help channel](https://discord.com/channels/832608007313424444/832653828901568562) of [our Discord server](https://discord.gg/fdCxQAccpG). We're open to discuss anything code related, but we will not be able to provide assistance with environment setup or basic computer science fundamentals.
