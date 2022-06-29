<img width=25% height=25% src="https://user-images.githubusercontent.com/19150229/111063610-04f4bf00-84f3-11eb-9e39-a37c02dd7cd0.png"></img>

# Blueberry

Open-source modding API for Minecraft

## 🤔 Why?
- Faster update (since it uses mojang mapping, no need to make our mapping)
  - it does not mean "fast update", because sometimes i don't have enough time to work on this project :(
- Deobfuscated code (classes, fields, methods, and sometimes local variables)
- Simple steps to get started making mods

## ⚠️ Important Warning
**Blueberry is a very small project.**
Whilst I try not to break API compatibility, but things could change at anytime!

## ✨ Key features
- Live Compiler (compiles the mod from .java files when loading a mod)
  - `Recompile` button on the mod list menu to recompile and reload the mod (see issues for known issues)
  - See the [documentation](https://www.blueberrymc.net/reference/source-mod/) for more details and how to use it properly
- [Mixin](https://github.com/SpongePowered/Mixin) support
- File selection screen within the client (FileDialogScreen)
- Bundled Kotlin (v1.7.0)

## ✏️ Translations
All translation PRs are welcome!

To start the translation of the project, please follow these steps:
- Fork the project
- Clone the project with VSCode or anything you can browse files and edit them
- Go to `Blueberry-API/src/main/resources/assets/blueberry/lang`, create or modify the translation file! (Do not edit `en_us.json` unless they are misspelled)
- Create a PR with your changes.

## 🔗 Links
- [Documentation](https://blueberrymc.net/)
- [Javadoc](https://jd.blueberrymc.net/)
