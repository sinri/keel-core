package io.github.sinri.keel.test.lab.mysql;

import io.vertx.sqlclient.data.Numeric;

public class DebugC {
    public static void main(String[] args) {
        Numeric numeric = Numeric.create(0.00000001);
        System.out.println(numeric);
        System.out.println(numeric.doubleValue());
        System.out.println(numeric.bigDecimalValue().toString());
        System.out.println(numeric.bigDecimalValue().toPlainString());
        System.out.println(numeric.bigDecimalValue().stripTrailingZeros().toPlainString());
    }
}
