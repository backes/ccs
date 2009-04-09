package de.unisb.cs.depend.ccs_sem.plugin.views.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import de.unisb.cs.depend.ccs_sem.evaluators.Evaluator;
import de.unisb.cs.depend.ccs_sem.evaluators.SequentialEvaluator;
import de.unisb.cs.depend.ccs_sem.parser.ParsingResult;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSDocument;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.editors.IParsingListener;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
import de.unisb.cs.depend.ccs_sem.semantics.types.Transition;

public class StepByStepTraverseFrame extends Composite {

    private final IParsingListener parsingListener = new IParsingListener() {

        public void parsingDone(final IDocument document, final ParseStatus result) {        	
            if (isDisposed()) {
                // oops, we are disposed...
                if (document instanceof CCSDocument) {
                    ((CCSDocument)document).removeParsingListener(this);
                }
                return;
            }
            
            Runnable runnable = new Runnable() {
                public void run() {
                    synchronized (StepByStepTraverseFrame.this) {
                        if (activeEditor == null || activeEditor.getDocument() != document
                                || isDisposed())
                            return;

                        ParsingResult parsingResult = result.getParsingResult();
                        Program program = result.getParsedProgram();
                        if (result.getSeverity() != IStatus.OK
                                || parsingResult == null
                                || parsingResult.hasParsingErrors()
                                || program == null) {
                            tree.setEnabled(false);
                            tree.setItemCount(0);
                            tree.clearAll(true);
                        } else {
                            Expression newExpr = program.getExpression();
                            
                            currentExpression = newExpr;
                            if (evaluateProtected(newExpr)) {
                                tree.setEnabled(true);
                                tree.setItemCount(newExpr.getTransitions().size());
                            } else {
                                tree.setEnabled(false);
                                tree.setItemCount(1);
                            }
                            tree.clearAll(true);
                        }
                    }
                }

            };
            if (result.isSyncExec())
                getDisplay().syncExec(runnable);
            else
                getDisplay().asyncExec(runnable);
        }

    };


    protected CCSEditor activeEditor;
    protected volatile Expression currentExpression;
    protected Evaluator evaluator = new SequentialEvaluator();

    protected Tree tree;


    private final Listener treeDataListener = new Listener() {
        public void handleEvent(Event event) {
            TreeItem item = (TreeItem)event.item;
            TreeItem parentItem = item.getParentItem();
            Expression expr = parentItem == null ? currentExpression
                : (Expression)parentItem.getData();
            if (!evaluateProtected(expr)) {
                item.setText(new String[] { "--ERROR--", "--ERROR--"});
                item.setItemCount(0);
                return;
            }
            List<Transition> transitions = expr.getTransitions();
            if (!(transitions instanceof ArrayList))
                transitions = new ArrayList<Transition>(transitions);
            Collections.sort(transitions, new Comparator<Transition>() {
                public int compare(Transition o1, Transition o2) {
                    return o1.getAction().compareTo(o2.getAction());
                }
            });
            int index = parentItem == null ? tree.indexOf(item) : parentItem.indexOf(item);
            if (index < 0 || index >= transitions.size()) {
                item.setText(new String[] { "--", "--" });
                item.setItemCount(0);
            } else {
                Transition trans = transitions.get(index);
                Expression target = trans.getTarget();
                if (evaluateProtected(target)) {
                    item.setText(new String[] { trans.getAction().toString(), target.toString() });
                    item.setData(target);
                    item.setItemCount(target.getTransitions().size());
                } else {
                    item.setText(new String[] { "--ERROR--", "--ERROR--"});
                    item.setItemCount(0);
                }
            }
        }
    };


    public StepByStepTraverseFrame(Composite parent, int style) {
        super(parent, style);
        createComponent();
    }

    public StepByStepTraverseFrame(Composite parent, int style, CCSEditor editor) {
        this(parent, style);
        changeEditor(editor);
    }

    private void createComponent() {
        setLayout(new FillLayout(SWT.VERTICAL));

        int parentWidth = getParent().getClientArea().width;
        parentWidth = Math.max(600, parentWidth);
        tree = new Tree(this, SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL | SWT.FULL_SELECTION);
        tree.setLinesVisible(true);
        final TreeColumn col1 = new TreeColumn(tree, SWT.LEFT);
        col1.setText("Action");
        col1.setWidth(parentWidth*3/9);
        final TreeColumn col2 = new TreeColumn(tree, SWT.LEFT);
        col2.setText("Target");
        col2.setWidth(parentWidth*5/9);
        tree.setHeaderVisible(true);

        tree.addListener(SWT.SetData, treeDataListener);

        final Menu popupMenu = new Menu(tree);
        final MenuItem item = new MenuItem(popupMenu, SWT.NONE);
        item.setText("Copy target expression to clipboard");
        popupMenu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown(MenuEvent e) {
                final TreeItem[] selection = tree.getSelection();
                item.setEnabled(selection.length == 1);
                if (selection.length == 1)
                    item.setData(selection[0]);
            }
        });
        item.addSelectionListener(new SelectionAdapter(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!item.isEnabled() && !(item.getData() instanceof TreeItem))
                    return;
                final TreeItem selection = (TreeItem)item.getData();
                final String text = selection.getText(1);
                if (text == null || text.length() == 0)
                    return;
                final Clipboard clipboard = new Clipboard(getDisplay());
                final TextTransfer textTransfer = TextTransfer.getInstance();
                clipboard.setContents(new Object[] { text },
                    new Transfer[] { textTransfer });
            }
        });
        tree.setMenu(popupMenu);
    }

    private synchronized void changeEditor(CCSEditor editor) {
        if (activeEditor != null) {
            final IDocument doc = activeEditor.getDocument();
            if (doc instanceof CCSDocument) {
                ((CCSDocument)doc).removeParsingListener(parsingListener);
            }
        }
        activeEditor = editor;
        currentExpression = null;
        if (activeEditor != null) {
            final IDocument doc = activeEditor.getDocument();
            if (doc instanceof CCSDocument) {
                ((CCSDocument)doc).addParsingListener(parsingListener);
                final ParseStatus result = ((CCSDocument)doc).reparseIfNecessary();
                if (result != null) {
                    parsingListener.parsingDone(doc, result);
                }
            }
        }
    }

    @Override
    public void dispose() {
        changeEditor(null);
        super.dispose();
    }

    protected boolean evaluateProtected(final Expression newExpr) {
        final Callable<Boolean> evaluatorJob = new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return evaluator.evaluate(newExpr);
            }
        };
        final FutureTask<Boolean> task = new FutureTask<Boolean>(evaluatorJob);
        try {
            new Thread(task, "ProtectedEvaluatorForStepByStepTraverse").start();
            return task.get(5, TimeUnit.SECONDS);
        } catch (final Exception e) {
            // catch any Exception (including InterruptedException)
            task.cancel(true);
            return false;
        }
    }
}
