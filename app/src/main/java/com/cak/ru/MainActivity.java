package com.cak.ru;

import android.app.AlertDialog;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {
    
    private ListView listView;
    private ArrayList<HashMap<String, String>> items;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> displayItems;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "crud_data";
    private static final String KEY_DATA = "items_json";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        listView = findViewById(R.id.listView);
        items = new ArrayList<>();
        displayItems = new ArrayList<>();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Inisialisasi adapter FIRST
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayItems);
        listView.setAdapter(adapter);
        
        // THEN load data
        loadData();
        
        // Tombol tambah
        findViewById(R.id.btnAdd).setOnClickListener(v -> showDialog(-1, null, null));
        
        // Long click untuk hapus
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });
        
        // Click untuk edit
        listView.setOnItemClickListener((parent, view, position, id) -> {
            HashMap<String, String> item = items.get(position);
            showDialog(position, item.get("title"), item.get("desc"));
        });
    }
    
    private void showDialog(final int position, String title, String desc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_item, null);
        
        final EditText editTitle = view.findViewById(R.id.editTitle);
        final EditText editDesc = view.findViewById(R.id.editDesc);
        Button btnSave = view.findViewById(R.id.btnSave);
        
        if (title != null) {
            editTitle.setText(title);
            editDesc.setText(desc);
        }
        
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        
        btnSave.setOnClickListener(v -> {
            String newTitle = editTitle.getText().toString().trim();
            String newDesc = editDesc.getText().toString().trim();
            
            if (!newTitle.isEmpty()) {
                if (position == -1) {
                    // Tambah baru
                    HashMap<String, String> newItem = new HashMap<>();
                    newItem.put("title", newTitle);
                    newItem.put("desc", newDesc);
                    items.add(newItem);
                } else {
                    // Edit existing
                    HashMap<String, String> item = items.get(position);
                    item.put("title", newTitle);
                    item.put("desc", newDesc);
                }
                saveData();
                refreshList();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showDeleteDialog(final int position) {
        new AlertDialog.Builder(this)
            .setTitle("Hapus Data")
            .setMessage("Yakin ingin menghapus item ini?")
            .setPositiveButton("Ya", (dialog, which) -> {
                items.remove(position);
                saveData();
                refreshList();
                Toast.makeText(this, "Data dihapus", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Tidak", null)
            .show();
    }
    
    private void saveData() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (HashMap<String, String> item : items) {
                JSONObject obj = new JSONObject();
                obj.put("title", item.get("title"));
                obj.put("desc", item.get("desc"));
                jsonArray.put(obj);
            }
            prefs.edit().putString(KEY_DATA, jsonArray.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadData() {
        try {
            String jsonString = prefs.getString(KEY_DATA, "[]");
            JSONArray jsonArray = new JSONArray(jsonString);
            items.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                HashMap<String, String> item = new HashMap<>();
                item.put("title", obj.getString("title"));
                item.put("desc", obj.getString("desc"));
                items.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        refreshList();
    }
    
    private void refreshList() {
        displayItems.clear();
        for (HashMap<String, String> item : items) {
            displayItems.add(item.get("title"));
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
