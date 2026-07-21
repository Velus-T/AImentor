package com.example.aimentor.activities.categories;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aimentor.R;
import com.example.aimentor.activities.MenuActivity;
import com.example.aimentor.repository.CategoryRepository;

public class AddCategoryActivity extends AppCompatActivity {
    EditText edtCategoryName, edtDescription;
    Button btnSave, btnBack;
    CategoryRepository categoryRepository;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        edtCategoryName = findViewById(R.id.edtCategoryName);
        edtDescription  = findViewById(R.id.edtDescription);
        btnSave = findViewById(R.id.btnSaveCategory);
        btnBack = findViewById(R.id.btnBackCategory);
        categoryRepository = new CategoryRepository(AddCategoryActivity.this);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtCategoryName.getText().toString().trim();
                if (TextUtils.isEmpty(name)){
                    edtCategoryName.setError("Name's Category is required");
                    return;
                }
                String description = edtDescription.getText().toString().trim();
                long insert = categoryRepository.saveNewCategory(name, description);
                if (insert == -1) {
                    // Error
                    Toast.makeText(AddCategoryActivity.this, "Created fail", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(AddCategoryActivity.this, "Created successfully", Toast.LENGTH_SHORT).show();
                // quay ve lai menu
                Intent intent = new Intent(AddCategoryActivity.this, MenuActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("MENU_TAB", "Category");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddCategoryActivity.this, MenuActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("MENU_TAB", "Category");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}
