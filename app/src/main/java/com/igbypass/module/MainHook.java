package com.igbypass.module;

import android.app.Activity;
import android.os.Bundle;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.igbypass.module")) {
            XposedHelpers.findAndHookMethod("com.igbypass.module.MainActivity",
                lpparam.classLoader, "isModuleActive", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        return true;
                    }
                });
            return;
        }

        if (!lpparam.packageName.equals("com.myinsta.android")) return;
        XposedBridge.log("IGBypass: Loaded into MyInsta");

        XposedHelpers.findAndHookMethod(Activity.class, "onCreate",
            Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    String name = activity.getClass().getSimpleName().toLowerCase();
                    if (name.contains("upgrade") || name.contains("update")
                        || name.contains("force") || name.contains("nag")) {
                        XposedBridge.log("IGBypass: Killing update activity: " + name);
                        activity.finish();
                    }
                }
            });

        XposedHelpers.findAndHookMethod(Activity.class, "onResume",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    String name = activity.getClass().getSimpleName().toLowerCase();
                    if (name.contains("upgrade") || name.contains("update")
                        || name.contains("force") || name.contains("nag")) {
                        XposedBridge.log("IGBypass: Killing update activity onResume: " + name);
                        activity.finish();
                    }
                }
            });
    }
}
