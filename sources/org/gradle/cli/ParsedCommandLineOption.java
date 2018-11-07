package org.gradle.cli;

import java.util.ArrayList;
import java.util.List;

public class ParsedCommandLineOption {
    private final List<String> values = new ArrayList();

    public String getValue() {
        if (!hasValue()) {
            throw new IllegalStateException("Option does not have any value.");
        } else if (this.values.size() <= 1) {
            return (String) this.values.get(0);
        } else {
            throw new IllegalStateException("Option has multiple values.");
        }
    }

    public List<String> getValues() {
        return this.values;
    }

    public void addArgument(String argument) {
        this.values.add(argument);
    }

    public boolean hasValue() {
        return this.values.isEmpty() ^ 1;
    }
}
