package org.hikeny;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class FileMatcher {
    private static final int BUFFER_SIZE = 100;
    private static final int MAX_MACTCH = 512;

    // 获取指定目录下的所有文件
    private List<String> getFiles(String directory) throws IOException {
        List<String> fileList = new ArrayList<>();
        Files.walk(Paths.get(directory))
                .filter(Files::isRegularFile)
                .forEach(file -> fileList.add(file.toString()));
        return fileList;
    }

    // 从 YAML 文件中加载规则
    private Map loadRules(String yamlFile) {
        Map rules = new HashMap();
        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(yamlFile)) {
            Object loadedData = (LinkedHashMap) yaml.load(reader);
            LinkedHashMap loadedDatak = (LinkedHashMap)loadedData;
            loadedData = loadedDatak.get("rules");
            if (loadedData instanceof List) {
                List<?> list = (List<?>) loadedData;
                List<?> ruleGroups = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) item;
                        List<Map<String, Object>> ruleList = (List<Map<String, Object>>) map.get("rule");
                        if (ruleList!= null) {
                            for (Map<String, Object> ruleMap : ruleList) {
                                rules.put((String) ruleMap.get("name"), (String) ruleMap.get("f_regex"));
                            }
                        }
                    }
                }
                return rules;
            } else {
                System.out.println("The loaded data is not a list.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //判断是否为二进制文件
    public static boolean isBinaryFile(String filePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        for (int i = 0; i < Math.min(fileBytes.length, MAX_MACTCH); i++) {
            byte b = fileBytes[i];
            if (b < 0x20 && b > 0x00 && b != 0x09 && b != 0x0A && b != 0x0D) {
                fileBytes = null;
                return true;
            }
        }
        return false;
    }

    // 获取文件扩展名
    public static String getExtension(String filePath) {
        int index = filePath.lastIndexOf('.');
        if (index > 0) {
            return filePath.substring(index + 1);
        } else {
            return "";
        }
    }

    // 对文件进行匹配并将结果写入 CSV 文件
    private void matchAndWrite(List<String> files, Map rules, String outputCsv) {
        try (FileWriter writer = new FileWriter("output.csv");
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            // 写入 CSV 文件的头部信息
            int count = files.size();
            int i = 0;
            csvPrinter.printRecord("File", "Rule Name", "Matched Content");
            for (String file : files) {
                i = i + 1;
                String ext = getExtension(file);
                if (isBinaryFile(file)) {
                    System.out.println("(" + i + "/" + count + ")   " + "skipping binary file：" + file);
                }else {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        Iterator<Map.Entry<String, String>> entries = rules.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry<String, String> entry = entries.next();
                            System.out.println("(" + i + "/" + count + ")   " + file);
                            Pattern pattern = Pattern.compile(entry.getValue());
                            Matcher matcher = pattern.matcher(content);
                            while (matcher.find()) {
                                //限制一下大小
                                if (matcher.group().length() < MAX_MACTCH) {
                                    csvPrinter.printRecord(file, entry.getKey(), matcher.group());
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("读取文件 " + file + " 出错: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 主处理流程
    public void process(String yamlFile, String directory, String outputCsv) {
        try {
            List<String> files = getFiles(directory);
            Map rules = loadRules(yamlFile);
            matchAndWrite(files, rules, outputCsv);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class ezmm {
    public static void main(String[] args) {
        String userHome = System.getProperty("user.home");
        String yamlFile = userHome + "/.config/ezmm/Rules.yml";
        String currentDir = System.getProperty("user.dir");
        String outputCsv = "output.csv";
        FileMatcher fileMatcher = new FileMatcher();
        fileMatcher.process(yamlFile, currentDir, outputCsv);
    }
}