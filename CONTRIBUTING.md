# Contributing to Blueberry

## Requirements
To get started with PRing changes, you'll need the following software (for now, at least):
- IDE (not required but highly recommended)
- git
- JDK 17+ (If you do not have one, you can download from here: [Adoptium](https://adoptium.net/))
- Maven (to build MagmaCube)

## Directories (Subprojects)
- `Blueberry-API` - APIs for Blueberry-Client (Includes `MagmaCube`)
- `Blueberry-Client` - Modifications to `MagmaCube`/`Minecraft` (Includes `Blueberry-API`, `MinecraftForge-API`)
- `MinecraftForge-API` - Modifications to `Blueberry-API`, but with Minecraft Forge compatibility features (Includes `Blueberry-API`)

While `Blueberry-API` and `MinecraftForge-API` are *not* based on patches, but `Blueberry-Client` is based on patches and git, so a basic understanding of how to use git is required. A basic tutorial can be found here: https://git-scm.com/docs/gittutorial or, search with your preferred search engine.

## Getting started with patches

Assuming you have already forked the repository:
1. Clone your fork to your local machine (ignore the magmacube related errors from IDE if there is any)
2. Type `gradlew patchMinecraft applyBlueberryPatches` in a terminal to apply the changes from upstream (It takes some time!)
   - Alternatively, you can also do `gradlew applyBlueberryPatches` to just apply patches from MagmaCube-Patches. It can be useful when there are no upstream (MagmaCube) changes, and you don't want to waste time.
3. Run `mvn install` inside `MagmaCube` directory to install the MagmaCube into local repository (Maven is required here)
4. Use `gradlew bakeInstaller` to create an installer for the client.
5. To test changes, you can use "Run" feature in the IDE for now.
6. cd into `Blueberry-Client` for client changes.

- Every single commit in `Blueberry-Client` is a patch
- `upstream/master` points to a directory similar to `MagmaCube/Minecraft`

> **Note**
> You've updated the project and if you are getting an error something like shadowServerJar at step 2, try deleting the Blueberry-Client directory and try again.

## Adding patches
1. Modify `Blueberry-Client`
2. Type `git add .` inside that directory to add your changes
3. Run `git commit` with the desired patch message
4. Run `gradlew rebuildBlueberryPatches` in the main directory to convert commits into patches.
5. PR the generated patch file(s) back to this repository.

(To reset everything, simply run: `gradlew applyBlueberryPatches`)

> **Note**
> Please note that if you have some specific implementation detail you'd like to document, you should do so in the patch message or in comments.

## Modifying patches

You can modify patches in some ways, they are complicated though.

Editing the patch files by hand should be avoided, as they will most likely cause problems. (Great care should be taken if you do so, and make sure the patches applies without ANY problems.)

### Method 1
This method works by temporarily resetting your `HEAD` to the desired commit to edit it using `git rebase`.

> **Warning**
> While in the middle of a rebase, you might not be able to compile the module (which means you may not be able to test changes in-game until rebase is completed).

1. cd into `Blueberry-Client`, and run `git rebase -i upstream/master`
  - If your editor does not have a "menu" at the bottom, you're probably using `vim`.
    If you don't know how to use `vim` and don't want to learn, enter `:q!` and press enter. Before redoing this step, do `export EDITOR=nano` for an easier editor to use.
  - If you're using `nano`, you can use Ctrl+X to (save and) exit the editor.
2. Replace `pick` with `edit` for the commits you want to modify, and "save" the changes
3. Make the changes you want to make to the patch
4. Type `git rebase --continue` to continue/finish the rebase
5. Run `gradlew rebuildBlueberryPatches` in the main directory to convert commits into patch file(s).
6. PR your modified patch file(s) back to this repository.

### Method 2
If you are simply editing a more recent commit, or your change is small, simply making the change at HEAD and then moving the commit after you have tested it may be easier.

In this method, you can compile the module to test your changes without messing with your HEADs and heads.

1. Make your changes while at HEAD
2. Make a temporary commit
3. Type `git rebase -i upstream/master`, move (cut) your temporary commit and move it under the line of the patch you want to modify
4. Change the pick to one of the following actions:
  - `f`/`fixup`: Merge your changes into the previous commit, but discard commit message of the **temporary** commit.
  - `s`/`squash`: Merge your changes into the previous commit, but discard commit message of the **previous** commit.
5. "Save" the file
6. Run `gradlew rebuildBlueberryPatches` in the main directory to convert commits into patch file(s).
  - This step should modify existing patches.
7. PR your modified patch file(s) back to this repository.

## PR Policy
We'll accept changes that make sense.
We have very few APIs for now, so feature requests are also welcome.

Please see the formatting guide below before making changes to the source file(s).

## Formatting
All modifications to non-Blueberry files should be marked.

- Multi-line changes start with `// Blueberry start` and end with `// Blueberry end`;
- You can put a comment with an explanation if it isn't obvious, like this: `// Blueberry start - reason`.
  - The comments should generally be about the reason the change was made, what it was before, or what the change is.
  - Multi-line messages should start with `// Blueberry start` and use `/* Multi line message here */` for the message itself.
- One-line changes should have `// Blueberry` or `// Blueberry - reason`.
- Watch out for accidental imports, especially when using "Refactor" feature in the IDE.
  - Absolutely no wildcard imports.

Here's an example of how to mark changes by Blueberry:
```diff
- Minecraft.getInstance().destroyPC();
+ // Minecraft.getInstance().destroyPC(); // Blueberry - don't destroy PC
  entity.getServer().getAdmins().forEach(Entity::explode);
  int i = 0;
  while (true) {
      if (i == 5) {
          entity.explode();
          break;
      }
      i++;
  }
  entity.getWorld().explode(entity.getLocation());
+ // Blueberry start - commit and push changes
+ ((Person) entity).commitChanges(changes);
+ ((Person) entity).push().join();
+ // Blueberry end - commit and push changes
```
