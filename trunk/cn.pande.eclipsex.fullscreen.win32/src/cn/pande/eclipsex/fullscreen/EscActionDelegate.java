/*
 * $Log: EscActionDelegate.java,v $
 */
package cn.pande.eclipsex.fullscreen;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Restore from full screen mode.
 */
public class EscActionDelegate implements IWorkbenchWindowActionDelegate {

	public void run(IAction action) {
		if (FullScreenActionDelegate.fullscreen) {
			new Action("") {
				public void run() {
					new FullScreenActionDelegate().run(this);
				}
			}.run();
		}
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
}
