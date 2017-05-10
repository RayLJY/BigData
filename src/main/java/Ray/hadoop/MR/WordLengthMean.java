package Ray.hadoop.MR;


import com.google.common.base.Charsets;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 * Created by Ray on 17/4/8.
 * Copy from {@link org.apache.hadoop.examples.WordMean}.
 * I rewrite the readAndCalcMean function for many output files.
 * This Map function just can emit two keys, but that don't means
 * we only use one Reduce function.
 *
 * This Map/Reduce program can be used to calculate the total mean.
 */
public class WordLengthMean extends Configured implements Tool {

    private final static Text COUNT = new Text("count");
    private final static Text LENGTH = new Text("length");
    private final static LongWritable ONE = new LongWritable(1);

    /**
     * Maps words from line of text into 2 key-value pairs; one key-value pair for
     * counting the word, another for counting its length.
     */
    public static class WordMeanMapper extends
            Mapper<Object, Text, Text, LongWritable> {

        private LongWritable wordLen = new LongWritable();

        /**
         * Emits 2 key-value pairs for counting the word and its length. Outputs are
         * (Text, LongWritable).
         *
         * @param value This will be a line of text coming in from our input file.
         */
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                String string = itr.nextToken();
                this.wordLen.set(string.length());
                context.write(LENGTH, this.wordLen);
                context.write(COUNT, ONE);
            }
        }
    }

    /**
     * Performs integer summation of all the values for each key.
     */
    public static class WordMeanReducer extends
            Reducer<Text, LongWritable, Text, LongWritable> {

        private LongWritable sum = new LongWritable();

        /**
         * Sums all the individual values within the iterator and writes them to the
         * same key.
         *
         * @param key    This will be one of 2 constants: LENGTH_STR or COUNT_STR.
         * @param values This will be an iterator of all the values associated with that
         *               key.
         */
        public void reduce(Text key, Iterable<LongWritable> values, Context context)
                throws IOException, InterruptedException {

            long theSum = 0L;
            for (LongWritable val : values) {
                theSum += val.get();
            }
            sum.set(theSum);
            context.write(key, sum);
        }
    }

    /**
     * Reads the output file and parses the summation of lengths, and the word
     * count, to perform a quick calculation of the mean.
     *
     * @param path The path to find the output file in. Set in main to the output
     *             directory.
     * @throws IOException If it cannot access the output directory, we throw an exception.
     */
    private double readAndCalcMean(Path path, Configuration conf)
            throws IOException {
        FileSystem fs = FileSystem.get(conf);

        FileStatus[] stats = fs.listStatus(path);
        Path[] files = FileUtil.stat2Paths(stats);

        BufferedReader br = null;

        long count = 0;
        long length = 0;
        // average = total sum / number of elements;
        try {
            for (Path file : files) {
                if (!file.getName().startsWith("part-r-")) continue;

                br = new BufferedReader(new InputStreamReader(fs.open(file), Charsets.UTF_8));

                String line;
                while ((line = br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line);

                    // grab type
                    String type = st.nextToken();

                    // differentiate
                    if (type.equals(COUNT.toString())) {
                        String countLit = st.nextToken();
                        count += Long.parseLong(countLit);
                    } else if (type.equals(LENGTH.toString())) {
                        String lengthLit = st.nextToken();
                        length += Long.parseLong(lengthLit);
                    }
                }
            }
            double theMean = (((double) length) / ((double) count));
            System.out.println("\nThe mean is: " + theMean);
            return theMean;
        } finally {
            if (br != null) {
                br.close();
            }
        }
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = getConf();

        Job job = Job.getInstance(conf, "word's length mean");

        job.setJarByClass(WordLengthMean.class);
        job.setMapperClass(WordMeanMapper.class);
        job.setCombinerClass(WordMeanReducer.class);
        job.setReducerClass(WordMeanReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        //job.setNumReduceTasks(2);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        Path output = new Path(args[1]);
        FileOutputFormat.setOutputPath(job, output);

        if (FileUtil.fullyDelete(new File(output.toString())))
            System.out.println("delete path : " + output);

        boolean result = job.waitForCompletion(true);

        if (result)
            readAndCalcMean(output, conf);

        return (result ? 0 : 1);
    }

    public static void main(String[] s) throws Exception {
        String[] args = {"data/hadoop/mr/wordCount", "res/hadoop/mr/wordCount/1"};

        if (args.length != 2) System.err.println("Usage: word length mean <in> <out>");

        ToolRunner.run(new Configuration(), new WordLengthMean(), args);
    }
}