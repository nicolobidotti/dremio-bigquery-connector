/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis;

import com.google.api.client.util.SecurityUtils;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for the Google API Client Library.
 *
 * @since 1.12
 * @author rmistry@google.com (Ravi Mistry)
 */
public final class GoogleUtils {

    /** Current release version. */
    public static final String VERSION = getVersion();

    // NOTE: Integer instead of int so compiler thinks it isn't a constant, so it won't inline it
    /**
     * Major part of the current release version.
     *
     * @since 1.14
     */
    public static final Integer MAJOR_VERSION;

    /**
     * Minor part of the current release version.
     *
     * @since 1.14
     */
    public static final Integer MINOR_VERSION;

    /**
     * Bug fix part of the current release version.
     *
     * @since 1.14
     */
    public static final Integer BUGFIX_VERSION;

    @VisibleForTesting
    static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(-SNAPSHOT)?");

    static {
        Matcher versionMatcher = VERSION_PATTERN.matcher(VERSION);
        versionMatcher.find();
        MAJOR_VERSION = Integer.parseInt(versionMatcher.group(1));
        MINOR_VERSION = Integer.parseInt(versionMatcher.group(2));
        BUGFIX_VERSION = Integer.parseInt(versionMatcher.group(3));
    }

    /** Cached value for {@link #getCertificateTrustStore()}. */
    static KeyStore certTrustStore;

    /**
     * Returns the key store for trusted root certificates to use for Google APIs.
     *
     * <p>Value is cached, so subsequent access is fast.
     *
     * @since 1.14
     */
    public static synchronized KeyStore getCertificateTrustStore()
            throws IOException, GeneralSecurityException {
        if (certTrustStore == null) {
            certTrustStore = SecurityUtils.getPkcs12KeyStore();
            InputStream keyStoreStream = GoogleUtils.class.getResourceAsStream("google.p12");
            SecurityUtils.loadKeyStore(certTrustStore, keyStoreStream, "notasecret");
        }
        return certTrustStore;
    }

    private static String getVersion() {
        String readGACVersionFromProperties = System.getProperty("readGACVersionFromProperties");
        if (readGACVersionFromProperties == null || readGACVersionFromProperties.equals("false")) {
            // short-circuit the default logic as the classloading breaks resulting in null version and regex failures
            // in the static initializer
            // this is due to maven shade modifying string literals due to a pattern rule, including the ones used below
            return "1.32.1";
        } else {
            // attempt to read the library's version from a properties file generated during the build
            // this value should be read and cached for later use
            String version = null;
            try (InputStream inputStream =
                         GoogleUtils.class.getResourceAsStream("google-api-client.properties")) {
                if (inputStream != null) {
                    Properties properties = new Properties();
                    properties.load(inputStream);
                    version = properties.getProperty("google-api-client.version");
                }
            } catch (IOException e) {
                // ignore
            }
            return version == null ? "unknown-version" : version;
        }
    }

    private GoogleUtils() {}
}
