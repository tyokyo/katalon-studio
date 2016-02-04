package com.kms.katalon.composer.testcase.ast.treetable;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;

import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.testcase.constants.StringConstants;
import com.kms.katalon.composer.testcase.util.AstTreeTableUtil;

public class AstTryStatementTreeTableNode extends AstStatementTreeTableNode {
    private TryCatchStatement tryCatchStatement;

    public AstTryStatementTreeTableNode(TryCatchStatement statement, AstTreeTableNode parentNode, ASTNode parentObject,
            ClassNode scriptClass) {
        super(statement, parentNode, parentObject, scriptClass);
        tryCatchStatement = statement;
    }

    @Override
    public String getItemText() {
        return StringConstants.TREE_TRY_STATEMENT;
    }

    @Override
    public boolean hasChildren() {
        return tryCatchStatement.getTryStatement() != null;
    }

    @Override
    public void reloadChildren() {
        try {
            children = AstTreeTableUtil.getChildren(tryCatchStatement.getTryStatement(), this,
                    tryCatchStatement.getTryStatement(), scriptClass);
        } catch (Exception e) {
            LoggerSingleton.logError(e);
        }
    }

    @Override
    public void addChildObject(ASTNode astObject, int index) {
        AstTreeTableUtil.addChild(tryCatchStatement.getTryStatement(), astObject, index);
    }

    @Override
    public void removeChildObject(ASTNode astObject) {
        AstTreeTableUtil.removeChild(tryCatchStatement.getTryStatement(), astObject);
    }

    @Override
    public int getChildObjectIndex(ASTNode astObject) {
        return AstTreeTableUtil.getIndex(tryCatchStatement.getTryStatement(), astObject);
    }

}
