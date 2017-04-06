package Ray.hadoop.MR;

import Ray.hadoop.MR.io.IntPair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by Ray on 17/4/1.
 * <p>
 * This class copy from {@link org.apache.hadoop.examples.SecondarySort}.
 */
public class SecondarySortByInt extends Configured implements Tool {

    /**
     * Read two integers from each line and generate a key, value pair
     * as ((left, right), right).
     */
    public static class MapClass
            extends Mapper<LongWritable, Text, IntPair, IntWritable> {

        private final IntPair key = new IntPair();
        private final IntWritable value = new IntWritable();

        @Override
        public void map(LongWritable inKey, Text inValue,
                        Context context) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(inValue.toString());
            int left;
            int right = 0;
            if (itr.hasMoreTokens()) {
                left = Integer.parseInt(itr.nextToken());
                if (itr.hasMoreTokens()) {
                    right = Integer.parseInt(itr.nextToken());
                }
                key.set(left, right);
                value.set(right);
                context.write(key, value);
            }
        }
    }

    /**
     * A reducer class that just emits the sum of the input values.
     */
    public static class Reduce
            extends Reducer<IntPair, IntWritable, Text, IntWritable> {
        private static final Text SEPARATOR =
                new Text("------------------------------------------------");
        private final Text first = new Text();

        @Override
        public void reduce(IntPair key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            context.write(SEPARATOR, null);
            first.set(Integer.toString(key.getFirst()));
            for (IntWritable value : values) {
                context.write(first, value);
            }
        }
    }

    /**
     * Partition based on the first part of the pair.
     */
    public static class FirstPartitioner extends Partitioner<IntPair, IntWritable> {
        @Override
        public int getPartition(IntPair key, IntWritable value,
                                int numPartitions) {
            return Math.abs(key.getFirst() * 127) % numPartitions;
        }
    }

    /**
     * Compare only the first part of the pair, so that reduce is called once
     * for each value of the first part.
     */
    public static class FirstGroupingComparator implements RawComparator<IntPair> {
        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return WritableComparator.compareBytes(b1, s1, Integer.SIZE / 8,
                    b2, s2, Integer.SIZE / 8);
        }

        @Override
        public int compare(IntPair o1, IntPair o2) {
            int l = o1.getFirst();
            int r = o2.getFirst();
            return l == r ? 0 : (l < r ? -1 : 1);
        }
    }

    @Override
    public int run(String[] args) throws Exception {

        if (args.length != 2) {
            throw new Exception("args: input output");
        }
        Path input = new Path(args[0].trim());
        Path output = new Path(args[1].trim());

        Job job = Job.getInstance(getConf(), "secondary sort");
        job.setJarByClass(SecondarySortByInt.class);
        job.setMapperClass(MapClass.class);
        job.setReducerClass(Reduce.class);

        // group and partition by the first int in the pair
        job.setPartitionerClass(FirstPartitioner.class);
        job.setGroupingComparatorClass(FirstGroupingComparator.class);

        // the map output is IntPair, IntWritable
        job.setMapOutputKeyClass(IntPair.class);
        job.setMapOutputValueClass(IntWritable.class);

        // the reduce output is Text, IntWritable
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

//        job.setNumReduceTasks(2);

        FileInputFormat.setInputPaths(job, input);
        FileOutputFormat.setOutputPath(job, output);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] s) throws Exception {

        String[] args = {"data/hadoop/mr/sort/s1", "res/hadoop/mr/sort/s1/1"};

        if (args.length != 2) {
            throw new Exception("args: input directory output directory");
        }

        int exitCode = ToolRunner.run(new Configuration(), new SecondarySortByInt(), args);

        System.out.println("exit code :" + exitCode);
    }
}
