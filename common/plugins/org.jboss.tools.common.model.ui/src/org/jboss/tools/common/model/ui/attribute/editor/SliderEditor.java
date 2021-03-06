/*******************************************************************************
  * Copyright (c) 2007-2008 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
package org.jboss.tools.common.model.ui.attribute.editor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.jboss.tools.common.model.ui.widgets.IWidgetSettings;

public class SliderEditor extends ValueEditor {

	public SliderEditor() {}
	
	public SliderEditor(IWidgetSettings settings) {
		super(settings);
		lineEditor = true;
	}
	
	public void dispose() {
		super.dispose();
		if (cellEditor != null) {
			cellEditor.dispose();
		}
		cellEditor = null;
		if (fieldEditor != null) {
			fieldEditor.dispose();
		}
		fieldEditor = null;
	}
	
	@Override
	protected CellEditor createCellEditor(Composite parent) {
		cellEditor = new SliderCellEditorEx();
		return cellEditor;
	}

	@Override
	protected ExtendedFieldEditor createFieldEditor(Composite parent) {
		fieldEditor = new SliderFieldEditor();
		fieldEditor.setLabelText(getLabelText());
		return fieldEditor;
	}

}
