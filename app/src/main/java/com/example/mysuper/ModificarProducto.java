package com.example.mysuper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mysuper.models.ProductoModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ModificarProducto extends AppCompatActivity {
    private EditText textViewActualizarCantidad, textViewActualizarProducto, textViewActualizarPrecio;
    private Button buttonActualizar;

    private ProductoModel model;

    private String idUser = FirebaseAuth.getInstance().getUid();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference = database.getReference().child("Usuarios").child(idUser).child("Producto");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Modificar Producto");
        setContentView(R.layout.activity_modificar_producto);

        textViewActualizarCantidad = findViewById(R.id.textViewActualizarCantidad);
        textViewActualizarProducto = findViewById(R.id.textViewActualizarProducto);
        textViewActualizarPrecio = findViewById(R.id.textViewActualizarPrecio);
        buttonActualizar = findViewById(R.id.buttonActualizar);
        model = new ProductoModel();

        String id = getIntent().getStringExtra("id");
        if (id != null && !id.equals("")){
            reference.child(id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    model = dataSnapshot.getValue(ProductoModel.class);
                    if(model!=null){
                        textViewActualizarCantidad.setText(model.getCantidad());
                        textViewActualizarProducto.setText(model.getProducto());
                        textViewActualizarPrecio.setText(model.getPrecio());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ModificarProducto.this,"Error Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }

        buttonActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cantidad = textViewActualizarCantidad.getText().toString();
                String producto = textViewActualizarProducto.getText().toString();
                String  precio = textViewActualizarPrecio.getText().toString();

                if(cantidad.isEmpty()){
                    textViewActualizarCantidad.setError("Cantidad no debe quedar vacío");
                } else if(producto.isEmpty()){
                    textViewActualizarProducto.setError("Producto no debe quedar vacío");
                }else if(precio.isEmpty()){
                    textViewActualizarPrecio.setError("Precio no debe quedar vacío");
                } else{
                    Long total = Long.parseLong(cantidad) * Long.parseLong(precio);
                    String id = model.getId();
                    if(id != null && !id.equals("")){
                        final Double totalModificado = Double.parseDouble(cantidad) * Double.parseDouble(precio);
                        model.setCantidad(cantidad);
                        model.setProducto(producto);
                        model.setPrecio(precio);
                        model.setTotal(String.valueOf(totalModificado));
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


                    Toast.makeText(ModificarProducto.this,"Producto Actualizado", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }
}