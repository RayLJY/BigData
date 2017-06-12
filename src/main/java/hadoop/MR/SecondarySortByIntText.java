
package hadoop.MR;

import hadoop.MR.io.IntTextPair;
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
 * This class be used for secondary sort that is grouped by Int type
 * and sorted by character type.
 */
public class SecondarySortByIntText extends Configured implements Tool {

    /**
     * Read one integer and one character from each line and generate a key, value pair
     * as ((left, right), right).
     */
    public static class MapClass
            extends Mapper<LongWritable, Text, IntTextPair, Text> {

        private final IntTextPair key = new IntTextPair();
        private final Text value = new Text();

        @Override
        public void map(LongWritable inKey, Text inValue,
                        Context context) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(inValue.toString());
            int left;
            String right = null;
            if (itr.hasMoreTokens()) {
                left = Integer.parseInt(itr.nextToken());
                if (itr.hasMoreTokens()) {
                    right = itr.nextToken();
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
            extends Reducer<IntTextPair, Text, IntWritable, Text> {
        private static final Text SEPARATOR =
                new Text("------------------------------------------------");

        @Override
        public void reduce(IntTextPair key, Iterable<Text> values,
                           Context context) throws IOException, InterruptedException {
            context.write(null, SEPARATOR);
            for (Text value : values) {
                context.write(key.getFirst(), value);
            }
        }
    }

    /**
     * Partition based on the first part of the pair.
     */
    public static class FirstPartitioner extends Partitioner<IntTextPair, Text> {
        @Override
        public int getPartition(IntTextPair key, Text value,
                                int numPartitions) {
            return Math.abs(key.getFirst().get() * 127) % numPartitions;
        }
    }

    /**
     * Compare only the first part of the pair, so that reduce is called once
     * for each value of the first part.
     */
    public static class FirstGroupingComparator implements RawComparator<IntTextPair> {
        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return WritableComparator.compareBytes(b1, s1, Integer.SIZE / 8,
                    b2, s2, Integer.SIZE / 8);
        }

        @Override
        public int compare(IntTextPair o1, IntTextPair o2) {
            int l = o1.getFirst().get();
            int r = o2.getFirst().get();
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
        job.setJarByClass(SecondarySortByIntText.class);
        job.setMapperClass(MapClass.class);
        job.setReducerClass(Reduce.class);

        // group and partition by the first int in the pair
        job.setPartitionerClass(FirstPartitioner.class);
        job.setGroupingComparatorClass(FirstGroupingComparator.class);

        // the map output is IntTextPair, IntWritable
        job.setMapOutputKeyClass(IntTextPair.class);
        job.setMapOutputValueClass(Text.class);

//        job.setNumReduceTasks(3);

        // the reduce output is Text, IntWritable
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.setInputPaths(job, input);
        FileOutputFormat.setOutputPath(job, output);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] s) throws Exception {

        String[] args = {"data/hadoop/mr/sort/s2", "res/hadoop/mr/sort/s2/1"};

        if (args.length != 2) {
            throw new Exception("args: input directory output directory");
        }

        int exitCode = ToolRunner.run(new Configuration(), new SecondarySortByIntText(), args);

        System.out.println("exit code :" + exitCode);
    }
}
