package com.example.myapplication.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.utils.PrivateKey;
import com.example.myapplication.utils.SecureStorage;
import com.example.myapplication.R;
import com.example.myapplication.models.User;
import com.example.myapplication.utils.AESUtil;
import com.example.myapplication.utils.MyKeyPair;
import com.example.myapplication.utils.RSAUtils;
import com.example.myapplication.utils.SecureStorageException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.math.BigInteger;


public class LoginActivity extends AppCompatActivity {
    private EditText etName, etPassword,etEmail, etP, etQ;
    private Button btnRegister;
    // En tu clase Application o Activity principal
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etP = findViewById(R.id.etP);
        etQ = findViewById(R.id.etQ);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            // Validar campos
            String password = etPassword.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String bigInt1Str = etP.getText().toString().trim();
            String bigInt2Str = etQ.getText().toString().trim();
            int num1 = Integer.parseInt(bigInt1Str);
            int num2 = Integer.parseInt(bigInt2Str);
            BigInteger p = RSAUtils.generarNumeroConDigitos(num1);
            BigInteger q =RSAUtils.generarNumeroConDigitos(num2);

            // Al inicio de la validación, antes de procesar los números
            if (num1 < 150 || num2 < 150) {
                etP.setError("p Debe tener al menos 150 dígitos");
                etQ.setError("q Debe tener al menos 150 dígitos");
                return;
            }
            if (num1 > 300 || num2 > 300) {
                etP.setError(" P Debe tener menos de 300 dígitos");
                etQ.setError(" q Debe tener menos de 300 dígitos");
                return;
            }
            if (!RSAUtils.esPrimo(p)) {
                p = RSAUtils.primoMasCercano(p);
            }

            if (!RSAUtils.esPrimo(q)) {
                q = RSAUtils.primoMasCercano(q);
            }

            if (p.equals(q)) {
                p = p.add(BigInteger.ONE);
                if (!RSAUtils.esPrimo(p)) {
                    p = RSAUtils.primoMasCercano(p);
                }
            }


// Convertir a String
            if (bigInt1Str.isEmpty() || bigInt2Str.isEmpty()) {
                Toast.makeText(this, "Ingresa la cantidad de dígitos para p y q", Toast.LENGTH_SHORT).show();
                return;
            }


            if (name.isEmpty() || email.isEmpty() ||
                    bigInt1Str.isEmpty() || bigInt2Str.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Convertir Strings a BigIntegers
                //BigInteger bigInt1 = new BigInteger(bigInt1Str);
                //BigInteger bigInt2 = new BigInteger(bigInt2Str);
                String encryptionKey = name+password;
                // Registrar usuario
                registrarUsuario(name, email, password, p, q, encryptionKey);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Los valores numéricos no son válidos", Toast.LENGTH_SHORT).show();
            } catch (SecureStorageException e) {
                throw new RuntimeException(e);
            }
        });


    }

    boolean userregister=false;
    public void registrarUsuario(String nombre, String email, String password,
                                 BigInteger bigInteger1, BigInteger bigInteger2,
                                 String claveEncriptacion) throws SecureStorageException {

        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Registrando usuario...");
        progress.setCancelable(false);
        progress.show();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Registro", "Usuario autenticado en FirebaseAuth");

                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        String userId = firebaseUser.getUid();

                        try {
                            MyKeyPair mykeyPair = RSAUtils.generateKeyPair(bigInteger1, bigInteger2);
                            //String publicKeyStr = Base64.encodeToString(mykeyPair.getPublicKey().getEncoded(), Base64.DEFAULT);
                            String publicKeyStr = mykeyPair.getPublicKey().toStringRepresentation();
                            String privateKeyStr = mykeyPair.getPrivateKey().getD().toString() + "|" + mykeyPair.getPrivateKey().getN().toString();
                            String privateKeyEncrypted = AESUtil.encrypt(privateKeyStr, claveEncriptacion);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                try {
                                    SecureStorage.savePrivateKey(mykeyPair.getPrivateKey(), this);
                                    PrivateKey storedKey = SecureStorage.getPrivateKey(this);
                                    if (storedKey == null ||
                                            !storedKey.getD().toString().equals(mykeyPair.getPrivateKey().getD().toString()) ||
                                            !storedKey.getN().toString().equals(mykeyPair.getPrivateKey().getN().toString())) {
                                        Log.e("Validación", "D esperado: " + mykeyPair.getPrivateKey().getD());
                                        Log.e("Validación", "D obtenido: " + storedKey.getD());
                                        Log.e("Validación", "N esperado: " + mykeyPair.getPrivateKey().getN());
                                        Log.e("Validación", "N obtenido: " + storedKey.getN());
                                        throw new SecureStorageException("Validación de clave fallida");
                                    }

                                } catch (SecureStorageException e) {
                                    handleRegistrationError(progress, firebaseUser,
                                            "Error crítico de seguridad. Registro abortado.");
                                    return;
                                }
                            }

                            User usuario = new User(
                                    userId, nombre, email,
                                    bigInteger1.toString(), bigInteger2.toString(),
                                    publicKeyStr, privateKeyEncrypted
                            );

                            db.collection("usuarios").document(userId)
                                    .set(usuario)
                                    .addOnSuccessListener(aVoid -> {
                                        progress.dismiss();  // ✅ Cerrar al completar
                                        Log.d("Registro", "Usuario guardado en Firestore");
                                        // 🔹 Obtener token FCM
                                        FirebaseMessaging.getInstance().getToken()
                                                .addOnCompleteListener(tokenTask -> {
                                                    if (tokenTask.isSuccessful()) {
                                                        String fcmToken = tokenTask.getResult();
                                                        if (fcmToken != null) {
                                                            db.collection("usuarios").document(userId)
                                                                    .update("fcmToken", fcmToken)
                                                                    .addOnSuccessListener(aVoid1 ->
                                                                            Log.d("Registro", "Token FCM guardado"))
                                                                    .addOnFailureListener(e ->
                                                                            Log.e("Registro", "Error guardando token FCM", e));
                                                        }
                                                    } else {
                                                        Log.e("Registro", "No se pudo obtener token FCM", tokenTask.getException());
                                                    }
                                                });
                                        startActivity(new Intent(LoginActivity.this, ChatListActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progress.dismiss();
                                        Log.e("Registro", "Error al guardar en Firestore", e);
                                        Toast.makeText(this, "Error guardando usuario", Toast.LENGTH_SHORT).show();
                                        firebaseUser.delete();
                                    });

                        } catch (Exception e) {
                            progress.dismiss();
                            Log.e("Registro", "Error al generar claves RSA", e);
                            Toast.makeText(this, "Error generando claves", Toast.LENGTH_SHORT).show();
                            firebaseUser.delete();
                        }

                    } else {
                        progress.dismiss();
                        Log.e("Registro", "Error FirebaseAuth", task.getException());
                        Toast.makeText(this, "Error creando usuario: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });


    }
    // Crear un método helper para manejo consistente de errores
    private void handleRegistrationError(ProgressDialog progress, FirebaseUser user, String message) {
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        if (user != null) {
            user.delete();
        }
    }



}