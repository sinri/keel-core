package io.github.sinri.keel.integration.mysql.dev;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * As of 3.0.18 Finished Technical Preview.
 * As of 3.1.0 Add support for AES encryption.
 * As of 3.1.7 Add deprecated field annotation.
 * As of 4.1.0 Add tableExpression.
 *
 * @since 3.0.15
 */
class TableRowClassField {
    private static final Pattern patternForLooseEnum;
    private static final Pattern patternForStrictEnum;
    private static final Pattern patternForAnyEnvelope;

    static {
        patternForLooseEnum = Pattern.compile("Enum\\{([A-Za-z0-9_, ]+)}");
        patternForStrictEnum = Pattern.compile("Enum<([A-Za-z0-9_.]+)>");
        patternForAnyEnvelope = Pattern.compile("Envelope<([A-Za-z0-9_.]+)>");
    }

    private final String field;
    private final String type;
    private final String comment;
    private final @Nullable String strictEnumPackage;
    private final @Nullable String envelopePackage;
    /**
     * @since 3.1.10
     */
    private final boolean nullable;
    private @Nonnull
    final String tableExpression;
    private String returnType;
    private String readMethod;
    private @Nullable TableRowClassFieldLooseEnum looseEnum;
    private @Nullable TableRowClassFieldStrictEnum strictEnum;
    /**
     * @since 4.1.1
     */
    private @Nullable TableRowClassFieldAnyEnvelope envelope;
    /**
     * @since 3.1.7
     */
    private boolean fieldDeprecated = false;
    private String actualComment;

    public TableRowClassField(
            @Nonnull String tableExpression,
            @Nonnull String field,
            @Nonnull String type,
            boolean nullable,
            @Nullable String comment,
            @Nullable String strictEnumPackage,
            @Nullable String envelopePackage
    ) {
        this.tableExpression = tableExpression;
        this.field = field;
        this.type = type;
        this.nullable = nullable;
        this.comment = comment;
        this.strictEnumPackage = strictEnumPackage;
        this.envelopePackage = envelopePackage;

        parseType();
        parseComment();
    }

    protected void parseType() {
        returnType = "Object";
        readMethod = "readValue";

        if (type.contains("bigint")) {
            returnType = "Long";
            readMethod = "readLong";
        } else if (type.contains("int")) {
            // tinyint smallint mediumint
            returnType = "Integer";
            readMethod = "readInteger";
        } else if (type.contains("float")) {
            returnType = "Float";
            readMethod = "readFloat";
        } else if (type.contains("double")) {
            returnType = "Double";
            readMethod = "readDouble";
        } else if (type.contains("decimal")) {
            returnType = "Number";
            readMethod = "readNumber";
        } else if (type.contains("datetime") || type.contains("timestamp")) {
            returnType = "String";
            readMethod = "readDateTime";
        } else if (type.contains("date")) {
            returnType = "String";
            readMethod = "readDate";
        } else if (type.contains("time")) {
            returnType = "String";
            readMethod = "readTime";
        } else if (type.contains("text") || type.contains("char")) {
            // mediumtext, varchar, etc.
            returnType = "String";
            readMethod = "readString";
        }
    }

    protected void parseComment() {
        if (type.contains("char") && comment != null) {
            // supportLooseEnum
            Matcher matcherForLoose = patternForLooseEnum.matcher(comment);
            if (matcherForLoose.find()) {
                String enumValuesString = matcherForLoose.group(1);
                String[] enumValueArray = enumValuesString.split("[, ]+");
                if (enumValueArray.length > 0) {
                    looseEnum = new TableRowClassFieldLooseEnum(field, List.of(enumValueArray));
                }
            }
            // supportStrictEnum
            Matcher matcherForStrict = patternForStrictEnum.matcher(comment);
            if (matcherForStrict.find()) {
                String enumClassPathTail = matcherForStrict.group(1);
                strictEnum = new TableRowClassFieldStrictEnum(field, strictEnumPackage, enumClassPathTail);
            }

            // Any Envelope
            Matcher matcherForAnyEnvelope = patternForAnyEnvelope.matcher(comment);
            if (matcherForAnyEnvelope.find()) {
                String any = matcherForAnyEnvelope.group(1);
                envelope = new TableRowClassFieldAnyEnvelope(any, Objects.requireNonNull(this.envelopePackage));
            }
        }

        if (comment != null) {
            String[] split = comment.split("@[Dd]eprecated", 2);
            if (split.length > 1) {
                // this table is deprecated
                this.fieldDeprecated = true;
                actualComment = Keel.stringHelper().escapeForHttpEntity(split[1]);
            } else {
                actualComment = Keel.stringHelper().escapeForHttpEntity(comment);
            }
        } else {
            actualComment = "";
        }
    }

    public String build() {
        String getter = "get" + Keel.stringHelper().fromUnderScoreCaseToCamelCase(field);

        StringBuilder code = new StringBuilder();
        if (looseEnum != null) {
            code.append(looseEnum.build()).append("\n")
                .append("\t/**\n")
                .append("\t * Field {@code ").append(tableExpression).append(".").append(field).append("}.\n")
                .append("\t * ").append(actualComment).append("\n")
                .append("\t * <p>\n")
                .append("\t * Loose Enum of Field `").append(field).append("` of type `").append(type).append("`.\n")
                .append("\t */\n");
            if (fieldDeprecated) {
                code.append("\t@Deprecated\n");
            }
            if (nullable) {
                code.append("\t@Nullable\n");
            } else {
                code.append("\t@Nonnull\n");
            }
            code.append("\tpublic ").append(looseEnum.looseEnumName()).append(" ").append(getter).append("() {\n")
                .append("\t\t@Nullable String enumExpression=").append(readMethod).append("(\"").append(field)
                .append("\");\n");
            if (nullable) {
                code.append("\t\tif (enumExpression==null) return null;\n");
            } else {
                code.append("\t\tObjects.requireNonNull(enumExpression,\"The Enum Field `").append(field)
                    .append("` should not be null!\");\n");
            }
            code.append("\t\treturn ").append(looseEnum.looseEnumName()).append(".valueOf(enumExpression);\n")
                .append("\t}\n");
        } else if (strictEnum != null) {
            code.append("\t/**\n")
                .append("\t * Field {@code ").append(tableExpression).append(".").append(field).append("}.\n")
                .append("\t * ").append(actualComment).append("\n")
                .append("\t * <p>\n")
                .append("\t * Strict Enum of Field `").append(field).append("` of type `").append(type).append("`.\n")
                .append("\t */\n");
            if (fieldDeprecated) {
                code.append("\t@Deprecated\n");
            }
            if (nullable) {
                code.append("\t@Nullable\n");
            } else {
                code.append("\t@Nonnull\n");
            }
            code.append("\tpublic ").append(strictEnum.fullEnumRef()).append(" ").append(getter).append("() {\n")
                .append("\t\t@Nullable String enumExpression=").append(readMethod).append("(\"").append(field)
                .append("\");\n");
            if (nullable) {
                code.append("\t\tif (enumExpression==null) return null;\n");
            } else {
                code.append("\t\tObjects.requireNonNull(enumExpression,\"The Enum Field `").append(field)
                    .append("` should not be null!\");\n");
            }
            code.append("\t\treturn ").append(strictEnum.fullEnumRef()).append(".valueOf(enumExpression);\n")
                .append("\t}\n");
        } else {
            code.append("\t/**\n");
            code.append("\t * Field {@code ").append(tableExpression).append(".").append(field).append("}.\n");
            if (comment != null) {
                code.append("\t * ").append(actualComment).append("\n")
                    .append("\t * <p>\n");
            }
            code.append("\t * Field `").append(field).append("` of type `").append(type).append("`.\n")
                .append("\t */\n");
            if (fieldDeprecated) {
                code.append("\t@Deprecated\n");
            }
            if (nullable) {
                code.append("\t@Nullable\n");
            } else {
                code.append("\t@Nonnull\n");
            }
            code.append("\tpublic ").append(returnType).append(" ").append(getter).append("() {\n")
                .append("\t\treturn ")
                .append(nullable ? "" : "Objects.requireNonNull(")
                .append(readMethod).append("(\"").append(field).append("\")")
                .append(nullable ? "" : ")").append(";\n")
                .append("\t}\n");
        }

        if (envelope != null) {
            code.append("\t/**\n")
                .append("\t * EXTRACTED VALUE of {@code ").append(tableExpression).append(".").append(field)
                .append("}.\n");
            if (comment != null) {
                code.append("\t * ").append(actualComment).append("\n")
                    .append("\t * <p>\n");
            }
            code.append("\t */\n")
                .append("\t@Nullable\n")
                .append("\tpublic ").append("String").append(" ").append(getter).append("Extracted() {\n")
                .append("\t\treturn ")
                .append(envelope.buildCallClassMethodCode(readMethod + "(\"" + field + "\")")).append("\n")
                .append("\t}\n");
        }

        return code.toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
