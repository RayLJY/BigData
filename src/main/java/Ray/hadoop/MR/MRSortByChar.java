package Ray.hadoop.MR;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.net.URI;

/**
 * Created by Ray on 17/3/30.
 * Total sort by characters type.
 */
public class MRSortByChar extends Configured implements Tool {
    private static final String REDUCES_PER_HOST = "mapreduce.sort.reducesperhost";
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
        job.setJobName("characters sorter");
        job.setJarByClass(MRSortByChar.class);

        job.setMapperClass(Mapper.class);
        job.setReducerClass(Reducer.class);

        job.setPartitionerClass(TotalOrderPartitioner.class);

        // this code is very important
        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.setInputPaths(job, input);
        FileOutputFormat.setOutputPath(job, output);

        job.setNumReduceTasks(num_reduces);

        // this code means that total descending sort
        job.setSortComparatorClass(TextDescComparator.class);

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

        String[] args = {"data/hadoop/mr/sort/s0", "res/hadoop/mr/sort/s/2"};

        if (args.length != 2) {
            throw new Exception("args: input directory output directory");
        }

        int exitCode = ToolRunner.run(new Configuration(), new MRSortByChar(), args);

        System.out.println("exit code :" + exitCode);
    }
}

/**
 * Descending sort comparator for characters (Text) type.
 * This class copy from {@link Text.Comparator}.
 */
class TextDescComparator extends WritableComparator {
    public TextDescComparator() {
        super(Text.class);
    }

    @Override
    public int compare(byte[] b1, int s1, int l1,
                       byte[] b2, int s2, int l2) {
        int n1 = WritableUtils.decodeVIntSize(b1[s1]);
        int n2 = WritableUtils.decodeVIntSize(b2[s2]);
        return compareBytes(b2, s2 + n2, l2 - n2, b1, s1 + n1, l1 - n1);
    }
}