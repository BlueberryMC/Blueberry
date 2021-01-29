package net.blueberrymc.config;

import com.google.common.base.Preconditions;
import net.blueberrymc.common.bml.ModInfo;
import net.blueberrymc.config.yaml.YamlArray;
import net.blueberrymc.config.yaml.YamlObject;
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
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_-]*$");
    private static final Logger LOGGER = LogManager.getLogger();
    @NotNull protected final String modId;
    @NotNull protected final String version;
    @NotNull protected final String mainClass;
    @NotNull protected final String name;
    @Nullable protected final String authors;
    @Nullable protected final String credits;
    @Nullable protected final List<String> description;
    protected final boolean unloadable;
    @NotNull protected final Set<String> depends;

    public ModDescriptionFile(@NotNull String modId,
                              @NotNull String version,
                              @NotNull String mainClass,
                              @NotNull String name,
                              @Nullable String authors,
                              @Nullable String credits,
                              @Nullable List<String> description,
                              boolean unloadable,
                              @Nullable List<String> depends) {
        if (!PATTERN.matcher(modId).matches()) throw new IllegalArgumentException("Mod ID must match the pattern: '^[a-zA-Z0-9][a-zA-Z0-9_-]$'");
        this.modId = modId;
        this.version = version;
        this.mainClass = mainClass;
        this.name = name;
        this.authors = authors;
        this.credits = credits;
        this.description = description;
        this.unloadable = unloadable;
        this.depends = depends == null ? new HashSet<>() : new HashSet<>(depends);
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
    public String getAuthors() {
        return authors;
    }

    @Nullable
    public String getCredits() {
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

    public static ModDescriptionFile read(YamlObject yaml) {
        String modId = yaml.getString("id");
        String version = yaml.getString("version");
        String mainClass = yaml.getString("main");
        String name = yaml.getString("name");
        StringBuilder authors = new StringBuilder(yaml.getString("author", ""));
        YamlArray authorsArray = yaml.getArray("authors");
        if (authorsArray != null && !authorsArray.isEmpty()) {
            authors = new StringBuilder();
            boolean first = true;
            for (Object o : authorsArray) {
                String s = o instanceof String ? (String) o : o.toString();
                if (first) {
                    first = false;
                } else {
                    authors.append(", ");
                }
                authors.append(s);
            }
        }
        StringBuilder credits = new StringBuilder(yaml.getString("credits", ""));
        YamlArray creditsArray = yaml.getArray("credits");
        if (creditsArray != null && !creditsArray.isEmpty()) {
            credits = new StringBuilder();
            boolean first = true;
            for (Object o : creditsArray) {
                String s = o instanceof String ? (String) o : o.toString();
                if (first) {
                    first = false;
                } else {
                    authors.append(", ");
                }
                credits.append(s).append("\n");
            }
        }
        YamlArray descriptionArray = yaml.getArray("description");
        List<String> description = descriptionArray == null ? null : descriptionArray.mapAsType(o -> o instanceof String ? (String) o : o.toString());
        boolean unloadable = yaml.getBoolean("unloadable", false);
        List<String> depends = yaml.getArray("depends") == null ? new ArrayList<>() : yaml.getArray("depends").mapAsType(o -> o instanceof String ? (String) o : o.toString());
        Preconditions.checkNotNull(modId, "modId (id) is missing");
        Preconditions.checkNotNull(version, "version (version) is missing");
        Preconditions.checkNotNull(mainClass, "mainClass (main) is missing");
        if (name == null) {
            name = modId;
            LOGGER.info("Mod name for " + name + " is missing, using modId instead");
        }
        if (description == null) {
            LOGGER.info("Mod description for " + name + " is missing");
        }
        return new ModDescriptionFile(modId, version, mainClass, name, authors.toString(), credits.toString(), description, unloadable, depends);
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
                '}';
    }
}
