/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.item.crosstab.internal.ui.editors.action;

import org.eclipse.birt.report.designer.ui.newelement.DesignElementFactory;
import org.eclipse.birt.report.designer.ui.util.UIUtil;
import org.eclipse.birt.report.designer.util.DEUtil;
import org.eclipse.birt.report.item.crosstab.core.ICrosstabConstants;
import org.eclipse.birt.report.item.crosstab.core.de.AggregationCellHandle;
import org.eclipse.birt.report.item.crosstab.core.de.ComputedMeasureViewHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabCellHandle;
import org.eclipse.birt.report.item.crosstab.core.de.CrosstabReportItemHandle;
import org.eclipse.birt.report.item.crosstab.core.de.MeasureViewHandle;
import org.eclipse.birt.report.item.crosstab.internal.ui.dialogs.AddComputedSummaryDialog;
import org.eclipse.birt.report.item.crosstab.internal.ui.editors.model.CrosstabAdaptUtil;
import org.eclipse.birt.report.item.crosstab.internal.ui.util.CrosstabUIHelper;
import org.eclipse.birt.report.item.crosstab.ui.i18n.Messages;
import org.eclipse.birt.report.model.api.ComputedColumnHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.Expression;
import org.eclipse.birt.report.model.api.ExtendedItemHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.activity.SemanticException;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;
import org.eclipse.birt.report.model.api.metadata.DimensionValue;
import org.eclipse.birt.report.model.api.util.DimensionUtil;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * 
 */

public class AddComputedMeasureAction extends AbstractCrosstabAction
{
	private static final double DEFAULT_COLUMN_WIDTH = 1.0;
	private MeasureViewHandle measureViewHandle;
	
	/** action ID */
	public static final String ID = "org.eclipse.birt.report.item.crosstab.internal.ui.editors.action.AddComputedMeasureAction"; //$NON-NLS-1$

	/**
	 * Trans name
	 */
	// private static final String NAME = "Add measure handle";
	private static final String NAME = Messages.getString( "AddComputedMesureHandleAction.DisplayName" );//$NON-NLS-1$
	private static final String ACTION_MSG_MERGE = Messages.getString( "AddComputedMesureHandleAction.TransName" );//$NON-NLS-1$
	
	public AddComputedMeasureAction( DesignElementHandle handle )
	{
		super( handle );
		// TODO Auto-generated constructor stub
		setId( ID );
		setText( NAME );
		ExtendedItemHandle extendedHandle = CrosstabAdaptUtil.getExtendedItemHandle( handle );
		setHandle( extendedHandle );
		measureViewHandle = CrosstabAdaptUtil.getMeasureViewHandle( extendedHandle );

		Image image = CrosstabUIHelper.getImage( CrosstabUIHelper.ADD_DERIVED_MEASURE );
		setImageDescriptor( ImageDescriptor.createFromImage( image ) );
	}
	
	public boolean isEnabled( )
	{
		return !DEUtil.isReferenceElement( measureViewHandle.getCrosstabHandle( ) );
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run( )
	{
		transStar( NAME );
		
		CrosstabReportItemHandle reportHandle = measureViewHandle.getCrosstab( );
		AddComputedSummaryDialog computedSummaryDialog = new AddComputedSummaryDialog(UIUtil.getDefaultShell( ), reportHandle);
		if(computedSummaryDialog.open( ) == Dialog.OK)
		{
			// do adding operation
			String measureName = computedSummaryDialog.getName( );
			Expression expression = computedSummaryDialog.getExpression( );
			String dataType = computedSummaryDialog.getDataType( );
			
			int index = reportHandle.getAllMeasures().indexOf( measureViewHandle ) + 1;
			
			try
			{
				ComputedMeasureViewHandle computedMeasure = reportHandle.insertComputedMeasure( measureName, index );
				computedMeasure.addHeader( );

//				LabelHandle labelHandle = DesignElementFactory.getInstance( )
//						.newLabel( null );
//				labelHandle.setText( measureName );
//				computedMeasure.getHeader( ).addContent( labelHandle );
				
				ExtendedItemHandle crosstabModelHandle = (ExtendedItemHandle) reportHandle.getModelHandle( );
				ComputedColumn bindingColumn = StructureFactory.newComputedColumn( crosstabModelHandle,
						measureName );				
				ComputedColumnHandle bindingHandle = crosstabModelHandle.addColumnBinding( bindingColumn,
						false );
				bindingHandle.setExpressionProperty( ComputedColumn.EXPRESSION_MEMBER,
						expression );
				bindingHandle.setDataType( dataType );
				
				DataItemHandle dataHandle = DesignElementFactory.getInstance( )
				.newDataItem( measureName );
				CrosstabAdaptUtil.formatDataItem( computedMeasure.getCubeMeasure( ), dataHandle );
				dataHandle.setResultSetColumn( bindingHandle.getName( ) );
		
				AggregationCellHandle cell = computedMeasure.getCell( );
				
				//There must a set a value to the column
				if (ICrosstabConstants.MEASURE_DIRECTION_HORIZONTAL.equals( reportHandle.getMeasureDirection( ) ))
				{
					CrosstabCellHandle cellHandle = computedMeasure.getHeader( );
					if (cellHandle == null)
					{
						cellHandle = cell;
					}
					String defaultUnit = reportHandle.getModelHandle( ).getModuleHandle( ).getDefaultUnits( );
					DimensionValue dimensionValue = DimensionUtil.convertTo( DEFAULT_COLUMN_WIDTH, DesignChoiceConstants.UNITS_IN, defaultUnit );
					reportHandle.setColumnWidth( cellHandle,
							dimensionValue );
				}
				cell.addContent( dataHandle );

			}
			catch ( SemanticException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				rollBack();
			}
		}
		transEnd();
	}

}
