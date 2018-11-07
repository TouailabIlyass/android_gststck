package org.gradle.cli;

public abstract class AbstractCommandLineConverter<T> implements CommandLineConverter<T> {
    protected abstract T newInstance();

    public T convert(Iterable<String> args) throws CommandLineArgumentException {
        CommandLineParser parser = new CommandLineParser();
        configure(parser);
        return convert(parser.parse((Iterable) args));
    }

    public T convert(ParsedCommandLine args) throws CommandLineArgumentException {
        return convert(args, newInstance());
    }

    public T convert(Iterable<String> args, T target) throws CommandLineArgumentException {
        CommandLineParser parser = new CommandLineParser();
        configure(parser);
        return convert(parser.parse((Iterable) args), (Object) target);
    }
}
