package org.gradle.cli;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandLineOption {
    private Class<?> argumentType = Void.TYPE;
    private String deprecationWarning;
    private String description;
    private final Set<CommandLineOption> groupWith = new HashSet();
    private boolean incubating;
    private final Set<String> options = new HashSet();

    public CommandLineOption(Iterable<String> options) {
        for (String option : options) {
            this.options.add(option);
        }
    }

    public Set<String> getOptions() {
        return this.options;
    }

    public CommandLineOption hasArgument() {
        this.argumentType = String.class;
        return this;
    }

    public CommandLineOption hasArguments() {
        this.argumentType = List.class;
        return this;
    }

    public String getDescription() {
        StringBuilder result = new StringBuilder();
        if (this.description != null) {
            result.append(this.description);
        }
        if (this.deprecationWarning != null) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append("[deprecated - ");
            result.append(this.deprecationWarning);
            result.append("]");
        }
        if (this.incubating) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append("[incubating]");
        }
        return result.toString();
    }

    public CommandLineOption hasDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean getAllowsArguments() {
        return this.argumentType != Void.TYPE;
    }

    public boolean getAllowsMultipleArguments() {
        return this.argumentType == List.class;
    }

    public CommandLineOption deprecated(String deprecationWarning) {
        this.deprecationWarning = deprecationWarning;
        return this;
    }

    public CommandLineOption incubating() {
        this.incubating = true;
        return this;
    }

    public String getDeprecationWarning() {
        return this.deprecationWarning;
    }

    Set<CommandLineOption> getGroupWith() {
        return this.groupWith;
    }

    void groupWith(Set<CommandLineOption> options) {
        this.groupWith.addAll(options);
        this.groupWith.remove(this);
    }
}
