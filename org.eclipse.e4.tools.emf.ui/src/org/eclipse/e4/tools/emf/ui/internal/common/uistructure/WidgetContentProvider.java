package org.eclipse.e4.tools.emf.ui.internal.common.uistructure;

import java.util.Collection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

public class WidgetContentProvider implements ITreeContentProvider {

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	public Object[] getElements(Object inputElement) {
		return ((Collection<?>) inputElement).toArray();
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Menu) {
			Menu m = (Menu) parentElement;
			MenuItem[] items = m.getItems();
			Object[] rv = new Object[items.length];
			System.arraycopy(items, 0, rv, 0, rv.length);
			return rv;
		} else if (parentElement instanceof MenuItem) {
			MenuItem item = (MenuItem) parentElement;
			if (item.getMenu() != null) {
				MenuItem[] items = item.getMenu().getItems();
				Object[] rv = new Object[items.length];
				System.arraycopy(items, 0, rv, 0, rv.length);
				return rv;
			}
		} else if (parentElement instanceof ToolBar) {
			ToolBar toolbar = (ToolBar) parentElement;
			ToolItem[] items = toolbar.getItems();
			Object[] rv = new Object[items.length];
			System.arraycopy(items, 0, rv, 0, rv.length);
			return rv;
		} else if (parentElement instanceof CoolBar) {
			CoolBar coolbar = (CoolBar) parentElement;
			CoolItem[] items = coolbar.getItems();
			Object[] rv = new Object[items.length];
			System.arraycopy(items, 0, rv, 0, rv.length);
			return rv;
		} else if (parentElement instanceof TabItem) {
			TabItem item = (TabItem) parentElement;
			if (item.getControl() != null) {
				return new Object[] { item.getControl() };
			}
		} else if (parentElement instanceof CTabItem) {
			CTabItem item = (CTabItem) parentElement;
			if (item.getControl() != null) {
				return new Object[] { item.getControl() };
			}
		} else if (parentElement instanceof org.eclipse.e4.ui.widgets.CTabItem) {
			org.eclipse.e4.ui.widgets.CTabItem item = (org.eclipse.e4.ui.widgets.CTabItem) parentElement;
			if (item.getControl() != null) {
				return new Object[] { item.getControl() };
			}
		} else if (parentElement instanceof Tree) {
			Tree tree = (Tree) parentElement;

		} else if (parentElement instanceof Tree) {
			Table table = (Table) parentElement;

		} else if (parentElement instanceof List) {
			List list = (List) parentElement;

		} else if (parentElement instanceof Combo) {
			Combo combo = (Combo) parentElement;

		} else if (parentElement instanceof CTabFolder) {
			CTabFolder tabFolder = (CTabFolder) parentElement;
			CTabItem[] items = tabFolder.getItems();
			Object[] rv = new Object[items.length];
			System.arraycopy(items, 0, rv, 0, rv.length);
			return rv;
		} else if (parentElement instanceof org.eclipse.e4.ui.widgets.CTabFolder) {
			org.eclipse.e4.ui.widgets.CTabFolder tabFolder = (org.eclipse.e4.ui.widgets.CTabFolder) parentElement;
			org.eclipse.e4.ui.widgets.CTabItem[] items = tabFolder.getItems();
			Object[] rv = new Object[items.length];
			System.arraycopy(items, 0, rv, 0, rv.length);
			return rv;
		} else if (parentElement instanceof TabFolder) {
			TabFolder tabFolder = (TabFolder) parentElement;
			TabItem[] items = tabFolder.getItems();
			Object[] rv = new Object[items.length];
			System.arraycopy(items, 0, rv, 0, rv.length);
			return rv;
		} else if (parentElement instanceof Composite) {
			Composite comp = (Composite) parentElement;
			Control controls[] = comp.getChildren();
			Object[] rv = new Object[controls.length];
			System.arraycopy(controls, 0, rv, 0, rv.length);
			return rv;
		}

		return new Object[0];
	}

	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

}
