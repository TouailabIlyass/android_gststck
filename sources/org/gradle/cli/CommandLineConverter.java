package org.gradle.cli;

public interface CommandLineConverter<T> {
    void configure(CommandLineParser commandLineParser);

    T convert(Iterable<String> iterable) throws CommandLineArgumentException;

    T convert(Iterable<String> iterable, T t) throws CommandLineArgumentException;

    T convert(ParsedCommandLine parsedCommandLine) throws CommandLineArgumentException;

    T convert(ParsedCommandLine parsedCommandLine, T t) throws CommandLineArgumentException;
}
