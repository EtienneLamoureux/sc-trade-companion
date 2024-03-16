# SC Trade Companion
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
