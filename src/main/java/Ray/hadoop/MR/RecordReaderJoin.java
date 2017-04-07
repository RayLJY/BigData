package Ray.hadoop.MR;

import Ray.hadoop.MR.comparator.CharacterComparator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.join.CompositeInputFormat;
import org.apache.hadoop.mapreduce.lib.join.TupleWritable;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;


/**
 * Created by Ray on 17/4/3.
 * Copy from {@link org.apache.hadoop.examples.Join}.
 * Join files have been sorted in same order.
 */
public class RecordReaderJoin extends Configured implements Tool {
    public static final String REDUCES_PER_HOST = "mapreduce.join.reduces_per_host";

    public int run(String[] args) throws Exception {

        if (args.length != 3)
            throw new Exception("args: input output");

        Path inputA = new Path(args[0].trim());
        Path inputB = new Path(args[1].trim());
        Path output = new Path(args[2].trim());

        Configuration conf = getConf();
        JobClient client = new JobClient(conf);
        ClusterStatus cluster = client.getClusterStatus();
        int num_reduces = (int) (cluster.getMaxReduceTasks() * 0.9);
        String join_reduces = conf.get(REDUCES_PER_HOST);
        if (join_reduces != null) {
            num_reduces = cluster.getTaskTrackers() *
                    Integer.parseInt(join_reduces);
        }
        num_reduces = num_reduces > 0 ? num_reduces : 1;

        Job job = Job.getInstance(conf);
        job.setJobName("record reader join");
        job.setJarByClass(RecordReaderJoin.class);

        job.setMapperClass(Mapper.class);
        //job.setMapperClass(PrintMap.class);
        job.setReducerClass(Reducer.class);

        // Set user-supplied (possibly default) job configs
        job.setNumReduceTasks(num_reduces);

        // Using in test
        FileSystem fs = FileSystem.newInstance(job.getConfiguration());
        if (fs.exists(output)) {
            if (fs.delete(output, true)) {
                System.out.println("delete directory : " + output.toString());
            }
        }

        FileOutputFormat.setOutputPath(job, output);

        job.setInputFormatClass(CompositeInputFormat.class);

        /* This kind of join using Text type as Key.
           Necessary condition: the length of Keys must equals. */
        job.getConfiguration().set(CompositeInputFormat.JOIN_EXPR,
                CompositeInputFormat.compose("outer", KeyValueTextInputFormat.class, inputA, inputB));
        /*
         * When using compare function, 9 and 10, as number, 10 > 9, but as character,9 > 10.
         * So, I rewrite compare function of Text Class.
         */
        job.getConfiguration().setClass(CompositeInputFormat.JOIN_COMPARATOR,
                CharacterComparator.class, WritableComparator.class);

        /* This kind of join using IntWritable type as Key. */
        //job.getConfiguration().set(CompositeInputFormat.JOIN_EXPR,
        //        CompositeInputFormat.compose("inner", KeyLineInputFormat.class, inputA, inputB));


        job.setOutputKeyClass(Text.class);
        //job.setOutputKeyClass(IntWritable.class);
        //job.setOutputValueClass(Text.class);
        job.setOutputValueClass(TupleWritable.class);

        job.setOutputFormatClass(TextOutputFormat.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }


    public static void main(String[] s) throws Exception {

        String[] args = {"data/hadoop/mr/join/a", "data/hadoop/mr/join/b", "res/hadoop/mr/join/1"};

        if (args.length != 3) throw new Exception("args: input directory output directory");

        int exitCode = ToolRunner.run(new Configuration(), new RecordReaderJoin(), args);
        System.out.println("exit code :" + exitCode);
    }


    /**
     * This class just is used to prove that join process happened in record reader.
     */
    public static class PrintMap extends Mapper<Text, TupleWritable, Text, Text> {
        static Text out = new Text();

        @Override
        protected void map(Text key, TupleWritable value, Context context) throws IOException, InterruptedException {
            if (value.size() == 1) {
                System.out.println(value.get(0).toString());
                out.set(value.get(0).toString());
            } else {
                out.set(value.toString().substring(1));
            }
            context.write(key, out);
        }
    }

}
