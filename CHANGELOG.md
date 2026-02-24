# SC Trade Companion
## 1.4.1
### Bugs
- Clarify the .NET dependency in the logs tab when the host system requires an update
- Fix an issue when pressing the bound key without releasing for a long time

## 1.4.0
### Features
- Add ability to change hotkey
- Change OCR engine
  - Improve OCR success rate, including on non-stanton kiosks
  - Improve text recognition
  - Improve parsed-price accuracy
  - Improve available box sizes detection
- Handle slight kiosk misalignment through perspective correction

## 1.3.1
### Other
- Improve text recognition
- Sort logs by latest on top by default
- Add clear [Code 9] error for missing or outdated .NET
- Provide better contribution tools, now with a fully-fledged test harness

## 1.3.0
### Features
- Reduce the image processing time
- Reduce the application's CPU and RAM usage
- Reduce the application's size

## 1.2.1
### Bugs
- Fix many causes of "[Code 5] Listings could not be read"

## 1.2.0
### Features
- Add available box sizes to the /my-data CSV output file
- Support Pyro commodity kosks
- Support Levski commodity kosks

### Bugs
- Fix many causes of "[Code 5] Listings could not be read"
- Fix many causes of "[Code 6] The shop/city name was never selected in the "Your inventories" dropdown"

### Other
- Improve OCR success rate
- Improve parsed-price accuracy

## 1.1.2
### Bugs
- Fix `java.lang.ClassNotFoundException: com.sun.java.accessibility.AccessBridge` crash on start-up

## 1.1.1
### Bugs
- Fix "Failed to resolve 'sc-trade.tools'"
- Fix duplicate entries in the website's leaderboard
  - New duplicate entries will not be created, but the existing ones will be cleaned-up by hand in the coming days
- Fix the app crashing on startup for Wine users

### Other
- Disable log reading
  - The experiment has concluded for now

## 1.1.0
### Features
- Add a setting letting the user choose which monitor to capture and process

### Bugs
- Fix listings fail to be written to the local CSV file
- Fix 'sc-trade.tools' fails to resolve

### Other
- Add missing translations

## 1.0.4
### Other
- Read location from game logs
  - *You can set your LIVE game path through a new setting*
  - *You should still select the location in the dropdown when taking screenshots*
- Improve performance and security

## 1.0.3
### Other
- Handle 3.24 kiosk layout
- Improve character recognition accuracy

## 1.0.2
### Other
- Improve text-reading accuracy
- Include a "Run as administrator" script
- Improve datetime format to allow proper sorting

## 1.0.1
### Bugs
- Improve buy/sell detection in sub-optimal conditions

### Other
- Add an INFO level logs when listings are sent to the website

## 1.0.0
### Features
- Spellcheck and auto-correct location and commodity names
- Add a setting to allow users to send a username when submitting data
- Notify users of important errors through the UI
- Improve shop location detection
- Improve price accuracy detection

### Bugs
- Fix ships listed as locations
- Fix companion app opening as white screen
- Correctly parse listings from low-contrast screens
- Correctly ignore commodity listings that cannot be spellchecked
- Correctly ignore batches of commodity listings that cannot be attributed to a location
- Completely close app when pressing "X"

### Other
- Add German localization
- Limit RAM usage
- Apply dark theme to the UI

## 0.2.1
### Bugs
- Fix data not being exported to the /my-data folder
- Fix data not being published to https://sc-trade.tools

## 0.2.0
### Features
- Add a sound when a screenshot is taken

### Other
- Remove external Java dependency
  - This app now ships as a stand-alone application, requiring no additional install
  - If you had installed Java specifically for this app, you can uninstall it
- Add the current version to the application's title bar

## 0.1.0
### Features
- Automatic commodity kiosk scraping by pressing the <kbd>F3</kbd> key
- All extracted data saved to the `/my-data` folder as CSV files
- All screenshots saved to the `/my-images` folder
- Available in: 
  - 🇬🇧 English
  - 🇫🇷 French
- [Known issues](https://github.com/EtienneLamoureux/sc-trade-companion/issues?q=is%3Aopen+is%3Aissue+label%3Abug)
