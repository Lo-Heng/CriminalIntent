package com.bignerdranch.android.criminalintent;


import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.UUID;

public class lzhCrimeActivity extends SingleFragmentActivity {

    private static final String EXTRA_CRIME_ID =
            "com.bignerdranch.android.criminalintent.crime_id";

    public static Intent newIntent(Context packageContext, UUID crime_Id){
        Intent intent = new Intent(packageContext,lzhCrimeActivity.class);
        intent.putExtra(EXTRA_CRIME_ID,crime_Id);
        return intent;
    }

    @Override
    protected  Fragment createFragment(){
        UUID crimeId = (UUID)getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        return CrimeFragment.newInstance(crimeId);
    }
}
