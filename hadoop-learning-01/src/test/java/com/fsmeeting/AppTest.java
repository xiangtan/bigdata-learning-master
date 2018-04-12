package com.fsmeeting;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 47.98.108.252 node-1
 * 39.105.29.24 node-2
 * Unit test for simple App.
 */
public class AppTest {

    private FileSystem fileSystem = null;

    @Before
    public void init() throws URISyntaxException, IOException, InterruptedException {
        Configuration conf = new Configuration();
        //System.setProperty("HADOOP_USER_NAME","root");
        //conf.set("fs.defaultFS","hdfs://node-1:9000");
        fileSystem = FileSystem.get(new URI("hdfs://node-2:9000"), conf, "root");
    }

    /**
     * hdfs 2 local file system
     */
    @Test
    public void testDownload() throws IOException {
        InputStream in = fileSystem.open(new Path("/test01.md"));
        OutputStream out = new FileOutputStream("x://test//tmp01.txt");
        IOUtils.copyBytes(in, out, 1024, true);
    }

    @Test
    public void testUpload() throws IOException {

        fileSystem.copyFromLocalFile(new Path("x://test//up.txt"), new Path("/"));
    }

    @After
    public void destroy() throws IOException {
        fileSystem.close();
    }
}
