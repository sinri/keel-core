package io.github.sinri.keel.integration.mysql.statement.templated;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

import static io.github.sinri.keel.facade.KeelInstance.Keel;


/**
 * Represents a SQL statement that can be constructed from a template and arguments.
 * <p>
 * This interface provides methods to load SQL templates from a file, retrieve the SQL template,
 * get the argument mapping, and build the final SQL string by replacing placeholders in the template
 * with their corresponding values.
 * </p>
 *
 * @see TemplatedReadStatement
 * @see TemplatedModifyStatement
 * @since 3.0.8
 */
public interface TemplatedStatement {
    static TemplatedReadStatement loadTemplateToRead(@Nonnull String templatePath) {
        try {
            byte[] bytes = Keel.fileHelper().readFileAsByteArray(templatePath, true);
            String sqlTemplate = new String(bytes);
            return new TemplatedReadStatement(sqlTemplate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static TemplatedModifyStatement loadTemplateToModify(@Nonnull String templatePath) {
        try {
            byte[] bytes = Keel.fileHelper().readFileAsByteArray(templatePath, true);
            String sqlTemplate = new String(bytes);
            return new TemplatedModifyStatement(sqlTemplate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String getSqlTemplate();

    TemplateArgumentMapping getArguments();

    default String build() {
        AtomicReference<String> sqlRef = new AtomicReference<>(getSqlTemplate());

        getArguments().forEach((argumentName, argumentValue) -> {
            String s = sqlRef.get()
                             .replaceAll("\\{" + argumentName + "}", Matcher.quoteReplacement(argumentValue.toString()));
            sqlRef.set(s);
        });

        return sqlRef.get();
    }
}
