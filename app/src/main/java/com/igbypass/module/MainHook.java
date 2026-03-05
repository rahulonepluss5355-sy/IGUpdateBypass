package com.igbypass.module;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final String[] KEYWORDS = {
        "upgrade", "update", "force", "nag", "latest version", "boost", "new version"
    };

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Hook our own MainActivity to confirm module is active
        if (lpparam.packageName.equals("com.igbypass.module")) {
            XposedHelpers.findAndHookMethod("com.igbypass.module.MainActivity",
                lpparam.classLoader, "isXposedActive", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(true);
                    }
                });
            return;
        }

        if (!lpparam.packageName.equals("com.myinsta.android")) return;
        XposedBridge.log("IGBypass: Loaded into MyInsta");

        // Hook Activity lifecycle - use full class name for obfuscated classes
        XposedHelpers.findAndHookMethod(Activity.class, "onCreate",
            Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    String name = activity.getClass().getName().toLowerCase();
                    if (containsKeyword(name)) {
                        XposedBridge.log("IGBypass: Killing update activity: " + name);
                        activity.finish();
                        return;
                    }
                    // Scan view hierarchy for obfuscated update screens
                    try {
                        View decorView = activity.getWindow().getDecorView();
                        if (scanViewsForKeyword(decorView)) {
                            XposedBridge.log("IGBypass: Killing update screen via view scan (onCreate)");
                            activity.finish();
                        }
                    } catch (Throwable t) {
                        // ignore
                    }
                }
            });

        XposedHelpers.findAndHookMethod(Activity.class, "onResume",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    String name = activity.getClass().getName().toLowerCase();
                    if (containsKeyword(name)) {
                        XposedBridge.log("IGBypass: Killing update activity onResume: " + name);
                        activity.finish();
                        return;
                    }
                    // Scan view hierarchy for obfuscated update screens
                    try {
                        View decorView = activity.getWindow().getDecorView();
                        if (scanViewsForKeyword(decorView)) {
                            XposedBridge.log("IGBypass: Killing update screen via view scan (onResume)");
                            activity.finish();
                        }
                    } catch (Throwable t) {
                        // ignore
                    }
                }
            });

        // Hook Dialog.show() - catches AlertDialog and custom dialogs
        XposedHelpers.findAndHookMethod(Dialog.class, "show",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Dialog dialog = (Dialog) param.thisObject;

                    // Check dialog message text content
                    try {
                        TextView messageView = dialog.findViewById(android.R.id.message);
                        if (messageView != null) {
                            String text = messageView.getText().toString().toLowerCase();
                            if (containsKeyword(text)) {
                                XposedBridge.log("IGBypass: Dismissing update dialog (text): " + text);
                                dialog.dismiss();
                                return;
                            }
                        }
                    } catch (Throwable t) {
                        // ignore - dialog may not have standard message view
                    }

                    // Fallback: check class name
                    String name = dialog.getClass().getName().toLowerCase();
                    if (containsKeyword(name)) {
                        XposedBridge.log("IGBypass: Dismissing update dialog (name): " + name);
                        dialog.dismiss();
                    }
                }
            });
    }

    private boolean scanViewsForKeyword(View root) {
        if (root instanceof TextView) {
            CharSequence text = ((TextView) root).getText();
            if (text != null && containsKeyword(text.toString().toLowerCase())) {
                return true;
            }
        }
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                if (scanViewsForKeyword(group.getChildAt(i))) return true;
            }
        }
        return false;
    }

    private boolean containsKeyword(String text) {
        for (String keyword : KEYWORDS) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
}
