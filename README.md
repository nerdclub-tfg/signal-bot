# signal-bot
A bot for signal. 
This bot can connect to the Signal Service as the primary device using a dedicated number or secondary to your main device. 
It answers incoming messages automatically using plugins implemented as Java classes.

## Building
This project uses gradle as its build management system and git for version control. Therefore:

```bash
git clone git@github.com:nerdclub-tfg/signal-bot.git
cd signal-bot
./gradlew fatJar
```

If you have already used gradle you might be wondering why there is `fatJar` and not `build`. 
`fatJar` is a custom task we defined to  build a jar which contains all the dependencies, 
so you can run it easily and everywhere on a Java Machine supporting Java 8:

```bash
java -jar build/libs/signal-bot-all.jar
```

## Contributing

### Reporting Bugs & Requesting Features
Please search for your issue on the issue tracker of this project to see if it is already reported. 
If not, you are free to create a new ticket. Please use the `plugin proposal` tag (blue) for any new plugins you would like to have.

### Pull Requests
Please use meaningful commit messages and branch names and rebase changes on master into your fork (instead of creating merge commits). 
We love to see PRs for new features and plugins!

### Tips for Developing

#### Development Environment
Most Java IDEs nowadays support gradle projects out of the box, so just point your IDE at the project's root.
There are multiple options for testing:

- For debugging we have implemented an offline feature, which emulates a connection on the console and allows you to send custom crafted messages without registering the bot to the actual Signal servers.

- Registering the bot as a secondary device to your main device is possible and might be the only option for an online test, 
but please keep in mind that the bot will process all messages sent to you and not just your debugging messages. 

- If you have a phone number you can receive SMS on you can just register the bot as the primary device on that number. 
This can even be done on the staging and production server and allows you to have one bot for your friends to use and one for debugging.

Additionally, Whispersystems wants you to use the staging and not the production server for all testing. 
To get two instances of Signal on your Android phone I suggest to use MultiROM.
Please note that Signal-Desktop can be registered as a standalone device using the developer console. Refer to [CONTRIBUTING.md in the github repository](https://github.com/WhisperSystems/Signal-Desktop/blob/master/CONTRIBUTING.md#standalone-registration).

#### Adding a new Plugin
Plugins are classes that extend `Plugin` and are located in the package `plugins`. 
So for creating a new one, subclass `Plugin` and add it to the package. 
You also have to add an entry to the `PLUGINS` array in `Plugin` and to `defaultConfig.json` in the resources package. 
The boolean there specifies whether your plugin is enabled by default or disabled. 
An existing config will be updated with this value.

Plugins consist of currently two methods:

- `accepts` is called to check if this plugin wants to process an incoming message. 
You can check if an attachment is present and if it is of type PNG, for example. 
- `onMessage` is only called if `accepts` returns true.

If you want your plugin to only activate if the message's text is prefixed with something like `!myplugin` 
you can use `PrefixedPlugin` which implements `accepts` and a method `stripPrefix` to get the substring of the body which contains the parameters. 
We suggest to prefix all commands with an exclamation mark to clearly differentiate it from normal messages.

