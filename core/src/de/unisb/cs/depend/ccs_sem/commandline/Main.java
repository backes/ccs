package de.unisb.cs.depend.ccs_sem.commandline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.unisb.cs.depend.ccs_sem.evaluators.EvaluationMonitor;
import de.unisb.cs.depend.ccs_sem.evaluators.Evaluator;
import de.unisb.cs.depend.ccs_sem.evaluators.ParallelEvaluator;
import de.unisb.cs.depend.ccs_sem.evaluators.SequentialEvaluator;
import de.unisb.cs.depend.ccs_sem.evaluators.ThreadBasedEvaluator;
import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exceptions.ParseException;
import de.unisb.cs.depend.ccs_sem.exporters.AiSeeGraphExporter;
import de.unisb.cs.depend.ccs_sem.exporters.ETMCCExporter;
import de.unisb.cs.depend.ccs_sem.exporters.Exporter;
import de.unisb.cs.depend.ccs_sem.exporters.GraphVizExporter;
import de.unisb.cs.depend.ccs_sem.exporters.IntegrationtestExporter;
import de.unisb.cs.depend.ccs_sem.exporters.bcg.BCGExporter;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class Main {

    private static long startTime;
    private File inputFile = null;
    private Evaluator evaluator = null;
    private final List<Exporter> exporters = new ArrayList<Exporter>(2);
    private boolean minimizeWeak = false;
    private boolean minimizeStrong = false;
    private final boolean allowUnguarded = true; //false;
    private final boolean allowUnregular = true; //false;


    public Main(String[] args) {
        parseCommandLine(args);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        new Main(args).run();
    }

    private boolean run() throws InterruptedException {
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
            final List<Token> tokens;
            try {
                tokens = new CCSLexer().lex(inputFileReader);
            } finally {
                try {
                    inputFileReader.close();
                } catch (final IOException e) {
                    // ignore
                }
            }
            log("Parsing...");
            program = new CCSParser().parse(tokens);
        } catch (final LexException e) {
            System.err.println("Error lexing input file: " + e.getMessage());
            System.err.println("around this context: \"" + e.getEnvironment() + "\"");
            return false;
        } catch (final ParseException e) {
            System.err.println("Error parsing input file: " + e.getMessage());
            System.err.println("around this context: \"" + e.getEnvironment() + "\"");
            return false;
        }

        log("Checking regularity/guardedness...");
        if (!program.isGuarded()) {
            if (allowUnguarded) {
                log("Warning: Your recursive definitions are not guarded. "
                    + "This can lead to uncomputable transitions.");
            } else {
                log("ERROR: Your recursive definitions are not guarded. "
                    + "This can lead to uncomputable transitions.");
                return false;
            }
        }
        if (!program.isRegular()) {
            if (allowUnregular) {
                log("Warning: Your recursive definitions are not regular. "
                    + "This can lead to an infinite transition system.");
            } else {
                log("ERROR: Your recursive definitions are not regular. "
                    + "This can lead to an infinite transition system.");
                return false;
            }
        }


        log("Evaluating...");
        final EvaluationMonitor monitor = new EvalMonitor(false);
        if (!program.evaluate(evaluator, monitor)) {
            System.err.println("Exiting due to a severe error.");
            System.exit(-1);
        }

        /*
        log("Counting...");
        int stateCount = StateNumerator.numerateStates(program.getExpression()).size();
        int transitionCount = TransitionCounter.countTransitions(program.getExpression());
        log(stateCount + " states, " + transitionCount + " Transitions.");
        */

        if (minimizeWeak) {
            log("Minimizing...");
            final EvaluationMonitor minimizationMonitor = new EvalMonitor(true);
            program.minimizeTransitions(evaluator, minimizationMonitor, false);
        } else if (minimizeStrong) {
            log("Minimizing (w.r.t. strong bisimulation)...");
            final EvaluationMonitor minimizationMonitor = new EvalMonitor(true);
            program.minimizeTransitions(evaluator, minimizationMonitor, true);
        }


        log("Exporting...");
        boolean errors = false;
        for (final Exporter exporter: exporters) {
            log("  - " + exporter.getIdentifier());
            try {
                exporter.export(program);
            } catch (final ExportException e) {
                System.err.println("Error exporting: " + e.getMessage());
                errors = true;
                // but continue with the next one
            }
        }

        log("Ready." + (errors ? " There were errors." : ""));

        // get used memory information
        /*
        final Runtime runtime = Runtime.getRuntime();
        final long memoryBytesUsed = runtime.totalMemory() - runtime.freeMemory();
        log("Memory used: " + (memoryBytesUsed>>>20) + " MB");
        */

        return errors;
    }

    private void checkCommandLine() {
        if (inputFile == null) {
            System.err.println("You didn't specify an input file.");
            printHelp(System.err);
            System.exit(-1);
        }
        if (evaluator == null) {
            evaluator = new ThreadBasedEvaluator();
            //evaluator = new SequentialEvaluator();
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
                    System.err.println("Expecting argument for \"--output\" switch.");
                    System.exit(-1);
                }
                parseOutputFile(args[index++]);
            } else if ("--policy".equals(arg)) {
                if (index == args.length) {
                    System.err.println("Expecting argument for \"--policy\" switch.");
                    System.exit(-1);
                }
                try {
                    setPolicy(Integer.valueOf(args[index++]));
                } catch (final NumberFormatException e) {
                    System.err.println("Integer expected after \"--policy\" switch.");
                    System.exit(-1);
                }
            } else if ("--minimize".equals(arg) || "--minimizeWeak".equals(arg)) {
                minimizeWeak = true;
            } else if ("--minimizeStrong".equals(arg)) {
                minimizeStrong = true;
            } else if (arg.length() >= 2 && arg.charAt(0) == '-' && arg.charAt(1) != '-') {
                for (int i = 1; i < arg.length(); ++i) {
                    final char c = arg.charAt(i);
                    switch (c) {
                    case 'h':
                        printHelp(System.out);
                        System.exit(0);
                        break;

                    case 'm':
                        minimizeWeak = true;
                        break;

                    case 'M':
                        minimizeStrong = true;
                        break;

                    case 'o':
                        if (i < arg.length() - 1 || index == args.length) {
                            System.err.println("Expecting argument for \"-o\" switch.");
                            System.exit(-1);
                        }
                        parseOutputFile(args[index++]);
                        break;

                    case 'p':
                        if (i < arg.length() - 1 || index == args.length) {
                            System.err.println("Expecting argument for \"-p\" switch.");
                            System.exit(-1);
                        }
                        try {
                            setPolicy(Integer.valueOf(args[index++]));
                        } catch (final NumberFormatException e) {
                            System.err.println("Integer expected after \"-p\" switch.");
                            System.exit(-1);
                        }
                        break;

                    default:
                        System.err.println("Illegal switch: \"" + c + "\"");
                        printHelp(System.err);
                        break;
                    }
                }
            } else if (arg.length()> 0 && !arg.startsWith("-") && inputFile == null) {
                inputFile = new File(arg);
            } else {
                System.err.println("Illegal parameter: \"" + arg + "\"");
                printHelp(System.err);
                System.exit(-1);
            }
        }
    }

    private void setPolicy(int policy) {
        if (policy == 0)
            evaluator = new ParallelEvaluator();
        else if (policy == 1)
            evaluator = new SequentialEvaluator();
        else if (policy < 0)
            evaluator = new ParallelEvaluator(-policy);
        else
            evaluator = new ThreadBasedEvaluator(policy);
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
            System.err.println("Please specify a valid filename as output file.");
            System.exit(-1);
        }
        if ("aisee".equalsIgnoreCase(format) || "gdl".equalsIgnoreCase(format)) {
            exporters.add(new AiSeeGraphExporter(new File(filename)));
        } else if ("etmcc".equalsIgnoreCase(format) || "tra".equalsIgnoreCase(format)) {
            exporters.add(new ETMCCExporter(new File(filename)));
        } else if ("integrationtest".equalsIgnoreCase(format) || "junit".equalsIgnoreCase(format)) {
            exporters.add(new IntegrationtestExporter(new File(filename)));
        } else if ("graphviz".equalsIgnoreCase(format) || "dot".equalsIgnoreCase(format)) {
            exporters.add(new GraphVizExporter(new File(filename)));
        } else if ("bcg".equalsIgnoreCase(format)) {
            try {
                exporters.add(new BCGExporter(new File(filename)));
            } catch (final ExportException e) {
                System.err.println("Error initializing exporter for '"
                    + filename + "': " + e.getMessage());
                System.exit(-1);
            }
        } else {
            System.err.println("Unknown format: \"" + format + "\"");
            System.exit(-1);
        }
    }

    private void printHelp(PrintStream out) {
        out.println("usage: java " + getClass().getName() + " <parameter> <input file>");
        out.println("  where <parameter> can be:");
        out.println();
        out.println("  -h, --help");
        out.println("     shows this help");
        out.println();
        out.println("  -m, --minimize");
        out.println("     minimize the graph after evaluation w.r.t. weak bisimulation");
        out.println();
        out.println("  -M, --minimizeStrong");
        out.println("     minimize the graph after evaluation w.r.t. strong bisimulation");
        out.println();
        out.println("  -o, --output=<format>:<filename>.<extension>");
        out.println("     sets the output file. This parameter can occure several times to several output files.");
        out.println("     If the format is omitted, it is assumed to be the same as the extension.");
        out.println("     Currently the following formats are accepted:");
        out.println("       - tra (for ETMCC)");
        out.println("       - gdl (for aiSee)");
        out.println("       - dot (for GraphViz)");
        out.println();
        out.println("  -p, --policy=<integer>");
        out.println("     sets the number of threads used to evaluate the ccs expression.");
        out.println("     There are some special numbers:");
        out.println("     0 means: <nr of available processors>+1");
        out.println("     1 means: evaluate sequentially (this is sometimes faster than parallel evaluation of a dual-core system)");
        out.println("     any other number means: take that much threads for parallel evaluation.");
        out.println();
    }

    public static void log(String output) {
        final long newTime = System.nanoTime();
        if (startTime == 0)
            startTime = newTime;

        final long diff = newTime - startTime;

        System.out.format((Locale)null, "[%7.3f] %s%n", 1e-9 * diff, output);
    }

    private static class EvalMonitor implements EvaluationMonitor {

        private static final int EVALUATION_INTERVAL = 10000;
        private static final int MINIMIZATION_INTERVAL = 1000000;
        private int transitions = 0;
        private int states = 0;
        private final boolean isMinimization;
        private final int showInterval;

        public EvalMonitor(boolean isMinimization) {
            this.isMinimization = isMinimization;
            this.showInterval = isMinimization ? MINIMIZATION_INTERVAL : EVALUATION_INTERVAL;
        }

        public synchronized void newTransitions(int size) {
            transitions += size;
        }

        public synchronized void newState() {
            ++states;
            if (states % showInterval == 0)
                log(states + " states, " + transitions + " transitions so far...");
        }

        public void ready() {
            log((isMinimization ? "Minimized " : "Evaluated ") + states + " states and " + transitions + " transitions.");
        }

        public void error(String errorString) {
            log("An error occured during " + (isMinimization ? "minimization: " : "evaluation: ") + errorString);
        }

        public synchronized void newState(int numTransitions) {
            newTransitions(numTransitions);
            newState();
        }

    }

}
