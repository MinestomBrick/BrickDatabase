# BrickUtils

This repository contains some useful tools for rapid developing with [Minestom](https://github.com/Minestom/Minestom).

```
repositories {
    maven { url "https://repo.jorisg.com/snapshots" }
}
```

## Commands


**Features**:

* ArgumentPlayer
* BrickCommand - extend this class for useful methods
* Condition builder - permissions, player only, console only, errors with translations
* Apply conditions of subcommands to the root command
* Apply conditions of syntaxes to the command by default

```
dependencies {
    implementation 'com.gufli.brickutils:commands:1.0-SNAPSHOT'
}
```

```java
public class GamemodeCommand extends BrickCommand {

    public GamemodeCommand() {
        super("gamemode", "gm");

        setInvalidUsageMessage("cmd.gamemode.usage"); // see translations

        ArgumentPlayer player = new ArgumentPlayer("player");
        setInvalidArgumentMessage(player, "cmd.error.args.player"); // see translations

        ArgumentEnum<GameMode> gamemode = ArgumentType.Enum("gamemode", GameMode.class)
                .setFormat(ArgumentEnum.Format.LOWER_CASED);
        setInvalidArgumentMessage(gamemode);

        addConditionalSyntax(
            b -> b.permission("brickessentials.gamemode.other"),
            (sender, context) -> {
                Player target = context.get("player");
                GameMode gamemode = context.get("gamemode");
                target.setGameMode(gamemode);
                TranslationManager.get().send(sender, "cmd.gamemode.other", 
                    target.getUsername(), gamemode.name());
            }, 
            player, 
            gamemode
        );
    }
}
```

## Database

Add [EBean](https://ebean.io) ORM to your minestom extension.

```
dependencies {
    implementation 'com.gufli.brickutils:database:1.0-SNAPSHOT'
}
```

```java
public class CustomDatabaseContext extends DatabaseContext {

    public final static String DATASOURCE_NAME = "FancyDatabase";

    public BrickPermissionsDatabaseContext() {
        super(DATASOURCE_NAME);
    }

    @Override
    protected void buildConfig(DatabaseConfig config) {
        // register converters
        config.addClass(PosConverter.class);

        // register beans
        config.addClass(User.class);
    }
}
```

## Translation

Add internationalization to your extension.

```
dependencies {
    implementation 'com.gufli.brickutils:translation:1.0-SNAPSHOT'
}
```

```java
// Initialize
SimpleTranslationManager tm = new SimpleTranslationManager(this, Locale.ENGLISH); // default locale
tm.loadTranslations(this, "languages"); // automatically find and load language files

// Usage
TranslationManager.get().send(player, "welcome", sender.getName());
```

resources/languages/en.json
```json
{
    "welcome": "Welcome to the server {0}!",
}
```

## Credits

* The [Minestom](https://github.com/Minestom/Minestom) project

## Contributing

Check our [contributing info](CONTRIBUTING.md)

