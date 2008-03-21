package de.unisb.cs.depend.ccs_sem.plugin.views.components;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import de.unisb.cs.depend.ccs_sem.evaluators.SequentialEvaluator;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSDocument;
import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
import de.unisb.cs.depend.ccs_sem.plugin.editors.IParsingListener;
import de.unisb.cs.depend.ccs_sem.plugin.jobs.ParseCCSProgramJob.ParseStatus;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;
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

                        if (result.getSeverity() != IStatus.OK) {
                            tree.setEnabled(false);
                            tree.setItemCount(0);
                            tree.clearAll(true);
                        } else {
                            Expression newExpr = result.getParsedProgram().getExpression();
                            if (newExpr != currentExpression) {
                                currentExpression = newExpr;
                                try {
                                    evaluator.evaluate(newExpr);
                                } catch (InterruptedException e) {
                                    // reset interruption flag
                                    Thread.currentThread().interrupt();
                                    return;
                                }
                                tree.setEnabled(true);
                                tree.setItemCount(newExpr.getTransitions().size());
                                tree.clearAll(true);
                            }
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
    protected SequentialEvaluator evaluator = new SequentialEvaluator();

    protected Tree tree;


    private final Listener treeListener = new Listener() {
        public void handleEvent(Event event) {
            TreeItem item = (TreeItem)event.item;
            TreeItem parentItem = item.getParentItem();
            Expression expr = parentItem == null ? currentExpression
                : (Expression)parentItem.getData();
            try {
                evaluator.evaluate(expr);
            } catch (InterruptedException e) {
                // reset interruption flag
                Thread.currentThread().interrupt();
                return;
            }
            List<Transition> transitions = expr.getTransitions();
            int index = parentItem == null ? tree.indexOf(item) : parentItem.indexOf(item);
            if (index < 0 || index >= transitions.size()) {
                item.setText(new String[] { "--", "--" });
                item.setItemCount(0);
            } else {
                Transition trans = transitions.get(index);
                Expression target = trans.getTarget();
                try {
                    evaluator.evaluate(target);
                } catch (InterruptedException e) {
                    // reset interruption flag
                    Thread.currentThread().interrupt();
                    return;
                }
                item.setText(new String[] { trans.getAction().getLabel(), target.toString() });
                item.setData(target);
                item.setItemCount(target.getTransitions().size());
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
        final TreeColumn col1 = new TreeColumn(tree, SWT.LEFT);
        col1.setText("Action");
        col1.setWidth(parentWidth*3/9);
        final TreeColumn col2 = new TreeColumn(tree, SWT.LEFT);
        col2.setText("Target");
        col2.setWidth(parentWidth*5/9);
        tree.setHeaderVisible(true);

        tree.addListener(SWT.SetData, treeListener);
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

}
