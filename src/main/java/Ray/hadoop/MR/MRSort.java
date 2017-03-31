package Ray.hadoop.MR;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.net.URI;

/**
 * Created by ray on 17/3/30.
 * <p>
 * using MapReduce for sort
 */
public class MRSort extends Configured implements Tool {

    public static class ExchangeMap extends Mapper<LongWritable, Text, LongWritable, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] kv = value.toString().split("\t");
            if (kv.length == 2) {
                context.write(new LongWritable(Long.valueOf(kv[1])), new Text(kv[0]));
            }
        }
    }

    public static class PrintReduce extends Reducer<LongWritable, Text, LongWritable, Text> {
        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text v:values ) {
                context.write(key, v);
            }
        }
    }


    public int run(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("args: input output");
        }
        Path input = new Path(args[0].trim());
        Path output = new Path(args[1].trim());

        InputSampler.Sampler<LongWritable, Text> sampler = new InputSampler.RandomSampler<>(0.1, 10, 10);

        Configuration conf = getConf();

        Job job = Job.getInstance(conf);
        job.setJobName("sorter");
        job.setJarByClass(MRSort.class);

        job.setMapperClass(ExchangeMap.class);
        job.setReducerClass(PrintReduce.class);

        job.setNumReduceTasks(2);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);


        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);


        FileInputFormat.setInputPaths(job, input);
        FileOutputFormat.setOutputPath(job, output);


        job.setPartitionerClass(TotalOrderPartitioner.class);

        Path inputDir = FileInputFormat.getInputPaths(job)[0];
        FileSystem fs = inputDir.getFileSystem(conf);
        inputDir = inputDir.makeQualified(fs.getUri(), fs.getWorkingDirectory());
        Path partitionFile = new Path(inputDir, "_sortPartitioning");
        TotalOrderPartitioner.setPartitionFile(job.getConfiguration(), partitionFile);
        InputSampler.writePartitionFile(job, sampler);
        URI partitionUri = new URI(partitionFile.toString() +
                "#" + "_sortPartitioning");
        job.addCacheFile(partitionUri);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] s) throws Exception {

        String[] args = {"data/hadoop/mr/sort", "res/hadoop/mr/sort/2"};

        if (args.length != 2) {
            throw new Exception("args: input directory output directory");
        }

        int exitCode = ToolRunner.run(new Configuration(), new MRSort(), args);

        System.out.println("exit code :" + exitCode);
    }
}
