package com.sofar.gradle;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    B b = new B();
    b.run();

    toastTest();
  }

  private void toastTest() {
    Button button = findViewById(R.id.toast_btn);
    button.setOnClickListener(v -> {
      Toast.makeText(this, "toast测试", Toast.LENGTH_LONG).show();
    });
  }
}