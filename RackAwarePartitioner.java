package com.newrelic.fetchfromfollower;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.internals.BuiltInPartitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Partition keyless data randomly, but try choosing partitions led by brokers in the same rack as the producer.
 */
public class RackAwarePartitioner implements Partitioner {

    private final String rackId;

    public RackAwarePartitioner(final String rackId) {
        this.rackId = rackId;
    }

    public int partition(final byte[] key, final int numPartitions) {
        if (key != null) {
            return BuiltInPartitioner.partitionForKey(key, numPartitions);
        }
        final int random = Utils.toPositive(ThreadLocalRandom.current().nextInt());
        return random % numPartitions;
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // if we have a key or could not determine the rack ID, use the default partitioner
        if (key != null || rackId == null || rackId.isEmpty()) {
            return partition(keyBytes, cluster.partitionCountForTopic(topic));
        }

        List<PartitionInfo> partitionInfos = cluster.availablePartitionsForTopic(topic);
        if (partitionInfos.isEmpty()) {
            partitionInfos = cluster.partitionsForTopic(topic);
        }

        // grab the partitions led by brokers in the same rack as the producer
        final List<Integer> eligiblePartitions = partitionInfos.stream()
                .filter(partitionInfo -> rackId.equals(Optional.ofNullable(partitionInfo.leader()).map(Node::rack).orElse("")))
                .map(PartitionInfo::partition)
                .toList();

        // if there are no eligible partitions, fall back to the default code path (we know at this point that the key is null)
        if (eligiblePartitions.isEmpty()) {
            return partition(null, cluster.partitionCountForTopic(topic));
        }

        // choose a random partition from the eligible ones
        final int random = Utils.toPositive(ThreadLocalRandom.current().nextInt());
        return eligiblePartitions.get(random % eligiblePartitions.size());
    }

    @Override
    public void close() {}

    @Override
    public void configure(Map<String, ?> configs) {}
}
