/*
 * $Log: Win32.java,v $
 */
package cn.pande.eclipsex.fullscreen.win32;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.widgets.Shell;

public final class Win32 {

	static final int WS_DLGFRAME = 0x00400000;

	public static void swtSetFullScreen(Shell shell, boolean fullscreen,
			boolean wasMaximized, Rectangle wasBounds) {
		String platform = SWT.getPlatform();
		if ("win32".equals(platform)) {
			_win32SetFullScreen(shell, fullscreen, wasMaximized, wasBounds);
		}
		/*
		 * 由于SWT的结构--不同的Platform使用不同的Code Base(Library) 使得调用Internal函数的程序不能够跨系统.
		 * 比如 win32: shell.handle 而carbon下叫shell.shellHandle.
		 * 使用调用shell.handle或者shell.shellHandle难以跨平台编译!
		 * 
		 * 在Eclipse3.4的正式版出来之后，如果3.4自身没有实现全屏功能， 我在实现一个Cross Platform的全屏插件！
		 */
	}

	private static void _win32SetFullScreen(Shell shell, boolean fullscreen,
			boolean wasMaximized, Rectangle wasBounds) {
		int handle = shell.handle;
		int style = shell.getStyle();
		int stateFlags = fullscreen ? OS.SW_SHOWMAXIMIZED : OS.SW_RESTORE;
		int styleFlags = OS.GetWindowLong(handle, OS.GWL_STYLE);

		int mask = SWT.TITLE | SWT.CLOSE | SWT.MIN | SWT.MAX;
		if ((style & mask) != 0) {
			if (fullscreen) {
				styleFlags = styleFlags & ~OS.WS_CAPTION;
			} else {
				styleFlags = styleFlags | OS.WS_CAPTION;
			}
		}

		/*
		 * 如果原来的shell就是没有WS_DLGFRAME和WS_THICKFRAME
		 * 2次Action改变了shell原来的style。Eclipse IDE没关系的。
		 * 那样的shell也没必要用这个插件， 还有SWT库本身改好后，可以
		 * 使用它的代码，所以没必要在这里记录和判断。
		 */
		if (fullscreen) {
			// hide border handler
			styleFlags &= ~(WS_DLGFRAME | OS.WS_THICKFRAME);

			// set shell bounds as full screen
			if (OS.GetSystemMetrics(OS.SM_CMONITORS) < 2) {
				int width = OS.GetSystemMetrics(OS.SM_CXSCREEN);
				int height = OS.GetSystemMetrics(OS.SM_CYSCREEN);
				shell.setBounds(0, 0, width, height);
			} else {
				int pwidth = OS.GetSystemMetrics(OS.SM_CXSCREEN);
				int pheight = OS.GetSystemMetrics(OS.SM_CYSCREEN);

				int x = OS.GetSystemMetrics(OS.SM_XVIRTUALSCREEN);
				int y = OS.GetSystemMetrics(OS.SM_YVIRTUALSCREEN);
				int width = OS.GetSystemMetrics(OS.SM_CXVIRTUALSCREEN);
				int height = OS.GetSystemMetrics(OS.SM_CYVIRTUALSCREEN);
				Rectangle b = shell.getBounds();
				int x0 = b.x + b.width / 2;
				int y0 = b.y + b.height / 2;
				if (x0 < pwidth && y0 < pheight) {
					// in primary monitor
					shell.setBounds(0, 0, pwidth, pheight);
				} else {
					// in second monitor
					shell.setBounds(x + pwidth, y, width - pwidth, height);
				}
			}
		} else {
			// show border handler
			styleFlags |= (WS_DLGFRAME | OS.WS_THICKFRAME);
			// restore shell bounds
			if (wasBounds != null)
				shell.setBounds(wasBounds);
		}

		boolean visible = shell.isVisible();
		OS.SetWindowLong(handle, OS.GWL_STYLE, styleFlags);
		if (wasMaximized) {
			OS.ShowWindow(handle, OS.SW_HIDE);
			stateFlags = OS.SW_SHOWMAXIMIZED;
		}
		if (visible)
			OS.ShowWindow(handle, stateFlags);
		OS.UpdateWindow(handle);
	}
}
