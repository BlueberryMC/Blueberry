<img width=25% height=25% src="https://user-images.githubusercontent.com/19150229/111063610-04f4bf00-84f3-11eb-9e39-a37c02dd7cd0.png"></img>

# Blueberry

Open-source modding API for Minecraft

## Why?
- Faster update (since it uses mojang mapping, no need to make our mapping)
- Deobfuscated code (classes, fields, methods)

## Important Warning
**Blueberry is a very small project.**
Whilst I try not to break API compatibility, but things could change at anytime!

## Known Issues
- Cannot stop debug profiler using `/debug stop` after using `/debug start`
- Debug pie is completely black, and blinks when the chat screen is open
- Can't join using BungeeCord/Waterfall? (needs testing)

## Todo
- Provide easy way to create profile or version in launcher for Blueberry
  - probably with bsdiff
- multiplayer support
