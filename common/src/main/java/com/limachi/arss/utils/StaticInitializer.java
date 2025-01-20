package com.limachi.arss.utils;

import com.limachi.arss.utils.annotations.StaticInit;

public class StaticInitializer {
    public static void initialize(Stage stage) {
        ModBase.extractor.runOnMethods(StaticInit.class, (m, a)->{
            if (a.value() == stage)
                m.get();
        });
    }
}
