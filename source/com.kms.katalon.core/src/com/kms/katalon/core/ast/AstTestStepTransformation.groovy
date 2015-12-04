package com.kms.katalon.core.ast;

import groovy.transform.CompileStatic

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.CatchStatement
import org.codehaus.groovy.ast.stmt.DoWhileStatement
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.TryCatchStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import com.kms.katalon.core.constants.StringConstants
import com.kms.katalon.core.keyword.BuiltinKeywords
import com.kms.katalon.core.logging.KeywordLogger

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class AstTestStepTransformation implements ASTTransformation {

    private static final String KEYWORD_LOGGER_GET_INSTANCE_METHOD_NAME = "getInstance";

    private static final String KEYWORD_LOGGER_SET_PENDING_DESCRIPTION_METHOD_NAME = "setPendingDescription";

    private static final String KEYWORD_MAIN_RUN_KEYWORD_METHOD_NAME = "runKeyword";

    private static final List<ImportNode> importNodes = new ArrayList<ImportNode>()

    private static final String KEYWORD_DEFAULT_NAME = "Statement"

    private static final String RUN_METHOD_NAME = "run"

    private static final String COMMENT_STATEMENT_KEYWORD_NAME = "Comment";

    @CompileStatic
    public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if (!(astNodes != null)
        || !(astNodes[0] != null)
        || !(astNodes[1] != null)
        || (!(astNodes[0] instanceof AnnotationNode))
        || !((AnnotationNode) astNodes[0]).getClassNode().getName()
        .equals(RequireAstTestStepTransformation.class.getName())
        || (!(astNodes[1] instanceof ClassNode))) {
            return;
        }
        ClassNode annotatedClass = (ClassNode) astNodes[1];

        importNodes.clear();
        for (ImportNode importNode : annotatedClass.getModule().getImports()) {
            importNodes.add(importNode);
        }
        for (MethodNode method : annotatedClass.getMethods()) {
            if (method.getName().equalsIgnoreCase(RUN_METHOD_NAME) && method.getCode() instanceof BlockStatement) {
                visit((BlockStatement) method.getCode(), null, 1);
            }
        }
    }

    @CompileStatic
    private Class<?> loadClass(String className) {
        Class<?> type = null;
        try {
            type = Class.forName(className);
            return type;
        } catch (ClassNotFoundException ex) {
            // not found, do nothing
        }
        for (ImportNode importNode : importNodes) {
            if (importNode.getClassName().endsWith(className)) {
                try {
                    type = Class.forName(importNode.getClassName());
                    return type;
                } catch (ClassNotFoundException ex) {
                    continue;
                }
            }
        }
        return null;
    }

    @CompileStatic
    private String getComment(Statement statement) {
        if (statement instanceof ExpressionStatement) {
            ExpressionStatement expressionStatement = (ExpressionStatement) statement;
            if (expressionStatement.getExpression() instanceof ConstantExpression) {
                ConstantExpression constant = (ConstantExpression) expressionStatement.getExpression();
                if (constant.getValue() instanceof String) {
                    return constant.getValue();
                }
            }
        }
        return null;
    }

    // exclude loop statements
    @CompileStatic
    private boolean isParentStatement(Statement statement) {
        return (statement instanceof BlockStatement || statement instanceof IfStatement
                || statement instanceof CatchStatement || statement instanceof SwitchStatement
                || statement instanceof CaseStatement || statement instanceof TryCatchStatement);
    }

    @CompileStatic
    public void visit(Statement statement, Stack<Statement> deferedStatements = new Stack<Statement>(),
            Map<Statement, Integer> indexMap = new HashMap<Statement, Integer>(), int nestedLevel) {
        if (statement instanceof BlockStatement) {
            visit((BlockStatement) statement, deferedStatements, nestedLevel);
        } else if (statement instanceof ForStatement) {
            visit((ForStatement) statement, nestedLevel);
        } else if (statement instanceof WhileStatement) {
            visit((WhileStatement) statement, nestedLevel);
        } else if (statement instanceof IfStatement) {
            visit((IfStatement) statement, deferedStatements, indexMap, nestedLevel);
        } else if (statement instanceof TryCatchStatement) {
            visit((TryCatchStatement) statement, indexMap, nestedLevel);
        } else if (statement instanceof SwitchStatement) {
            visit((SwitchStatement) statement, indexMap, nestedLevel);
        }
    }

    @CompileStatic
    public void visit(ForStatement forStatement, int nestedLevel) {
        visit(forStatement.getLoopBlock(), nestedLevel);
    }

    @CompileStatic
    public void visit(IfStatement ifStatement, Stack<Statement> deferedStatements = new Stack<Statement>(), Map<Statement, Integer> indexMap, int nestedLevel) {
        visit(ifStatement.getIfBlock(), deferedStatements, nestedLevel);
        if (ifStatement.getElseBlock() == null || ifStatement.getElseBlock() instanceof EmptyStatement) {
            return;
        }
        if (ifStatement.getElseBlock() instanceof IfStatement) {
            deferedStatements.push(
                    new ExpressionStatement(createNewStartKeywordMethodCall(KEYWORD_DEFAULT_NAME + " - Else " +
                    AstTextValueUtil.getTextValue(ifStatement.getElseBlock()),
                    ifStatement.getElseBlock(), indexMap, nestedLevel - 1)));
        } else {
            deferedStatements.push(
                    new ExpressionStatement(createNewStartKeywordMethodCall(KEYWORD_DEFAULT_NAME + " - Else",
                    ifStatement.getElseBlock(), indexMap, nestedLevel - 1)));
            if (!(ifStatement.getElseBlock() instanceof BlockStatement)) {
                BlockStatement elseBlock = new BlockStatement();
                elseBlock.getStatements().add(ifStatement.getElseBlock());
                ifStatement.setElseBlock(elseBlock);
            }
        }
        visit(ifStatement.getElseBlock(), deferedStatements, indexMap, nestedLevel);
    }

    @CompileStatic
    public void visit(WhileStatement whileStatement, int nestedLevel) {
        visit(whileStatement.getLoopBlock(), nestedLevel);
    }

    @CompileStatic
    public void visit(DoWhileStatement doWhileStatement, int nestedLevel) {
        visit(doWhileStatement.getLoopBlock(), nestedLevel);
    }

    @CompileStatic
    public void visit(TryCatchStatement tryCatchStatement, Map<Statement, Integer> indexMap, int nestedLevel) {
        visit(tryCatchStatement.getTryStatement(), nestedLevel);
        for (CatchStatement catchStatement : tryCatchStatement.getCatchStatements()) {
            Stack<Statement> deferedStatements = new Stack<Statement>();
            deferedStatements.push(
                    new ExpressionStatement(createNewStartKeywordMethodCall(getKeywordNameForStatement(catchStatement),
                    catchStatement, indexMap, nestedLevel - 1)));
            visit(catchStatement.getCode(), deferedStatements, nestedLevel);
        }
        if (tryCatchStatement.getFinallyStatement() == null) {
            return;
        }
        Stack<Statement> deferedStatements = new Stack<Statement>();
        deferedStatements.push(
                new ExpressionStatement(createNewStartKeywordMethodCall(KEYWORD_DEFAULT_NAME + " - Finally",
                tryCatchStatement.getFinallyStatement(), indexMap, nestedLevel - 1)));
        visit(tryCatchStatement.getFinallyStatement(), deferedStatements, nestedLevel);
    }

    @CompileStatic
    public void visit(SwitchStatement switchStatement, Map<Statement, Integer> indexMap, int nestedLevel) {
        for (CaseStatement caseStatement : switchStatement.getCaseStatements()) {
            Stack<Statement> deferedStatements = new Stack<Statement>();
            deferedStatements.push(
                    new ExpressionStatement(createNewStartKeywordMethodCall(getKeywordNameForStatement(caseStatement),
                    caseStatement, indexMap, nestedLevel)));
            visit(caseStatement.getCode(), deferedStatements, nestedLevel + 1);
        }
        if (switchStatement.getDefaultStatement() == null) {
            return;
        }
        Stack<Statement> deferedStatements = new Stack<Statement>();
        deferedStatements.push(
                new ExpressionStatement(createNewStartKeywordMethodCall(KEYWORD_DEFAULT_NAME + " - Default",
                switchStatement.getDefaultStatement(), indexMap, nestedLevel)));
        visit(switchStatement.getDefaultStatement(), deferedStatements, nestedLevel + 1);
    }

    @CompileStatic
    public void visit(BlockStatement blockStatement, Stack<Statement> deferedStatements, int nestedLevel) {
        int index = 0;
        Map<Statement, Integer> indexMap = getIndexMapForBlockStatement(blockStatement);
        if (deferedStatements != null) {
            Stack<Statement> copyDeferedStatements = (Stack<Statement>) deferedStatements.clone();
            while (!copyDeferedStatements.isEmpty()) {
                blockStatement.getStatements().add(0, copyDeferedStatements.pop());
                index++;
            }
        }
        Stack<Statement> commentStatementsStack = new Stack<Statement>();
        while (index < blockStatement.getStatements().size()) {
            Statement statement = blockStatement.getStatements().get(index);
            String comment = getComment(statement);
            if (comment != null) {
                commentStatementsStack.push(statement);
                index++;
                continue;
            }
            if (!(statement instanceof BlockStatement)) {
                if (!commentStatementsStack.isEmpty()) {
                    Statement descriptionStatement = commentStatementsStack.pop();
                    String commentContent = getComment(descriptionStatement);
                    blockStatement.getStatements().add(index, new ExpressionStatement(createNewAddDescriptionMethodCall(commentContent)));
                    index += (popCommentStatements(commentStatementsStack, blockStatement, index, indexMap, nestedLevel) + 1);
                }
                String keywordName = getKeywordNameForStatement(statement);
                blockStatement.getStatements().add(index, new ExpressionStatement(createNewStartKeywordMethodCall(keywordName, statement, indexMap, nestedLevel)));
            }
            visit(statement, new Stack<Statement>(), indexMap, nestedLevel + 1);
            index += 2;
        }
    }

    @CompileStatic
    private int popCommentStatements(Stack<Statement> commentStatementsStack, BlockStatement blockStatement, int index, Map<Statement, Integer> indexMap, int nestedLevel) {
        int commentNumber = 0;
        while (commentStatementsStack != null && !commentStatementsStack.isEmpty()) {
            Statement commentStatement = commentStatementsStack.pop();
            blockStatement.getStatements().add(index, new ExpressionStatement(createNewStartKeywordMethodCall(
                    COMMENT_STATEMENT_KEYWORD_NAME + " - " + AstTextValueUtil.getTextValue(commentStatement), commentStatement, indexMap, nestedLevel)));
            commentNumber++;
        }
        return commentNumber;
    }

    @CompileStatic
    private String getKeywordNameForStatement(Statement statement) {
        String keywordName = null;
        String builtinKeywordName = getBuiltinKeywordMethodCallStatement(statement);
        if (builtinKeywordName != null) {
            keywordName = builtinKeywordName;
        } else {
            String customKeywordName = getCustomKeywordMethodCallStatement(statement);
            if (customKeywordName != null) {
                keywordName = customKeywordName;
            } else {
                keywordName = KEYWORD_DEFAULT_NAME + " - " + AstTextValueUtil.getTextValue(statement);
            }
        }
        return keywordName;
    }

    @CompileStatic
    private String getBuiltinKeywordMethodCallStatement(Statement statement) {
        if (!(statement instanceof ExpressionStatement)) {
            return null;
        }
        ExpressionStatement expressionStatement = (ExpressionStatement) statement;
        if (!(expressionStatement.getExpression() instanceof MethodCallExpression)) {
            return null;
        }
        MethodCallExpression methodCall = (MethodCallExpression) expressionStatement.getExpression();
        Class<?> methodClass = loadClass(methodCall.getObjectExpression().getText());
        if (methodClass != null && BuiltinKeywords.class.isAssignableFrom(methodClass)) {
            return methodCall.getMethod().getText();
        }
        return null;
    }

    @CompileStatic
    private String getCustomKeywordMethodCallStatement(Statement statement) {
        if (!(statement instanceof ExpressionStatement)) {
            return null;
        }
        ExpressionStatement expressionStatement = (ExpressionStatement) statement;
        if (!(expressionStatement.getExpression() instanceof MethodCallExpression)) {
            return null;
        }
        MethodCallExpression methodCall = (MethodCallExpression) expressionStatement.getExpression();
        if (methodCall.getObjectExpression().getText().equals(StringConstants.CUSTOM_KEYWORD_CLASS_NAME)) {
            return methodCall.getMethod().getText();
        }
        return null;
    }

    @CompileStatic
    private MethodCallExpression createNewAddDescriptionMethodCall(String comment) {
        List<Expression> expressionArguments = new ArrayList<Expression>();
        MethodCallExpression loggerGetInstanceMethodCall = new MethodCallExpression(
                new ClassExpression(new ClassNode(KeywordLogger.class)), KEYWORD_LOGGER_GET_INSTANCE_METHOD_NAME,
                new ArgumentListExpression(expressionArguments));
        expressionArguments = new ArrayList<Expression>();
        expressionArguments.add(new ConstantExpression(comment));
        MethodCallExpression methodCall = new MethodCallExpression(loggerGetInstanceMethodCall,
                KEYWORD_LOGGER_SET_PENDING_DESCRIPTION_METHOD_NAME, new ArgumentListExpression(expressionArguments))
        return methodCall
    }

    @CompileStatic
    private MethodCallExpression createNewStartKeywordMethodCall(String keywordName, Statement statement, Map<Statement, Integer> indexMap, int nestedLevel) {
        List<Expression> expressionArguments = new ArrayList<Expression>();
        MethodCallExpression loggerGetInstanceMethodCall = new MethodCallExpression(
                new ClassExpression(new ClassNode(KeywordLogger.class)), KEYWORD_LOGGER_GET_INSTANCE_METHOD_NAME,
                new ArgumentListExpression(expressionArguments));
        expressionArguments = new ArrayList<Expression>();
        expressionArguments.add(new ConstantExpression(keywordName));
        expressionArguments.add(createPropertiesMapExpressionForKeyword(statement, indexMap));
        expressionArguments.add(new ConstantExpression(nestedLevel));
        MethodCallExpression methodCall = new MethodCallExpression(loggerGetInstanceMethodCall,
                StringConstants.LOG_START_KEYWORD_METHOD, new ArgumentListExpression(expressionArguments))
        return methodCall
    }

    @CompileStatic
    private MapExpression createPropertiesMapExpressionForKeyword(Statement statement, Map<Statement, Integer> indexMap) {
        List<MapEntryExpression> mapEntryList = new ArrayList<MapEntryExpression>();
        mapEntryList.add(new MapEntryExpression(new ConstantExpression(StringConstants.XML_LOG_START_LINE_PROPERTY), new ConstantExpression(statement.getLineNumber().toString())));
        mapEntryList.add(new MapEntryExpression(new ConstantExpression(StringConstants.XML_LOG_STEP_INDEX), new ConstantExpression(indexMap.get(statement).toString())));
        MapExpression map = new MapExpression(mapEntryList)
        return map
    }

    @CompileStatic
    private static Map<Statement, Integer> getIndexMapForBlockStatement(BlockStatement blockStatement) {
        Map<Statement, Integer> indexMap = new HashMap<Statement, Integer>();
        Statement pendingDescriptionStatement = null;
        int pendingDescriptionIndex = -1;
        int index = 1;
        for (Statement statement : blockStatement.getStatements()) {
            if (statement instanceof IfStatement) {
                IfStatement ifStatement = (IfStatement) statement;
                indexMap.put(ifStatement, index);
                index++;
                pendingDescriptionStatement = null;
                while (ifStatement.getElseBlock() instanceof IfStatement) {
                    ifStatement = (IfStatement) ifStatement.getElseBlock();
                    indexMap.put(ifStatement, index);
                    index++;
                }
                if (ifStatement.getElseBlock() == null || ifStatement.getElseBlock() instanceof EmptyStatement) {
                    continue;
                }
                indexMap.put(ifStatement.getElseBlock(), index);
                index++;
            } else if (statement instanceof TryCatchStatement) {
                TryCatchStatement tryCatchStatement = (TryCatchStatement) statement;
                indexMap.put(tryCatchStatement, index);
                index++;
                pendingDescriptionStatement = null;
                for (CatchStatement catchStatement : tryCatchStatement.getCatchStatements()) {
                    indexMap.put(catchStatement, index);
                    index++;
                }
                if (tryCatchStatement.getFinallyStatement() == null) {
                    continue;
                }
                indexMap.put(tryCatchStatement.getFinallyStatement(), index);
                index++;
            } else if (statement instanceof SwitchStatement) {
                SwitchStatement switchStatement = (SwitchStatement) statement;
                indexMap.put(switchStatement, index);
                pendingDescriptionStatement = null;
                String caseStringPrefix = index + ".";
                index++;
                int caseIndex = 1;
                for (CaseStatement caseStatement : switchStatement.getCaseStatements()) {
                    indexMap.put(caseStatement, caseIndex);
                    caseIndex++;
                }
                if (switchStatement.getDefaultStatement() == null) {
                    continue;
                }
                indexMap.put(switchStatement.getDefaultStatement(), caseIndex);
            } else if (statement instanceof ExpressionStatement
            && ((ExpressionStatement) statement).getExpression() instanceof ConstantExpression
            && ((ConstantExpression) ((ExpressionStatement) statement).getExpression()).getValue() instanceof String) {
                if (pendingDescriptionStatement != null) {
                    indexMap.put(pendingDescriptionStatement, pendingDescriptionIndex);
                    index++
                }
                pendingDescriptionStatement = statement;
                pendingDescriptionIndex = index;
            } else {
                indexMap.put(statement, index);
                index++;
                pendingDescriptionStatement = null;
            }
        }
        return indexMap;
    }
}