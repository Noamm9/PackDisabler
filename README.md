# multiloader-multiversion-template

A multiversion, multiloader template powered by Archloom and Stonecutter. Made for Fabric & NeoForge.

This template is made primarily suited for my own needs, but should be very simple to edit what you need!

## Important Notes

Mixins are automatically registered. This allows easy preprocessing of mixins without needing to add preprocessing to the mixin json file as they get added during compile, you can now simply preprocess out the @Mixin annotation.

## What to Change

### In `gradle.properties`:

Change any properties needed. Some properties are versioned, meaning you need to edit them for every Minecraft version or loader.

These `gradle.properties` files can be found in the `versions` folder. You may need to edit some of these folders/properties depending on what versions you are planning on supporting.

**Note**: mc_dep fields must be in the format `x`, `>=x`, or `>=x <=y`

### In `settings.gradle.kts`:

Change the rootProject.name at the bottom to your mod's name.

The supported Minecraft versions and loaders are also stated here.

### In `fabric.mod.json`

The `authors` field.

### In `neoforge.mods.toml`

The `authors` field.

### File & Folder Names

Remember to rename your files and folders to your group and project name.

### In `examplemod.mixins.json`

The `package` field.

### Change License

Remember to properly set up a license file. This template has one for the template itself (CC0-1.0) and one for myself that I use for all my mods (LGPL-3.0).

## Other Notes

### Publishing

This template has a setup for Modrinth and CurseForge publishing.

To enable publishing

- Set your Modrinth and CurseForge project IDs in the project-level gradle.properties file
- Set your API tokens in your Gradle user-level properties file

For windows, this file is located at `C:\Users\{user}\.gradle\gradle.properties`

Example entries for the user-level file:

    modrinth.token=your_modrinth_token
    curseforge.token=your_curseforge_token

When publishing, the `CHANGELOG.md` file will be used to set the changelog.

### Mixin Debug & HotSwap

This setup attempts to set up HotSwap, Mixin HotSwap, and Mixin export debugging for every run config. HotSwap requires using JBR 21, so remember to switch to that or disable the JVM args in the `loom.runs` block in `build.gradle.kts`.

Also, note that Mixin HotSwap is not supported on NeoForge. There should be no issue keeping the arguments for NeoForge, but let me know if issues do arise and it can easily be turned off.

### Run Directory

This setup uses the same run directory for all versions. See the `loom` block in `build.gradle.kts` to disable this.

### ModMenu English Description

This setup uses processResources to string replace the English description to be the one set in `gradle.properties`. You can change the default translation file in the `processResources` block in `build.gradle.kts`.
