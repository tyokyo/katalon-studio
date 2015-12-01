package com.kms.katalon.composer.components.impl.dialogs;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

import com.kms.katalon.composer.components.impl.constants.ImageConstants;
import com.kms.katalon.composer.components.impl.constants.StringConstants;
import com.kms.katalon.composer.components.impl.providers.AbstractEntityViewerFilter;
import com.kms.katalon.composer.components.impl.providers.IEntityLabelProvider;
import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.components.util.ColorUtil;

@SuppressWarnings("restriction")
public class TreeEntitySelectionDialog extends ElementTreeSelectionDialog {
	private static final String SEARCH_TEXT_DEFAULT_VALUE = StringConstants.DIA_SEARCH_TEXT_DEFAULT_VALUE;
	private static final String IMAGE_SEARCH_TOOLTIP = StringConstants.DIA_IMAGE_SEARCH_TOOLTIP;
	private static final String IMAGE_CLOSE_SEARCH_TOOLTIP = StringConstants.DIA_IMAGE_CLOSE_SEARCH_TOOLTIP;
	private static final String KEYWORD_SEARCH_ALL = StringConstants.DIA_KEYWORD_SEARCH_ALL;
	private static final Image IMAGE_SEARCH_LOCATION = ImageConstants.IMG_16_SEARCH;
	private static final Image IMAGE_CLOSE_SEARCH_LOCATION = ImageConstants.IMG_16_CLOSE_SEARCH;
	
	protected Text txtInput;
	protected CLabel lblSearch;
	protected IEntityLabelProvider labelProvider;
	protected AbstractEntityViewerFilter entityViewerFilter;
	protected boolean isSearched;

	public TreeEntitySelectionDialog(Shell parent, IEntityLabelProvider labelProvider,
			ITreeContentProvider contentProvider, AbstractEntityViewerFilter entityViewerFilter) {
		super(parent, labelProvider, contentProvider);
		this.labelProvider = labelProvider;
		this.entityViewerFilter = entityViewerFilter;
	}

	@Override
	public TreeViewer createTreeViewer(Composite parent) {
//		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		parent.setLayout(new GridLayout(1, false));

		Composite searchComposite = new Composite(parent, SWT.BORDER);
		searchComposite.setBackground(ColorUtil.getWhiteBackgroundColor());
		GridLayout glSearchComposite = new GridLayout(2, false);
		glSearchComposite.verticalSpacing = 0;
		glSearchComposite.horizontalSpacing = 0;
		glSearchComposite.marginWidth = 0;
		glSearchComposite.marginHeight = 0;
		searchComposite.setLayout(glSearchComposite);
		GridData grSearchComposite = new GridData(GridData.FILL_HORIZONTAL);
		grSearchComposite.heightHint = 24;
		searchComposite.setLayoutData(grSearchComposite);

		txtInput = new Text(searchComposite, SWT.NONE);
		txtInput.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		txtInput.setMessage(SEARCH_TEXT_DEFAULT_VALUE);
		GridData gdTxtInput = new GridData(GridData.FILL_HORIZONTAL);
		gdTxtInput.grabExcessVerticalSpace = true;
		gdTxtInput.verticalAlignment = SWT.CENTER;
		txtInput.setLayoutData(gdTxtInput);
		txtInput.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				filterSearchedText();
			}
		});
		Canvas canvasSearch = new Canvas(searchComposite, SWT.NONE);
		canvasSearch.setLayout(new FillLayout(SWT.HORIZONTAL));
		canvasSearch.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

		isSearched = false;
		lblSearch = new CLabel(canvasSearch, SWT.NONE);
		updateStatusSearchLabel();

		lblSearch.setCursor(new Cursor(Display.getCurrent(), SWT.CURSOR_HAND));
		lblSearch.addListener(SWT.MouseUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				Display.getDefault().timerExec(200, new Runnable() {
					@Override
					public void run() {
						if (isSearched) {
							isSearched = !isSearched;
							txtInput.setText(StringUtils.EMPTY);
						}
					}
				});
			}
		});

		TreeViewer treeViewer = super.createTreeViewer(parent);
		expandTreeViewerToInitialElements();
		treeViewer.addFilter(entityViewerFilter);
		treeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer.getTree().setFocus();

		return treeViewer;
	}

	protected void expandTreeViewerToInitialElements() {
		if (getInitialElementSelections() != null && !getInitialElementSelections().isEmpty()) {
			getTreeViewer().expandToLevel(getInitialElementSelections().get(0), TreeViewer.ALL_LEVELS);
		}
	}

	protected void updateStatusSearchLabel() {
		if (isSearched) {
			lblSearch.setImage(IMAGE_CLOSE_SEARCH_LOCATION);
			lblSearch.setToolTipText(IMAGE_CLOSE_SEARCH_TOOLTIP);
		} else {
			lblSearch.setImage(IMAGE_SEARCH_LOCATION);
			lblSearch.setToolTipText(IMAGE_SEARCH_TOOLTIP);
		}
	}
	
	protected String getSearchMessage() {
		try {
			return KEYWORD_SEARCH_ALL + ":" + txtInput.getText();
		} catch (Exception e) {
			LoggerSingleton.getInstance().getLogger().error(e);
			return StringUtils.EMPTY;
		}
	}

	protected void filterSearchedText() {
		final String searchString = txtInput.getText();
		Display.getDefault().timerExec(500, new Runnable() {

			@Override
			public void run() {
				try {
					if (txtInput.isDisposed()) return;
					if (searchString.equals(txtInput.getText()) && getTreeViewer().getInput() != null) {
						String broadcastMessage = getSearchMessage();
						labelProvider.setSearchString(broadcastMessage);
						entityViewerFilter.setSearchString(broadcastMessage);
						getTreeViewer().refresh();
						if (searchString != null && !searchString.isEmpty()) {
							isSearched = true;
							getTreeViewer().expandAll();
						} else {
							isSearched = false;
							getTreeViewer().collapseAll();
						}
						updateStatusSearchLabel();
					}
				} catch (Exception e) {
					LoggerSingleton.getInstance().getLogger().error(e);
				}
			}
		});
	}
}
