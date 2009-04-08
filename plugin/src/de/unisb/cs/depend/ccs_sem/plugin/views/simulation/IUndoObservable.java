package de.unisb.cs.depend.ccs_sem.plugin.views.simulation;

public interface IUndoObservable {
	public void addUndoListener(IUndoListener listener);
	public void removeUndoListener(IUndoListener listener);
}
