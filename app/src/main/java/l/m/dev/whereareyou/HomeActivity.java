package l.m.dev.whereareyou;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity"; // Identificador para logs
    private EditText etEmailSearch;
    private ListView lvFriends;
    private Button btnAddFriend, btnProfile, btnNotifications, btnFriends, btnLogout;
    private ArrayAdapter<String> friendsAdapter;
    private ArrayList<String> friendsList;
    private ListView lvNotifications;
    private ArrayList<String> notificationsList;
    private ArrayAdapter<String> notificationsAdapter;


    private FirebaseAuth auth;
    private DatabaseReference database;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d(TAG, "onCreate: Inicializando elementos de la interfaz");
        // Inicializar los elementos de la interfaz
        etEmailSearch = findViewById(R.id.etEmailSearch);
        lvFriends = findViewById(R.id.lvFriends);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        btnProfile = findViewById(R.id.btnProfile);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnFriends = findViewById(R.id.btnFriends);
        btnLogout = findViewById(R.id.btnLogout);

        lvNotifications = findViewById(R.id.lvNotifications);
        notificationsList = new ArrayList<>();
        notificationsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notificationsList);
        lvNotifications.setAdapter(notificationsAdapter);


        // Inicializar la lista de amigos y el adaptador
        friendsList = new ArrayList<>();
        friendsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, friendsList);
        lvFriends.setAdapter(friendsAdapter);

        Log.d(TAG, "onCreate: Inicializando Firebase");
        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Verificar si el usuario está autenticado
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "onCreate: Usuario no autenticado");
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "onCreate: Usuario autenticado con ID " + userId);

        // Cargar la lista de amigos
        loadFriends(userId);

        // Botón para agregar amigos
        btnAddFriend.setOnClickListener(v -> {
            String emailToSearch = etEmailSearch.getText().toString().trim();
            if (emailToSearch.isEmpty()) {
                Log.w(TAG, "onClick: El campo de correo está vacío");
                Toast.makeText(HomeActivity.this, "Por favor ingrese un correo", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "onClick: Buscando usuario con correo " + emailToSearch);
            searchAndSendFriendRequest(userId, emailToSearch);
        });

        // Funcionalidad para el botón de perfil
        btnProfile.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Botón de perfil presionado");
            Toast.makeText(HomeActivity.this, "Función de perfil no implementada aún", Toast.LENGTH_SHORT).show();
        });

        // Funcionalidad para el botón de notificaciones
        btnNotifications.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Botón de notificaciones presionado");
            loadNotifications(userId); // Cargar las notificaciones
        });

        // Botón para cargar amigos
        btnFriends.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Botón de cargar amigos presionado");
            loadFriends(userId);
        });

        // Funcionalidad para el botón de cerrar sesión
        btnLogout.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Botón de cerrar sesión presionado");
            logout();
        });
    }

    // Método para cargar amigos desde Firebase
    private void loadFriends(String userId) {
        Log.d(TAG, "loadFriends: Cargando amigos para el usuario con ID " + userId);
        database.child("users").child(userId).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendsList.clear();
                Log.d(TAG, "loadFriends: Número de amigos encontrados: " + snapshot.getChildrenCount());
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();
                    Log.d(TAG, "loadFriends: Procesando amigo con ID " + friendId);
                    database.child("users").child(friendId).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot emailSnapshot) {
                            String email = emailSnapshot.getValue(String.class);
                            if (email != null) {
                                Log.d(TAG, "loadFriends: Amigo encontrado: " + email);
                                friendsList.add(email);
                                friendsAdapter.notifyDataSetChanged();
                            } else {
                                Log.w(TAG, "loadFriends: El correo del amigo con ID " + friendId + " es nulo");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "loadFriends: Error cargando correo de amigo: " + error.getMessage());
                            Toast.makeText(HomeActivity.this, "Error cargando amigos", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadFriends: Error cargando amigos: " + error.getMessage());
                Toast.makeText(HomeActivity.this, "Error cargando amigos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para buscar y enviar solicitud de amistad
    private void searchAndSendFriendRequest(String userId, String email) {
        Log.d(TAG, "searchAndSendFriendRequest: Buscando usuario con correo " + email);
        database.child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "searchAndSendFriendRequest: Usuario encontrado");
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String friendId = userSnapshot.getKey();
                        if (friendId != null && !friendId.equals(userId)) {
                            Log.d(TAG, "searchAndSendFriendRequest: Enviando solicitud a " + friendId);
                            sendFriendRequest(userId, friendId);
                        } else {
                            Log.w(TAG, "searchAndSendFriendRequest: Intento de agregarse a sí mismo");
                            Toast.makeText(HomeActivity.this, "No puedes agregarte a ti mismo", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.w(TAG, "searchAndSendFriendRequest: Usuario no encontrado");
                    Toast.makeText(HomeActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "searchAndSendFriendRequest: Error buscando usuario: " + error.getMessage());
                Toast.makeText(HomeActivity.this, "Error buscando usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para enviar una solicitud de amistad
    private void sendFriendRequest(String userId, String friendId) {
        Log.d(TAG, "sendFriendRequest: Enviando solicitud de amistad de " + userId + " a " + friendId);

        // Verificar si ya existe una solicitud enviada
        database.child("users").child(userId).child("friendRequests").child("sent").child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    database.child("users").child(friendId).child("friendRequests").child("received").child(userId).setValue(true);
                    database.child("users").child(userId).child("friendRequests").child("sent").child(friendId).setValue(true);
                    Toast.makeText(HomeActivity.this, "Solicitud de amistad enviada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HomeActivity.this, "Ya has enviado una solicitud a este usuario", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "sendFriendRequest: Error enviando solicitud de amistad: " + error.getMessage());
                Toast.makeText(HomeActivity.this, "Error enviando solicitud", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para cerrar sesión
    private void logout() {
        // Cerrar sesión de Firebase
        FirebaseAuth.getInstance().signOut();

        // Cerrar sesión de Google
        GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
                .addOnCompleteListener(this, task -> {
                    // Redirigir al login después de cerrar sesión correctamente
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Asegurar que no se pueda regresar a la pantalla anterior
                    startActivity(intent);
                    finish();
                });
    }

    // Método para cargar las notificaciones
    private void loadNotifications(String userUid) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference().child("users").child(userUid).child("notifications");
        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationsList.clear(); // Limpiar las notificaciones anteriores
                for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                    String notification = notificationSnapshot.getValue(String.class);
                    if (notification != null) {
                        notificationsList.add(notification); // Agregar la notificación a la lista
                    }
                }
                notificationsAdapter.notifyDataSetChanged(); // Notificar al adaptador para que actualice la vista
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadNotifications: Error cargando notificaciones: " + error.getMessage());
                Toast.makeText(HomeActivity.this, "Error cargando notificaciones", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
