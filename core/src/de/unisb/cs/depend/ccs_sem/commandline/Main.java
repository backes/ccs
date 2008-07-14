package de.unisb.cs.depend.ccs_sem.commandline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import de.unisb.cs.depend.ccs_sem.evaluators.EvaluationMonitor;
import de.unisb.cs.depend.ccs_sem.evaluators.Evaluator;
import de.unisb.cs.depend.ccs_sem.evaluators.ParallelEvaluator;
import de.unisb.cs.depend.ccs_sem.evaluators.SequentialEvaluator;
import de.unisb.cs.depend.ccs_sem.evaluators.ThreadBasedEvaluator;
import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
import de.unisb.cs.depend.ccs_sem.exceptions.LexException;
import de.unisb.cs.depend.ccs_sem.exporters.AiSeeGraphExporter;
import de.unisb.cs.depend.ccs_sem.exporters.CCSExporter;
import de.unisb.cs.depend.ccs_sem.exporters.ETMCCExporter;
import de.unisb.cs.depend.ccs_sem.exporters.GraphVizExporter;
import de.unisb.cs.depend.ccs_sem.exporters.IntegrationtestExporter;
import de.unisb.cs.depend.ccs_sem.exporters.bcg.BCGExporter;
import de.unisb.cs.depend.ccs_sem.exporters.helpers.FileWrapperExporter;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.categories.Token;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.parser.IParsingProblemListener;
import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.utils.Globals;


public class Main implements IParsingProblemListener {

    private static AtomicLong startTime = new AtomicLong(0);
    private File inputFile = null;
    private Evaluator evaluator = null;
    private final List<FileWrapperExporter> exporters = new ArrayList<FileWrapperExporter>(2);
    private boolean minimizeWeak = false;
    private boolean minimizeStrong = false;
    private int[] lineOffsets = null;
    private boolean errorsOccured = false;

    // TODO add parameter for controlling this
    private static final boolean allowUnguarded = true; // false;
    private static final boolean allowUnregular = true; // false;


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
            System.err.println("File " + inputFile.getAbsolutePath()
                    + " not found.");
            return false;
        }

        log("Lexing...");
        final List<Token> tokens;
        try {
            try {
                tokens = new CCSLexer().lex(inputFileReader);
            } finally {
                try {
                    inputFileReader.close();
                } catch (final IOException e) {
                    // ignore
                }
            }
        } catch (final LexException e) {
            // for conformity, we use the method reportParsingProblem
            reportParsingProblem(new ParsingProblem(ParsingProblem.ERROR,
                    e.getMessage(), e.getPosition(), e.getPosition()));
            return false;
        }
        log("Parsing...");
        final CCSParser parser = new CCSParser();
        parser.addProblemListener(this);
        final Program program = parser.parse(tokens);

        if (errorsOccured) {
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
        for (final FileWrapperExporter exporter: exporters) {
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
        if (evaluator == null)
            evaluator = Globals.getDefaultEvaluator();
    }

    private void parseCommandLine(String[] args) {
        int index = 0;
        String arg = null;
        String next = null;
        while (index < args.length || next != null) {
            if (next == null) {
                arg = args[index++];
            } else {
                arg = next;
                next = null;
            }
            if (arg.startsWith("--")) {
                final int indexOfEquals = arg.indexOf('=');
                if (indexOfEquals != -1) {
                    next = arg.substring(indexOfEquals + 1);
                    arg = arg.substring(0, indexOfEquals);
                }
            }
            if (next == null && index < args.length)
                next = args[index++];

            if ("--help".equals(arg)) {
                printHelp(System.out);
                System.exit(0);
            } else if ("--output".equals(arg)) {
                if (next == null) {
                    System.err.println("Expecting argument for \"--output\" switch.");
                    System.exit(-1);
                }
                parseOutputFile(next);
                next = null;
            } else if ("--policy".equals(arg) || "--threads".equals(arg)) {
                if (next == null) {
                    System.err.println("Expecting argument for \"--threads\" switch.");
                    System.exit(-1);
                }
                try {
                    setPolicy(Integer.valueOf(next));
                } catch (final NumberFormatException e) {
                    System.err.println("Integer expected after \"--threads\" switch.");
                    System.exit(-1);
                }
                next = null;
            } else if ("--minimize".equals(arg) || "--minimizeWeak".equals(arg)) {
                minimizeWeak = true;
            } else if ("--minimizeStrong".equals(arg)) {
                minimizeStrong = true;
            } else if (arg.length() >= 2 && arg.charAt(0) == '-'
                    && arg.charAt(1) != '-') {
                arg = arg.substring(1);
                while (arg.length() > 0) {
                    final char c = arg.charAt(0);
                    arg = arg.substring(1);
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
                        if (arg.length() == 0) {
                            if (next == null) {
                                System.err.println("Expecting argument for \"-o\" switch.");
                                System.exit(-1);
                            }
                            arg = next;
                            next = null;
                        }
                        parseOutputFile(arg);
                        arg = "";
                        break;

                    case 'p':
                    case 't':
                        if (arg.length() == 0) {
                            if (next == null) {
                                System.err.println("Expecting argument for \"-t\" switch.");
                                System.exit(-1);
                            }
                            arg = next;
                            next = null;
                        }
                        try {
                            setPolicy(Integer.valueOf(arg));
                        } catch (final NumberFormatException e) {
                            System.err.println("Integer expected after \"-t\" switch.");
                            System.exit(-1);
                        }
                        arg = "";
                        break;

                    default:
                        System.err.println("Illegal switch: \"" + c + "\"");
                        printHelp(System.err);
                        break;
                    }
                }
            } else if (arg.length() > 0 && !arg.startsWith("-")
                    && inputFile == null) {
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
            evaluator = Globals.getDefaultEvaluator();
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
            filename = arg.substring(index + 1);
        } else {
            index = arg.indexOf('.');
            if (index == -1) {
                System.err.println("Cannot extract format from filename \""
                        + arg + "\"");
                System.exit(-1);
            }
            format = arg.substring(index + 1);
            filename = arg;
        }
        if (filename.length() == 0) {
            System.err.println("Please specify a valid filename as output file.");
            System.exit(-1);
        }
        if ("aisee".equalsIgnoreCase(format) || "gdl".equalsIgnoreCase(format)) {
            exporters.add(new FileWrapperExporter(new File(filename), new AiSeeGraphExporter()));
        } else if ("etmcc".equalsIgnoreCase(format) || "tra".equalsIgnoreCase(format)) {
            exporters.add(new FileWrapperExporter(new File(filename), new ETMCCExporter()));
        } else if ("integrationtest".equalsIgnoreCase(format) || "junit".equalsIgnoreCase(format)) {
            exporters.add(new FileWrapperExporter(new File(filename), new IntegrationtestExporter()));
        } else if ("graphviz".equalsIgnoreCase(format) || "dot".equalsIgnoreCase(format)) {
            exporters.add(new FileWrapperExporter(new File(filename), new GraphVizExporter()));
        } else if ("bcg".equalsIgnoreCase(format)) {
            try {
                exporters.add(new FileWrapperExporter(new File(filename), new BCGExporter()));
            } catch (final ExportException e) {
                System.err.println("Error initializing exporter for '"
                        + filename + "': " + e.getMessage());
                System.exit(-1);
            }
        } else if ("ccs".equalsIgnoreCase(format)) {
            exporters.add(new FileWrapperExporter(new File(filename), new CCSExporter("PROC")));
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
        out.println("       - ccs (for an auto-generated CCS file)");
        out.println("       - dot (for GraphViz)");
        out.println("       - gdl (for aiSee)");
        out.println("       - tra (for ETMCC)");
        out.println();
        out.println("  -t, --threads=<integer>");
        out.println("     sets the number of threads used to evaluate the ccs expression.");
        out.println("     There are some special numbers:");
        out.println("     0 means: <number of available processors>+1");
        out.println("     1 means: evaluate sequentially (this is sometimes faster than parallel evaluation of a dual-core system)");
        out.println("     any other number means: take that much threads for parallel evaluation.");
        out.println();
    }

    public static void log(String output) {
        final long newTime = System.nanoTime();
        long start = startTime.get();
        if (start == 0)
            start = startTime.compareAndSet(0, newTime) ? newTime : startTime.get();

        final long diff = newTime - start;

        System.out.format((Locale)null, "[%7.3f] %s%n", 1e-9 * diff, output);
    }

    public void reportParsingProblem(ParsingProblem problem) {
        if (problem.getType() == ParsingProblem.ERROR)
            errorsOccured = true;
        else if (problem.getType() == ParsingProblem.IGNORE)
            return;

        System.out.println();
        System.out.print(problem.getType() == ParsingProblem.ERROR
            ? "Error:       " : "Warning:     ");
        System.out.println(problem.getMessage());
        System.out.print("at location: ");
        if (lineOffsets == null) {
            lineOffsets = readLineOffsets(inputFile);
        }
        assert lineOffsets != null;
        final int startLine = getLineOfOffset(problem.getStartPosition());
        final int endLine = getLineOfOffset(problem.getEndPosition());
        if (startLine == -1 || endLine == -1) {
            System.out.println("(no information)");
        } else {
            int startOffset = problem.getStartPosition();
            if (startLine > 1)
                startOffset -= lineOffsets[startLine - 2];
            int endOffset = problem.getEndPosition();
            if (endLine > 1)
                endOffset -= lineOffsets[endLine - 2];
            if (startLine == endLine) {
                System.out.print("line " + startLine);
                if (startOffset == endOffset)
                    System.out.println(", character " + (startOffset + 1));
                else
                    System.out.println(", characters " + (startOffset + 1)
                            + " to " + (endOffset + 1));
                System.out.print("context:     ");
                Reader reader = null;
                try {
                    reader = new FileReader(inputFile);
                    if (startLine > 1)
                        reader.skip(lineOffsets[startLine - 2]);
                    int ch;
                    int skipChars = 13;
                    int markChars = 0;
                    int o = 0;
                    StringBuilder sb = new StringBuilder();
                    while ((ch = reader.read()) != -1 && ch != '\r'
                            && ch != '\n') {
                        if (o < startOffset)
                            skipChars += ch == '\t' ? 4 : 1;
                        else if (o <= endOffset)
                            markChars += ch == '\t' ? 4 : 1;
                        ++o;
                        if (ch == '\t')
                            sb.append("    ");
                        else
                            sb.append((char) ch);
                    }
                    System.out.println(sb.toString());
                    sb = new StringBuilder();
                    for (int i = 0; i < skipChars; ++i)
                        sb.append(' ');
                    for (int i = 0; i < markChars; ++i)
                        sb.append('^');
                    System.out.println(sb.toString());
                } catch (final IOException e) {
                    System.err.println("Error reading input file \""
                            + inputFile + "\": " + e.getMessage());
                    System.exit(-1);
                }
                if (reader != null)
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        // ignore
                    }
            } else {
                System.out.println("line " + startLine + ", character "
                        + (startOffset + 1) + " to line " + endLine
                        + ", character " + (endOffset + 1));
                System.out.print("context:     ");
                Reader reader = null;
                for (int line = startLine; line <= endLine; ++line) {
                    try {
                        if (line != startLine)
                            System.out.print("             ");
                        reader = new FileReader(inputFile);
                        if (line > 1)
                            reader.skip(lineOffsets[line - 2]);
                        int ch;
                        while ((ch = reader.read()) != -1 && ch != '\r'
                                && ch != '\n') {
                            System.out.print(ch == '\t' ? "    " : (char) ch);
                        }
                        System.out.println();
                    } catch (final IOException e) {
                        // ignore
                    }
                    if (reader != null)
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            // ignore
                        }
                }
            }
        }
        System.out.println();
    }

    private int getLineOfOffset(int startPosition) {
        if (lineOffsets == null) {
            lineOffsets = readLineOffsets(inputFile);
        }
        if (lineOffsets.length == 0)
            return 1;
        if (startPosition >= lineOffsets[lineOffsets.length - 1])
            return lineOffsets.length + 1;
        if (startPosition < lineOffsets[0])
            return 1;

        // binary search
        int left = 0;
        int right = lineOffsets.length;
        while (left < right) {
            final int mid = (left + right) / 2;
            if (lineOffsets[mid] > startPosition)
                right = mid;
            else if (lineOffsets[mid + 1] <= startPosition)
                left = mid + 1;
            else
                return mid + 2;
        }
        return -1;
    }

    @SuppressWarnings("fallthrough")
    private int[] readLineOffsets(File file) {
        PushbackReader reader = null;
        try {
            reader = new PushbackReader(new FileReader(file));
        } catch (final FileNotFoundException e) {
            System.err.println("Input file " + file.getAbsolutePath()
                    + " not found: " + e.getMessage());
            System.exit(-1);
        }
        try {
            int[] offsets = new int[16];
            int pos = 0;
            int lineNumber = 0;
            int ch;
            while ((ch = reader.read()) != -1) {
                ++pos;
                switch (ch) {
                case '\r':
                    // ignore following '\n'
                    if ((ch = reader.read()) == '\n')
                        ++pos;
                    else if (ch != -1)
                        reader.unread(ch);
                    // fallthrough
                case '\n':
                    if (lineNumber >= offsets.length) {
                        final int[] oldOffsets = offsets;
                        offsets = new int[oldOffsets.length * 2];
                        System.arraycopy(oldOffsets, 0, offsets, 0,
                                oldOffsets.length);
                    }
                    offsets[lineNumber++] = pos;
                    break;

                default:
                    break;
                }
            }
            final int[] realOffsets = new int[lineNumber];
            System.arraycopy(offsets, 0, realOffsets, 0, lineNumber);
            return realOffsets;
        } catch (final IOException e) {
            System.err.println("Error reading input file "
                    + file.getAbsolutePath() + ": " + e.getMessage());
            System.exit(-1);
        } finally {
            try {
                reader.close();
            } catch (final IOException e) {
                System.err.println("Input file " + file.getAbsolutePath()
                        + " cannot be closed: " + e.getMessage());
            }
        }
        return null;
    }

    private static class EvalMonitor implements EvaluationMonitor {

        private static final int EVALUATION_INTERVAL = 10000;
        private static final int MINIMIZATION_INTERVAL = 1000000;
        private final AtomicInteger transitions = new AtomicInteger(0);
        private final AtomicInteger states = new AtomicInteger(0);
        private final boolean isMinimization;
        private final int showInterval;

        public EvalMonitor(boolean isMinimization) {
            this.isMinimization = isMinimization;
            this.showInterval = isMinimization ? MINIMIZATION_INTERVAL
                    : EVALUATION_INTERVAL;
        }

        public void newTransitions(int count) {
            transitions.addAndGet(count);
        }

        public void newState() {
            final int totalStates = states.incrementAndGet();
            if (totalStates % showInterval == 0)
                log(totalStates + " states, " + transitions
                        + " transitions so far...");
        }

        public void ready() {
            log((isMinimization ? "Minimized " : "Evaluated ") + states
                    + " states and " + transitions + " transitions.");
        }

        public void error(String errorString) {
            log("An error occured during "
                    + (isMinimization ? "minimization: " : "evaluation: ")
                    + errorString);
        }

        public void newState(int numTransitions) {
            newTransitions(numTransitions);
            newState();
        }

    }

}
