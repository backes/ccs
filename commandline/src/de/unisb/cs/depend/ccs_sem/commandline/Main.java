package de.unisb.cs.depend.ccs_sem.commandline;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Locale;

import de.unisb.cs.depend.ccs_sem.evalutators.ParallelEvaluator;
import de.unisb.cs.depend.ccs_sem.evalutators.SequentialEvaluator;
import de.unisb.cs.depend.ccs_sem.exporters.ETMCCExporter;
import de.unisb.cs.depend.ccs_sem.exporters.Exporter;
import de.unisb.cs.depend.ccs_sem.lexer.CCSLexer;
import de.unisb.cs.depend.ccs_sem.lexer.tokens.Token;
import de.unisb.cs.depend.ccs_sem.parser.CCSParser;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;


public class Main {

    private static long oldTime;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO

        if (args.length != 3) {
            System.err.println("3 Argumente!");
            System.exit(-1);
        }

        final File programFile = new File(args[0]);
        final File traFile = new File(args[1]);
        final int method = Integer.valueOf(args[2]);

        final FileReader fr = new FileReader(programFile);

        time("Lexing...");
        final List<Token> tokens = new CCSLexer().lex(fr);
        time("Parsing...");
        final Program program = new CCSParser().parse(tokens);

        time("Evaluating...");
        switch (method) {
        case 0:
            program.evaluate(new ParallelEvaluator());
            break;
        case 1:
            program.evaluate(new SequentialEvaluator());
            break;
        default:
            program.evaluate(new ParallelEvaluator(method));
            break;
        }
        time("Exporting.");

        final Exporter export = new ETMCCExporter(traFile);
        export.export(program.getMainExpression());
        time("Ready.");
    }

    private static void time(String output) {
        final long newTime = System.currentTimeMillis();
        if (oldTime == 0)
            oldTime = newTime;

        final long diff = newTime - oldTime;

        System.out.format((Locale)null, "[%3.3f] %s%n", 1e-3 * diff, output);
    }

}
