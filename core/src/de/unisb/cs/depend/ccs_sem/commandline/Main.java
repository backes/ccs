package de.unisb.cs.depend.ccs_sem.commandline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.unisb.cs.depend.ccs_sem.evalutators.Evaluator;
import de.unisb.cs.depend.ccs_sem.evalutators.ParallelEvaluator;
import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.exporters.AiSeeGraphExporter;
import de.unisb.cs.depend.ccs_sem.exporters.ETMCCExporter;
import de.unisb.cs.depend.ccs_sem.exporters.Exporter;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class Main {

    private static long oldTime;
    private File inputFile = null;
    private Evaluator evaluator = null;
    private final List<Exporter> exporters = new ArrayList<Exporter>(2);

    public Main(String[] args) {
        parseCommandLine(args);
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {
        new Main(args).run();
    }

    private boolean run() {
        checkCommandLine();

        FileReader inputFileReader;
        try {
            inputFileReader = new FileReader(inputFile);
        } catch (final FileNotFoundException e) {
            System.err.println("File " + inputFile.getAbsolutePath() + " not found.");
            return false;
        }

        final Program program;
        try {
            log("Lexing...");
            final List<Token> tokens = new CCSLexer().lex(inputFileReader);
            log("Parsing...");
            program = new CCSParser().parse(tokens);
        } catch (final LexException e) {
            System.err.println("Error lexing input file: " + e.getMessage());
            System.err.println("around this context: \"" + e.getEnvironment() + "\"");
            return false;
        } catch (final ParseException e) {
            System.err.println("Error parsing input file: " + e.getMessage());
            return false;
        }

        log("Evaluating...");
        program.evaluate(evaluator);

        log("Exporting...");
        boolean errors = false;
        for (final Exporter exporter: exporters) {
            log("  - " + exporter.getIdentifier());
            try {
                exporter.export(program.getMainExpression());
            } catch (final ExportException e) {
                System.err.println("Error exporting: " + e.getMessage());
                errors = true;
                // but continue with the next one
            }
        }

        log("Ready." + (errors ? " There were errors." : ""));

        return errors;
    }

    private void checkCommandLine() {
        if (inputFile == null) {
            System.err.println("You didn't specify an input file.");
            printHelp(System.err);
            System.exit(-1);
        }
        if (evaluator == null) {
            evaluator = new ParallelEvaluator();
        }
    }

    private void parseCommandLine(String[] args) {
        int index = 0;
        while (index < args.length) {
            final String arg = args[index++];

            if ("--help".equals(arg)) {
                printHelp(System.out);
                System.exit(0);
            } else if ("--output".equals(arg)) {
                if (index == args.length) {
                    System.err.println("Expecting argument for \"-o\" switch.");
                    System.exit(-1);
                }
                parseOutputFile(args[index++]);
            } else if (arg.length() >= 2 && arg.charAt(0) == '-' && arg.charAt(1) != '-') {
                for (int i = 1; i < arg.length(); ++i) {
                    final char c = arg.charAt(i);
                    switch (c) {
                    case 'h':
                        printHelp(System.out);
                        System.exit(0);
                        break;

                    case 'o':
                        if (i < arg.length() - 1 || index == args.length) {
                            System.err.println("Expecting argument for \"-o\" switch.");
                            System.exit(-1);
                        }
                        parseOutputFile(args[index++]);
                        break;

                    default:
                        System.err.println("Illegal switch: \"" + c + "\"");
                        printHelp(System.err);
                        break;
                    }
                }
            } else if (!arg.isEmpty() && !arg.startsWith("-") && inputFile == null) {
                inputFile = new File(arg);
            } else {
                System.err.println("Illegal parameter: \"" + arg + "\"");
                printHelp(System.err);
                System.exit(-1);
            }
        }
    }

    private void parseOutputFile(String arg) {
        int index = arg.indexOf(':');
        String format, filename;
        if (index != -1) {
            format = arg.substring(0, index);
            filename = arg.substring(index+1);
        } else {
            index = arg.indexOf('.');
            if (index == -1) {
                System.err.println("Cannot extract format from filename \"" + arg + "\"");
                System.exit(-1);
            }
            format = arg.substring(index+1);
            filename = arg;
        }
        if (filename.length() == 0) {
            // TODO default filename
        }
        if ("aisee".equalsIgnoreCase(format) || "gdl".equalsIgnoreCase(format)) {
            exporters.add(new AiSeeGraphExporter(new File(filename)));
        } else if ("etmcc".equalsIgnoreCase(format) || "tra".equalsIgnoreCase(format)) {
            exporters.add(new ETMCCExporter(new File(filename)));
        } else {
            System.err.println("Unknown format: \"" + format + "\"");
            System.exit(-1);
        }
    }

    private void printHelp(PrintStream out) {
        out.println("usage: java " + getClass().getName() + " <parameter> <input file>");
    }

    private void log(String output) {
        final long newTime = System.currentTimeMillis();
        if (oldTime == 0)
            oldTime = newTime;

        final long diff = newTime - oldTime;

        System.out.format((Locale)null, "[%7.3f] %s%n", 1e-3 * diff, output);
    }

}
