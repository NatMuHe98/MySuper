package com.example.mysuper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.ColorSpace;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mysuper.models.ProductoModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NuevoProducto extends AppCompatActivity {
    EditText editTextCantidad, editTextProducto, editTextPrecio;
    String idUser = FirebaseAuth.getInstance().getUid();

    ProductoModel model;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference = database.getReference().child("Usuarios").child(idUser).child("Producto");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_producto);

        editTextCantidad = (EditText) findViewById(R.id.editTextCantidad);
        editTextProducto = (EditText) findViewById(R.id.editTextProducto);
        editTextPrecio = (EditText) findViewById(R.id.editTextPrecio);

    }

    public void AgregarProducto(final View view){
        String cantidad = editTextCantidad.getText().toString();
        String producto = editTextProducto.getText().toString();
        String  precio = editTextPrecio.getText().toString();

        if(cantidad.isEmpty()){
            editTextCantidad.setError("Cantidad no debe quedar vacío");
        } else if(producto.isEmpty()){
            editTextProducto.setError("Producto no debe quedar vacío");
        }else if(precio.isEmpty()){
            editTextPrecio.setError("Precio no debe quedar vacío");
        } else{
            Long total = Long.parseLong(cantidad) * Long.parseLong(precio);
            String id = reference.push().getKey();
            if(id != null){
                model = new ProductoModel(id, cantidad, producto, precio, String.valueOf(total));
                reference.child(id).setValue(model)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("database", "no se pudo guardar");
                            }
                        });
            }else{
                Log.d("database", "falta id");
            }


            Toast.makeText(this,"Producto Agregado", Toast.LENGTH_SHORT).show();

        }

    }
}