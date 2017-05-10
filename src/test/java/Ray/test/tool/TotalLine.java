package Ray.test.tool;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ray on 17/5/10.
 * Count the number of code line in the Ray package.
 */
public class TotalLine {

    private final static String packagePath = "src/main/java/Ray";
    private static long counter = 0L;
    private static List<String> javaFileList = new ArrayList<>(300);

    public static void main(String[] args) {

        File packageDir = new File(packagePath);

        findJavaFile(packageDir);

        for (String javaFilePath : javaFileList) {
            countLine(javaFilePath);
        }

        System.out.println("\nThere are " + javaFileList.size() + " java files " +
                "and " + counter + " code lines in the Ray package.");

    }

    private static void countLine(String javaFilePath) {
        int count = 0;
        File file = new File(javaFilePath);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() > 0)  // not include null line
                    count++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("not find java file : " + javaFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                    counter += count;
                    System.out.println(file.getName() + " is " + count + " lines.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void findJavaFile(File dir) {
        File[] files;
        try {
            files = dir.listFiles();
            assert files != null;
            for (File file : files)
                if (file.isFile()) {
                    String javaFilePath = file.getAbsolutePath();
                    if (javaFilePath.endsWith(".java"))
                        javaFileList.add(javaFilePath);
                } else if (file.isDirectory()) {
                    findJavaFile(file);
                } else {
                    System.out.println("it is not file or directory : " + file.getAbsolutePath());
                }
        } catch (NullPointerException e) {
            System.out.println("Not find directory : " + dir.getAbsolutePath());
        }
    }
}
