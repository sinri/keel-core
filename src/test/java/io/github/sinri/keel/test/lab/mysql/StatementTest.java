package io.github.sinri.keel.test.lab.mysql;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunnerResult;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.integration.mysql.statement.component.CaseOperator;
import io.github.sinri.keel.integration.mysql.statement.component.CaseOperatorPair;
import io.github.sinri.keel.integration.mysql.statement.component.UpdateSetAssignmentComponent;
import io.github.sinri.keel.integration.mysql.statement.impl.UpdateStatement;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;

public class StatementTest extends KeelInstantRunner {
    @Nonnull
    @Override
    protected Future<Void> starting() {
        return Future.succeededFuture();
    }

    @Nonnull
    @Override
    protected Future<Void> ending(List<InstantRunnerResult> testUnitResults) {
        return Future.succeededFuture();
    }

    @InstantRunUnit
    public Future<Void> testUpdateStatement() {
        var sql = new UpdateStatement()
                .setWithValue("a1", "b'c")
                .setWithExpression("a2", "b\\'c")
                .setWithValue("a3", 1.44)
                .setWithAssignment(new UpdateSetAssignmentComponent("a4").assignmentToExpression("'b\\'r'"))
                .setWithAssignment(new UpdateSetAssignmentComponent("a5").assignmentToNull())
                .setWithAssignment(new UpdateSetAssignmentComponent("a6").assignmentToValue(214))
                .setWithAssignment(new UpdateSetAssignmentComponent("a7").assignmentToValue("b'p"))
                .setWithAssignment(new UpdateSetAssignmentComponent("a8").assignmentToCaseOperator(
                        new CaseOperator()
                                .setCaseValueAsString("c")
                                .addWhenThenPair(new CaseOperatorPair().setWhenAsString("i'w").setThenAsString("r'g"))
                                .addWhenThenPair(new CaseOperatorPair().setWhenAsNumber(1).setThenAsNumber(5))
                                .addWhenThenPair(new CaseOperatorPair().setWhenExpression("'fasdf'").setThenExpression("'sdf'"))
                                .setElseResultAsString("asad'4t")
                ))
                .table("table")
                .where(conditionsComponent -> conditionsComponent
                        .comparison(compareCondition -> compareCondition
                                .filedEqualsValue("aaa", "333")))
                .toString();
        System.out.println(sql);
        return Future.succeededFuture();
    }
}
