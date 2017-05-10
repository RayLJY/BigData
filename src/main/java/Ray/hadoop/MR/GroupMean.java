package Ray.hadoop.MR;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ByteWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by Ray on 17/4/8.
 * This Map/Reduce program can be used to calculate the mean in each group.
 * Use the first character of the word to group word into 26 groups.
 * Calculate the mean of word of length for each group.
 */
public class GroupMean extends Configured implements Tool {

    public static class GroupingWordsMapper extends Mapper<Object, Text, ByteWritable, IntWritable> {

        private ByteWritable initial = new ByteWritable();
        private IntWritable wordLen = new IntWritable();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                String string = itr.nextToken();
                int f = string.codePointAt(0);
                f = f > 96 ? f - 32 : f; // to uppercase
                if ((f > 64 && f < 91)) {
                    wordLen.set(string.length());
                    initial.set((byte) f);
                    context.write(initial, wordLen);
                }
            }
        }
    }

    public static class GroupedMeanReducer extends
            Reducer<ByteWritable, IntWritable, ByteWritable, DoubleWritable> {

        private DoubleWritable mean = new DoubleWritable();

        public void reduce(ByteWritable key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {

            long theSum = 0L;
            long count = 0L;

            for (IntWritable val : values) {
                theSum += val.get();
                count++;
            }
            mean.set(theSum * 1.0 / count);
            context.write(key, mean);
        }
    }

    @Override
    public int run(String[] args) throws Exception {

        Configuration conf = getConf();
        Job job = Job.getInstance(conf, "grouped word mean");

        job.setJarByClass(GroupMean.class);
        job.setMapperClass(GroupingWordsMapper.class);
        job.setReducerClass(GroupedMeanReducer.class);

        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(ByteWritable.class);
        job.setOutputValueClass(DoubleWritable.class);

        //job.setNumReduceTasks(2);

        FileInputFormat.addInputPath(job, new Path(args[0].trim()));
        Path output = new Path(args[1].trim());
        FileOutputFormat.setOutputPath(job, output);

        if (FileUtil.fullyDelete(new File(output.toString())))
            System.out.println("delete path : " + output);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] s) throws Exception {
        String[] args = {"data/hadoop/mr/wordCount", "res/hadoop/mr/wordCount/1"};

        if (args.length != 2) System.err.println("Usage: grouped word mean <in> <out>");

        ToolRunner.run(new Configuration(), new GroupMean(), args);
    }
}