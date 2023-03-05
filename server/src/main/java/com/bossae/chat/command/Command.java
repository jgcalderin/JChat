package com.bossae.chat.command;

import com.bossae.chat.ClassServer;
import com.bossae.chat.ClientClosedConnection;
import com.bossae.chat.file.FileHelper;

import java.io.File;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Command {

    public static Command getCommand(String input) throws ClientClosedConnection {
        Matcher m = null;
        Pattern pattern = null;

        pattern = Pattern.compile("^::sum (-?\\d*)\\s(-?\\d*)$");
        m = pattern.matcher(input);
        if (m.find())
            return (new CommandSum(m.group(1), m.group(2)));

        pattern = Pattern.compile("^::mul (-?\\d*)\\s(-?\\d*)$");
        m = pattern.matcher(input);
        if (m.find())
            return (new CommandMul(m.group(1), m.group(2)));

        pattern = Pattern.compile("^::bye$");
        m = pattern.matcher(input);
        if (m.find())
            throw new ClientClosedConnection();

        pattern = Pattern.compile("^::echo (.*)$");
        m = pattern.matcher(input);
        if (m.find())
            return (new CommandEcho(m.group(1)));

        pattern = Pattern.compile("^::get (.*)$");
        m = pattern.matcher(input);
        if (m.find())
            return (new CommandGetFile(m.group(1)));

        return null;
    }

    public static boolean isACommand(String line)
    {
        Matcher m = Pattern.compile("^::(get|sum|mul|div|bye|echo)").matcher(line);
        return m.find();
    }

    public abstract String execute();

}
class CommandSum extends Command
{
    private double a;
    private double b;
    public CommandSum(String group1, String group2) {
        super();
        a = Double.parseDouble(group1);
        b = Double.parseDouble(group2);
    }

    public String execute() {
        Double sum = a + b;
        return sum.toString();
    }
}

class CommandMul extends Command
{
    private double a;
    private double b;
    public CommandMul(String group1, String group2) {
        super();
        a = Double.parseDouble(group1);
        b = Double.parseDouble(group2);
    }

    public String execute() {
        Double mul = a * b;
        return mul.toString();
    }
}

class CommandGetFile extends Command
{
    private String path;
    public CommandGetFile(String _path)
    {
        path = _path;

    }

    public String execute()
    {
        try {
            return new String(FileHelper.fileToBytes(ClassServer.docroot + File.separator + path));
        } catch (IOException e) {
            return "File Not Found !";
        }
    }
}

class CommandEcho extends Command
{
    private String line;
    public CommandEcho(String _line)
    {
        line = _line;
    }

    public String execute()
    {
        return line;
    }
}