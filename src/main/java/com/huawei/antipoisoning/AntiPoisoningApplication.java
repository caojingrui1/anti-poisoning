package com.huawei.antipoisoning;

import com.huawei.antipoisoning.common.util.AntiMainUtil;
import com.huawei.antipoisoning.common.util.JGitUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AntiPoisoningApplication {
    public static void main(String[] args) {
        SpringApplication.run(AntiPoisoningApplication.class, args);
    }

}
