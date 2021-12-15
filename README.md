<img width=25% height=25% src="https://user-images.githubusercontent.com/19150229/111063610-04f4bf00-84f3-11eb-9e39-a37c02dd7cd0.png"></img>

# Blueberry

Open-source modding API for Minecraft

## 🤔 Why?
- Faster update (since it uses mojang mapping, no need to make our mapping)
  - it does not mean "fast update", because sometimes i don't have enough time to work on this project :(
- Deobfuscated code (classes, fields, methods)

## ⚠️ Important Warning
**Blueberry is a very small project.**
Whilst I try not to break API compatibility, but things could change at anytime!

## ✨ Key features
- Live Compiler (Automatically compiles the mod from .java files)
  - Reload (You can recompile the mod in-game using "Recompile" button on the mod list menu, if the mod was compiled by live compiler... but buggy for now.)

## 📝 Todo
- multiplayer support
  - "deny" incompatible clients, it just shows "incompatible" in client/server menu and users can connect
- write a ton of [docs](https://github.com/BlueberryMC/Blueberry-docs) and javadoc
- Migrate to gradle

## ✏️ Translations
All translation PRs are welcome!

To start the translation of the project, please follow these steps:
- Fork the project
- Clone the project with VSCode or anything you can browse files and edit them
- Go to `Blueberry-API/src/main/resources/assets/blueberry/lang`, create or modify the translation file! (Do not edit `en_us.json` unless they are misspelled)
- Create a PR with your changes.

## 🔗 Links
- [Documentation](https://blueberrymc.github.io/Blueberry-docs/)
