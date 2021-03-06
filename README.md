<img width=25% height=25% src="https://user-images.githubusercontent.com/19150229/111063610-04f4bf00-84f3-11eb-9e39-a37c02dd7cd0.png"></img>

# Blueberry

Open-source modding API for Minecraft

## Why?
- Faster update (since it uses mojang mapping, no need to make our mapping)
  - it does not mean "fast update", because sometimes i don't have enough time to work on this project :(
- Deobfuscated code (classes, fields, methods)

## Important Warning
**Blueberry is a very small project.**
Whilst I try not to break API compatibility, but things could change at anytime!

## Known Issues
- Cannot stop debug profiler using `/debug stop` after using `/debug start`
- Can't join using BungeeCord/Waterfall? (needs testing)
- `CPU: <unknown>`
  - Does not happen on MagmaCube, so it is specific to Blueberry

## Todo
- Apply bspatch at installer, not at runtime.
- multiplayer support
  - "deny" incompatible clients, it just shows "incompatible" in client/server menu and users can connect

## Translations
All translation PRs are welcome!

To start the translation of the project, please follow these steps:
- Fork the project
- Clone the project with VSCode or anything you can browse files and edit them
- Go to `Blueberry-API/src/main/resources/assets/blueberry/lang`, create or modify the translation file! (Do not edit `en_us.json` unless they are misspelled)
- Create a PR with your changes.

## Links
- [Changelogs](https://cl-b.acrylicstyle.xyz/)
