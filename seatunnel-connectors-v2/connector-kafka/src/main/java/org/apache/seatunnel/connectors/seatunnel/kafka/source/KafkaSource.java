/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.connectors.seatunnel.kafka.source;

import org.apache.seatunnel.api.common.JobContext;
import org.apache.seatunnel.api.configuration.ReadonlyConfig;
import org.apache.seatunnel.api.source.Boundedness;
import org.apache.seatunnel.api.source.SeaTunnelSource;
import org.apache.seatunnel.api.source.SourceReader;
import org.apache.seatunnel.api.source.SourceSplitEnumerator;
import org.apache.seatunnel.api.source.SupportParallelism;
import org.apache.seatunnel.api.table.catalog.CatalogTable;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.connectors.seatunnel.kafka.state.KafkaSourceState;

import java.util.List;
import java.util.stream.Collectors;

public class KafkaSource
        implements SeaTunnelSource<SeaTunnelRow, KafkaSourceSplit, KafkaSourceState>,
                SupportParallelism {

    private JobContext jobContext;

    private final KafkaSourceConfig kafkaSourceConfig;

    public KafkaSource(ReadonlyConfig readonlyConfig) {
        kafkaSourceConfig = new KafkaSourceConfig(readonlyConfig);
    }

    @Override
    public Boundedness getBoundedness() {
        return JobMode.BATCH.equals(jobContext.getJobMode())
                ? Boundedness.BOUNDED
                : Boundedness.UNBOUNDED;
    }

    @Override
    public String getPluginName() {
        return org.apache.seatunnel.connectors.seatunnel.kafka.config.Config.CONNECTOR_IDENTITY;
    }

    @Override
    public List<CatalogTable> getProducedCatalogTables() {
        return kafkaSourceConfig.getMapMetadata().values().stream()
                .map(ConsumerMetadata::getCatalogTable)
                .collect(Collectors.toList());
    }

    @Override
    public SourceReader<SeaTunnelRow, KafkaSourceSplit> createReader(
            SourceReader.Context readerContext) {
        return new KafkaSourceReader(
                kafkaSourceConfig,
                readerContext,
                kafkaSourceConfig.getMessageFormatErrorHandleWay());
    }

    @Override
    public SourceSplitEnumerator<KafkaSourceSplit, KafkaSourceState> createEnumerator(
            SourceSplitEnumerator.Context<KafkaSourceSplit> enumeratorContext) {
        return new KafkaSourceSplitEnumerator(kafkaSourceConfig, enumeratorContext, null);
    }

    @Override
    public SourceSplitEnumerator<KafkaSourceSplit, KafkaSourceState> restoreEnumerator(
            SourceSplitEnumerator.Context<KafkaSourceSplit> enumeratorContext,
            KafkaSourceState checkpointState) {
        return new KafkaSourceSplitEnumerator(
                kafkaSourceConfig, enumeratorContext, checkpointState);
    }

    @Override
    public void setJobContext(JobContext jobContext) {
        this.jobContext = jobContext;
    }
}
