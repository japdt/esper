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
package com.espertech.esper.common.internal.epl.join.base;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.internal.collection.MultiKey;
import com.espertech.esper.common.internal.collection.UniformPair;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.epl.index.base.EventTableVisitor;
import com.espertech.esper.common.internal.epl.join.strategy.QueryStrategy;
import com.espertech.esper.common.internal.metrics.instrumentation.InstrumentationCommon;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implements the function to determine a join result for a all-unidirectional full-outer-join (all streams),
 * in which a single stream's events are ever only evaluated and repositories don't exist.
 */
public class JoinSetComposerAllUnidirectionalOuter implements JoinSetComposer {
    private final QueryStrategy[] queryStrategies;

    private Set<MultiKey<EventBean>> emptyResults = new LinkedHashSet<MultiKey<EventBean>>();
    private Set<MultiKey<EventBean>> newResults = new LinkedHashSet<MultiKey<EventBean>>();

    public JoinSetComposerAllUnidirectionalOuter(QueryStrategy[] queryStrategies) {
        this.queryStrategies = queryStrategies;
    }

    public boolean allowsInit() {
        return false;
    }

    public void init(EventBean[][] eventsPerStream, ExprEvaluatorContext exprEvaluatorContext) {
    }

    public void destroy() {
    }

    public UniformPair<Set<MultiKey<EventBean>>> join(EventBean[][] newDataPerStream, EventBean[][] oldDataPerStream, ExprEvaluatorContext exprEvaluatorContext) {
        InstrumentationCommon instrumentationCommon = exprEvaluatorContext.getInstrumentationProvider();
        instrumentationCommon.qJoinCompositionStreamToWin();

        newResults.clear();

        for (int i = 0; i < queryStrategies.length; i++) {
            if (newDataPerStream[i] != null) {
                instrumentationCommon.qJoinCompositionQueryStrategy(true, i, newDataPerStream[i]);
                queryStrategies[i].lookup(newDataPerStream[i], newResults, exprEvaluatorContext);
                instrumentationCommon.aJoinCompositionQueryStrategy();
            }
        }

        instrumentationCommon.aJoinCompositionStreamToWin(newResults);
        return new UniformPair<>(newResults, emptyResults);
    }

    public Set<MultiKey<EventBean>> staticJoin() {
        throw new UnsupportedOperationException("Iteration over a unidirectional join is not supported");
    }

    public void visitIndexes(EventTableVisitor visitor) {
    }
}
