public class RackAwareReplicaSelector implements ReplicaSelector {

    @Override
    public Optional<ReplicaView> select(TopicPartition topicPartition,
                                        ClientMetadata clientMetadata,
                                        PartitionView partitionView) {
        // if clientMetadata contains rackId
            // find replicas on the same rack as the client
            // if no replicas are found, return the leader
            // if the partition leader is among them, return the leader
            // otherwise, return the most caught-up replica
        // else
            // return the leader
    }
}
