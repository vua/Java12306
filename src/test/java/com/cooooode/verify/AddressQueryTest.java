package com.cooooode.verify;

import com.cooooode.verify.service.RecordService;
import org.junit.jupiter.api.Test;

/**
 * @program: verify
 * @description:
 * @author: vua
 * @create: 2020-03-05 10:38
 */
public class AddressQueryTest {
    @Test
    public void test(){
        System.out.println(new RecordService().address("12.234.12.10"));
    }
}
