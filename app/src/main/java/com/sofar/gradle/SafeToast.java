package com.sofar.gradle;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.widget.Toast;

public class SafeToast {

  private static Map<Integer, CharSequence> store = new HashMap<>();
  private static Map<Integer, Integer> store2 = new HashMap<>();

  public static Toast makeToast(Context context, CharSequence c, int len) {
    Toast toast = Toast.makeText(context, c, len);
    store.put(toast.hashCode(), c);
    return toast;
  }

  public static Toast makeToast(Context context, int c, int len) {
    Toast toast = Toast.makeText(context, c, len);
    store2.put(toast.hashCode(), c);
    return toast;
  }

  public static void showToastContent(Toast toast) {
    int hashCode = toast.hashCode();
    if (store.containsKey(hashCode)) {
      CharSequence c = store.get(hashCode);
      toast.setText("我Hook了" + c);
      toast.show();
    } else if (store2.containsKey(hashCode)) {
    } else {
      toast.setText("我Hook了 toast");
      toast.show();
    }
  }

}
