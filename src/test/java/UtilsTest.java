import org.junit.Assert;
import org.junit.Test;
import org.kemeter.cytoscape.internal.hdb.HanaConnectionCredentials;
import org.kemeter.cytoscape.internal.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class UtilsTest {

    @Test
    public void testCacheAndRestoreCredentials(){
        String testFileAbsPath = "";
        try{
            File testFile = File.createTempFile("cyhana", ".properties");
            testFile.deleteOnExit();
            testFileAbsPath = testFile.getAbsolutePath();
        } catch (IOException e){
            Assert.fail();
        }

        HanaConnectionCredentials cacheCreds = new HanaConnectionCredentials(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString()
        );

        try{
            IOUtils.cacheCredentials(testFileAbsPath, cacheCreds, true);
        }catch (IOException e){
            Assert.fail();
        }

        try{
            HanaConnectionCredentials restoreCreds = IOUtils.loadCredentials(testFileAbsPath);
            Assert.assertEquals(cacheCreds.host, restoreCreds.host);
            Assert.assertEquals(cacheCreds.port, restoreCreds.port);
            Assert.assertEquals(cacheCreds.username, restoreCreds.username);
            Assert.assertEquals(cacheCreds.password, restoreCreds.password);

            IOUtils.clearCachedCredentials(testFileAbsPath);
        }catch(IOException e) {
            Assert.fail();
        }
    }

}
