/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bedrock.common.spi.utils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Stephane Nicoll
 * @author Vedran Pavic
 * @since 5.0
 */
public class PropertiesMarshallerUtil {

    public static void write(Map<String, Set<String>> map, OutputStream out) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, UTF_8));
        Set<String> keySet = map.keySet();
        for (String k : keySet) {
            writer.write(k);
            StringJoiner sj = new StringJoiner(",\\\n  ", "=\\\n  ", "");
            map.get(k).forEach(sj::add);
            writer.write(sj.toString());
            writer.newLine();
        }
        writer.flush();
    }


    public static Map<String, Set<String>> read(InputStream in) throws IOException {
        Map<String, Set<String>> config = new HashMap<>();
        Properties props = new Properties();
        props.load(in);
        props.forEach((type, value) -> {
            String[] values = ((String) value).split(",");
            Set<String> valueSet = Arrays.stream(values)
                    .collect(Collectors.toSet());
            config.put((String) type, valueSet);
        });
        return config;
    }


    public static Set<String> readAutoConfiguration(InputStream in) throws IOException {
        Set<String> autoConfiguration = new HashSet<>();
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(in))
        ) {
            reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.startsWith("#"))
                    .forEach(autoConfiguration::add);
        }
        return autoConfiguration;
    }

    public static void writeAutoConfiguration(Set<String> autoConfiguration, OutputStream outputStream) throws IOException {

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8));
        StringJoiner joiner = new StringJoiner("\n");
        for (String configurationImport : autoConfiguration) {
            joiner.add(configurationImport);
        }
        writer.write(joiner.toString());
        writer.flush();

    }

    public static void writeDevToolsFile(String projectName, OutputStream out) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, UTF_8));
        String format = "restart.include.%s=/%s[\\\\w-]+\\.jar";
        writer.write(String.format(format, projectName, projectName));
        writer.flush();
    }
}
