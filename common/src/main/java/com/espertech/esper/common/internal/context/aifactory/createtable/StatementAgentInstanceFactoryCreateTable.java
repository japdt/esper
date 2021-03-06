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
package com.espertech.esper.common.internal.context.aifactory.createtable;

import com.espertech.esper.common.client.EventPropertyValueGetter;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.internal.context.aifactory.core.ModuleIncidentals;
import com.espertech.esper.common.internal.context.aifactory.core.StatementAgentInstanceFactory;
import com.espertech.esper.common.internal.context.aifactory.core.StatementAgentInstanceFactoryResult;
import com.espertech.esper.common.internal.context.airegistry.AIRegistryRequirements;
import com.espertech.esper.common.internal.context.module.StatementReadyCallback;
import com.espertech.esper.common.internal.context.util.*;
import com.espertech.esper.common.internal.epl.agg.core.AggregationRowFactory;
import com.espertech.esper.common.internal.epl.table.core.Table;
import com.espertech.esper.common.internal.epl.table.core.TableInstance;
import com.espertech.esper.common.internal.epl.table.core.TableInstanceViewable;
import com.espertech.esper.common.internal.epl.table.core.TableMetadataInternalEventToPublic;
import com.espertech.esper.common.internal.serde.DataInputOutputSerdeWCollation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatementAgentInstanceFactoryCreateTable implements StatementAgentInstanceFactory, StatementReadyCallback {
    private static final Logger log = LoggerFactory.getLogger(StatementAgentInstanceFactoryCreateTable.class);

    private EventType publicEventType;
    private String tableName;
    private TableMetadataInternalEventToPublic eventToPublic;
    private AggregationRowFactory aggregationRowFactory;
    private DataInputOutputSerdeWCollation aggregationSerde;
    private EventPropertyValueGetter primaryKeyGetter;

    private Table table;

    public void setEventToPublic(TableMetadataInternalEventToPublic eventToPublic) {
        this.eventToPublic = eventToPublic;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setPublicEventType(EventType publicEventType) {
        this.publicEventType = publicEventType;
    }

    public void setAggregationRowFactory(AggregationRowFactory aggregationRowFactory) {
        this.aggregationRowFactory = aggregationRowFactory;
    }

    public void setAggregationSerde(DataInputOutputSerdeWCollation aggregationSerde) {
        this.aggregationSerde = aggregationSerde;
    }

    public void setPrimaryKeyGetter(EventPropertyValueGetter primaryKeyGetter) {
        this.primaryKeyGetter = primaryKeyGetter;
    }

    public void ready(StatementContext statementContext, ModuleIncidentals moduleIncidentals, boolean recovery) {
        table = statementContext.getTableManagementService().getTable(statementContext.getDeploymentId(), tableName);
        if (table == null) {
            throw new IllegalStateException("Table '" + tableName + "' has not be registered");
        }
        table.setStatementContextCreateTable(statementContext);
        table.setEventToPublic(eventToPublic);
        table.setAggregationRowFactory(aggregationRowFactory);
        table.setTableSerdes(statementContext.getTableManagementService().getTableSerdes(table, aggregationSerde, statementContext));
        table.setPrimaryKeyGetter(primaryKeyGetter);
        table.tableReady();
    }

    public EventType getStatementEventType() {
        return publicEventType;
    }

    public void statementCreate(StatementContext statementContext) {
    }

    public void statementDestroy(StatementContext statementContext) {
        statementContext.getTableManagementService().destroyTable(statementContext.getDeploymentId(), tableName);
    }

    public StatementAgentInstanceFactoryResult newContext(AgentInstanceContext agentInstanceContext, boolean isRecoveringResilient) {
        TableInstance tableState = agentInstanceContext.getTableManagementService().allocateTableInstance(table, agentInstanceContext);
        TableInstanceViewable finalView = new TableInstanceViewable(table, tableState);

        AgentInstanceStopCallback stop = new AgentInstanceStopCallback() {
            public void stop(AgentInstanceStopServices services) {
                TableInstance instance = table.getTableInstance(agentInstanceContext.getAgentInstanceId());
                if (instance == null) {
                    log.warn("Table instance by name '" + tableName + "' has not been found");
                } else {
                    instance.destroy();
                }
            }
        };

        return new StatementAgentInstanceFactoryCreateTableResult(finalView, stop, agentInstanceContext, tableState);
    }

    public AIRegistryRequirements getRegistryRequirements() {
        return AIRegistryRequirements.noRequirements();
    }

    public StatementAgentInstanceLock obtainAgentInstanceLock(StatementContext statementContext, int agentInstanceId) {
        return AgentInstanceUtil.newLock(statementContext);
    }
}
