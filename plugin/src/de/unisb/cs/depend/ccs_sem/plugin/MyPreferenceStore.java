package de.unisb.cs.depend.ccs_sem.plugin;

import java.util.LinkedList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import de.unisb.cs.depend.ccs_sem.parser.ParsingProblem;
import de.unisb.cs.depend.ccs_sem.plugin.utils.ISemanticDependend;
import de.unisb.cs.depend.ccs_sem.semantics.expressions.Expression;


public class MyPreferenceStore {

    private static IPreferenceStore preferenceStore;
    static {
        initializePreferenceStore();
        toNotify = new LinkedList<ISemanticDependend> ();
        
        getStore().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if( event.getProperty().equals(getTauSemanticsKey()) ) {
					if( !event.getNewValue().equals(event.getOldValue()) ) {
						Expression.setVisibleTau(getVisibleTauSemantic());
						notifySemanticDependend();
					}
				}
			}
        });
    }

    private static final String PREFERENCE_DOT_KEY = "dotExecutable";
    private static final String PREFERENCE_DEFAULT_DOT = "dot";

    private static final String PREFERENCE_UNREGULAR_ERROR_TYPE_KEY = "unregularErrorType";
    private static final String PREFERENCE_UNGUARDED_ERROR_TYPE_KEY = "unguardedErrorType";
    
    private static final String PREFERENCE_MAX_GRAPH_SIZE = "maxGraphSize";
    
    private static final String PREFERENCE_TAU_SEMANTICS = "tauSemantics";

    private MyPreferenceStore() {
        // this private constructor is never called
        assert false;
    }

    private static void initializePreferenceStore() {
        preferenceStore = Activator.getDefault().getPreferenceStore();
        preferenceStore.setDefault(PREFERENCE_DOT_KEY, PREFERENCE_DEFAULT_DOT);
        preferenceStore.setDefault(PREFERENCE_UNREGULAR_ERROR_TYPE_KEY, ParsingProblem.WARNING);
        preferenceStore.setDefault(PREFERENCE_UNGUARDED_ERROR_TYPE_KEY, ParsingProblem.ERROR);
        preferenceStore.setDefault(PREFERENCE_MAX_GRAPH_SIZE, 300);
        preferenceStore.setDefault(PREFERENCE_TAU_SEMANTICS, false);
    }

    public static IPreferenceStore getStore() {
        return preferenceStore;
    }

    public static String getDotKey() {
        return PREFERENCE_DOT_KEY;
    }

    public static String getDot() {
        return getStore().getString(getDotKey());
    }

    public static String getUnregularErrorTypeKey() {
        return PREFERENCE_UNREGULAR_ERROR_TYPE_KEY;
    }

    public static int getUnregularErrorType() {
        return getStore().getInt(getUnregularErrorTypeKey());
    }

    public static String getUnguardedErrorTypeKey() {
        return PREFERENCE_UNGUARDED_ERROR_TYPE_KEY;
    }

    public static int getUnguardedErrorType() {
        return getStore().getInt(getUnguardedErrorTypeKey());
    }
    
    public static String getMaxGraphSizeKey() {
    	return PREFERENCE_MAX_GRAPH_SIZE;
    }
    
    public static int getMaxGraphSize() {
    	return getStore().getInt(getMaxGraphSizeKey());
    }
    
    public static String getTauSemanticsKey() {
    	return PREFERENCE_TAU_SEMANTICS;
    }
    
    public static boolean getVisibleTauSemantic() {
    	return getStore().getBoolean(getTauSemanticsKey());
    }
    
    public static void setVisibleTauSemantic(boolean b) {
    		getStore().setValue(getTauSemanticsKey(), b);
    }

    
    /*
     * Start Semantic observer implementation
     * (non-Javadoc)
     */
    private static LinkedList<ISemanticDependend> toNotify;
    
	public static void addSemanticObserver(ISemanticDependend semDep) {
		toNotify.addLast(semDep);
	}

	private static void notifySemanticDependend() {
		for( ISemanticDependend semDep : toNotify ) {
			semDep.updateSemantic();
		}
	}

	public static void removeSemanticObserver(ISemanticDependend semDep) {
		toNotify.remove(semDep);
	}
}