package com.example.mysuper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mysuper.adapters.ProductoAdapter;
import com.example.mysuper.models.ProductoModel;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 16;
    List<AuthUI.IdpConfig> providers;

    Button btn_sign_out;
    ImageView btn_agregar_producto, btn_eliminar_lista;
    DatabaseReference mDatabase;

    private ArrayList<ProductoModel> list;
    private ProductoModel model;

    private ListView lv_producto;

    private String idUser = FirebaseAuth.getInstance().getUid();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference  =  database.getReference().child("Usuarios").child(idUser).child("Producto");;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Mi Super");
        setContentView(R.layout.activity_main);

        //Provedores de inicio de sesión
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build()
        );
        
        IniciarSesion();

        lv_producto = findViewById(R.id.lv_producto);
        list = new ArrayList<>();
        model = new ProductoModel();


        btn_agregar_producto = (ImageView) findViewById(R.id.imageView_agregar);
        btn_agregar_producto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,NuevoProducto.class);
                startActivity(i);
            }
        });


        btn_sign_out = (Button)findViewById(R.id.button_cerrar_sesion);
        btn_sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                btn_sign_out.setEnabled(false);
                                IniciarSesion();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list = new ArrayList<>();
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    model = child.getValue(ProductoModel.class);
                    list.add(model);
                }
                lv_producto.setAdapter(new ProductoAdapter(MainActivity.this, list));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,"Error Firebase", Toast.LENGTH_SHORT).show();
            }
        });

        lv_producto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                model = (ProductoModel) adapterView.getItemAtPosition(i);
                if(!model.getId().equals("") && model.getId() != null){
                    Intent editar = new Intent(MainActivity.this,EditarProducto.class);
                    editar.putExtra("id", model.getId());
                    startActivity(editar);
                }
            }
        });

        btn_eliminar_lista = (ImageView) findViewById(R.id.imageView_eliminar);
        btn_eliminar_lista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Eliminar Carrito de Compras");
                alertDialog.setMessage("Se eliminaran todos sus productos agregador actualmente.");
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Esto es para eliminar la lista completa
                        reference.removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, "Lista Eliminada", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("database", "no se pudo guardar");
                                    }
                                });
                    }
                });
                alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        });

    }

    private void IniciarSesion() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.MyBackground)
                        .setLogo(R.drawable.logo)
                .build(),MY_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if(!currentUser.isEmailVerified()){
                currentUser.sendEmailVerification()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this,
                                        "Se le ha enviado un correo para validar su cuenta",
                                        Toast.LENGTH_SHORT).show();
                                Log.d("infoApp","Envío de correo exitoso");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("infoApp","error al enviar el correo");
                            }
                        });
                IniciarSesion();
            }else{

            }
            if(resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(MainActivity.this, " "+user.getDisplayName(), Toast.LENGTH_SHORT).show();
                btn_agregar_producto.setEnabled(true);
            }else{
                Toast.makeText(MainActivity.this, " "+response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}