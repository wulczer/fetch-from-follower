public class RackAwarePartitioner implements Partitioner<K> {

    private final String rackId;

    public RackAwarePartitioner(String rackId) {
        this.rackId = rackId;
    }

    @Override
    public int partition(String topic, K key, Cluster cluster) {
        // if the key is set, use it to calculate the partition
        // if the rackId is empty, choose a random partition

        // find all partitions led by brokers in the same rack

        // if the set is empty, choose random partition
        // otherwise, choose a random partition from the set
    }
}
