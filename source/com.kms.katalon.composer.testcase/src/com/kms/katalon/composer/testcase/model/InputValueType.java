package com.kms.katalon.composer.testcase.model;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.openqa.selenium.Keys;

import com.kms.katalon.composer.testcase.ast.editors.ClassExpressionTypeSelectionDialogCellEditor;
import com.kms.katalon.composer.testcase.ast.editors.ClassNodeTypeSelectionDialogCellEditor;
import com.kms.katalon.composer.testcase.ast.editors.PropertyTypeSelectionDialogCellEditor;
import com.kms.katalon.composer.testcase.ast.editors.VariableTypeSelectionDialogCellEditor;
import com.kms.katalon.composer.testcase.groovy.ast.ASTNodeWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.ClassNodeWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.BinaryExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.BooleanExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.ClassExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.ClosureListExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.ConstantExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.ConstructorCallExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.ListExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.MapExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.MethodCallExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.PropertyExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.RangeExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.VariableExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.statements.ThrowStatementWrapper;
import com.kms.katalon.composer.testcase.util.AstEntityInputUtil;
import com.kms.katalon.composer.testcase.util.AstValueUtil;

public enum InputValueType {
    String,
    Number,
    Boolean,
    Null,
    Variable,
    MethodCall,
    List,
    Map,
    ClosureList,
    Condition,
    Binary,
    Range,
    Property,
    GlobalVariable,
    TestDataValue,
    TestCase,
    TestObject,
    TestData,
    Class,
    This,
    Throwable,
    Keys,
    Key;

    private static final java.lang.String THIS_VARIABLE = "this";

    public String getName() {
        return name();
    }

    public boolean isEditable(Object astObject) {
        if (this == Null) {
            return false;
        }
        return true;
    }

    public CellEditor getCellEditorForValue(Composite parent, Object astObject) {
        switch (this) {
            case Binary:
                return AstValueUtil.getCellEditorForBinaryExpression(parent,
                        (BinaryExpressionWrapper) astObject);
            case Boolean:
                return AstValueUtil.getCellEditorForBooleanConstantExpression(parent);
            case Class:
                return getCellEditorForClass(parent, astObject);
            case ClosureList:
                return AstValueUtil.getCellEditorForClosureListExpression(parent,
                        (ClosureListExpressionWrapper) astObject);
            case Condition:
                return AstValueUtil.getCellEditorForBooleanExpression(parent,
                        (BooleanExpressionWrapper) astObject);
            case GlobalVariable:
                return AstValueUtil.getCellEditorForGlobalVariableExpression(parent);
            case List:
                return AstValueUtil.getCellEditorForListExpression(parent, (ListExpressionWrapper) astObject);
            case Map:
                return AstValueUtil.getCellEditorForMapExpression(parent, (MapExpressionWrapper) astObject);
            case MethodCall:
                return AstValueUtil.getCellEditorForMethodCallExpression(parent,
                        (MethodCallExpressionWrapper) astObject);
            case Number:
                return AstValueUtil.getCellEditorForNumberConstantExpression(parent);
            case Property:
                return AstValueUtil.getCellEditorForPropertyExpression(parent,
                        (PropertyExpressionWrapper) astObject);
            case Range:
                return AstValueUtil.getCellEditorForRangeExpression(parent, (RangeExpressionWrapper) astObject);
            case String:
                return AstValueUtil.getCellEditorForStringConstantExpression(parent);
            case TestCase:
                return AstValueUtil.getCellEditorForCallTestCase(parent,
                        (MethodCallExpressionWrapper) astObject);
            case TestData:
                return AstValueUtil.getCellEditorForTestData(parent, (MethodCallExpressionWrapper) astObject);
            case TestDataValue:
                return AstValueUtil.getCellEditorForTestDataValue(parent,
                        (MethodCallExpressionWrapper) astObject);
            case TestObject:
                return AstValueUtil.getCellEditorForTestObject(parent, (MethodCallExpressionWrapper) astObject);
            case Throwable:
                return AstValueUtil.getCellEditorForThrowable(parent,
                        (ConstructorCallExpressionWrapper) astObject);
            case Variable:
                return AstValueUtil.getCellEditorForVariableExpression(parent,
                        (VariableExpressionWrapper) astObject);
            case Key:
                return AstValueUtil.getCellEditorForKeyExpression(parent);
            case Keys:
                return AstValueUtil.getCellEditorForKeysExpression(parent,
                        (MethodCallExpressionWrapper) astObject);
            default:
                return null;
        }
    }

    private CellEditor getCellEditorForClass(Composite parent, Object astObject) {
        if (astObject instanceof ClassExpressionWrapper) {
            return new ClassExpressionTypeSelectionDialogCellEditor(parent,
                    ((ClassExpressionWrapper) astObject).getText());
        } else if (astObject instanceof VariableExpressionWrapper) {
            return new VariableTypeSelectionDialogCellEditor(parent,
                    ((VariableExpressionWrapper) astObject).getText());
        } else if (astObject instanceof PropertyExpressionWrapper) {
            return new PropertyTypeSelectionDialogCellEditor(parent,
                    ((PropertyExpressionWrapper) astObject).getText());
        } else if (astObject instanceof ClassNodeWrapper) {
            return new ClassNodeTypeSelectionDialogCellEditor(parent, ((ClassNodeWrapper) astObject).getName());
        }
        return null;
    }

    public Object getNewValue(ASTNodeWrapper parent) {
        switch (this) {
            case String:
                return new ConstantExpressionWrapper("", parent);
            case Number:
                return new ConstantExpressionWrapper(0, parent);
            case Boolean:
                return new ConstantExpressionWrapper(true, parent);
            case Null:
                return new ConstantExpressionWrapper(parent);
            case Binary:
                return new BinaryExpressionWrapper(parent);
            case Variable:
                return new VariableExpressionWrapper(parent);
            case MethodCall:
                return new MethodCallExpressionWrapper(parent);
            case Condition:
                return new BooleanExpressionWrapper(parent);
            case List:
                return new ListExpressionWrapper(parent);
            case Map:
                return new MapExpressionWrapper(parent);
            case ClosureList:
                return new ClosureListExpressionWrapper(parent);
            case Range:
                return new RangeExpressionWrapper(parent);
            case Property:
                return new PropertyExpressionWrapper(parent);
            case GlobalVariable:
                return new PropertyExpressionWrapper(InputValueType.GlobalVariable.name(), parent);
            case TestObject:
                return AstEntityInputUtil.createNewFindTestObjectMethodCall(parent);
            case Class:
                return new ClassExpressionWrapper(String.class, parent);
            case TestDataValue:
                return AstEntityInputUtil.createNewGetTestDataValueExpression(null, 1, 1, parent);
            case TestData:
                return AstEntityInputUtil.createNewFindTestDataExpression(null, parent);
            case TestCase:
                return AstEntityInputUtil.createNewFindTestCaseMethodCall(null, parent);
            case This:
                return new VariableExpressionWrapper(THIS_VARIABLE, parent);
            case Throwable:
                return new ConstructorCallExpressionWrapper(ThrowStatementWrapper.DEFAULT_THROW_TYPE, parent);
            case Key:
                parent.getScriptClass().addImport(Keys.class);
                return new PropertyExpressionWrapper(Keys.class.getSimpleName(), "ENTER", parent);
            case Keys:
                parent.getScriptClass().addImport(Keys.class);
                return new MethodCallExpressionWrapper(Keys.class, "chord", parent);
            default:
                return new ConstantExpressionWrapper(parent);
        }
    }

    public Object getValueToEdit(Object astObject) {
        return astObject;
    }

    public Object changeValue(Object astObject, Object newValue) {
        return newValue;
    }

    public String getValueToDisplay(Object astObject) {
        if (!(astObject instanceof ASTNodeWrapper)) {
            return "";
        }
        if (astObject instanceof MethodCallExpressionWrapper) {
            return getValueToDisplayForMethodCall(astObject);
        }
        return ((ASTNodeWrapper) astObject).getText();
    }

    private String getValueToDisplayForMethodCall(Object astObject) {
        MethodCallExpressionWrapper methodCall = (MethodCallExpressionWrapper) astObject;
        if (AstEntityInputUtil.isFindTestCaseMethodCall(methodCall)) {
            return AstEntityInputUtil.getTextValueForTestCaseArgument(methodCall);
        }
        if (AstEntityInputUtil.isCallTestCaseMethodCall(methodCall)) {
            return (methodCall.getArguments()).getExpression(0).getText();
        }
        if (AstEntityInputUtil.isFindTestObjectMethodCall(methodCall)) {
            return AstEntityInputUtil.getTextValueForTestObjectArgument(methodCall);
        }
        if (AstEntityInputUtil.isFindTestDataMethodCall(methodCall)) {
            return AstEntityInputUtil.getTextValueForTestDataArgument(methodCall);
        }
        if (AstEntityInputUtil.isGetTestDataValueMethodCall(methodCall)) {
            return AstEntityInputUtil.getTextValueForTestDataValueArgument(methodCall);
        }
        return methodCall.getText();
    }
}
