package com.igbypass.module;

import android.app.AlertDialog;
import android.app.Dialog;
import android.view.View;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TARGET_PACKAGE = "com.myinsta.android";
    private static final String TAG = "IGUpdateBypass";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) return;
        XposedBridge.log(TAG + ": Hooks applying...");
        hookDialogShow();
        hookAlertDialogBuilder();
    }

    private void hookDialogShow() {
        XposedHelpers.findAndHookMethod(Dialog.class, "show", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Dialog dialog = (Dialog) param.thisObject;
                    if (isUpdateDialog(dialog)) {
                        param.setResult(null);
                    }
                } catch (Throwable t) {}
            }
        });
    }

    private void hookAlertDialogBuilder() {
        XposedHelpers.findAndHookMethod(
            AlertDialog.Builder.class, "setMessage", CharSequence.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        CharSequence msg = (CharSequence) param.args[0];
                        if (msg != null && isUpdateRelatedText(msg.toString())) {
                            param.args[0] = "";
                        }
                    } catch (Throwable t) {}
                }
            }
        );
    }

    private boolean isUpdateDialog(Dialog dialog) {
        try {
            if (dialog.getWindow() != null) {
                return containsUpdateText(dialog.getWindow().getDecorView());
            }
        } catch (Throwable t) {}
        return false;
    }

    private boolean containsUpdateText(View view) {
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();
            if (text != null && isUpdateRelatedText(text.toString())) return true;
        }
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                if (containsUpdateText(group.getChildAt(i))) return true;
            }
        }
        return false;
    }

    private boolean isUpdateRelatedText(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return lower.contains("update your instagram")
            || lower.contains("update instagram")
            || lower.contains("latest version")
            || lower.contains("force_upgrade")
            || lower.contains("forceupgrade")
            || lower.contains("update required")
            || lower.contains("upgrade your app");
    }
}
