package org.kemeter.cytoscape.internal.utils;

import org.kemeter.cytoscape.internal.hdb.HanaConnectionCredentials;

import java.io.*;
import java.nio.file.Paths;
import java.util.Properties;

public class IOUtils {

    /**
     * The filename where credentials will be stored between sessions
     */
    public static String getCacheFile(){
        return Paths.get(System.getProperty("user.home"), "cyhana_cache.properties").toString();
    }

    /**
     *
     * @param cred
     * @param savePassword
     * @throws IOException
     */
    public static void cacheCredentials(HanaConnectionCredentials cred, boolean savePassword) throws IOException {
        cacheCredentials(getCacheFile(), cred, savePassword);
    }

    /**
     * Stores current credentials to a properties file. Password will
     * only be stored, if the respective checkbox has been selected.
     *
     * @param file
     * @param cred
     * @param savePassword
     * @throws IOException
     */
    public static void cacheCredentials(String file, HanaConnectionCredentials cred, boolean savePassword) throws IOException {
        Properties credProps = new Properties();
        credProps.setProperty("hdb.host", cred.host);
        credProps.setProperty("hdb.port", cred.port);
        credProps.setProperty("hdb.username", cred.username);

        if (savePassword) {
            credProps.setProperty("hdb.password", cred.password);
        } else {
            // overwrite previously saved passwords
            credProps.setProperty("hdb.password", "");
        }

        try (OutputStream output = new FileOutputStream(getCacheFile())){
            credProps.store(output, null);
        } catch(IOException e){
            System.err.println("Cannot store connection credentials");
            System.err.println(e);
            throw e;
        }
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public static HanaConnectionCredentials loadCredentials() throws IOException {
        return loadCredentials(getCacheFile());
    }

    /**
     * Loads previously cached credentials from a properties file. If a password
     * has been saved before, it assumes that the user wants to do the same again
     * and pre-selects the checkbox to store passwords in plain text.
     *
     * @param file
     * @return
     */
    public static HanaConnectionCredentials loadCredentials(String file) throws IOException {

        try (InputStream input = new FileInputStream(getCacheFile())) {
            // load cached credentials
            Properties credProps = new Properties();
            credProps.load(input);

            String host = credProps.getProperty("hdb.host");
            String port = credProps.getProperty("hdb.port");
            String username = credProps.getProperty("hdb.username");
            String password = credProps.getProperty("hdb.password");

            return new HanaConnectionCredentials(host, port, username, password);

        } catch (IOException e) {
            // this will happen at least on the first start and is likely
            // not an issue
            System.err.println("Cannot load cached connection credentials");
            System.err.println(e);
            throw e;
        }
    }

    /**
     *
     * @param file
     * @throws IOException
     */
    public static void clearCachedCredentials(String file) throws IOException {
        cacheCredentials(file, new HanaConnectionCredentials("", "", "", ""), true);
    }
}
