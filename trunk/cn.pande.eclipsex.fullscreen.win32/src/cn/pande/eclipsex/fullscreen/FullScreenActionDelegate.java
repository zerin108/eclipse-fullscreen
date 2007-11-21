/*
 * $Log: FullScreenActionDelegate.java,v $
 * Revision 1.12  2007/11/15 13:43:36  pan
 * fixs
 *
 * Revision 1.11  2007/11/15 12:21:36  pan
 * add "ESC exit full screen mode"
 *
 * Revision 1.10  2007/11/15 11:45:02  pan
 * force hiding taskbar on winxp with classic style
 *
 * Revision 1.9  2007/11/13 09:05:15  pan
 * remove warning
 *
 * Revision 1.8  2007/11/13 09:04:22  pan
 * back to java1.5
 *
 * Revision 1.7  2007/11/13 08:53:35  pan
 * fix a bug(static)
 *
 * Revision 1.6  2007/11/12 23:39:27  pan
 * remove sysout
 *
 * Revision 1.5  2007/11/12 23:37:58  pan
 * first full-working version
 *
 * Revision 1.4  2007/11/12 23:01:50  pan
 * move internal code to wn32 package
 *
 * Revision 1.3  2007/11/12 20:41:26  pan
 * restore / hide menu bar
 *
 * Revision 1.2  2007/11/11 11:29:47  pan
 * support full screen for eclipse
 */
package cn.pande.eclipsex.fullscreen;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import cn.pande.eclipsex.fullscreen.win32.Win32;

public class FullScreenActionDelegate implements IWorkbenchWindowActionDelegate {

	/*
	 * These must be static!!!
	 */
	static boolean fullscreen;
	static Menu menuBar;
	static Rectangle wasBounds;
	static boolean wasMaximized;
	static IWorkbenchWindow window;
	static List<Control> controls = new ArrayList<Control>();

	public void init(IWorkbenchWindow window) {
		FullScreenActionDelegate.window = window;
	}

	public void run(IAction action) {
		setFullScreen(fullscreen = !fullscreen);
	}

	private void setFullScreen(boolean fullscreen) {
		if (window == null)
			return;

		Shell mainShell = window.getShell();
		// show/hide menubar
		if (fullscreen) {
			menuBar = mainShell.getMenuBar();
			mainShell.setMenuBar(null);
			Control[] children = mainShell.getChildren();
			for (Control child : children) {
				if (child.getClass().equals(Canvas.class))
					continue;
				if (child.getClass().equals(Composite.class))
					continue;

				child.setVisible(false);
				System.out.println(child.getClass().getName());
				controls.add(child);
			}
		} else {
			mainShell.setMenuBar(menuBar);
			for (Control control : controls) {
				control.setVisible(true);
			}
			controls.clear();
		}

		if (fullscreen) { // remember things for restore
			wasMaximized = mainShell.getMaximized();
			if (!wasMaximized) {
				wasBounds = mainShell.getBounds();
			} else {
				/*
				 * 有点小错误比闪动好！如果小错误不常出现和影响不大的话！
				 * 		大部分时间Eclipse是在Maximized的状态下
				 */
				wasBounds = null;
				/*
				 * XXX 既然总要闪动一次， 还不在Full Screen的时候闪动呢!
				 * 这要闪两次！
				 */
				//				mainShell.setMaximized(false);
				//				wasBounds = mainShell.getBounds();
				// 需要maximize之前的大小和位置
				//				try {
				//					WorkbenchWindow ww = (WorkbenchWindow) window;
				//					XMLMemento memento = XMLMemento
				//							.createWriteRoot("FullScreenHack");
				//					ww.saveState(memento);
				//					int x = memento.getInteger(IWorkbenchConstants.TAG_X);
				//					int y = memento.getInteger(IWorkbenchConstants.TAG_Y);
				//					int width = memento
				//							.getInteger(IWorkbenchConstants.TAG_WIDTH);
				//					int height = memento
				//							.getInteger(IWorkbenchConstants.TAG_HEIGHT);
				//					wasBounds = new Rectangle(x, y, width, height);
				//				} catch (Throwable e) {
				//					mainShell.setMaximized(false);
				//					wasBounds = mainShell.getBounds();
				//				}
			}
		}

		// Call native methods to make full screen
		Win32.swtSetFullScreen(mainShell, fullscreen, wasMaximized, wasBounds);

		mainShell.layout(true);
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}
}
