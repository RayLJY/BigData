package hadoop.MR.comparator;

import org.apache.hadoop.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Ray on 17/4/3.
 * This comparator be used to solve the problem that
 * 10 and 9 as character,9 bigger than 10.
 */
public class CharacterComparator extends WritableComparator {

    Logger logger = LoggerFactory.getLogger(CharacterComparator.class);

    public CharacterComparator() {
        super(Text.class);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {

        // show problem
        logger.debug("res:" + a.compareTo(b) + " a: " + a.toString() + " b: " + b.toString());
        // fact :
        //   res:8 a: k9999 b: k10000
        //   res:-8 a: k10000 b: k9999
        //   res:0 a: k9999 b: k9999
        // I want :
        //   res:-1 a: k9999 b: k10000
        //   res:1 a: k10000 b: k9999
        //   res:0 a: k9999 b: k9999

        if (a.equals(b)) {
            return 0;
        } else {
            String as = a.toString();
            String bs = b.toString();
            int al = as.length();
            int bl = bs.length();

            if (as.length() == bs.length()) {
                return as.compareTo(bs);
            } else {
                return al > bl ? 1 : -1;
            }
        }
    }
}
