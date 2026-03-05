package com.igbypass.module;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
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
        "upgrade", "force update", "nag", "latest version", "new version",
        "update your instagram", "update your app", "update the app"
    };

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
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

        // PRIMARY: Hook TextView.setText - catches update text the moment it is set,
        // regardless of whether it's in an Activity, Fragment, or bottom sheet.
        XposedHelpers.findAndHookMethod(TextView.class, "setText",
            CharSequence.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    CharSequence text = (CharSequence) param.args[0];
                    if (text == null || text.length() < 5 || text.length() > 200) return;
                    final String lower = text.toString().toLowerCase();
                    if (!containsKeyword(lower)) return;

                    final TextView tv = (TextView) param.thisObject;
                    // Post so we don't interfere with the setText call itself
                    tv.post(new Runnable() {
                        @Override
                        public void run() {
                            Activity activity = getActivity(tv.getContext());
                            if (activity != null && !activity.isFinishing()) {
                                XposedBridge.log("IGBypass: Killing via setText hook: " + lower);
                                activity.finish();
                            }
                        }
                    });
                }
            });

        // FALLBACK: Activity lifecycle hooks
        XposedHelpers.findAndHookMethod(Activity.class, "onCreate",
            Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    checkActivity((Activity) param.thisObject, "onCreate");
                }
            });

        XposedHelpers.findAndHookMethod(Activity.class, "onResume",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    final Activity activity = (Activity) param.thisObject;
                    if (checkActivity(activity, "onResume")) return;
                    activity.getWindow().getDecorView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkActivity(activity, "onResume+delay");
                        }
                    }, 500);
                }
            });

        XposedHelpers.findAndHookMethod(Activity.class, "onWindowFocusChanged",
            boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!(boolean) param.args[0]) return;
                    checkActivity((Activity) param.thisObject, "onWindowFocusChanged");
                }
            });

        // Dialog hook
        XposedHelpers.findAndHookMethod(Dialog.class, "show",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Dialog dialog = (Dialog) param.thisObject;
                    try {
                        TextView msg = dialog.findViewById(android.R.id.message);
                        if (msg != null) {
                            String text = msg.getText().toString().toLowerCase();
                            if (containsKeyword(text)) {
                                XposedBridge.log("IGBypass: Dismissing update dialog: " + text);
                                dialog.dismiss();
                                return;
                            }
                        }
                    } catch (Throwable t) { /* ignore */ }
                    String name = dialog.getClass().getName().toLowerCase();
                    if (containsKeyword(name)) {
                        XposedBridge.log("IGBypass: Dismissing update dialog (name): " + name);
                        dialog.dismiss();
                    }
                }
            });
    }

    private Activity getActivity(Context ctx) {
        if (ctx instanceof Activity) return (Activity) ctx;
        if (ctx instanceof ContextWrapper) return getActivity(((ContextWrapper) ctx).getBaseContext());
        return null;
    }

    private boolean checkActivity(Activity activity, String trigger) {
        if (activity == null || activity.isFinishing()) return false;
        String name = activity.getClass().getName().toLowerCase();
        if (containsKeyword(name)) {
            XposedBridge.log("IGBypass: Killing update activity [" + trigger + "]: " + name);
            activity.finish();
            return true;
        }
        try {
            View decorView = activity.getWindow().getDecorView();
            if (scanViewsForKeyword(decorView)) {
                XposedBridge.log("IGBypass: Killing update screen via view scan [" + trigger + "]");
                activity.finish();
                return true;
            }
        } catch (Throwable t) { /* ignore */ }
        return false;
    }

    private boolean scanViewsForKeyword(View root) {
        if (root instanceof TextView) {
            CharSequence text = ((TextView) root).getText();
            if (text != null && containsKeyword(text.toString().toLowerCase())) return true;
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
