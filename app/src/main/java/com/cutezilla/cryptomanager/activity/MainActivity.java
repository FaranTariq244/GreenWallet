package com.cutezilla.cryptomanager.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.cutezilla.cryptomanager.R;
import com.cutezilla.cryptomanager.adapter.CurrencyAdapter;
import com.cutezilla.cryptomanager.adapter.MyCurrencyAdapter;
import com.cutezilla.cryptomanager.model.Account;
import com.cutezilla.cryptomanager.model.Currency;
import com.cutezilla.cryptomanager.model.Ledger;
import com.cutezilla.cryptomanager.model.LedgerBuyEntry;
import com.cutezilla.cryptomanager.services.QueryService;
import com.cutezilla.cryptomanager.util.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import pl.utkala.searchablespinner.SearchableSpinner;

public class MainActivity extends AppCompatActivity   implements NavigationView.OnNavigationItemSelectedListener{

    LinearLayout  ll_header;
    FirebaseAuth mAuth;
    BaseActivity baseActivity;
    NavigationView navigationView;
    QueryService queryService = new QueryService();
    String  email;
    CardView cv_btn_buy, cv_btn_sell;
    LottieAnimationView ltv_loading;
    long date_ship_millis;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iniUiComponents();
        headerComponents();
        ll_header.setVisibility(View.VISIBLE);
        navigationView.setNavigationItemSelectedListener(this);
        if (mAuth.getCurrentUser() != null) {
            email = mAuth.getCurrentUser().getEmail();
            Common.email = email;
            fetchNameAndGender(email);
        }
    }



    private void iniUiComponents() {
        baseActivity = new BaseActivity();
        cv_btn_sell =  (CardView) findViewById(R.id.btn_sell);
        cv_btn_buy = (CardView) findViewById(R.id.btn_buy);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        ltv_loading = (LottieAnimationView) findViewById(R.id.lav_loading);
        mAuth = FirebaseAuth.getInstance();
        cv_btn_buy.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i(Common.LOGTRACE, this.getClass()+ " Buy clicked " );
                showBuyPopup();
            }
        });
        cv_btn_sell.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i(Common.LOGTRACE, this.getClass()+ " Sell clicked " );
            }
        });
    }


    private void headerComponents() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        View backPressed = findViewById(R.id.back_btn);
        backPressed.setVisibility(View.GONE);
        View home = findViewById(R.id.imageviewHome);
        home.setVisibility(View.GONE);
        ll_header = findViewById(R.id.ll_head);
        ll_header.setVisibility(View.INVISIBLE);
        View navBtn = findViewById(R.id.iv_navbar);
        navBtn.setVisibility(View.VISIBLE);
        navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        View logout = findViewById(R.id.powerImage);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDialog();
            }
        });
    }

    private void logoutDialog() {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setCanceledOnTouchOutside(false);
        sweetAlertDialog.setTitleText("Logout");
        sweetAlertDialog.setContentText("Are you sure you want to Logout?");
        sweetAlertDialog.setConfirmText("Yes");
        sweetAlertDialog.setCancelText("No");
        sweetAlertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.cancel();
            }
        });
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                mAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LandingPage.class);
                sweetAlertDialog.cancel();
                startActivity(intent);
                finish();
            }
        });
        sweetAlertDialog.show();

    }



    //Predefined code
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.index, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        item.setCheckable(false);
//        if (id == R.id.nav_profile) {
//            Intent intent = new Intent(this, ProfileSettingActivity.class);
//            startActivity(intent);
//        }
//        else
            if (id == R.id.nav_changepassword) {
            showChangePasswordDialog();
        }
            else if (id == R.id.nav_addCurrency) {
                showAddCurrencyPopup();
            }
//            else {
//                Toast.makeText(IndexActivity.this, "Device is already attached", Toast.LENGTH_SHORT).show();
//            }
//        } else if (id == R.id.nav_terms) {
//            Toast.makeText(this, "Terms and condition", Toast.LENGTH_SHORT).show();
//        }
////        else if (id == R.id.nav_faqs) {
////            Toast.makeText(this, "FAQs", Toast.LENGTH_SHORT).show();
////        }
//        else if (id == R.id.nav_about_us) {
//            Intent intent = new Intent(IndexActivity.this, AboutUsActivity.class);
//            startActivity(intent);
//        } else if (id == R.id.nav_feedback) {
////            Intent intent = new Intent(IndexActivity.this, CustomerFeedBackActivity.class);
//            Intent intent = new Intent(IndexActivity.this, RateAppActivity.class);
//            startActivity(intent);
//        } else if (id == R.id.nav_contact) {
//            Intent intent = new Intent(IndexActivity.this, ContactUs.class);
//            startActivity(intent);
//        }
        else if (id == R.id.nav_logout) {
            if (mAuth.getCurrentUser() != null) {
                logoutDialog();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showAddCurrencyPopup() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.add_currency_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final EditText currencyName = (EditText) dialog.findViewById(R.id.et_addCurrencyName);

        ((AppCompatButton) dialog.findViewById(R.id.addcur_bt_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ((AppCompatButton) dialog.findViewById(R.id.addcur_bt_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (currencyName.getText().toString().equals("")){
                   Toast.makeText(MainActivity.this,"Add currency Name",Toast.LENGTH_SHORT);
               }else{

                   FirebaseDatabase.getInstance().getReference(Common.STR_Currency)
                           .child(Objects.requireNonNull(FirebaseDatabase.getInstance().getReference(Common.STR_Currency).push().getKey()))
                           .setValue(new Currency(Common.userAccount.getEmail(),currencyName.getText().toString(),false))
                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull  Task<Void> task) {
                                   baseActivity.sucessDialog(MainActivity.this, "Currency added successful", "Currency", null);
                               }
                           });

                   Toast.makeText(MainActivity.this,"Added new currency",Toast.LENGTH_SHORT);
               }
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void showBuyPopup(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.buy_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        Calendar calendar;
        calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        final Button buyDate = (Button) dialog.findViewById(R.id.et_buy_date);
        final SearchableSpinner currency = (SearchableSpinner) dialog.findViewById(R.id.ss_id);
        final EditText buyPrice = (EditText) dialog.findViewById(R.id.et_buyprice);
        final EditText investedAmount = (EditText) dialog.findViewById(R.id.et_investedamount);
        buyPrice.requestFocus();

//        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        buyDate.setText(format.format(calendar.getTime()));

        ArrayList arrayList=new ArrayList<>();

        Query defaultCurrQ = queryService.getDefaultAndUserCurrency();
        defaultCurrQ.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot dataSnapshot) {
                for(DataSnapshot defaultCrr:dataSnapshot.getChildren()){
                    Currency  crr = defaultCrr.getValue(Currency.class);
                    if(crr!=null){
                        arrayList.add(crr.getName());
                    }
                }
                ArrayAdapter arrayAdapter=new ArrayAdapter(MainActivity.this, android.R.layout.simple_dropdown_item_1line, arrayList);

                currency.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {
                Toast.makeText(MainActivity.this,"Failed to load curr",Toast.LENGTH_SHORT);
            }
        });

        buyDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDatePickerLight((Button) v);
            }
        });
        ((AppCompatButton) dialog.findViewById(R.id.bt_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ((AppCompatButton) dialog.findViewById(R.id.bt_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strBuyDate,strCurrency,strBuyPrice,strInvestedAmount;
                strBuyDate = buyDate.getText().toString();
                strCurrency = currency.getSelectedItem().toString();
                strBuyPrice = buyPrice.getText().toString();
                strInvestedAmount = investedAmount.getText().toString();

                if(validateBuyLedgerData(strBuyDate,strCurrency,strBuyPrice,strInvestedAmount)){
                    LedgerBuyEntry LBE = new LedgerBuyEntry();
                    String ledgerBuyId = Common.createLedgerEntryId(strCurrency);
                    LBE.setLedger_id(ledgerBuyId);
                    LBE.setBuyingDate(strBuyDate);
                    LBE.setCurrency(strCurrency);
                    LBE.setBuyingPrice(Float.parseFloat(strBuyPrice));
                    LBE.setInvestedAmount(Float.parseFloat(strInvestedAmount));

                    FirebaseDatabase.getInstance().getReference(Common.STR_LedgerBuy)
                            .child(Objects.requireNonNull(FirebaseDatabase.getInstance().getReference(Common.STR_LedgerBuy).push().getKey()))
                            .setValue(LBE).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Ledger ledger = new Ledger();

                            Query ledgerQuery = queryService.getLedgerByLedgerId(ledgerBuyId);
                                     ledgerQuery
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull  DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()){
                                        queryService.getLedgerObjectByLedgerId(ledgerBuyId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                                        Ledger ledger1 = new Ledger();
                                                        for(DataSnapshot dataSnapshot1: snapshot.getChildren()){
                                                            ledger1 = dataSnapshot1.getValue(Ledger.class);
                                                        }
                                                        if(ledger1!=null){
                                                            if (ledger1.getHighestBuyingPrice()<LBE.getBuyingPrice()){
                                                                ledger1.setHighestBuyingPrice(LBE.getBuyingPrice());
                                                            }
                                                            else if (ledger1.getLowestBuyingPrice()>LBE.getBuyingPrice()){
                                                                ledger1.setLowestBuyingPrice(LBE.getBuyingPrice());
                                                            }
                                                            ledger1.setTotalInvested(ledger1.getTotalInvested()+LBE.getInvestedAmount());
                                                            FirebaseDatabase.getInstance().getReference(Common.STR_Ledger)
                                                                .child(ledgerBuyId)
                                                                    .setValue(ledger1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull  Task<Void> task) {
                                                                    if (task.isComplete()) {
                                                                        baseActivity.sucessDialog(MainActivity.this, "Ledger entry added", "BUY", null);
                                                                        dialog.dismiss();
                                                                    }
                                                                }
                                                            });
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull  DatabaseError error) {

                                                    }
                                                });

                                    }else{
                                        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

                                        ledger.setLedger_id(Common.removeSpecialCharacter(Common.userAccount.getEmail()));
                                        ledger.setLedgerEntry_id(ledgerBuyId);
                                        ledger.setCurrency_name(strCurrency);
                                        ledger.setHighestBuyingPrice(Float.parseFloat(strBuyPrice));
                                        ledger.setLowestBuyingPrice(Float.parseFloat(strBuyPrice));
                                        ledger.setTime(format.format(calendar.getTime()).toString());
                                        ledger.setTotalInvested(Float.parseFloat(strInvestedAmount));

                                        FirebaseDatabase.getInstance().getReference(Common.STR_Ledger)
                                                .child(ledgerBuyId)
                                                .setValue(ledger).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull  Task<Void> task) {
                                                baseActivity.sucessDialog(MainActivity.this, "Ledger entry added", "BUY", null);
                                                dialog.dismiss();
                                            }
                                        });
                                        Toast.makeText(MainActivity.this,"Ledger not Found",Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    });

                }

            }
        });


        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private boolean validateBuyLedgerData(String buyDate, String currency, String buyPrice, String investedAmount) {
        boolean validated = true;
        if(buyDate.trim().isEmpty()){
            Toast.makeText(MainActivity.this, "Enter Buy date", Toast.LENGTH_SHORT).show();
            validated =false;
        }
        else if(currency.trim().isEmpty()){
            Toast.makeText(MainActivity.this, "select currency", Toast.LENGTH_SHORT).show();
            validated =false;
        }
        else if(buyPrice.trim().isEmpty()){
            Toast.makeText(MainActivity.this, "Enter Bought price", Toast.LENGTH_SHORT).show();
            validated =false;
        }
        else if(investedAmount.trim().isEmpty()){
            Toast.makeText(MainActivity.this, "Enter Buy date", Toast.LENGTH_SHORT).show();
            validated =false;
        }
        return validated;
    }





    private void dialogDatePickerLight(final Button bt) {
        Calendar cur_calender = Calendar.getInstance();

        DatePickerDialog datePicker = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        date_ship_millis = calendar.getTimeInMillis();


//                        Toast.makeText(ProfileEditActivity.this, Tools.getFormattedDateSimple(date_ship_millis), Toast.LENGTH_SHORT).show();
                        String date = DateFormat.format("dd-MM-yyyy", calendar).toString();
                        bt.setText(date);

                    }

                },
                cur_calender.get(Calendar.YEAR),
                cur_calender.get(Calendar.MONTH),
                cur_calender.get(Calendar.DAY_OF_MONTH)
        );
        //set dark light
        datePicker.setThemeDark(true);

        datePicker.setAccentColor(getResources().getColor(R.color.colorPrimary));
//        datePicker.setMinDate(cur_calender);
        datePicker.setMaxDate(cur_calender);
        datePicker.show(getFragmentManager(), "Datepickerdialog");
    }
    private void showChangePasswordDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.change_password_dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final EditText oldPassword = (EditText) dialog.findViewById(R.id.et_oldPassword);
        final EditText newPassword = (EditText) dialog.findViewById(R.id.et_newpassword);
        final EditText confirmPassword = (EditText) dialog.findViewById(R.id.confirmPassword);
        final CircularImageView civ_profile = (CircularImageView) dialog.findViewById(R.id.civ_dialog_avatar);
        final TextView tv_name = (TextView) dialog.findViewById(R.id.tv_name);
        final TextView tv_email = (TextView) dialog.findViewById(R.id.tv_email);

//        if (!Common.userAccount.getGender().equals("Male")) {
//            civ_profile.setImageResource(R.drawable.girl);
//        }
        tv_name.setText(Common.userAccount.getName());
        tv_email.setText(Common.userAccount.getEmail());

        ((AppCompatButton) dialog.findViewById(R.id.bt_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ((AppCompatButton) dialog.findViewById(R.id.bt_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newPassword.getText().toString().trim().equals(confirmPassword.getText().toString().trim())
                        & !newPassword.getText().toString().trim().equals("")
                        & !newPassword.getText().toString().trim().isEmpty()
                        & !confirmPassword.getText().toString().trim().isEmpty()
                        & !confirmPassword.getText().toString().trim().equals("")
                        & !oldPassword.getText().toString().trim().isEmpty()) {
                    updatePasswordInDb(newPassword.getText().toString().trim(), oldPassword.getText().toString().trim());
                    dialog.dismiss();
                } else if (oldPassword.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter old password", Toast.LENGTH_SHORT).show();
                } else if (newPassword.getText().toString().trim().isEmpty()
                        || confirmPassword.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter new or confirm password", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Password must match confirm password", Toast.LENGTH_SHORT).show();
                    newPassword.setText("");
                    confirmPassword.setText("");
                }
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
    private void updatePasswordInDb(final String newPassword, String oldPassword) {
        final SweetAlertDialog prgressDialog = baseActivity.progressDialog(MainActivity.this, "Please Wait", "Processing...");
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(Common.userAccount.getEmail(), oldPassword);
        if (user != null) {
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    prgressDialog.cancel();
                                    baseActivity.sucessDialog(MainActivity.this, "Password change successful", "Successful", null);

                                } else {
                                    prgressDialog.cancel();
                                    baseActivity.errorDialog(MainActivity.this, "Failed to change password", "Failed");
                                }
                            }
                        });
                    } else {
                        prgressDialog.cancel();
                        baseActivity.errorDialog(MainActivity.this, "Old password is wrong", "Failed");
                    }
                }
            });

        }
    }
    private void fetchNameAndGender(String email) {
        ltv_loading.setVisibility(View.VISIBLE);
        Query accountQuery;
        if (email != null) {

//            query = FirebaseDatabase.getInstance().getReference(Common.STR_Account)
////                    .orderByChild("email")
//                    .equalTo(email);

            accountQuery = queryService.getAccount(email);
        } else {
            return;
        }

        accountQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot accountDataSnapShot : dataSnapshot.getChildren()) {
                    Account account = accountDataSnapShot.getValue(Account.class);

                    Common.userAccount = account;

                    if (account != null) {
                        //do something
                        ltv_loading.setVisibility(View.GONE);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().show();
                    }
                }
                ll_header.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Database Error: " + databaseError, Toast.LENGTH_SHORT).show();
                ltv_loading.setVisibility(View.GONE);
            }
        });
    }
}