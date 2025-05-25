package com.example.icusheba;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    FirebaseAuth sAuth;
    FirebaseFirestore fStore;
    CardView AddNewHospital,AddNewHR;
    ImageView Admin_profile,AdminDrawerToggle;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        EdgeToEdge.enable( this );
        setContentView( R.layout.activity_admin );
        Admin_profile = findViewById( R.id.Admin_profile );
        AdminDrawerToggle = findViewById( R.id.AdminDrawerToggle );
        AddNewHospital = findViewById( R.id.AddNewHospital );
        AddNewHR = findViewById( R.id.AddNewHR );
        sAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        //noinspection DataFlowIssue
        userId = sAuth.getCurrentUser().getUid();
        drawerLayout = findViewById( R.id.drawer_Layout );
        AdminDrawerToggle.setOnClickListener( new View.OnClickListener() {
               @Override
                 public void onClick(View v) {
                   drawerLayout.open();
                    }
                });
        Admin_profile.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        } );
        AddNewHospital.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( AdminActivity.this,Add_New_Hospital.class );
                startActivity( intent );

            }
        } );
        AddNewHR.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( AdminActivity.this,Add_New_HR.class );
                startActivity( intent );

            }
        } );

        ViewCompat.setOnApplyWindowInsetsListener( findViewById( R.id.main ), (v, insets) -> {
            Insets systemBars = insets.getInsets( WindowInsetsCompat.Type.systemBars() );
            v.setPadding( systemBars.left, systemBars.top, systemBars.right, systemBars.bottom );
            return insets;
        } );
    }

    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(AdminActivity.this,Admin_profile);
        popupMenu.getMenuInflater().inflate( R.menu.popup_menu_admin, popupMenu.getMenu() );
        popupMenu.setOnMenuItemClickListener( new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId()==R.id.button_logout_admin){
                    sAuth.signOut();
                    finish();
                    Toast.makeText( AdminActivity.this, "Logged Out", Toast.LENGTH_SHORT ).show();
                    Intent intent = new Intent( AdminActivity.this,LoginActivity.class );
                    startActivity( intent );
                }if (item.getItemId()==R.id.button_add_hr){
                    Toast.makeText( AdminActivity.this, "Add New HR", Toast.LENGTH_SHORT ).show();
                    Intent intent = new Intent( AdminActivity.this,Add_New_HR.class );
                    startActivity( intent );
                }if (item.getItemId()==R.id.button_add_Hospital){
                    Toast.makeText( AdminActivity.this, "Add New Hospital", Toast.LENGTH_SHORT ).show();
                    Intent intent = new Intent( AdminActivity.this,Add_New_Hospital.class );
                    startActivity( intent );
                }else if (item.getItemId() == R.id.button_changepassword) {
                    showChangePasswordDialog();
                    return true;
                }else if (item.getItemId() == R.id.button_share) {
                    shareApp();
                    return true;

                } else if (item.getItemId() == R.id.button_about) {
                    showAboutDialog();
                    return true;
                }
                return false;
            }
        } );
        popupMenu.show();
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About This App");
        builder.setMessage("ICU Sheba\n\nVersion: 1.0.0\nDeveloped by:Student Of European University of Bangladesh (EUB)\n Department Of CSE \n Batch: 21 \n\nThis app helps users find and book ICU seats in hospitals.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    private void shareApp() {
        String shareText = "Check out this ICU Booking App:\nhttps://play.google.com/store/apps/details?id=" + getPackageName();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "ICU Booking App");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }


    private void showChangePasswordDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.change_password_dialog, null);

        EditText oldPassword = view.findViewById(R.id.oldPassword);
        EditText newPassword = view.findViewById(R.id.newPassword);
        EditText confirmPassword = view.findViewById(R.id.confirmPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle("Change Password");
        builder.setPositiveButton("Change", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button changeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            changeButton.setOnClickListener(v -> {
                String oldPwd = oldPassword.getText().toString().trim();
                String newPwd = newPassword.getText().toString().trim();
                String confirmPwd = confirmPassword.getText().toString().trim();

                if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPwd.equals(confirmPwd)) {
                    Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null || user.getEmail() == null) {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPwd);
                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPwd).addOnCompleteListener(passwordTask -> {
                            if (passwordTask.isSuccessful()) {
                                // ✅ Firestore এ Password ফিল্ড আপডেট
                                FirebaseFirestore.getInstance()
                                        .collection("Users")
                                        .document(user.getUid())
                                        .update("Password", newPwd)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Password changed but Firestore update failed", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        });
                            } else {
                                Toast.makeText(this, "Password update failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
    }
}