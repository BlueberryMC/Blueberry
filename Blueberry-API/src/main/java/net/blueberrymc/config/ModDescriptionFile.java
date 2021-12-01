package net.blueberrymc.config;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.ModInfo;
import net.blueberrymc.config.yaml.YamlArray;
import net.blueberrymc.config.yaml.YamlObject;
import net.blueberrymc.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ModDescriptionFile implements ModInfo {
    private static final Pattern MOD_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_-]*$");
    private static final Logger LOGGER = LogManager.getLogger();
    @NotNull protected final String modId;
    @NotNull protected final String version;
    @NotNull protected final String mainClass;
    @NotNull protected final String name;
    @Nullable protected final List<String> authors;
    @Nullable protected final List<String> credits;
    @Nullable protected final List<String> description;
    protected final boolean unloadable;
    @NotNull protected final Set<String> depends;
    protected final boolean source;
    @Nullable protected final String sourceDir;
    @Nullable protected final String include;

    public ModDescriptionFile(@NotNull String modId,
                              @NotNull String version,
                              @NotNull String mainClass,
                              @NotNull String name,
                              @Nullable List<String> authors,
                              @Nullable List<String> credits,
                              @Nullable List<String> description,
                              boolean unloadable,
                              @Nullable List<String> depends,
                              boolean source,
                              @Nullable String sourceDir,
                              @Nullable String include) {
        if (!MOD_ID_PATTERN.matcher(modId).matches()) throw new IllegalArgumentException("Mod ID must match the pattern: '^[a-zA-Z0-9][a-zA-Z0-9_-]$'");
        this.modId = modId;
        this.version = version;
        this.mainClass = mainClass;
        this.name = name;
        this.authors = authors;
        this.credits = credits;
        this.description = description;
        this.unloadable = unloadable;
        this.depends = depends == null ? new HashSet<>() : new HashSet<>(depends);
        this.source = source;
        this.sourceDir = sourceDir;
        this.include = include;
    }

    @Override
    @NotNull
    public String getModId() {
        return modId;
    }

    @NotNull
    public String getVersion() {
        return version;
    }

    @NotNull
    public String getMainClass() {
        return mainClass;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public List<String> getAuthors() {
        return authors;
    }

    @Nullable
    public List<String> getCredits() {
        return credits;
    }

    @Nullable
    public List<String> getDescription() {
        return description;
    }

    public boolean isUnloadable() {
        return unloadable;
    }

    @NotNull
    public Set<String> getDepends() {
        return depends;
    }

    /**
     * Returns whether the mod needs to be compiled before using it.
     * @return whether the mod contains source code or not
     */
    public boolean isSource() {
        return source;
    }

    @Nullable
    public String getSourceDir() {
        return sourceDir;
    }

    /**
     * @return additional resources directory
     */
    @Nullable
    public String getInclude() {
        return include;
    }

    @NotNull
    public static ModDescriptionFile read(@NotNull YamlObject yaml) {
        String modId = yaml.getString("id");
        String version = yaml.getString("version");
        String mainClass = yaml.getString("main");
        String name = yaml.getString("name");
        String author = yaml.getString("author");
        String credit = yaml.getString("credit");
        YamlArray authorsArray = yaml.getArray("authors");
        if (author != null) {
            if (authorsArray == null) authorsArray = new YamlArray();
            authorsArray.add(author);
        }
        YamlArray creditsArray = yaml.getArray("credits");
        if (credit != null) {
            if (creditsArray == null) creditsArray = new YamlArray();
            creditsArray.add(credit);
        }
        YamlArray descriptionArray = yaml.getArray("description");
        List<String> description = Util.mapOrElse(descriptionArray, YamlArray::mapToString, null);
        boolean unloadable = yaml.getBoolean("unloadable", false);
        List<String> depends = Util.mapOrGet(yaml.getArray("depends"), YamlArray::mapToString, ArrayList::new);
        boolean source = yaml.getBoolean("source", false);
        String sourceDir = yaml.getString("sourceDir");
        String include = yaml.getString("include");

        Preconditions.checkNotNull(modId, "modId (id) is missing");
        Preconditions.checkNotNull(version, "version (version) is missing");
        Preconditions.checkNotNull(mainClass, "mainClass (main) is missing");
        if (name == null) {
            name = modId;
            // TODO: remove log message, probably
            LOGGER.info("Mod name for " + name + " is missing, using modId instead");
        }
        if (description == null) {
            // TODO: remove log message, probably
            LOGGER.info("Mod description for " + name + " is missing");
        }
        return new ModDescriptionFile(
                modId,
                version,
                mainClass,
                name,
                authorsArray == null ? null : authorsArray.mapAsType(Object::toString),
                creditsArray == null ? null : creditsArray.mapAsType(Object::toString),
                description,
                unloadable,
                depends,
                source,
                sourceDir,
                include
        );
    }

    @Override
    public String toString() {
        return "ModDescriptionFile{" + "modId='" + modId + '\'' +
                ", version='" + version + '\'' +
                ", mainClass='" + mainClass + '\'' +
                ", name='" + name + '\'' +
                ", authors='" + authors + '\'' +
                ", credits='" + credits + '\'' +
                ", description=" + description +
                ", unloadable=" + unloadable +
                ", depends=" + depends +
                ", source=" + source +
                '}';
    }
}
