package com.example.icusheba;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;

public class ICU_Activity extends AppCompatActivity {
    private final String[][] Icu_Details1=
            {
        {"Beds Name : Manual Beds","Hospital Address:Mirpur 02,Dhaka","Available Beds:120","Mobile Number : 09666-750075","600"},
        {"Beds Name : Electric Beds","Hospital Address:Mirpur 02,Dhaka","Available Beds:120","Mobile Number : 09666-750075","620"},
        {"Beds Name : High-low Beds","Hospital Address:Mirpur 02,Dhaka","Available Beds:120","Mobile Number : 09666-750075","630"},
        {"Beds Name : Pediatric Beds","Hospital Address:Mirpur 02,Dhaka","Available Beds:120","Mobile Number : 09666-750075","650"},
        {"Beds Name : Neuro Beds","Hospital Address:Mirpur 02,Dhaka","Available Beds:120","Mobile Number : 09666-750075","700"}
    };
    private final String[][] Icu_Details2=
            {
                    {"Beds Name : Manual Beds","Hospital Address:House-06,Road-04,Dhanmondi,Dhaka","Available Beds:120","Mobile Number :01766663222","600"},
                    {"Beds Name : Electric Beds","Hospital Address:House-06,Road-04,Dhanmondi,Dhaka","Available Beds:120","Mobile Number : 01766663222","620"},
                    {"Beds Name : High-low Beds","Hospital Address:House-06,Road-04,Dhanmondi,Dhaka","Available Beds:120","Mobile Number : 01766663222","630"},
                    {"Beds Name : Pediatric Beds","Hospital Address:House-06,Road-04,Dhanmondi,Dhaka","Available Beds:120","Mobile Number : 01766663222","650"},
                    {"Beds Name : Neuro Beds","Hospital Address:House-06,Road-04,Dhanmondi,Dhaka","Available Beds:120","Mobile Number : 01766663222","700"}
            };
    private final String[][] Icu_Details3=
            {
                    {"Beds Name : Manual Beds","Hospital Address:Shahbagh,Dhaka","Available Beds:120","Mobile Number : 017140066706","600"},
                    {"Beds Name : Electric Beds","Hospital Address:Shahbagh,Dhaka","Available Beds:120","Mobile Number : 017140066706","620"},
                    {"Beds Name : High-low Beds","Hospital Address:Shahbagh,Dhaka","Available Beds:120","Mobile Number : 017140066706","630"},
                    {"Beds Name : Pediatric Beds","Hospital Address:Shahbagh,Dhaka","Available Beds:120","Mobile Number : 017140066706","650"},
                    {"Beds Name : Neuro Beds","Hospital Address:Shahbagh,Dhaka","Available Beds:120","Mobile Number : 017140066706","700"}
            };
    private final String[][] Icu_Details4=
            {
                    {"Beds Name : Manual Beds","Hospital Address:Shyamoli,Mirpur,Dhaka","Available Beds:120","Mobile Number : 09666700100","600"},
                    {"Beds Name : Electric Beds","Hospital Address:Shyamoli,Mirpur,Dhaka","Available Beds:120","Mobile Number : 09666700100","620"},
                    {"Beds Name : High-low Beds","Hospital Address:Shyamoli,Mirpur,Dhaka","Available Beds:120","Mobile Number : 09666700100","630"},
                    {"Beds Name : Pediatric Beds","Hospital Address:Shyamoli,Mirpur,Dhaka","Available Beds:120","Mobile Number : 09666700100","650"},
                    {"Beds Name : Neuro Beds","Hospital Address:Shyamoli,Mirpur,Dhaka","Available Beds:120","Mobile Number : 09666700100","700"}
            };
    private final String[][] Icu_Details5=
            {
                    {"Beds Name : Manual Beds","Hospital Address:Gulshan,Dhaka","Available Beds:120","Mobile Number : +88028836444","600"},
                    {"Beds Name : Electric Beds","Hospital Address:Gulshan,Dhaka","Available Beds:120","Mobile Number : +88028836444","620"},
                    {"Beds Name : High-low Beds","Hospital Address:Gulshan,Dhaka","Available Beds:120","Mobile Number : +88028836444","630"},
                    {"Beds Name : Pediatric Beds","Hospital Address:Gulshan,Dhaka","Available Beds:120","Mobile Number : +88028836444","650"},
                    {"Beds Name : Neuro Beds","Hospital Address:Gulshan,Dhaka","Available Beds:120","Mobile Number : +88028836444","700"}
            };
    private final String[][] Icu_Details6=
            {
                    {"Beds Name : Manual Beds","Hospital Address:Gandaria,Dhaka","Available Beds:120","Mobile Number : 01787683333","600"},
                    {"Beds Name : Electric Beds","Hospital Address:Gandaria,Dhaka","Available Beds:120","Mobile Number : 01787683333","620"},
                    {"Beds Name : High-low Beds","Hospital Address:Gandaria,Dhaka","Available Beds:120","Mobile Number : 01787683333","630"},
                    {"Beds Name : Pediatric Beds","Hospital Address:Gandaria,Dhaka","Available Beds:120","Mobile Number : 01787683333","650"},
                    {"Beds Name : Neuro Beds","Hospital Address:Gandaria,Dhaka","Available Beds:120","Mobile Number : 01787683333","700"}
            };

    TextView tv;
    String[][]Icu_Details = {};
    ArrayList<HashMap<String, String>> list;
    HashMap<String,String>item;
    Adapter sa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        EdgeToEdge.enable( this );
        setContentView( R.layout.activity_icu );
        tv = findViewById( R.id.Tittle );
        Intent it = getIntent();
        String title = it.getStringExtra( "Title" );
        tv.setText( title );
        assert title != null;
        //noinspection IfCanBeSwitch
        if (title.equals( "National Heart Foundation Hospital" )) {
            Icu_Details = Icu_Details1;
            }
        else if (title.equals( "LabAid Hospital" )) {
            Icu_Details = Icu_Details2;
            }
        else if (title.equals( "Ibrahim Cardiac Hospital" )) {
            Icu_Details = Icu_Details3;
            }
        else if (title.equals( "Bangladesh Specialized Hospital Ltd" )) {
            Icu_Details = Icu_Details4;
            }
        else if (title.equals( "United Hospital" )) {
            Icu_Details = Icu_Details5;
        }
        else if (title.equals( "Asgar Ali Hospital" )) {
            Icu_Details = Icu_Details6;
        }
        ViewCompat.setOnApplyWindowInsetsListener( findViewById( R.id.main ), (v, insets) -> {
            Insets systemBars = insets.getInsets( WindowInsetsCompat.Type.systemBars() );
            v.setPadding( systemBars.left, systemBars.top, systemBars.right, systemBars.bottom );
            return insets;
        } );
        list = new ArrayList<>();
        for (String[] icuDetail : Icu_Details) {
            item = new HashMap<>();
            item.put( "line1", icuDetail[0] );
            item.put( "line2", icuDetail[1] );
            item.put( "line3", icuDetail[2] );
            item.put( "line4", icuDetail[3] );
            item.put( "line5","Cons Fees:"+icuDetail[4]+"/-" );
            list.add( item );

        }
        sa = new SimpleAdapter( this, list, R.layout.multi_line,new String[]{"line1", "line2", "line3", "line4", "line5"},
                new int[]{R.id.line_a, R.id.line_b, R.id.line_c, R.id.line_d, R.id.line_e}
        );
        ListView lst = findViewById( R.id.List_View );
        lst.setAdapter( (ListAdapter) sa );
        lst.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent it = new Intent( ICU_Activity.this, Book_AppointmentActivity.class );
                it.putExtra( "text1", title );
                it.putExtra( "text2", Icu_Details[position][0] );
                it.putExtra( "text3", Icu_Details[position][1] );
                it.putExtra( "text4", Icu_Details[position][3] );
                it.putExtra( "text5", Icu_Details[position][4] );
                startActivity( it );

            }
        } );


    }

}