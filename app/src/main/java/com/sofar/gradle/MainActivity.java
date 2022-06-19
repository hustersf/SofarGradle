package com.sofar.gradle;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.sofar.account.IAccountServiceV2;
import com.sofar.router.log.Debugger;
import com.sofar.router.log.DefaultLogger;
import com.sofar.router.service.ServiceLoader;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    B b = new B();
    b.run();

    ServiceLoader.init();
    toastTest();
    routerTest();
  }

  private void toastTest() {
    Button button = findViewById(R.id.toast_btn);
    button.setOnClickListener(v -> {
      Toast.makeText(this, "toast测试", Toast.LENGTH_LONG).show();
    });
  }

  private void routerTest() {
    Debugger.setLogger(DefaultLogger.INSTANCE);
    Debugger.setEnableLog(true);
    Button button = findViewById(R.id.router_btn);
    button.setOnClickListener(v -> {
      ServiceLoader.get(IAccountServiceV2.class).login(this);
    });
  }
}