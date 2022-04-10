package net.blueberrymc;

import net.blueberrymc.common.util.FileUtil;
import org.junit.Test;

import java.io.File;

public class FileUtilTest {
    @Test
    public void isFileInsideBoundary() {
        File boundary = new File(".");
        File file = new File("./file");
        File file2 = new File("../file2");
        assert FileUtil.isFileInsideBoundary(boundary, file);
        assert !FileUtil.isFileInsideBoundary(boundary, file2);
    }
}
