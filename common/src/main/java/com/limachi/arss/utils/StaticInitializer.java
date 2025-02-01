package com.limachi.arss.utils;

import com.limachi.arss.utils.annotations.StaticInit;
import com.limachi.arss.utils.client.ClientStage;
import com.limachi.arss.utils.client.annotations.StaticInitClient;

public class StaticInitializer {
    public static void initialize(Stage stage, boolean before) {
        ModBase.extractor.runOnMethods(StaticInit.class, (m, a)->{
            if (a.value() == stage && before == a.before())
                m.get(null, true);
        });
    }

    public static void initialize(ClientStage stage, boolean before) {
        ModBase.extractor.runOnMethods(StaticInitClient.class, (m, a)->{
            if (a.value() == stage && before == a.before())
                m.get(null, true);
        });
    }
}
