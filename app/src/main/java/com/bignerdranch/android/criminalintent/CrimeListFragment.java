package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Administrator on 2017-10-11.
 */

public class CrimeListFragment extends Fragment{
    //
    private CrimeAdapter mAdapter;
    private RecyclerView mCrimeRecyclerView;
    private boolean mSubtitleVisible;
    private Crime mCrime;
    private TextView mNoCrimeTextView;
    private Button mNoCrimeButton;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    //配置视图
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_crime_list,container,false);

        mCrimeRecyclerView = (RecyclerView)view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mNoCrimeTextView = (TextView) view.findViewById(R.id.no_crime_textview);
        mNoCrimeButton = (Button)view.findViewById(R.id.no_crime_add_button);
        mNoCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePageActivity.newIntent(getActivity(),crime.getId());
                startActivity(intent);
            }
        });

        if(savedInstanceState != null){
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
        updateUI();
        return view;
    }
    @Override
    public void onResume(){
        super.onResume();
        updateUI();

    }
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE,mSubtitleVisible);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu,inflater);

        inflater.inflate(R.menu.fragment_crime_list,menu);
        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if(mSubtitleVisible){
            subtitleItem.setTitle(R.string.hide_subtitle);
        }
        else{
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePageActivity.newIntent(getActivity(),crime.getId());
                startActivity(intent);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubstitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private  void updateSubstitle(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        //int crimeCount = crimeLab.getCrimes().size();
        int crimeSize = crimeLab.getCrimes().size();
        Log.d("LOG", String.valueOf(crimeSize));
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural,crimeSize,crimeSize);
       // String subtitle = getString(subtitle,crimeCount);

        if(!mSubtitleVisible){
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }
    private void updateUI(){
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        if(crimes.size() != 0){
            mNoCrimeTextView.setVisibility(View.INVISIBLE);
            mNoCrimeButton.setVisibility(View.INVISIBLE);
        }
        else{
            mNoCrimeTextView.setVisibility(View.VISIBLE);
            mNoCrimeButton.setVisibility(View.VISIBLE);
        }
        if(mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        }
        else{
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }
        updateSubstitle();
    }
    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private Crime mCrime;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;
        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {

            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            itemView.setOnClickListener(this);
            mTitleTextView = (TextView) itemView.findViewById(R.id.crime_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.crime_date);
            mSolvedImageView = (ImageView)itemView.findViewById(R.id.crime_solved);

        }
        public void bind (Crime crime){
            mCrime = crime;
//            mTitleTextView.setText("123");
            mTitleTextView.setText(mCrime.getTitle());
            //Log.d("log",mCrime.getTitle());
            mDateTextView.setText(mCrime.getDate().toString());
            //Log.d("log",crime.isSolved()?"true":"false");
            mSolvedImageView.setVisibility(crime.isSolved()?View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {

//            Intent intent = lzhCrimeActivity.newIntent(getActivity(),mCrime.getId());
            Intent intent = CrimePageActivity.newIntent(getActivity(),mCrime.getId());
            startActivity(intent);
        }
    }
    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder>{
        private List<Crime> mCrimes;

        private CrimeAdapter(List<Crime> crimes){
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new CrimeHolder(layoutInflater,parent);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Log.d("log", "position"+ String.valueOf(position));
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();

        }
        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }

    }

}
