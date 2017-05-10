package Ray.hadoop.MR;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.ChainMapper;
import org.apache.hadoop.mapred.lib.ChainReducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Ray on 17/3/30.
 * Map chain. More than one Mapper/Reduce implementations in one job.
 */
public class MapReduceChain extends Configured implements Tool {

    public static class SplitMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, LongWritable> {
        private static LongWritable one = new LongWritable(1L);

        public void map(LongWritable key, Text value, OutputCollector<Text, LongWritable> output, Reporter reporter) throws IOException {
            String lines = value.toString().replaceAll("[^a-zA-Z0-9]", " ");
            String[] words = lines.split("\\s+");

            for (String word : words) {
                if (word.length() > 0) {
                    output.collect(new Text(word.toLowerCase()), one);
                }
            }
        }
    }

    public static class WrapMap extends MapReduceBase implements Mapper<Text, LongWritable, Text, LongWritable> {

        public void map(Text key, LongWritable value, OutputCollector<Text, LongWritable> output, Reporter reporter) throws IOException {
            byte[] v = (" [ " + value.toString() + " ]").getBytes();
            //在 key 中 拼接当前 value 值
            key.append(v, 0, v.length);
            output.collect(key, value);
        }
    }

    public static class CountReduce extends MapReduceBase implements Reducer<Text, LongWritable, Text, LongWritable> {

        public void reduce(Text key, Iterator<LongWritable> values, OutputCollector<Text, LongWritable> output, Reporter reporter) throws IOException {
            long count = 0L;
            while (values.hasNext()) {
                count += values.next().get();
            }
            output.collect(key, new LongWritable(count));
        }
    }

    public int run(String[] args) throws Exception {

        if (args.length != 2) {
            throw new Exception("args: input output");
        }
        Path input = new Path(args[0].trim());
        Path output = new Path(args[1].trim());

        //job config 全局
        JobConf conf = new JobConf(getConf(), MapReduceChain.class);
        conf.setJobName("map chain");
        conf.setJarByClass(MapReduceChain.class);

        //拆分 map config
        JobConf splitMapConf = new JobConf(false);
        ChainMapper.addMapper(conf, SplitMap.class, LongWritable.class, Text.class, Text.class, LongWritable.class, false, splitMapConf);

        //包装 map config
        JobConf wrapMapConf = new JobConf(false);
        ChainMapper.addMapper(conf, WrapMap.class, Text.class, LongWritable.class, Text.class, LongWritable.class, false, wrapMapConf);
        ChainMapper.addMapper(conf, WrapMap.class, Text.class, LongWritable.class, Text.class, LongWritable.class, false, wrapMapConf);

        //统计 reduce config
        JobConf countReduceConf = new JobConf(false);
        ChainReducer.setReducer(conf, CountReduce.class, Text.class, LongWritable.class, Text.class, LongWritable.class, false, countReduceConf);
        ChainReducer.addMapper(conf, WrapMap.class, Text.class, LongWritable.class, Text.class, LongWritable.class, false, wrapMapConf);
        ChainReducer.addMapper(conf, WrapMap.class, Text.class, LongWritable.class, Text.class, LongWritable.class, false, wrapMapConf);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf, input);
        FileOutputFormat.setOutputPath(conf, output);

        RunningJob job = JobClient.runJob(conf);

        // job 运行 顺序：
        // SplitMap => WrapMap => WrapMap => CountReduce => WrapMap => WrapMap => save as file
        // 只能有 一个 reduce 任务

        return job.isSuccessful() ? 0 : 1;
    }

    public static void main(String[] s) throws Exception {

        String[] args = {"data/hadoop/mr/wordCount", "res/hadoop/mr/wordCount/3"};

        if (args.length != 2) {
            throw new Exception("args: input directory output directory");
        }

        int exitCode = ToolRunner.run(new Configuration(), new MapReduceChain(), args);

        System.out.println("exit code :" + exitCode);
    }
}
