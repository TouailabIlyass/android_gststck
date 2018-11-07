package org.gradle.cli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParsedCommandLine {
    private final List<String> extraArguments = new ArrayList();
    private final Map<String, ParsedCommandLineOption> optionsByString = new HashMap();
    private final Set<String> presentOptions = new HashSet();

    ParsedCommandLine(Iterable<CommandLineOption> options) {
        for (CommandLineOption option : options) {
            ParsedCommandLineOption parsedOption = new ParsedCommandLineOption();
            for (String optionStr : option.getOptions()) {
                this.optionsByString.put(optionStr, parsedOption);
            }
        }
    }

    public String toString() {
        return String.format("options: %s, extraArguments: %s", new Object[]{quoteAndJoin(this.presentOptions), quoteAndJoin(this.extraArguments)});
    }

    private String quoteAndJoin(Iterable<String> strings) {
        StringBuilder output = new StringBuilder();
        boolean isFirst = true;
        for (String string : strings) {
            if (!isFirst) {
                output.append(", ");
            }
            output.append("'");
            output.append(string);
            output.append("'");
            isFirst = false;
        }
        return output.toString();
    }

    public boolean hasOption(String option) {
        option(option);
        return this.presentOptions.contains(option);
    }

    public boolean hasAnyOption(Collection<String> logLevelOptions) {
        for (String option : logLevelOptions) {
            if (hasOption(option)) {
                return true;
            }
        }
        return false;
    }

    public ParsedCommandLineOption option(String option) {
        ParsedCommandLineOption parsedOption = (ParsedCommandLineOption) this.optionsByString.get(option);
        if (parsedOption != null) {
            return parsedOption;
        }
        throw new IllegalArgumentException(String.format("Option '%s' not defined.", new Object[]{option}));
    }

    public List<String> getExtraArguments() {
        return this.extraArguments;
    }

    void addExtraValue(String value) {
        this.extraArguments.add(value);
    }

    ParsedCommandLineOption addOption(String optionStr, CommandLineOption option) {
        ParsedCommandLineOption parsedOption = (ParsedCommandLineOption) this.optionsByString.get(optionStr);
        this.presentOptions.addAll(option.getOptions());
        return parsedOption;
    }

    void removeOption(CommandLineOption option) {
        this.presentOptions.removeAll(option.getOptions());
    }
}
