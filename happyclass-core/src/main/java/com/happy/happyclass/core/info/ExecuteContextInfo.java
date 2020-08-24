package com.happy.happyclass.core.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * Author huangzhilin
 * Date 2020/1/3
 */
@Data
@ToString
@AllArgsConstructor
public class ExecuteContextInfo {
    private String password;
    private boolean isEncrypt;
}
