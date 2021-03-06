/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.internal.epl.table.core;

import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.event.core.ObjectArrayBackedEventBean;

import java.util.Set;

public interface TableInstanceGrouped extends TableInstance {
    ObjectArrayBackedEventBean getRowForGroupKey(Object groupKey);

    ObjectArrayBackedEventBean getCreateRowIntoTable(Object groupByKey, ExprEvaluatorContext exprEvaluatorContext);

    void handleRowUpdated(ObjectArrayBackedEventBean row);

    Set<Object> getGroupKeys();

    Table getTable();
}
