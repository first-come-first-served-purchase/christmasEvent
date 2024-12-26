package com.doosan.christmas.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;

@Service
public class RedisLogReader {

    @Autowired
    private RedisLogTranslator redisLogTranslator;

    public void readLogs(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                redisLogTranslator.log(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
