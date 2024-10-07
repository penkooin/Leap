package org.chaostocosmos.leap.http;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

public class ConfigTest {

    Path path;

    public ConfigTest() {
        this.path = Paths.get("D:\\0.github\\leap\\app\\src\\main\\resources\\webapp\\WEB-INF\\config.json");
    }

    @SuppressWarnings("unchecked")
    public void convertJsonToYaml() throws Exception {
        String allStr = Files.readAllLines(this.path).stream().collect(Collectors.joining());
        //from JSON        
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(allStr, Map.class);
        //from YAML
        DumperOptions options = new DumperOptions();  //Set dump options
        options.setDefaultFlowStyle(FlowStyle.BLOCK); 
        options.setPrettyFlow(true);        
        Yaml yaml = new Yaml(options);
        yaml.dump(map, new OutputStreamWriter(new FileOutputStream(this.path.getParent().resolve("config.yml").toFile()), "utf-8"));
    }

    public static void main(String[] args) throws Exception {
        //Method m = ConfigTest.class.getMethod("convertJsonToYaml", new Class<?>[0]);
        //System.out.println(m.getDeclaringClass().getName());

        String str = "aaa"+"\r\n\r\nbbb";
        str = Files.readString(Paths.get("/home/kooin/Documents/request-sample.txt"));
        System.out.println(str.substring(str.indexOf("\r\n\r\n")));
    }    
}
