# ZCore

**Core utility library plugin for Minecraft servers (1.8 - 1.21+)**

ZCore provides shared dependencies and helper classes for Minecraft plugins, reducing plugin size and ensuring compatibility.

## 🎯 Features

### Bundled Libraries
- **Adventure API 4.17.0** - Modern text component API
- **Adventure MiniMessage** - Gradient colors, hover events, clickable text
- **Adventure Platform Bukkit** - MC 1.8+ compatibility layer
- **BoostedYAML** - Enhanced YAML config management
- **org.json** - JSON parsing utilities

### Utility Classes
- `ColorUtil` - Smart text colorization with automatic hex support detection
- `MessageManager` - Multi-language message system with placeholders
- `ConfigManager` - Simplified config handling

## 📥 Installation (Server Owners)

1. Download the latest `ZCore.jar` from [Releases](https://github.com/THEzombiePL/ZCore/releases/latest)
2. Place it in your `plugins/` folder
3. Restart the server

**Note:** ZCore is a library plugin. It provides functionality for other plugins but doesn't add any commands or features on its own.

## 👨‍💻 For Developers

### Add Dependency

**Gradle (build.gradle):**
```gradle
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.THEzombiePL:ZCore:v1.0.0'
}
```

plugin.yml:
```yaml
depend: [ZCore]
```

Example Usage
ColorUtil - Smart Text Colorization:

```java
import me.thezombiepl.plugin.zcore.utils.ColorUtil;
import net.kyori.adventure.text.Component;

// MiniMessage format (MC 1.16+)
Component gradient = ColorUtil.colorize("<gradient:red:blue>Rainbow Text</gradient>");

// Legacy codes (MC 1.8+)
Component legacy = ColorUtil.colorize("&c&lRed Bold &a&oGreen Italic");

// Mixed (works everywhere!)
Component mixed = ColorUtil.colorize("&7Prefix: <red>Important</red>");

player.sendMessage(gradient);
```

MessageManager - Multi-language Messages:

```java
import me.thezombiepl.plugin.zcore.messages.MessageManager;

MessageManager messages = new MessageManager(plugin, configManager, "en");

// From messages_en.yml: "welcome: <green>Welcome, {player}!"
messages.send(player, "welcome", 
    Placeholder.unparsed("player", player.getName())
);
```

ConfigManager - Easy Config Handling:

```Java
import me.thezombiepl.plugin.zcore.config.ConfigManager;

ConfigManager config = new ConfigManager(plugin, "config.yml");

String value = config.getConfig().getString("setting.key");
int number = config.getConfig().getInt("setting.number", 10);

config.reload();
```

## 🔧 Building from Source

```bash
git clone https://github.com/THEzombiePL/ZCore.git
cd ZCore
./gradlew clean shadowJar
```

JAR will be in build/libs/ZCore-*.jar


## 📝 License
MIT License - see LICENSE file

## 🐛 Issues & Support

- Report bugs: [GitHub Issues](https://github.com/THEzombiePL/ZCore/issues)
- Questions: [GitHub Issues](https://github.com/THEzombiePL/ZCore/discussions)

## 🌟 Projects Using ZCore
- [ZGuard](https://github.com/THEzombiePL/ZGuard) - VPN/Proxy protection
