package hadoop.MR;

import hadoop.MR.InputFormat.KeyLineInputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.net.URI;

/**
 * Created by Ray on 17/3/30.
 * Total sort by number type
 */
public class MRSortByNum extends Configured implements Tool {
    public static final String REDUCES_PER_HOST = "mapreduce.sort.reducesperhost";
    private Job job = null;

    public int run(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("args: input output");
        }
        Path input = new Path(args[0].trim());
        Path output = new Path(args[1].trim());

        Configuration conf = getConf();
        JobClient client = new JobClient(conf);
        ClusterStatus cluster = client.getClusterStatus();

        int num_reduces = (int) (cluster.getMaxReduceTasks() * 0.9);
        String sort_reduces = conf.get(REDUCES_PER_HOST);
        if (sort_reduces != null) num_reduces = cluster.getTaskTrackers() * Integer.parseInt(sort_reduces);
        num_reduces = num_reduces > 0 ? num_reduces : 1;

        // Probability with which a key will be chosen.
        double freq = 0.1;
        // Total number of samples to obtain from all selected
        // advice : 10% of the whole data
        int numSamples = 10000;
        // maximum number of splits to examine.
        int maxSplits = Integer.MAX_VALUE;

        InputSampler.RandomSampler sampler = new InputSampler.RandomSampler(freq, numSamples, maxSplits);

        job = Job.getInstance(conf);
        job.setJobName("number sorter");
        job.setJarByClass(MRSortByNum.class);

        job.setMapperClass(Mapper.class);
        job.setReducerClass(Reducer.class);

        job.setPartitionerClass(TotalOrderPartitioner.class);

        // this code is very important
        job.setInputFormatClass(KeyLineInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.setInputPaths(job, input);
        FileOutputFormat.setOutputPath(job, output);

        job.setNumReduceTasks(num_reduces);

        // this code means that total descending sort
        job.setSortComparatorClass(IntDescComparator.class);

        Path inputDir = FileInputFormat.getInputPaths(job)[0];
        FileSystem fs = inputDir.getFileSystem(conf);
        inputDir = inputDir.makeQualified(fs.getUri(), fs.getWorkingDirectory());
        Path partitionFile = new Path(inputDir, "_sortPartitioning");
        TotalOrderPartitioner.setPartitionFile(job.getConfiguration(), partitionFile);
        InputSampler.writePartitionFile(job, sampler);
        URI partitionUri = new URI(partitionFile.toString() + "#" + "_sortPartitioning");
        job.addCacheFile(partitionUri);

        if (fs.deleteOnExit(partitionFile)) {
            System.out.println("delete partition File : " + partitionFile.toString());
        }

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] s) throws Exception {

        String[] args = {"data/hadoop/mr/sort/s0", "res/hadoop/mr/sort/s/1"};

        if (args.length != 2) {
            throw new Exception("args: input directory output directory");
        }

        int exitCode = ToolRunner.run(new Configuration(), new MRSortByNum(), args);

        System.out.println("exit code :" + exitCode);
    }
}

/**
 * Descending sort comparator for int (number) type.
 * This class copy from {@link IntWritable.Comparator}.
 */
class IntDescComparator extends WritableComparator {
    public IntDescComparator() {
        super(IntWritable.class);
    }

    @Override
    public int compare(byte[] b1, int s1, int l1,
                       byte[] b2, int s2, int l2) {
        int thisValue = readInt(b1, s1);
        int thatValue = readInt(b2, s2);
        return (thisValue > thatValue ? -1 : (thisValue == thatValue ? 0 : 1));
    }
}