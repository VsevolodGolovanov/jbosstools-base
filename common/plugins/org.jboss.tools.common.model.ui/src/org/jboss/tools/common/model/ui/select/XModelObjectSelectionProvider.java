/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.common.model.ui.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.jboss.tools.common.model.XModelObject;

public class XModelObjectSelectionProvider implements ISelectionProvider, ISelectionChangedListener {
	private List<ISelectionChangedListener> listeners;
	private ISelectionProvider host = null;
	private Map<String,ISelectionProvider> hostsMap = new HashMap<String,ISelectionProvider>();
	private ISelectionProvider[] hosts = new ISelectionProvider[0];
	boolean isFiringSelection = false;
	
	public XModelObjectSelectionProvider() {
		listeners = new ArrayList<ISelectionChangedListener>();
	}
	
	public boolean isFiringSelection() {
		return isFiringSelection;
	}

	public void dispose() {
		setHost(null);
		listeners.clear();
		hosts = new ISelectionProvider[0];
		hostsMap.clear();
	}
	
	public void addHost(String name, ISelectionProvider host) {
		addHost(name, host, false);
	}
	public void addHost(String name, ISelectionProvider host, boolean select) {
		if(host == null) hostsMap.remove(name); else hostsMap.put(name, host);
		hosts = (ISelectionProvider[])hostsMap.values().toArray(new ISelectionProvider[0]);
		if(host != null && select) setHost(host);
	}
	
	public void setHost(ISelectionProvider host) {
		if(this.host == host) return;
		if(this.host != null) {
			this.host.removeSelectionChangedListener(this);
		}
		this.host = host;
		if(host != null)
			host.addSelectionChangedListener(this);
	}	

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	public ISelection getSelection() {
		return (host == null) ? null : convertSelectionToAdapter(host.getSelection());
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}	

	public void setSelection(ISelection selection) {
		if(host == null) return;
		if(isFiringSelection) return;
		ISelection s = convertSelectionFromAdapter(selection);
		for (int i = 0; i < hosts.length; i++) hosts[i].setSelection(s);
	}
	
	public void selectionChanged(SelectionChangedEvent event) {
		if(isFiringSelection) return;
		isFiringSelection = true;
		try {
			if (!listeners.isEmpty()) {
				SelectionChangedEvent newEvent = new SelectionChangedEvent(this, convertSelectionToAdapter(event.getSelection()));
				Iterator<ISelectionChangedListener> iterator = listeners.iterator();
				while (iterator.hasNext())
					iterator.next().selectionChanged(newEvent);
			}
		} finally {
			isFiringSelection = false;
		}
	}
	
	public void postSelectionChanged(SelectionChangedEvent event) {
		if(isFiringSelection) return;
		isFiringSelection = true;
		try {
			if (!listeners.isEmpty()) {
				Iterator<ISelectionChangedListener> iterator = listeners.iterator();
				while (iterator.hasNext())
					iterator.next().selectionChanged(event);
			}
		} finally {
			isFiringSelection = false;
		}
	}
		
	private ISelection convertSelectionToAdapter(ISelection selection) {
		if(!(selection instanceof IStructuredSelection) || selection.isEmpty()) {
			return selection;
		}
		IStructuredSelection structuredSelection = (IStructuredSelection)selection;			
		List<?> objects = structuredSelection.toList();
		List<Object> adaptedObjects = new ArrayList<Object>();
		Iterator<?> iterator = objects.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object instanceof XModelObject) {
				XModelObject xmo = (XModelObject)object;
				if ("FileJAVA".equals(xmo.getModelEntity().getName())) { //$NON-NLS-1$
					Object adapter = xmo.getAdapter(ICompilationUnit.class);
					if(adapter != null) adaptedObjects.add(adapter);
				} else {
					adaptedObjects.add(xmo);
				}
			} else if(object instanceof IAdaptable) {
				adaptedObjects.add(object);			
			}
		}				
		return new StructuredSelection(adaptedObjects);
	}
		
	private ISelection convertSelectionFromAdapter(ISelection selection) {
		if(!(selection instanceof IStructuredSelection) || selection.isEmpty()) {
			return selection;
		}
		IStructuredSelection structuredSelection = (IStructuredSelection)selection;			
		List<?> objects = structuredSelection.toList();
		List<XModelObject> modelObjects = new ArrayList<XModelObject>();
		Iterator<?> iterator = objects.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (object instanceof XModelObject) {
				modelObjects.add((XModelObject)object);
			}
		}
		return new StructuredSelection(modelObjects);
	}
}
