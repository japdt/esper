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
package com.espertech.esper.common.internal.epl.agg.method.count;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMemberCol;
import com.espertech.esper.common.internal.bytecodemodel.core.CodegenCtor;
import com.espertech.esper.common.internal.epl.agg.core.AggregationPortableValidation;
import com.espertech.esper.common.internal.epl.agg.method.core.AggregationFactoryMethodBase;
import com.espertech.esper.common.internal.epl.agg.method.core.AggregatorMethod;
import com.espertech.esper.common.internal.epl.expression.agg.base.ExprAggregateNodeBase;
import com.espertech.esper.common.internal.epl.expression.agg.method.ExprCountNode;
import com.espertech.esper.common.internal.epl.expression.agg.method.ExprMethodAggUtil;
import com.espertech.esper.common.internal.epl.expression.core.*;

public class AggregationFactoryMethodCount extends AggregationFactoryMethodBase {
    protected final ExprCountNode parent;
    protected final boolean ignoreNulls;
    protected final Class countedValueType;

    private AggregatorCount aggregator;

    public AggregationFactoryMethodCount(ExprCountNode parent, boolean ignoreNulls, Class countedValueType) {
        this.parent = parent;
        this.ignoreNulls = ignoreNulls;
        this.countedValueType = countedValueType;
    }

    public Class getResultType() {
        return Long.class;
    }

    public void initMethodForge(int col, CodegenCtor rowCtor, CodegenMemberCol membersColumnized, CodegenClassScope classScope) {
        Class distinctType = !parent.isDistinct() ? null : countedValueType;
        aggregator = new AggregatorCount(this, col, rowCtor, membersColumnized, classScope, distinctType, parent.isHasFilter(), parent.getOptionalFilter(), false);
    }

    public AggregatorMethod getAggregator() {
        return aggregator;
    }

    public ExprForge[] getMethodAggregationForge(boolean join, EventType[] typesPerStream) throws ExprValidationException {
        return getMethodAggregationEvaluatorCountByForge(parent.getPositionalParams(), join, typesPerStream);
    }

    private static ExprForge[] getMethodAggregationEvaluatorCountByForge(ExprNode[] childNodes, boolean join, EventType[] typesPerStream)
            throws ExprValidationException {
        if (childNodes[0] instanceof ExprWildcard && childNodes.length == 2) {
            return ExprMethodAggUtil.getDefaultForges(new ExprNode[]{childNodes[1]}, join, typesPerStream);
        }
        if (childNodes[0] instanceof ExprWildcard && childNodes.length == 1) {
            return ExprNodeUtilityQuery.EMPTY_FORGE_ARRAY;
        }
        return ExprMethodAggUtil.getDefaultForges(childNodes, join, typesPerStream);
    }

    public ExprAggregateNodeBase getAggregationExpression() {
        return parent;
    }

    public AggregationPortableValidation getAggregationPortableValidation() {
        return new AggregationPortableValidationCount(parent.isDistinct(), false, parent.isDistinct(), countedValueType, ignoreNulls);
    }
}